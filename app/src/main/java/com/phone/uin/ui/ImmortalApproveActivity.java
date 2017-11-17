package com.phone.uin.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.phone.uin.R;
import com.phone.uin.widget.HomeKeyListener;


/**
 * Created by zhangxingsheng on 2017/6/20.
 * 实人认证
 */
public class ImmortalApproveActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btnGoImmortal;
    private TextView tvTitle;
    private ImageView arrowsBack;
    private HomeKeyListener homeKeyListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //判断该Activity是不是任务空间的源Activity，“非”也就是说是被系统重新实例化出来,
        // 如果你就放在launcher Activity中话，这里可以直接return了
        /**
         *Intent mainIntent=getIntent();
         String action=mainIntent.getAction();
         if(mainIntent.hasCategory(Intent.CATEGORY_LAUNCHER) && action.equals(Intent.ACTION_MAIN)) {
         finish();
         return;
         }
         */

        // finish()之后该活动会继续执行后面的代码，你可以logCat验证，加return避免可能的exception
        if (!isTaskRoot()) {
            finish();
        }
        setContentView(R.layout.immortal_approve_layout);
        this.init();
        homeKeyListener = new HomeKeyListener(this);
        homeKeyStart(); //处理方法

        homeKeyListener.startHomeListener(); //开启监听
    }

    /**
     * Home键开始监听
     */
    private void homeKeyStart() {
        homeKeyListener.setOnHomePressedListener(new HomeKeyListener.OnHomePressedListener() {
            /**
             * Home键短按
             */
            @Override
            public void onHomePressed() {
                // 这里获取到home键按下事件
                Log.i("lock", "onHomePressed ========================================");

            }

            /**
             * Home键长按
             */
            @Override
            public void onHomeLongPressed() {

                Log.i("lock", "onHomeLongPressed ========================================");

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
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("lock", "onDestroy ========================================");
        if (homeKeyListener != null) {
            homeKeyListener.stopHomeListener(); //关闭监听
        }
    }

    private void init() {
        this.initModule();
        this.initData();
        this.addListener();
    }

    private void initModule() {
        this.btnGoImmortal = (Button) findViewById(R.id.btnGoImmortal);
        this.tvTitle = (TextView) findViewById(R.id.tvTitle);
        this.arrowsBack = (ImageView) findViewById(R.id.arrowsBack);
    }

    private void addListener() {
        this.btnGoImmortal.setOnClickListener(this);
        this.arrowsBack.setOnClickListener(this);
    }

    private void initData() {
        this.tvTitle.setText(R.string.immortal_title);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnGoImmortal) {
            startActivity(new Intent(ImmortalApproveActivity.this, FaceVerifyActivity.class));
        } else if (id == R.id.arrowsBack) {
            ImmortalApproveActivity.this.finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("lock", "执行了onPause ========================================");
    }
}
