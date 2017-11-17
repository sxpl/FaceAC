package com.phone.uin.widget;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.widget.TextView;

import com.phone.uin.R;


/**
 * 等待对话框
 *
 * @author paytend_liu
 */
public class LoadingDialog {
    static Dialog mProgressDialog;

    /**
     * 展示对话框
     *
     * @param context
     * @param stringId
     * @param flag
     */
    public static void showDialog(Context context, int stringId, boolean flag) {
        if (mProgressDialog != null) {
            try {
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
            } catch (Exception e) {

            }
        }
        mProgressDialog = null;
        mProgressDialog = onCreatDialog(context);
        TextView tv_message = (TextView) mProgressDialog
                .findViewById(R.id.tv_dialog_message);
        tv_message.setText(stringId);
        mProgressDialog.setCancelable(flag);
        mProgressDialog.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                mProgressDialog.dismiss();
                mProgressDialog = null;

            }
        });
        try {
            mProgressDialog.show();
        } catch (Exception e) {

        }

    }

    /**
     * 展示对话框
     *
     * @param context
     * @param str
     * @param flag
     */
    public static void showDialog(Context context, String str, boolean flag) {
        if (str == null || str.length() < 1) {
            return;
        }
        if (mProgressDialog != null) {
            try {
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
            } catch (Exception e) {

            }
        }
        mProgressDialog = null;
        mProgressDialog = onCreatDialog(context);
        TextView tv_message = (TextView) mProgressDialog.findViewById(R.id.tv_dialog_message);
        tv_message.setText(str);
        mProgressDialog.setCancelable(flag);
        mProgressDialog.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                mProgressDialog.dismiss();
                mProgressDialog = null;

            }
        });
        try {
            mProgressDialog.show();
        } catch (Exception e) {

        }

    }

    /**
     * 生成对话框
     *
     * @param context
     * @return
     */
    private static Dialog onCreatDialog(Context context) {
        Dialog dialog = new Dialog(context, R.style.new_circle_progress);
        dialog.setContentView(R.layout.dialog_loading);

        return dialog;
    }

    /**
     * 关闭等待对话框
     */
    public static void closeDialog() {
        if (mProgressDialog == null)
            return;
        if (mProgressDialog.isShowing()) {
            try {
                mProgressDialog.dismiss();

            } catch (Exception e) {

            }
            mProgressDialog = null;
        }
    }

    /**
     * 设置等待对话框中显示的信息
     *
     * @param stringId 显示信息的Id
     */
    public static void setMessage(int stringId) {
        if (mProgressDialog == null)
            return;
        if (mProgressDialog.isShowing()) {

            TextView tv_message = (TextView) mProgressDialog.findViewById(R.id.tv_dialog_message);
            tv_message.setText(stringId);
        }
    }

    /**
     * 设置等待对话框中显示的信息
     *
     * @param Str 显示的信息
     */
    public static void setMessage(String Str) {
        if (mProgressDialog == null)
            return;
        if (mProgressDialog.isShowing()) {

            TextView tv_message = (TextView) mProgressDialog.findViewById(R.id.tv_dialog_message);
            tv_message.setText(Str);
        }
    }
}
