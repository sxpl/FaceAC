package com.phone.uin.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

/**
 * Created by zhangxingsheng on 2017/7/25.
 * 文件处理相关类
 */

public class FileUtils {
    //文件路径
    private static final String PATH = Environment.getExternalStorageDirectory().getPath() + File.separator + "AC/Image";
    private static File outFile;
    private static File fileDir;// 文件

    /**
     * 删除文件
     */
    public static void delFile(List<String> filePathList) {
        try {
            if (null != filePathList && filePathList.size() > 0) {
                for (int i = 0; i < filePathList.size(); i++) {
                    String filePath = filePathList.get(i);
                    if (!TextUtils.isEmpty(filePath)) {
                        File file = new File(filePath);
                        if (file.isFile()) {
                            file.delete();
                        }
                        file.exists();
                    }
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 图片名称
     *
     * @return
     */
    private static String getCameraPath() {
        Calendar calendar = Calendar.getInstance();
        StringBuilder sb = new StringBuilder();
        sb.append("IMG");
        sb.append(calendar.get(Calendar.YEAR));
        int month = calendar.get(Calendar.MONTH) + 1; // 0~11
        sb.append(month < 10 ? "0" + month : month);
        int day = calendar.get(Calendar.DATE);
        sb.append(day < 10 ? "0" + day : day);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        sb.append(hour < 10 ? "0" + hour : hour);
        int minute = calendar.get(Calendar.MINUTE);
        sb.append(minute < 10 ? "0" + minute : minute);
        int second = calendar.get(Calendar.SECOND);
        sb.append(second < 10 ? "0" + second : second);
        if (!new File(sb.toString() + ".jpg").exists()) {
            return sb.toString() + ".jpg";
        }

        StringBuilder tmpSb = new StringBuilder(sb);
        int indexStart = sb.length();
        for (int i = 1; i < Integer.MAX_VALUE; i++) {
            tmpSb.append('(');
            tmpSb.append(i);
            tmpSb.append(')');
            tmpSb.append(".jpg");
            if (!new File(tmpSb.toString()).exists()) {
                break;
            }

            tmpSb.delete(indexStart, tmpSb.length());
        }

        return tmpSb.toString();
    }

    /**
     * 保存图片文件
     *
     * @param croppedImage
     * @return
     * @throws IOException
     */
    public static String saveToFile(Context mContext, Bitmap croppedImage) throws IOException {
        String fileName = getCameraPath();
        // 创建文件
        try {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                fileDir = new File(PATH);
            } else {
                fileDir = new File(mContext.getFilesDir().getAbsolutePath());
            }
            if (!fileDir.exists()) {
                fileDir.mkdirs();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        outFile = new File(PATH + File.separator + fileName);
        System.out.println(outFile + "/" + fileName);
        FileOutputStream outputStream = new FileOutputStream(outFile); // 文件输出流
        croppedImage.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
        outputStream.flush();
        outputStream.close();
        return outFile.getAbsolutePath();
    }


    /**
     * 把原图按1/2的比例压缩
     *
     * @return 压缩后的图片
     */
    public static Bitmap getCompressPhoto() {
        Bitmap bmp = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inSampleSize = 2;  // 图片的大小设置为原来的二分之一
        if (null != outFile) {
            bmp = BitmapFactory.decodeFile(outFile.getAbsolutePath(), options);
        } else {
            bmp = null;
        }
        options = null;
        return bmp;
    }
}
