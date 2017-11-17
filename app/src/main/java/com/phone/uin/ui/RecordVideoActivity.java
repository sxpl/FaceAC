package com.phone.uin.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.cloud.FaceDetector;
import com.iflytek.cloud.util.Accelerometer;
import com.phone.uin.R;
import com.phone.uin.http.HttpUploadUtils;
import com.phone.uin.http.UrlApi;
import com.phone.uin.model.BaseResponse;
import com.phone.uin.utils.FaceRect;
import com.phone.uin.utils.FaceUtil;
import com.phone.uin.utils.JsonUtil;
import com.phone.uin.utils.MD5Util;
import com.phone.uin.utils.ParseResult;
import com.phone.uin.utils.StaticArguments;
import com.phone.uin.widget.HomeKeyListener;
import com.phone.uin.widget.LoadingDialog;
import com.phone.uin.widget.RecordVideoInterface;
import com.phone.uin.widget.RecordVideoSurfaceView;
import com.phone.uin.widget.ShowToast;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by zhangxingsheng on 2017/7/4.
 */
public class RecordVideoActivity extends Activity implements View.OnClickListener {
    private ImageView ivBack;
    private TextView tvTips;
    private String shoot_tips_1, shoot_tips_2, shoot_tips_3, shoot_tips_4;
    // 预览surFaceViewPreview
    private RecordVideoSurfaceView surFaceViewPreview;
    public Chronometer chronometer;
    // Camera nv21格式预览帧的尺寸，默认设置640*480
    private int PREVIEW_WIDTH = 640;
    private int PREVIEW_HEIGHT = 480;
    // 预览帧数据存储数组和缓存数组
    private byte[] nv21;
    private byte[] buffer;
    // 加速度感应器，用于获取手机的朝向
    private Accelerometer mAcceler;
    // FaceDetector对象，集成了离线人脸识别：人脸检测、视频流检测功能
    private FaceDetector mFaceDetector;
    private boolean mStopTrack;
    private int isAlign = 1;
    private static int mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;

