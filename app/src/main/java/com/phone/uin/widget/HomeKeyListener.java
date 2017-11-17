package com.phone.uin.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

/**
 * HOME键和电源键监听回调
 */
public class HomeKeyListener {
    static final String TAG = "HomeKeyListener";
    private Context mContext;
    private IntentFilter mFilter;
    private OnHomePressedListener mListener;
    private InnerReceiver mReceiver;

    /**
     * Home键监听构造初始化
     *
     * @param context
     */
    public HomeKeyListener(Context context) {
        this.mContext = context;
        this.mFilter = new IntentFilter();
        this.mFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        this.mFilter.addAction(Intent.ACTION_SCREEN_OFF);
        this.mFilter.addAction(Intent.ACTION_SCREEN_ON);
    }

    /**
     * 回调接口
     *
     * @author miaowei
     */
    public interface OnHomePressedListener {

        /**
         * Home键短按
         */
        void onHomePressed();

        /**
         * Home键长按
         */
        void onHomeLongPressed();

        /**
         * 监听电源键/开
         */
        void onScreenPressed();

        /**
         * 监听电源键/关
         */
        void offScreenPressed();
    }

    /**
     * 设置监听
     *
     * @param listener
     */
    public void setOnHomePressedListener(OnHomePressedListener listener) {
        this.mListener = listener;
        this.mReceiver = new InnerReceiver();
    }


    /**
     * 开始监听,注册广播
     */
    public void startHomeListener() {
        if (this.mReceiver != null) {
            this.mContext.registerReceiver(mReceiver, mFilter);
        }
    }

    /**
     * 停止监听，注销广播
     */
    public void stopHomeListener() {
        if (this.mReceiver != null) {
            this.mContext.unregisterReceiver(mReceiver);
        }
    }

    /**
     * 广播接收
     *
     * @author miaowei
     */
    class InnerReceiver extends BroadcastReceiver {
        final String SYSTEM_DIALOG_REASON_KEY = "reason";
        final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
        final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                if (reason != null) {
                    Log.e(TAG, "action:" + action + ",reason:" + reason);
                    if (mListener != null) {
                        if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {
                            // 短按home键
                            mListener.onHomePressed();
                        } else if (reason.equals(SYSTEM_DIALOG_REASON_RECENT_APPS)) {
                            // 长按home键
                            mListener.onHomeLongPressed();
                        }
                    }
                }
            }
            //监听电源键开
            if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                mListener.offScreenPressed();
            } else if (action.equals(Intent.ACTION_SCREEN_ON)) {
                mListener.onScreenPressed();
            }
        }
    }
}