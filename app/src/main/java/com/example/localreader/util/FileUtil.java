package com.example.localreader.util;

import android.os.Environment;

import org.mozilla.universalchardet.UniversalDetector;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by xialijuan on 08/12/2020.
 */
public class FileUtil {

    /**
     * 存储本地所有的txt文件，不可用于局部变量，否则txt文件找不全
     */
    public static List<File> txtList = new ArrayList<>();

    /**
     * 获取文件编码
     *
     * @param fileName 文件名
     * @return 文件编码
     * @throws IOException
     */
    public static String getCharset(String fileName) throws IOException {
        String charset;
        FileInputStream fis = new FileInputStream(fileName);
        byte[] buf = new byte[4096];
        UniversalDetector detector = new UniversalDetector(null);
        int read;
        while ((read = fis.read(buf)) > 0 && !detector.isDone()) {
            detector.handleData(buf, 0, read);
        }
        detector.dataEnd();
        charset = detector.getDetectedCharset();
        detector.reset();
        return charset;
    }

    /**
     * 格式化文件大小
     *
     * @param size long型的文件大小
     * @return 格式化后的文件大小
     */
    public static String formatFileSize(long size) {
        long byteSize = 1024;
        final long kbSize = 1024*1024;
        final long mbSize = 1024*1024*1024;
        if (size == 0) {
            return "0.00B";
        }

        DecimalFormat dFormat = new DecimalFormat("#.00");

        String fileSizeString;

        if (size < byteSize) {
            fileSizeString = dFormat.format((double) size) + "B";
        } else if (size < kbSize) {
            fileSizeString = dFormat.format((double) size / byteSize) + "KB";
        } else if (size < mbSize) {
            fileSizeString = dFormat.format((double) size / kbSize) + "MB";
        } else {
            fileSizeString = dFormat.format((double) size / mbSize) + "GB";
        }
        return fileSizeString;
    }

    /**
     * 格式化文件时间
     *
     * @param time long型时间
     * @return 格式化后的时间
     */
    public static String formatFileTime(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return sdf.format(time);
    }

    /**
     * 查询所有txt文件并放入txtList集合中
     *
     * @param parentFile txt文件
     */
    public static void getLocalTxt(File parentFile) {
        try {
            File[] files = parentFile.listFiles();
            for (File file : files) {
                if (!file.isDirectory()) {
                    // 排除一些小文件
                    if (file.getName().endsWith(".txt") && file.length() > 1024*100) {
                        txtList.add(file);
                    }
                } else {
                    getLocalTxt(file);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 通过name查找文件
     *
     * @param name 文件名
     * @return 文件
     */
    public static File getFileByName(String name) {
        String path = Environment.getExternalStorageDirectory().toString();
        FileUtil.getLocalTxt(new File(path));
        for (File file : txtList) {
            if (name.equals(file.getName())) {
                return file;
            }
        }
        return null;
    }

    /**
     * 查询name文件是否被选中
     *
     * @param checkMap 所有文件
     * @param name     文件名
     * @return 是否被选中
     */
    public static boolean isChecked(HashMap<File, Boolean> checkMap, String name) {
        for (File file : checkMap.keySet()) {
            if (name.equals(file.getName())) {
                return checkMap.get(file);
            }
        }
        return false;
    }
}