    // 进行人脸检测及活体检测的子线程
    private Thread mThread;
    private Context mContext;
    private Button btnSubmit;
    private Button btnTakeAgain;
    // 是否录像
    private boolean isRecord = false;
    private int mRecordMaxTime = 7;// 一次拍摄最长时间
    private int mTimeCount = 0;// 时间计数
    private Timer mTimer;// 计时器
    private int width;
    private int height;
    private DisplayMetrics metrics;
    private String TAG = RecordVideoActivity.class.getSimpleName();
    private static boolean isClickHome = false;//是否按下了home键
    private HomeKeyListener homeKeyListener;
    /**
     * clickPowerStatus 状态，
     * 0：表示初始状态
     * -1：表示未检测到人脸，就按下了电源键
     * 1：表示拍摄的过程中按下了电源键
     * 2：表示拍摄完成了按下了电源键
     */
    private static int clickPowerStatus = 0;//监听电源键状态
    /**
     * clickHomeKeyStatus 状态  0.表示初始状态   2.表示拍摄完成按下了HOME键
     */
    private static int clickHomeKeyStatus = 0;//监听Home键状态


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_video_layout);
        this.mContext = this;
        this.init();
    }

    private void init() {
        this.initModule();
        this.monitorClickHomeOrPower();
        this.prepareRecord();
        this.addListener();
    }

    /**
     * 监听Home键后电源键
     */
    private void monitorClickHomeOrPower() {
        this.homeKeyListener = new HomeKeyListener(this);
        this.homeKeyStart(); //处理方法
        this.homeKeyListener.startHomeListener(); //开启监听
    }

    private void initModule() {
        this.ivBack = (ImageView) findViewById(R.id.ivBack);
        this.tvTips = (TextView) findViewById(R.id.tvTips);
        this.surFaceViewPreview = (RecordVideoSurfaceView) findViewById(R.id.movieRecordSurfaceView);
        this.shoot_tips_1 = getResources().getString(R.string.shoot_tips_1);
        this.shoot_tips_2 = getResources().getString(R.string.shoot_tips_2);
        this.shoot_tips_3 = getResources().getString(R.string.shoot_tips_3);
        this.shoot_tips_4 = getResources().getString(R.string.shoot_tips_4);
        this.chronometer = (Chronometer) findViewById(R.id.chronometer);
        this.btnSubmit = (Button) findViewById(R.id.btnSubmit);
        this.btnTakeAgain = (Button) findViewById(R.id.btnTakeAgain);
        // 设置顶部tips
        this.tvTips.setText(shoot_tips_1);
        this.setSurfaceSize();
        this.mAcceler = new Accelerometer(this);
        this.mFaceDetector = FaceDetector.createDetector(this, null);
        // 创建录像存储路径
        RecordVideoInterface.getInstance(mContext).createRecordDir();
    }

    private void addListener() {
        this.ivBack.setOnClickListener(this);
        this.btnSubmit.setOnClickListener(this);
        this.btnTakeAgain.setOnClickListener(this);
    }

    /**
     * 设置SurfaceSize
     */
    private void setSurfaceSize() {
        this.metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        this.width = metrics.widthPixels;
        this.height = (int) (width * PREVIEW_WIDTH / (float) PREVIEW_HEIGHT);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        this.surFaceViewPreview.setLayoutParams(params);
    }

    /**
     * 设置计时器
     */
    public void initData() {
        this.chronometer.setBase(SystemClock.elapsedRealtime());//计时器清零
        this.chronometer.setFormat("%s"); // 00：00
    }

    /**
     * 准备录音
     */
    public void prepareRecord() {
        // 设置控件显示与隐藏
        this.setViewIsShow(false);
        // 计时器清零
        this.initData();
    }

    @Override
    public void onResume() {
        super.onResume();
        /**
         * clickPowerStatus 状态，
         * 0：表示初始状态
         * -1：表示未检测到人脸，就按下了电源键
         * 1：表示拍摄的过程中按下了电源键
         * 2：表示拍摄完成了按下了电源键
         */
        if (isClickHome) {//按下了HOME键
            this.tvTips.setText(shoot_tips_1);
            this.prepareRecord();
            if (null != mAcceler && null != mFaceDetector) {
                mAcceler.start();
            } else {
                this.mAcceler = new Accelerometer(this);
                this.mAcceler.start();
                this.mFaceDetector = FaceDetector.createDetector(this, null);
            }
            this.isRecord = false;
            this.mStopTrack = false;
            // 人脸识别
            this.setFaceDetection();
        } else if (clickPowerStatus == 1 || clickPowerStatus == -1) {//如果在拍摄过程中按下了电源键
            this.tvTips.setText(shoot_tips_1);
            this.prepareRecord();
            RecordVideoInterface.getInstance(mContext).doOpenCamera(surFaceViewPreview.getSurfaceHolder(), Camera.CameraInfo.CAMERA_FACING_FRONT);
            RecordVideoInterface.getInstance(mContext).doStartPreview(surFaceViewPreview.getSurfaceHolder(), 1.333f);
            if (null != mAcceler && null != mFaceDetector) {
                mAcceler.start();
            } else {
                this.mAcceler = new Accelerometer(this);
                this.mAcceler.start();
                this.mFaceDetector = FaceDetector.createDetector(this, null);
            }
            this.isRecord = false;
            this.mStopTrack = false;
            // 人脸识别
            this.setFaceDetection();
        } else if (clickPowerStatus == 2) {//拍摄完成后按下了电源键
            //什么都不干，保持原样
        } else {
            if (null != mAcceler) {
                mAcceler.start();
            }
            this.isRecord = false;
            this.mStopTrack = false;
            // 设置控件显示与隐藏
            this.setViewIsShow(false);
            // 人脸识别
            this.setFaceDetection();
        }
    }

    /**
     * 从.mp4的url视频中获取第一帧
     *
     * @param url
     * @return
     */
    private Bitmap getBitmapFormUrl(String url) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            if (Build.VERSION.SDK_INT >= 14) {
                retriever.setDataSource(url, new HashMap<String, String>());
            } else {
                retriever.setDataSource(url);
            }
            String duration = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION);
            // 取得视频的长度(单位为秒)
            int seconds = Integer.valueOf(duration);
            bitmap = retriever.getFrameAtTime(seconds * 1000, MediaMetadataRetriever.OPTION_CLOSEST);
        } catch (RuntimeException ex) { // Assume this is a corrupt video file. } finally { try { retriever.release(); } catch (RuntimeException ex) { // Ignore failures while cleaning up. } } return bitmap; }

        }
        return bitmap;
    }

    /**
     * 人脸检测
     */

    private void setFaceDetection() {
        this.mTimeCount = 0;
        this.mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                List<Object> previewDate = RecordVideoInterface.getInstance(mContext).getPreviewDate();
                buffer = (byte[]) previewDate.get(0);
                nv21 = (byte[]) previewDate.get(1);
                while (!mStopTrack) {
                    if (null == nv21) {
                        continue;
                    }
                    synchronized (nv21) {
                        System.arraycopy(nv21, 0, buffer, 0, nv21.length);
                    }
                    // 获取手机朝向，返回值0,1,2,3分别表示0,90,180和270度
                    int direction = Accelerometer.getDirection();
                    boolean frontCamera = (Camera.CameraInfo.CAMERA_FACING_FRONT == mCameraId);
                    // 前置摄像头预览显示的是镜像，需要将手机朝向换算成摄相头视角下的朝向。
                    // 转换公式：a' = (360 - a)%360，a为人眼视角下的朝向（单位：角度）
                    if (frontCamera) {
                        // SDK中使用0,1,2,3,4分别表示0,90,180,270和360度
                        direction = (4 - direction) % 4;
                    }
                    if (mFaceDetector == null) {
                        /**
                         * 离线视频流检测功能需要单独下载支持离线人脸的SDK 请开发者前往语音云官网下载对应SDK
                         */
                        ShowToast.showToast(mContext, "本SDK不支持离线视频流检测");
                        break;
                    }

                    String result = mFaceDetector.trackNV21(buffer, PREVIEW_WIDTH, PREVIEW_HEIGHT, isAlign, direction);
                    FaceRect[] faces = ParseResult.parseResult(result);
                    drawRect(frontCamera, faces);
                }
            }

            /**
             * 绘制人脸框，并进行活体检测
             *
             * @param frontCamera
             * @param faces
             */
            private void drawRect(boolean frontCamera, FaceRect[] faces) {
                if (faces == null || faces.length <= 0) {
                    clickPowerStatus = -1;
                    clickHomeKeyStatus = -1;
                    return;
                }
                if (null != faces && frontCamera == (Camera.CameraInfo.CAMERA_FACING_FRONT == mCameraId)) {
                    for (FaceRect face : faces) {
                        face.bound = FaceUtil.RotateDeg90(face.bound, PREVIEW_WIDTH, PREVIEW_HEIGHT);
                        if (face.point != null && face.point.length > 0) {
                            try {
                                if (isRecord) {
                                    break;
                                }
                                mHandler.sendEmptyMessage(StaticArguments.START_RECORD);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                    }
                } else {
                }
            }
        });
        mThread.start();
    }


    /**
     * 录制完成
     */
    private void recordComplete() {
        this.tvTips.setText(shoot_tips_4);
        this.chronometer.stop();
        this.initData();
        this.stop();
        this.setViewIsShow(true);
    }

    /**
     * 设置view是否显示隐藏
     *
     * @param isShow
     */
    private void setViewIsShow(boolean isShow) {
        this.chronometer.setVisibility(!isShow ? View.VISIBLE : View.GONE);
        this.btnSubmit.setVisibility(isShow ? View.VISIBLE : View.GONE);
        this.btnTakeAgain.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    /**
     * 停止拍摄
     */
    public void stop() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        RecordVideoInterface.getInstance(mContext).stopRecord();
        RecordVideoInterface.getInstance(mContext).releaseRecord();
        RecordVideoInterface.getInstance(mContext).doDestroyCamera();
        RecordVideoInterface.getInstance(mContext).doDestroyCameraInterface();
    }

    /**
     * 开始录制视频
     *
     * @param
     * @param
     */
    public void startRecord() {
        clickPowerStatus = 1;
        this.initData();
        try {
            mTimeCount = 0;
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {

                @Override
                public void run() {
                    // 开始计时器
                    mHandler.sendEmptyMessage(StaticArguments.START_CHRONOMETER);
                    if (mTimeCount == mRecordMaxTime) {// 达到指定时间，停止拍摄
                        // 显示发送，重拍按钮
                        mHandler.sendEmptyMessage(StaticArguments.RECORD_COMPLETE);
                    } else if (mTimeCount == 4) {//2s
                        // 显示左右摇头
                        mHandler.sendEmptyMessage(StaticArguments.SHOW_SHAKEHEAD);
                    } else if (mTimeCount == 6) { //4s
                        // 显示点头
                        mHandler.sendEmptyMessage(StaticArguments.SHOW_NODHEAD);
                    }
                    mTimeCount++;
                }
            }, 0, 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        RecordVideoInterface.getInstance(mContext).stopRecord();
        RecordVideoInterface.getInstance(mContext).releaseRecord();
        RecordVideoInterface.getInstance(mContext).doDestroyCamera();
        if (null != mAcceler) {
            mAcceler.stop();
        }
        mStopTrack = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RecordVideoInterface.getInstance(mContext).doDestroyCamera();
        this.mStopTrack = true;
        // 销毁对象
        if (null != mFaceDetector) {
            this.mFaceDetector.destroy();
            this.mFaceDetector = null;
        }
        clickPowerStatus = 0; //状态恢复为初始状态
        clickHomeKeyStatus = 0;
        isClickHome = false;
        // 取消广播监听
        if (homeKeyListener != null) {
            homeKeyListener.stopHomeListener(); //关闭监听
        }
        this.destroyThread();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ivBack) {
            this.clickPowerStatus = 0;
            this.clickHomeKeyStatus = 0;
            this.isClickHome = false;
            RecordVideoInterface.getInstance(mContext).delFile();
            this.stop();
            if (null != mAcceler) {
                mAcceler.stop();
                mAcceler = null;
            }
            if (null != mFaceDetector) {
                this.mFaceDetector.destroy();
                this.mFaceDetector = null;
            }
            this.finish();
        } else if (id == R.id.btnTakeAgain) {
            this.surFaceViewPreview.setVisibility(View.VISIBLE);
            RecordVideoInterface.getInstance(mContext).delFile();
            this.stop();
            if (null != mAcceler) {
                mAcceler.stop();
                mAcceler = null;
            }
            if (null != mFaceDetector) {
                this.mFaceDetector.destroy();
                this.mFaceDetector = null;
            }
            this.tvTips.setText(shoot_tips_1);
            this.prepareRecord();
            RecordVideoInterface.getInstance(mContext).doOpenCamera(surFaceViewPreview.getSurfaceHolder(), Camera.CameraInfo.CAMERA_FACING_FRONT);
            RecordVideoInterface.getInstance(mContext).doStartPreview(surFaceViewPreview.getSurfaceHolder(), 1.333f);
            if (null != mAcceler && null != mFaceDetector) {
                mAcceler.start();
            } else {
                this.mAcceler = new Accelerometer(this);
                this.mAcceler.start();
                this.mFaceDetector = FaceDetector.createDetector(this, null);
            }
            this.isRecord = false;
            this.mStopTrack = false;
            this.isClickHome = false;//是否按下了home键
            this.clickPowerStatus = 0;
            this.clickHomeKeyStatus = 0;
            // 人脸识别
            this.setFaceDetection();
        } else if (id == R.id.btnSubmit) {
            this.isRecord = false;
            this.mStopTrack = false;
            this.isClickHome = false;//是否按下了home键
            uploadVedio();
        }
    }

    /**
     * 上传视频文件
     */
    public void uploadVedio() {
        LoadingDialog.showDialog(mContext, "文件上传中,请稍后...", false);
        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
//        String android_imsi = tm.getSubscriberId();//获取手机IMSI号
        String imei = ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getDeviceId();
        String params = "";
        String secrect = "";
        final Map<String, Object> map = new HashMap<>();
        map.put("appver", 1);
        map.put("appName", "vsimAndroid");
        map.put("imsi", imei);
        map.put("ip", getLocalIpAddress());
        map.put("number", "13051499351");
        map.put("type", 4);
        map.put("token", "63e28f89-02ff-4765-8034-6c5ad9b83d1e");
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
            params += entry.getKey() + "=" + entry.getValue() + "&";
        }
        params = params.substring(0, params.length() - 1);

        secrect = MD5Util.GetMD5Code(params);
        map.put("secrect", secrect);
        String url = RecordVideoInterface.getInstance(mContext).getmVecordFile().getAbsolutePath();
        String[] uploadFile = new String[]{url};
        final HashMap<String, String[]> fileMap = new HashMap<String, String[]>();
        fileMap.put("userFile", uploadFile);
        new Thread() {
            @Override
            public void run() {
                //把网络访问的代码放在这里
                HttpUploadUtils.formUpload(new UrlApi().url, map, fileMap, mHandler);
            }
        }.start();

    }

    /**
     * 获取手机ip
     *
     * @return
     */
    public String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
        }
        return null;
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            try {
                switch (msg.what) {
                    case StaticArguments.START_RECORD: //扫描到人脸，开始录制
                        isRecord = true;//表示已经录制了
                        mStopTrack = true;
                        try {
                            Camera mCamera = RecordVideoInterface.getInstance(mContext).getCameraInstance();
                            if (mCamera != null) {
                                mCamera.unlock();
                            }
                            // 准备录制视频
                            RecordVideoInterface.getInstance(mContext).initRecord(surFaceViewPreview);
                            startRecord(); // 开始录制视频
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;

                    case StaticArguments.START_CHRONOMETER: //开启计时器
                        // 开始启动计时器
                        chronometer.start();
                        break;

                    case StaticArguments.RECORD_COMPLETE: //录制完成
                        clickPowerStatus = 2;
                        clickHomeKeyStatus = 2;
                        recordComplete();
                        break;
                    case StaticArguments.SHOW_SHAKEHEAD: //显示左右摇头
                        setViewIsShow(false);
                        tvTips.setText(shoot_tips_2);
                        break;
                    case StaticArguments.SHOW_NODHEAD: //显示点头
                        setViewIsShow(false);
                        tvTips.setText(shoot_tips_3);
                        break;
                    case StaticArguments.UPLOAD_SUCCESS://上传成功
                        try {
                            LoadingDialog.closeDialog();
                            if (null != msg.obj) {
                                String data = (String) msg.obj;
                                if (!TextUtils.isEmpty(data)) {
                                    BaseResponse baseResponse = JsonUtil.parseObject(data, BaseResponse.class);
                                    if (baseResponse.getResult().equals("1")) {
                                        Toast.makeText(mContext, "上传成功!", Toast.LENGTH_LONG).show();
                                        RecordVideoInterface.getInstance(mContext).delFile();
                                        stop();
                                        if (null != mAcceler) {
                                            mAcceler.stop();
                                            mAcceler = null;
                                        }
                                        if (null != mFaceDetector) {
                                            mFaceDetector.destroy();
                                            mFaceDetector = null;
                                        }
                                        startActivityForResult(new Intent(mContext, IdentifyVerifyActivity.class), 0);
                                    } else {
                                        Toast.makeText(mContext, "上传失败!" + baseResponse.getMessage(), Toast.LENGTH_LONG).show();
                                        finish();
                                    }
                                } else {
                                    Toast.makeText(mContext, "服务器异常!", Toast.LENGTH_LONG).show();
                                }

                            } else {
                                Toast.makeText(mContext, "服务器异常!", Toast.LENGTH_LONG).show();
                            }

                        } catch (Exception ex) {
                            ex.printStackTrace();
                            Toast.makeText(mContext, "服务器异常!", Toast.LENGTH_LONG).show();
                        }

                        break;
                    case StaticArguments.UPLOAD_FAIL://上传失败
                        try {
                            LoadingDialog.closeDialog();
                            String errorData = "";
                            if (null != msg.obj) {
                                errorData = (String) msg.obj;
                            }
                            Toast.makeText(mContext, "上传失败!" + errorData, Toast.LENGTH_LONG).show();
                            finish();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            Toast.makeText(mContext, "服务器异常!", Toast.LENGTH_LONG).show();
                        }
                        break;
                    default:
                        break;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    };


    /**
     * 销毁线程方法
     */
    private void destroyThread() {
        try {
            if (null != mThread && Thread.State.RUNNABLE == mThread.getState()) {
                try {
                    Thread.sleep(500);
                    mThread.interrupt();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mThread = null;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 根据上面发送过去的请求吗来区别
        switch (requestCode) {
            case 0:
                this.tvTips.setText(shoot_tips_1);
                this.prepareRecord();
                if (null != mAcceler && null != mFaceDetector) {
                    mAcceler.start();
                } else {
                    this.mAcceler = new Accelerometer(this);
                    this.mAcceler.start();
                    this.mFaceDetector = FaceDetector.createDetector(this, null);
                }
                this.isRecord = false;
                this.mStopTrack = false;
                // 人脸识别
                this.setFaceDetection();
                break;

            default:
                break;
        }
    }

    /**
     * Home键开始监听
     */
    private void homeKeyStart() {
        this.homeKeyListener.setOnHomePressedListener(new HomeKeyListener.OnHomePressedListener() {
            /**
             * Home键短按
             */
            @Override
            public void onHomePressed() {
                // 这里获取到home键按下事件
                Log.i("lock", "Home键短按 ========================================");
                isClickHome = true;
                clickPowerStatus = 0;
                if (null != chronometer) {//拍摄的过程中按下HOME键
                    chronometer.stop();
                    initData();//计时器清零
                    mTimeCount = 0;
                    if (mTimer != null) {
                        mTimer.cancel();
                        mTimer = null;
                    }
                }
            }

            /**
             * Home键长按
             */
            @Override
            public void onHomeLongPressed() {

                Log.i("lock", "Home键长按 ========================================");

            }

            /**
             * 监听电源键/开
             */
            @Override
            public void onScreenPressed() {
                Log.i("lock", "按下了开启电源键 ========================================");
            }

            /**
             * 监听电源键/关
             */
            @Override
            public void offScreenPressed() {
                Log.i("lock", "按下了关闭电源键 ========================================");
                if (null != chronometer) {
                    chronometer.stop();
                    initData();//计时器清零
                    mTimeCount = 0;
                    isClickHome = false;
                    if (clickPowerStatus == 1 || clickPowerStatus == -1) {//表示拍摄的过程中按下了电源键
                        RecordVideoInterface.getInstance(mContext).delFile();
                        if (mTimer != null) {
                            mTimer.cancel();
                            mTimer = null;
                        }
                        if (null != mAcceler) {
                            mAcceler.stop();
                            mAcceler = null;
                        }
                        if (null != mFaceDetector) {
                            mFaceDetector.destroy();
                            mFaceDetector = null;
                        }
                        destroyThread();
                    } else if (clickPowerStatus == 2 || clickHomeKeyStatus == 2) { //表示录制完成了
                        if (mTimer != null) {
                            mTimer.cancel();
                            mTimer = null;
                        }
                        if (null != mAcceler) {
                            mAcceler.stop();
                            mAcceler = null;
                        }
                        if (null != mFaceDetector) {
                            mFaceDetector.destroy();
                            mFaceDetector = null;
                        }
                        destroyThread();
                    }
                }
            }
        });
    }


}
