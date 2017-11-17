package com.phone.uin.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * UncaughtException处理类,当系统发生Uncaught异常时，将相关log和机器信息保存并上传到服务器
 */
public class CrashHandler implements UncaughtExceptionHandler {
    private static final String TAG = "CrashHandler";
    private static final boolean DEBUG = true;
    //文件路径
    private static final String PATH = Environment.getExternalStorageDirectory().getPath() + File.separator + "AC/LOG";
    private static final String FILE_NAME = "crash-";
    private static final String FILE_NAME_SUFEIX = ".log";
    private static Thread.UncaughtExceptionHandler mDefaultCrashHandler;
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");// 用于格式化日
    private static CrashHandler mCrashHandler = new CrashHandler();
    private Context mContext;
    private File fileDir;

    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        return mCrashHandler;
    }

    public void init(Context context) {
        mDefaultCrashHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
        mContext = context.getApplicationContext();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        try {
            //将文件写入sd卡
            writeToSDcard(ex);
            //写入后在这里可以进行上传操作
        } catch (IOException e) {
            e.printStackTrace();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        ex.printStackTrace();
        //如果系统提供了默认异常处理就交给系统进行处理，否则自己进行处理。
        if (mDefaultCrashHandler != null) {
            mDefaultCrashHandler.uncaughtException(thread, ex);
        } else {
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        }
    }

    //将异常写入文件
    private void writeToSDcard(Throwable ex) throws IOException, PackageManager.NameNotFoundException {
        String time = format.format(new Date());
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            fileDir = new File(PATH);
        } else {
            fileDir = new File(mContext.getFilesDir().getAbsolutePath());
        }

        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }

        File outFile = new File(PATH + File.separator + FILE_NAME + time + FILE_NAME_SUFEIX);
        PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(outFile)));
        Log.e("错误日志文件路径", "" + outFile.getAbsolutePath());
        pw.println(time);
        PackageManager pm = mContext.getPackageManager();
        PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(), PackageManager.GET_ACTIVITIES);
        //当前版本号
        pw.println("App Version:" + pi.versionName + "_" + pi.versionCode);
        //当前系统
        pw.println("OS version:" + Build.VERSION.RELEASE + "_" + Build.VERSION.SDK_INT);
        //制造商
        pw.println("Vendor:" + Build.MANUFACTURER);
        //手机型号
        pw.println("Model:" + Build.MODEL);
        //CPU架构
        pw.println("CPU ABI:" + Build.CPU_ABI);

        ex.printStackTrace(pw);
        pw.close();

    }

}
