package com.phone.uin.widget;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Process;
import android.view.SurfaceHolder;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created by zhangxingsheng on 2017/7/4.
 * 录音接口
 */

public class RecordVideoInterface implements MediaRecorder.OnErrorListener {
    private Camera mCamera;
    private static RecordVideoInterface cameraInter;
    private boolean isPreview = false;
    private Camera.Parameters mParams;
    // Camera nv21格式预览帧的尺寸，默认设置640*480
    private int PREVIEW_WIDTH = 640;
    private int PREVIEW_HEIGHT = 480;
    // 预览帧数据存储数组和缓存数组
    private byte[] nv21;
    private byte[] buffer;

    private MediaRecorder mMediaRecorder;
    private long sizePicture = 0;
    private static File fileDir;// 文件
    private static Context mContext;
    //文件路径
    private static final String PATH = Environment.getExternalStorageDirectory().getPath() + File.separator + "AC/Video";
    private static final String FILE_NAME = "record-";
    private static final String FILE_NAME_SUFEIX = ".mp4";
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");// 用于格式化日
    private String time;
    private static File outFile;

    private RecordVideoInterface() {
        this.nv21 = new byte[PREVIEW_WIDTH * PREVIEW_HEIGHT * 2];
        this.buffer = new byte[PREVIEW_WIDTH * PREVIEW_HEIGHT * 2];
    }


    public static synchronized RecordVideoInterface getInstance(Context context) {
        if (cameraInter == null) {
            cameraInter = new RecordVideoInterface();
        }
        mContext = context;
        return cameraInter;
    }

    /**
     * 打开相机 一直开启前置摄像头
     *
     * @param holder
     * @param cameraId
     */
    public void doOpenCamera(SurfaceHolder holder, int cameraId) {
        if (null != mCamera) {
            doDestroyCamera();
            return;
        }
        if (!checkCameraPermission()) {
            ShowToast.showToast(mContext, "摄像头权限未打开，请打开后再试");
            return;
        }
        try {
            mCamera = Camera.open(cameraId);
        } catch (Exception e) {
            doDestroyCamera();
            e.printStackTrace();
        }
    }

    /**
     * 返回录制的视频文件
     *
     * @return
     */
    public File getmVecordFile() {
        return outFile;
    }


    /**
     * 删除文件
     */
    public void delFile() {
        try {
            if (outFile != null) {
                File file = new File(outFile.getAbsolutePath());
                if (file.isFile()) {
                    file.delete();
                }
                file.exists();
            }
        } catch (Exception ex) {

        }
    }

    /**
     * 关闭照相机
     */
    public void doDestroyCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            isPreview = false;
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 重置实例
     */
    public void doDestroyCameraInterface() {
        if (null != cameraInter) {
            cameraInter = null;
        }
    }

    public void doStartPreview(SurfaceHolder holder, float previewRate) {
        if (isPreview) {
            mCamera.stopPreview();
            return;
        }
        if (mCamera != null) {
            mParams = mCamera.getParameters();
            mParams.setPictureFormat(PixelFormat.JPEG);// 设置拍照后存储的图片格式
            mParams.setPreviewFormat(ImageFormat.NV21);// 设置PreviewSize和PictureSize
            mParams.setPreviewSize(PREVIEW_WIDTH, PREVIEW_HEIGHT);
            mCamera.setParameters(mParams);
            mCamera.setDisplayOrientation(90);
            List<String> focusModes = mParams.getSupportedFocusModes();
            if (focusModes.contains("continuous-video")) {
                mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            }
            List<Camera.Size> supportedPictureSizes = mParams.getSupportedPictureSizes();
            for (Camera.Size size : supportedPictureSizes) {
                sizePicture = (size.height * size.width) > sizePicture ? size.height * size.width : sizePicture;
            }
            mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    System.arraycopy(data, 0, nv21, 0, data.length);
                }
            });

            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
            isPreview = true;
        }

    }

    /**
     * 初始化录视频相关
     *
     * @param holder
     * @throws IOException
     */
    @SuppressLint("NewApi")
    public void initRecord(RecordVideoSurfaceView holder) throws IOException {
        this.mMediaRecorder = new MediaRecorder();
        this.mMediaRecorder.reset();
        if (this.mCamera != null) {
            this.mMediaRecorder.setCamera(mCamera);
        }
        this.mMediaRecorder.setOnErrorListener(this);
        this.mMediaRecorder.setPreviewDisplay(holder.getSurfaceHolder().getSurface());
        this.mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);// 视频源
        this.mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);// 音频源
        this.mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);// 视频输出格式
        this.mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);// 音频格式
        this.mMediaRecorder.setVideoSize(PREVIEW_WIDTH, PREVIEW_HEIGHT);// 设置分辨率：
        if (sizePicture < 3000000) {//这里设置可以调整清晰度
            this.mMediaRecorder.setVideoEncodingBitRate(3 * 1024 * 512);
        } else if (sizePicture <= 5000000) {
            this.mMediaRecorder.setVideoEncodingBitRate(2 * 1024 * 512);
        } else {
            this.mMediaRecorder.setVideoEncodingBitRate(1 * 1024 * 512);
        }
        this.mMediaRecorder.setOrientationHint(270);// 输出旋转270度，保持竖屏录制
        this.mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);// 视频录制格式
        this.time = format.format(new Date());
        this.outFile = new File(PATH + File.separator + FILE_NAME + time + FILE_NAME_SUFEIX);
        this.mMediaRecorder.setOutputFile(outFile.getAbsolutePath());
        this.mMediaRecorder.prepare();
        try {
            this.mMediaRecorder.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止录制
     */
    public void stopRecord() {
        if (mMediaRecorder != null) {
            // 设置后不会崩
            this.mMediaRecorder.setOnErrorListener(null);
            this.mMediaRecorder.setOnInfoListener(null);
            this.mMediaRecorder.setPreviewDisplay(null);
            try {
                this.mMediaRecorder.stop();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (RuntimeException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 释放资源
     */
    public void releaseRecord() {
        if (this.mMediaRecorder != null) {
            this.mMediaRecorder.setOnErrorListener(null);
            this.mMediaRecorder.setOnErrorListener(null);
            this.mMediaRecorder.setOnInfoListener(null);
            this.mMediaRecorder.setPreviewDisplay(null);
            try {
                mMediaRecorder.release();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.mMediaRecorder = null;
    }


    /**
     * 创建文件存储路径
     */
    public void createRecordDir() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // 创建文件
            try {
                fileDir = new File(PATH);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                fileDir = new File(mContext.getFilesDir().getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
    }

    /**
     * 检测相机权限
     *
     * @return
     */

    private boolean checkCameraPermission() {
        int status = mContext.checkPermission(Manifest.permission.CAMERA, Process.myPid(), Process.myUid());
        if (PackageManager.PERMISSION_GRANTED == status) {
            return true;
        }

        return false;
    }

    /**
     * 获取相机实例
     *
     * @return
     */
    public Camera getCameraInstance() {
        return mCamera;
    }

    /**
     * 提供预览帧数据组和缓存数据
     *
     * @return
     */
    public List<Object> getPreviewDate() {
        List<Object> list = new ArrayList<Object>();
        list.add(buffer);
        list.add(nv21);
        return list;
    }


    @Override
    public void onError(MediaRecorder mr, int what, int extra) {

    }
}
