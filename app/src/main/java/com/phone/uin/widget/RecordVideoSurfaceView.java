package com.phone.uin.widget;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * 自定义录制视频surfaceview
 */
public class RecordVideoSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private Context mContext;
    private SurfaceHolder mHolder;
    private int PREVIEW_WIDTH = 640;
    private int PREVIEW_HEIGHT = 480;
    private Matrix mScaleMatrix = new Matrix();

    public RecordVideoSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        this.mHolder = getHolder();
        this.mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        this.mHolder.setFormat(PixelFormat.TRANSLUCENT);
        this.mHolder.addCallback(this);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        RecordVideoInterface.getInstance(mContext).doOpenCamera(mHolder, Camera.CameraInfo.CAMERA_FACING_FRONT);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        this.mScaleMatrix.setScale(width / (float) PREVIEW_HEIGHT, height / (float) PREVIEW_WIDTH);
        RecordVideoInterface.getInstance(mContext).doStartPreview(mHolder, 1.333f);

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        RecordVideoInterface.getInstance(mContext).doDestroyCamera();
    }

    public SurfaceHolder getSurfaceHolder() {
        return mHolder;
    }


    public Matrix getMatrixInstance() {
        return mScaleMatrix;
    }

}
