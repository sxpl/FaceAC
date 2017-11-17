/**
 *
 */
package com.phone.uin.utils;

import android.util.Log;

/**
 *
 */
public class LogUtil {
    static final boolean LOG = true;
    public static boolean debug;

    public static void showLog(String tag, String msg) {
        //	boolean debug = true;//是否debug,true表示处于debug状态，不可输错日志
        if (debug) {
            Log.i(tag, msg);
        }
    }

    public static void showLog(String tag, String msg, String level) {
        //boolean debug = false;//是否debug,true表示处于debug状态，不可输错日志
        if (debug) {
            Log.i(tag, msg);
        }
    }

    public static void showErrorLog(String tag, String msg) {
        //	boolean debug = true;//是否debug,true表示处于debug状态，不可输错日志
        if (debug) {
            Log.e(tag, msg);
        }
    }

    public static void i(String tag, String string) {
        if (LOG) Log.i(tag, string);
    }

    public static void e(String tag, String string) {
        if (LOG) Log.e(tag, string);
    }

    public static void d(String tag, String string) {
        if (LOG) Log.d(tag, string);
    }

    public static void v(String tag, String string) {
        if (LOG) Log.v(tag, string);
    }

    public static void w(String tag, String string) {
        if (LOG) Log.w(tag, string);
    }
}
