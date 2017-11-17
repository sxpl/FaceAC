package com.phone.uin.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.phone.uin.R;
import com.phone.uin.http.HttpUploadUtils;
import com.phone.uin.http.UrlApi;
import com.phone.uin.model.BaseResponse;
import com.phone.uin.utils.FileUtils;
import com.phone.uin.utils.JsonUtil;
import com.phone.uin.utils.MD5Util;
import com.phone.uin.utils.StaticArguments;
import com.phone.uin.widget.LoadingDialog;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by zhangxingsheng on 2017/6/20.
 * 身份认证
 */

public class IdentifyVerifyActivity extends Activity implements View.OnClickListener {
    private TextView tvTitle;
    private Button btnSubmit;
    private ImageView arrowsBack;
    private RelativeLayout ivIdCardFront, ivIdCardBack;
    private int SHOOT_ID_CARD_FRONT_RESULT = 0;
    private int SHOOT_ID_CARD_SIDE_RESULT = 1;
    private RelativeLayout linearLayoutShowFront;
    private RelativeLayout linearLayoutShowBack;

    private ImageView showIvIdCardFront;
    private ImageView imageViewFront;

    private ImageView showIvIdCardBack;
    private ImageView imageViewBack;

    private EditText etName;
    private EditText etIdCard;
    private String TAG = IdentifyVerifyActivity.class.getSimpleName();
    private List<String> list = new ArrayList<>();
    /**
     * 正则表达式：验证身份证
     */
    Pattern idNumPattern = Pattern.compile("(^[1-9]\\d{5}(18|19|([23]\\d))\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$)|(^[1-9]\\d{5}\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{2}$)");

