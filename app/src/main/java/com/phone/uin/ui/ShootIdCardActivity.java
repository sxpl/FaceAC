package com.phone.uin.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.phone.uin.R;
import com.phone.uin.utils.FileUtils;
import com.phone.uin.widget.CustomCameraPreview;
import com.phone.uin.widget.LoadingDialog;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by zhangxingsheng on 2017/7/10.
 */

public class ShootIdCardActivity extends Activity implements View.OnClickListener, View.OnTouchListener {
    private SurfaceView mSurfaceView;
    private TextView tvTitle, tvCancle, tvTips;
    private String tips;
    private ImageView takePic;
    private int tag;
    private ImageView photo_idcard_bg;
    private CustomCameraPreview preview;
    private Camera camera;
    private int mCurrentCameraId = 0; // 1是前置 0是后置
    private Context mContext;
    private Boolean mCurrentOrientation = true; // 当前设备方向 横屏false,竖屏true
    private OrientationEventListener mOrEventListener; // 设备方向监听器
    private String TAG = ShootIdCardActivity.class.getSimpleName();

    //文件路径
    private static final String PATH = Environment.getExternalStorageDirectory().getPath() + File.separator + "AC/Image";
    private static File outFile;

    private static File fileDir;// 文件

    private int PHOTO_SIZE_W = 2000;
    private int PHOTO_SIZE_H = 2000;
    private View focusIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shoot_id_card_layout);
        mContext = this;
        this.init();
    }

    private void init() {
        this.initModule();
        this.initData();
        this.addListener();
    }

    private void initModule() {
        this.focusIndex = findViewById(R.id.focus_index);
        this.tvTitle = (TextView) findViewById(R.id.tvTitle);
        this.tvTips = (TextView) findViewById(R.id.tvTips);
        this.tvCancle = (TextView) findViewById(R.id.tvCancle);
        this.mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        this.takePic = (ImageView) findViewById(R.id.takePic);
        this.photo_idcard_bg = (ImageView) findViewById(R.id.photo_idcard_bg);
        this.tag = getIntent().getIntExtra("tag", 0);
        this.tips = getIntent().getStringExtra("tips");
        if (!TextUtils.isEmpty(tips)) {
            this.tvTitle.setVisibility(View.VISIBLE);
            this.tvTips.setVisibility(View.VISIBLE);
            this.photo_idcard_bg.setVisibility(View.VISIBLE);
            this.tvTitle.setText(tips);
        } else {
            this.tvTitle.setVisibility(View.GONE);
            this.tvTips.setVisibility(View.GONE);
            this.photo_idcard_bg.setVisibility(View.GONE);
        }
    }

    /**
     * 设置SurfaceSize
     */
    private void initData() {
        this.preview = new CustomCameraPreview(this, mSurfaceView);
        this.preview.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        ((FrameLayout) findViewById(R.id.layout)).addView(preview);
    }

    private void addListener() {
        this.tvCancle.setOnClickListener(this);
        this.takePic.setOnClickListener(this);
        this.mSurfaceView.setOnTouchListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tvCancle) {
            ShootIdCardActivity.this.finish();
        } else if (id == R.id.takePic) {
            takePhoto();
        }
    }

    /**
     * 拍照
     */
    private void takePhoto() {
        try {
            camera.takePicture(shutterCallback, rawCallback, jpegCallback);
        } catch (Throwable t) {
            t.printStackTrace();
            Toast.makeText(getApplication(), "拍照失败，请重试！", Toast.LENGTH_LONG).show();
            try {
                camera.startPreview();
            } catch (Throwable e) {

            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        int numCams = Camera.getNumberOfCameras();
        if (numCams > 0) {
            try {
                this.startOrientationChangeListener();
                this.mCurrentCameraId = 0;
                this.camera = Camera.open(mCurrentCameraId);
                this.camera.startPreview();
                this.preview.setCamera(camera);
                this.preview.reAutoFocus();
            } catch (RuntimeException ex) {
                Toast.makeText(mContext, "未发现相机", Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (this.camera != null) {
            this.camera.stopPreview();
            this.preview.setCamera(null);
            this.camera.release();
            this.camera = null;
            this.preview.setNull();
        }
    }

    /**
     * 重置照相机
     */
    private void resetCamera() {
        this.camera.startPreview();
        this.preview.setCamera(camera);
    }

    /**
     * 快门声音回调函数
     */
    Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {
        }
    };


    Camera.PictureCallback rawCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
        }
    };
    Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            new SaveImageTask(data).execute();
//            resetCamera();
        }
    };

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                preview.pointFocus(event);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(focusIndex.getLayoutParams());
        layout.setMargins((int) event.getX() - 60, (int) event.getY() - 60, 0, 0);

        focusIndex.setLayoutParams(layout);
        focusIndex.setVisibility(View.VISIBLE);

        ScaleAnimation sa = new ScaleAnimation(3f, 1f, 3f, 1f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        sa.setDuration(800);
        focusIndex.startAnimation(sa);
        handler.postAtTime(new Runnable() {
            @Override
            public void run() {
                focusIndex.setVisibility(View.INVISIBLE);
            }
        }, 800);
        return false;
    }

    /**
     * 处理拍摄的照片
     */
    private class SaveImageTask extends AsyncTask<Void, Void, String> {
        private byte[] data;

        SaveImageTask(byte[] data) {
            this.data = data;
        }

        @Override
        protected String doInBackground(Void... params) {
            // Write to SD Card
            String path = "";
            try {
                showProgressDialog("图片处理中，请稍后...");
                path = saveToSDCard(data);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
            }
            return path;
        }


        @Override
        protected void onPostExecute(String path) {
            super.onPostExecute(path);
            dismissProgressDialog();

            if (!TextUtils.isEmpty(path)) {
                Log.d("DemoLog", "path=" + path);
                Bundle bundle = new Bundle();
                Intent mIntent = new Intent();
                if (tag == 0) {
                    bundle.putString("frontUrl", path);
                } else {
                    bundle.putString("backUrl", path);
                }
                bundle.putInt("tag", tag);
                bundle.putBoolean("mCurrentOrientation", mCurrentOrientation);
                mIntent.putExtras(bundle);
                // 设置结果，并进行传送
                setResult(RESULT_OK, mIntent);
                finish();
            } else {
                handler.sendEmptyMessage(2);
            }
        }
    }


    /**
     * 将拍下来的照片存放在SD卡中
     */
    public String saveToSDCard(byte[] data) throws IOException {
        Bitmap croppedImage;
        // 获得图片大小
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, options);
        // PHOTO_SIZE = options.outHeight > options.outWidth ? options.outWidth
        // : options.outHeight;
        PHOTO_SIZE_W = options.outWidth;
        PHOTO_SIZE_H = options.outHeight;
        options.inJustDecodeBounds = false;
        Rect r = new Rect(0, 0, PHOTO_SIZE_W, PHOTO_SIZE_H);
        try {
            croppedImage = decodeRegionCrop(data, r);
        } catch (Exception e) {
            return null;
        }
        String imagePath = "";
        try {
            imagePath = FileUtils.saveToFile(mContext, croppedImage);
        } catch (Exception e) {

        }
        croppedImage.recycle();
        return imagePath;
    }

    private Bitmap decodeRegionCrop(byte[] data, Rect rect) {
        InputStream is = null;
        System.gc();
        Bitmap croppedImage = null;
        Bitmap rotatedImage = null;
        try {
            is = new ByteArrayInputStream(data);
            BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(is, false);
            try {
                croppedImage = decoder.decodeRegion(rect, new BitmapFactory.Options());
            } catch (IllegalArgumentException e) {
            }
            Matrix m = new Matrix();
            if (tag == 2) {
                m.setRotate(90, PHOTO_SIZE_W / 2, PHOTO_SIZE_H / 2);
            } else {
                m.setRotate(0);
            }
            if (mCurrentCameraId == 1) {
                m.postScale(1, -1);
            }
            rotatedImage = Bitmap.createBitmap(croppedImage, 0, 0, PHOTO_SIZE_W, PHOTO_SIZE_H, m, true);
            if (rotatedImage != null) {
                if (rotatedImage != croppedImage)
                    croppedImage.recycle();
            } else {
                rotatedImage = croppedImage;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {

        }
        return rotatedImage;
    }


    /**
     * 显示dialog提示框
     *
     * @param msg
     */
    private void showProgressDialog(final String msg) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LoadingDialog.showDialog(mContext, msg, true);
            }
        });
    }

    /**
     * 关闭等待对话框
     */
    private void dismissProgressDialog() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LoadingDialog.closeDialog();
            }
        });
    }

    /**
     * 开启监听横屏竖屏
     */
    public final void startOrientationChangeListener() {
        mOrEventListener = new OrientationEventListener(mContext) {
            @Override
            public void onOrientationChanged(int rotation) {
                if (((rotation >= 0) && (rotation <= 45)) || (rotation >= 315)
                        || ((rotation >= 135) && (rotation <= 225))) {// portrait
                    mCurrentOrientation = true;
                    Log.i(TAG, "竖屏");
                } else if (((rotation > 45) && (rotation < 135))
                        || ((rotation > 225) && (rotation < 315))) {// landscape
                    mCurrentOrientation = false;
                    Log.i(TAG, "横屏");
                }
            }
        };
        mOrEventListener.enable();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    Toast.makeText(ShootIdCardActivity.this, "没有检测到内存卡!", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    Toast.makeText(ShootIdCardActivity.this, "拍照失败,请稍后重试！", Toast.LENGTH_LONG).show();
                    break;
                case 3:
                    Toast.makeText(ShootIdCardActivity.this, "图片保存失败,请重试！", Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;

            }
        }
    };

}
