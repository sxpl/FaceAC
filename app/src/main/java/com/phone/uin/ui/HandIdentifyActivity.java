package com.phone.uin.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.phone.uin.R;
import com.phone.uin.http.HttpUploadUtils;
import com.phone.uin.http.UrlApi;
import com.phone.uin.model.BaseResponse;
import com.phone.uin.utils.JsonUtil;
import com.phone.uin.utils.MD5Util;
import com.phone.uin.utils.StaticArguments;
import com.phone.uin.widget.LoadingDialog;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by zhangxingsheng on 2017/6/20.
 * 手持证件照
 */

public class HandIdentifyActivity extends Activity implements View.OnClickListener {
    private TextView tvTitle;
    private Button btnSubmit;
    private ImageView arrowsBack;
    private ImageView ivHandIdCard;
    private ImageView imageShowHandIdCard;
    private ImageView imageViewHandIdCard;
    private int SHOOT_ID_CARD_SIDE_RESULT = 0;
    private String str;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hand_identify_layout);
        this.init();
    }

    private void init() {
        this.initModule();
        this.initData();
        this.addListener();
    }

    private void initModule() {
        this.tvTitle = (TextView) findViewById(R.id.tvTitle);
        this.btnSubmit = (Button) findViewById(R.id.submit);
        this.arrowsBack = (ImageView) findViewById(R.id.arrowsBack);
        this.ivHandIdCard = (ImageView) findViewById(R.id.ivHandIdCard);
        this.imageShowHandIdCard = (ImageView) findViewById(R.id.imageShowHandIdCard);
        this.imageViewHandIdCard = (ImageView) findViewById(R.id.imageViewHandIdCard);
    }

    private void initData() {
        this.tvTitle.setText(R.string.hand_id_card_title);
    }

    private void addListener() {
        this.arrowsBack.setOnClickListener(this);
        this.btnSubmit.setOnClickListener(this);
        this.ivHandIdCard.setOnClickListener(this);
        this.imageViewHandIdCard.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.submit) {
            if (TextUtils.isEmpty(str)) {
                Toast.makeText(HandIdentifyActivity.this, "请拍摄手持身份证照片！", Toast.LENGTH_SHORT).show();
                return;
            }
            uploadIdCardInfo();
        } else if (id == R.id.arrowsBack) {
            HandIdentifyActivity.this.finish();
        } else if (id == R.id.ivHandIdCard) {
            Intent intent = new Intent(HandIdentifyActivity.this, ShootIdCardActivity.class);
            intent.putExtra("tips", "");
            intent.putExtra("tag", 2);
            startActivityForResult(intent, SHOOT_ID_CARD_SIDE_RESULT);
        } else if (id == R.id.imageViewHandIdCard) {
            Intent intent = new Intent(HandIdentifyActivity.this, ShootIdCardActivity.class);
            intent.putExtra("tips", "");
            intent.putExtra("tag", 2);
            startActivityForResult(intent, SHOOT_ID_CARD_SIDE_RESULT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SHOOT_ID_CARD_SIDE_RESULT) {
            switch (resultCode) {
                case RESULT_OK:
                    Bitmap bitmap = null;
                    Bundle b = data.getExtras(); //data为B中回传的Intent
                    str = b.getString("backUrl");//str即为回传的值
                    if (!TextUtils.isEmpty(str)) {
                        ivHandIdCard.setVisibility(View.GONE);
                        imageViewHandIdCard.setVisibility(View.VISIBLE);
                        bitmap = getCompressPhoto(str);
                        imageShowHandIdCard.setVisibility(View.VISIBLE);
                        imageShowHandIdCard.setImageBitmap(bitmap); //设置Bitmap
                    } else {
                        ivHandIdCard.setVisibility(View.VISIBLE);
                        imageViewHandIdCard.setVisibility(View.GONE);
                    }
                    break;
                default:
            }
        }
    }


    /**
     * 上传照片
     */
    private void uploadIdCardInfo() {
        LoadingDialog.showDialog(HandIdentifyActivity.this, "文件上传中,请稍后...", false);
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
        map.put("type", 3);
        map.put("token", "63e28f89-02ff-4765-8034-6c5ad9b83d1e");
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
            params += entry.getKey() + "=" + entry.getValue() + "&";
        }
        params = params.substring(0, params.length() - 1);
        secrect = MD5Util.GetMD5Code(params);
        map.put("secrect", secrect);

        String[] uploadFile = new String[]{str};
        final HashMap<String, String[]> fileMap = new HashMap<String, String[]>();
        fileMap.put("userFile", uploadFile);

        new Thread() {
            @Override
            public void run() {
                //把网络访问的代码放在这里
                HttpUploadUtils.formUpload(new UrlApi().url, map, fileMap, myHandler);
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

    private Handler myHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case StaticArguments.UPLOAD_SUCCESS:
                    try {
                        LoadingDialog.closeDialog();
                        if (null != msg.obj) {
                            String data = (String) msg.obj;
                            if (!TextUtils.isEmpty(data)) {
                                BaseResponse baseResponse = JsonUtil.parseObject(data, BaseResponse.class);
                                if (baseResponse.getResult().equals("1")) {
                                    Toast.makeText(HandIdentifyActivity.this, "上传成功!", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(HandIdentifyActivity.this, "上传失败!" + baseResponse.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(HandIdentifyActivity.this, "服务器异常！", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(HandIdentifyActivity.this, "服务器异常！", Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Toast.makeText(HandIdentifyActivity.this, "服务器异常！", Toast.LENGTH_LONG).show();
                    }
                    break;
                case StaticArguments.UPLOAD_FAIL:
                    try {
                        LoadingDialog.closeDialog();
                        String errorData = "";
                        if (msg.obj != null) {
                            errorData = (String) msg.obj;
                        }
                        if (!TextUtils.isEmpty(errorData)) {
                            Toast.makeText(HandIdentifyActivity.this, "上传失败!" + errorData, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(HandIdentifyActivity.this, "服务器异常！", Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Toast.makeText(HandIdentifyActivity.this, "服务器异常！", Toast.LENGTH_LONG).show();
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };

    /**
     * 把原图按1/2的比例压缩
     *
     * @param path 原图的路径
     * @return 压缩后的图片
     */
    public static Bitmap getCompressPhoto(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inSampleSize = 2;  // 图片的大小设置为原来的二分之一
        Bitmap bmp = BitmapFactory.decodeFile(path, options);
        options = null;
        return bmp;
    }

}