    private String frontUrl;
    private String backUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.identify_verify_layout);
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
        this.ivIdCardFront = (RelativeLayout) findViewById(R.id.ivIdCardFront);
        this.ivIdCardBack = (RelativeLayout) findViewById(R.id.ivIdCardBack);
        this.linearLayoutShowFront = (RelativeLayout) findViewById(R.id.linearLayoutShowFront);
        this.linearLayoutShowBack = (RelativeLayout) findViewById(R.id.linearLayoutShowBack);
        this.showIvIdCardFront = (ImageView) findViewById(R.id.showIvIdCardFront);
        this.imageViewFront = (ImageView) findViewById(R.id.imageViewFront);
        this.showIvIdCardBack = (ImageView) findViewById(R.id.showIvIdCardBack);
        this.imageViewBack = (ImageView) findViewById(R.id.imageViewBack);
        this.etName = (EditText) findViewById(R.id.etName);
        this.etIdCard = (EditText) findViewById(R.id.etIdCard);
    }

    private void initData() {
        this.tvTitle.setText(R.string.id_card_confirm);
    }

    private void addListener() {
        this.arrowsBack.setOnClickListener(this);
        this.btnSubmit.setOnClickListener(this);
        this.ivIdCardFront.setOnClickListener(this);
        this.ivIdCardBack.setOnClickListener(this);
        this.imageViewFront.setOnClickListener(this);
        this.imageViewBack.setOnClickListener(this);
        this.etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                btnSubmit.setTextColor(getResources().getColor(R.color.submit_color));
                btnSubmit.setEnabled(false);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    btnSubmit.setTextColor(getResources().getColor(R.color.white));
                    btnSubmit.setEnabled(true);
                }
            }
        });
        this.etIdCard.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                btnSubmit.setTextColor(getResources().getColor(R.color.submit_color));
                btnSubmit.setEnabled(false);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    btnSubmit.setTextColor(getResources().getColor(R.color.white));
                    btnSubmit.setEnabled(true);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.submit) {
            if (valiator()) {
                uploadIdCardInfo();
            }
        } else if (id == R.id.arrowsBack) {
            IdentifyVerifyActivity.this.finish();
        } else if (id == R.id.ivIdCardFront) {
            Intent intent = new Intent(IdentifyVerifyActivity.this, ShootIdCardActivity.class);
            String tips = getResources().getString(R.string.shoot_id_card_front);
            intent.putExtra("tips", tips);
            intent.putExtra("tag", 0);
            startActivityForResult(intent, SHOOT_ID_CARD_FRONT_RESULT);
        } else if (id == R.id.ivIdCardBack) {
            Intent intent = new Intent(IdentifyVerifyActivity.this, ShootIdCardActivity.class);
            String tips = getResources().getString(R.string.shoot_id_card_side);
            intent.putExtra("tips", tips);
            intent.putExtra("tag", 1);
            startActivityForResult(intent, SHOOT_ID_CARD_SIDE_RESULT);
        } else if (id == R.id.imageViewFront) {
            if (null == list) {
                list = new ArrayList<>();
            }
            list.add(frontUrl);
            FileUtils.delFile(list);
            Intent intent = new Intent(IdentifyVerifyActivity.this, ShootIdCardActivity.class);
            String tips = getResources().getString(R.string.shoot_id_card_front);
            intent.putExtra("tips", tips);
            intent.putExtra("tag", 0);
            startActivityForResult(intent, SHOOT_ID_CARD_FRONT_RESULT);
        } else if (id == R.id.imageViewBack) {
            if (null == list) {
                list = new ArrayList<>();
            }
            list.add(backUrl);
            FileUtils.delFile(list);
            Intent intent = new Intent(IdentifyVerifyActivity.this, ShootIdCardActivity.class);
            String tips = getResources().getString(R.string.shoot_id_card_side);
            intent.putExtra("tips", tips);
            intent.putExtra("tag", 1);
            startActivityForResult(intent, SHOOT_ID_CARD_SIDE_RESULT);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    private boolean valiator() {
        // 数字
        Pattern p = Pattern.compile("[0-9]*");
        //通过Pattern获得Matcher
        Matcher idNumMatcher = idNumPattern.matcher(etIdCard.getText().toString().trim());
        // 特殊字符
        String limitEx = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        if (TextUtils.isEmpty(etName.getText().toString().trim())) {
            Toast.makeText(this, "姓名不能为空！", Toast.LENGTH_SHORT).show();
            return false;
        }

        Matcher m = p.matcher(etName.getText().toString().trim());
        if (m.matches()) {
            Toast.makeText(this, "姓名不允许输入数字！", Toast.LENGTH_SHORT).show();
            return false;
        }

        Pattern pattern = Pattern.compile(limitEx);
        Matcher mLimitEx = pattern.matcher(etName.getText().toString().trim());

        if (mLimitEx.find()) {
            Toast.makeText(this, "姓名不允许输入特殊符号！", Toast.LENGTH_LONG).show();
            return false;
        }

        if (TextUtils.isEmpty(etIdCard.getText().toString().trim())) {
            Toast.makeText(this, "身份证号码不能为空！", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (etIdCard.getText().toString().trim().length() != 15 && etIdCard.getText().toString().trim().length() != 18) {
            Toast.makeText(this, "身份证号码长度应该为15位或18位!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!idNumMatcher.matches()) {
            Toast.makeText(this, "请输入有效的身份证号码！", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(frontUrl)) {
            Toast.makeText(this, "请拍摄身份证正面照片！", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(backUrl)) {
            Toast.makeText(this, "请拍摄身份证反面照片！", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SHOOT_ID_CARD_FRONT_RESULT) {
            switch (resultCode) {
                case RESULT_OK:
                    Bitmap bitmap = null;
                    Bundle b = data.getExtras(); //data为B中回传的Intent
                    frontUrl = b.getString("frontUrl");//str即为回传的值
                    if (!TextUtils.isEmpty(frontUrl)) {
                        ivIdCardFront.setVisibility(View.GONE);
                        linearLayoutShowFront.setVisibility(View.VISIBLE);
                        showIvIdCardFront.setVisibility(View.VISIBLE);
                        bitmap = getCompressPhoto(frontUrl);
                        showIvIdCardFront.setImageBitmap(bitmap); //设置Bitmap
                        btnSubmit.setTextColor(getResources().getColor(R.color.white));
                        btnSubmit.setEnabled(true);
                    } else {
                        ivIdCardFront.setVisibility(View.VISIBLE);
                        linearLayoutShowFront.setVisibility(View.GONE);
                        btnSubmit.setTextColor(getResources().getColor(R.color.submit_color));
                        btnSubmit.setEnabled(false);
                    }
                    break;
                default:
            }

        } else if (requestCode == SHOOT_ID_CARD_SIDE_RESULT) {
            switch (resultCode) {
                case RESULT_OK:
                    Bitmap bitmap = null;
                    Bundle b = data.getExtras(); //data为B中回传的Intent
                    backUrl = b.getString("backUrl");//str即为回传的值
                    if (!TextUtils.isEmpty(backUrl)) {
                        ivIdCardBack.setVisibility(View.GONE);
                        linearLayoutShowBack.setVisibility(View.VISIBLE);
                        bitmap = getCompressPhoto(backUrl);
                        showIvIdCardBack.setImageBitmap(bitmap); //设置Bitmap
                        btnSubmit.setTextColor(getResources().getColor(R.color.white));
                        btnSubmit.setEnabled(true);
                    } else {
                        ivIdCardBack.setVisibility(View.VISIBLE);
                        linearLayoutShowBack.setVisibility(View.GONE);
                        btnSubmit.setTextColor(getResources().getColor(R.color.submit_color));
                        btnSubmit.setEnabled(false);
                    }
                    break;
                default:
            }
        }
    }


    /**
     * 上传身份证正反面照片
     */
    private void uploadIdCardInfo() {
        LoadingDialog.showDialog(IdentifyVerifyActivity.this, "文件上传中,请稍后...", false);
        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        String android_imsi = tm.getSubscriberId();//获取手机IMSI号
        String imei = ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getDeviceId();
        String params = "";
        String secrect = "";
        final Map<String, Object> map = new HashMap<>();
        map.put("appver", 1);
        map.put("appName", "vsimAndroid");
        map.put("imsi", imei);
        map.put("ip", getLocalIpAddress());
        map.put("identityCard", "6124271989********");
        map.put("number", "13051499351");
        map.put("name", "zhangxingsheng");
        map.put("type", 1); // 1为身份证（正、反面）
        map.put("token", "a26e3e22-6cc4-4a5e-aca5-9190c72323b4");
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            System.out.println("Key = " + entry.getKey() + ", Value = "
                    + entry.getValue());
            params += entry.getKey() + "=" + entry.getValue() + "&";
        }
        params = params.substring(0, params.length() - 1);
        secrect = MD5Util.GetMD5Code(params);
        map.put("secrect", secrect);

        String[] uploadFile = new String[]{frontUrl, backUrl};
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
                                    Toast.makeText(IdentifyVerifyActivity.this, "上传成功!", Toast.LENGTH_LONG).show();
                                    if (null == list) {
                                        list = new ArrayList<>();
                                    }
                                    list.add(backUrl);
                                    list.add(frontUrl);
                                    FileUtils.delFile(list);
                                    startActivity(new Intent(IdentifyVerifyActivity.this, HandIdentifyActivity.class));
                                } else {
                                    Toast.makeText(IdentifyVerifyActivity.this, "上传失败!" + baseResponse.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(IdentifyVerifyActivity.this, "服务器异常!", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(IdentifyVerifyActivity.this, "服务器异常!", Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Toast.makeText(IdentifyVerifyActivity.this, "服务器异常!", Toast.LENGTH_LONG).show();
                    }

                    break;
                case StaticArguments.UPLOAD_FAIL:
                    try {
                        LoadingDialog.closeDialog();
                        String errorData = "";
                        if (msg.obj != null) {
                            errorData = (String) msg.obj;
                        }
                        Toast.makeText(IdentifyVerifyActivity.this, "上传失败!" + errorData, Toast.LENGTH_LONG).show();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Toast.makeText(IdentifyVerifyActivity.this, "服务器异常!", Toast.LENGTH_LONG).show();
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
