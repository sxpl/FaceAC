package com.phone.uin.ui;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.phone.uin.R;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

/**
 * Created by zhangxingsheng on 2017/6/20.
 * 人脸校验
 */
@RuntimePermissions
public class FaceVerifyActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView tvTitle;
    private Button btnStartShoot;
    private TextView tvShootTips;
    private ImageView arrowsBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.face_verify_layout);
        this.init();
    }

    private void init() {
        this.initModule();
        this.initData();
        this.addListener();
    }

    private void initModule() {
        this.tvTitle = (TextView) findViewById(R.id.tvTitle);
        this.btnStartShoot = (Button) findViewById(R.id.btnStartShoot);
        this.tvShootTips = (TextView) findViewById(R.id.tvShootTips);
        this.arrowsBack = (ImageView) findViewById(R.id.arrowsBack);
    }

    private void initData() {
        this.tvTitle.setText(R.string.face_verify);
        String startShootTips = getResources().getString(R.string.start_shoot_tips);
        this.tvShootTips.setText(Html.fromHtml(startShootTips));
    }

    private void addListener() {
        this.btnStartShoot.setOnClickListener(this);
        this.arrowsBack.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnStartShoot) {
            FaceVerifyActivityPermissionsDispatcher.showCameraWithCheck(this);
        } else if (id == R.id.arrowsBack) {
            FaceVerifyActivity.this.finish();
        }
    }

    @NeedsPermission({Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO,Manifest.permission.VIBRATE,Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.READ_PHONE_STATE})
    void showCamera() {
        startActivity(new Intent(FaceVerifyActivity.this, RecordVideoActivity.class));
    }

    @OnShowRationale({Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO,Manifest.permission.VIBRATE,Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.READ_PHONE_STATE})
    public void showRationaleForCamera(final PermissionRequest request) {
        new AlertDialog.Builder(this).setMessage("是否打开权限?").setPositiveButton("允许", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                request.proceed();
            }
        }).setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                request.cancel();
            }
        }).show();

    }

    /**
     * 如果用户拒绝该权限执行的方法
     */
    @OnPermissionDenied({Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO,Manifest.permission.VIBRATE,Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.READ_PHONE_STATE})
    public void showDeniedForPhone() {
        Toast.makeText(this, "获取权限失败", Toast.LENGTH_SHORT).show();
    }

    /**
     * 询问
     */
    @OnNeverAskAgain({Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO,Manifest.permission.VIBRATE,Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.READ_PHONE_STATE})
    public void showNeverAskForPhone() {
        Toast.makeText(this, "再次获取权限", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        FaceVerifyActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }
}
