package com.phone.uin.widget;


import android.content.Context;
import android.os.Handler;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.phone.uin.R;


/**
 * Toast通知
 */
public class ShowToast {
    static Toast toast;

    /**
     * 警告通知
     *
     * @param context
     * @param StringId
     */
    public static void showToast(Context context, int StringId) {
        if (context == null) {
            return;
        }
        try {
            toast = Toast.makeText(context, StringId, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            LinearLayout toastView = (LinearLayout) toast.getView();
            ImageView imageCodeProject = new ImageView(context);
            imageCodeProject.setImageResource(R.drawable.icon_information_fail);
            toastView.addView(imageCodeProject, 0);
            isShow();
        } catch (Exception e) {

        }
    }

    /**
     * 警告通知
     *
     * @param context
     * @param str
     */
    public static void showToast(Context context, String str) {
        if (context == null) {
            return;
        }

        if (str == null || str.length() < 0) {
            return;
        }
        try {
            toast = Toast.makeText(context, str, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            LinearLayout toastView = (LinearLayout) toast.getView();
            ImageView imageCodeProject = new ImageView(context);
            imageCodeProject.setImageResource(R.drawable.icon_information_fail);
            toastView.addView(imageCodeProject, 0);

            isShow();
        } catch (Exception e) {

        }
    }

    /**
     * 成功通知
     *
     * @param context
     * @param str
     */
    public static void showSuccessToast(Context context, String str) {
        if (context == null) {
            return;
        }
        if (str == null || str.length() < 1) {
            return;
        }
        try {
            toast = Toast.makeText(context, str, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            LinearLayout toastView = (LinearLayout) toast.getView();
            ImageView imageCodeProject = new ImageView(context);
            imageCodeProject
                    .setImageResource(R.drawable.icon_information_success);
            toastView.addView(imageCodeProject, 0);
            isShow();
        } catch (Exception e) {

        }
    }

    /**
     * 成功通知
     *
     * @param context
     * @param strId
     */
    public static void showSuccessToast(Context context, int strId) {
        if (context == null) {
            return;
        }
        try {
            toast = Toast.makeText(context, strId, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            LinearLayout toastView = (LinearLayout) toast.getView();
            ImageView imageCodeProject = new ImageView(context);
            imageCodeProject
                    .setImageResource(R.drawable.icon_information_success);
            toastView.addView(imageCodeProject, 0);
            isShow();
        } catch (Exception e) {

        }
    }

    /**
     * 成功通知
     *
     * @param context
     * @param str
     */
    public static void showSuccessToast(Context context, String str, int time) {
        if (context == null) {
            return;
        }
        if (str == null || str.length() < 1) {
            return;
        }
        try {
            toast = Toast.makeText(context, str, time);
            toast.setGravity(Gravity.CENTER, 0, 0);
            LinearLayout toastView = (LinearLayout) toast.getView();
            ImageView imageCodeProject = new ImageView(context);
            imageCodeProject
                    .setImageResource(R.drawable.icon_information_success);
            toastView.addView(imageCodeProject, 0);
            isShow();
        } catch (Exception e) {

        }
    }

    /**
     * 成功通知
     *
     * @param context
     * @param strId
     */
    public static void showSuccessToast(Context context, int strId, int time) {
        if (context == null) {
            return;
        }
        try {
            toast = Toast.makeText(context, strId, time);
            toast.setGravity(Gravity.CENTER, 0, 0);
            LinearLayout toastView = (LinearLayout) toast.getView();
            ImageView imageCodeProject = new ImageView(context);
            imageCodeProject
                    .setImageResource(R.drawable.icon_information_success);
            toastView.addView(imageCodeProject, 0);
            isShow();
        } catch (Exception e) {

        }
    }

    static boolean isFlag = false;

    private static void isShow() {
        if (!isFlag) {
            toast.show();
            isFlag = true;
            Handler mHandler = new Handler();
            mHandler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    isFlag = false;

                }
            }, 1000);
        }
    }
}