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
 * @author xialijuan
 * @date 2020/11/08
 */
public class FileUtil {

    private static long BYTE_SIZE = 1024;
    private static long KB_SIZE = 1048576;
    private static long MB_SIZE = 1073741824;

    /**
     * 获取文件编码
     * @param fileName
     * @return
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
     * @param size
     * @return
     */
    public static String formatFileSize(long size) {

        if (size == 0) {
            return "0.00B";
        }

        DecimalFormat dFormat = new DecimalFormat("#.00");

        String fileSizeString;

        if (size < BYTE_SIZE) {
            fileSizeString = dFormat.format((double) size) + "B";
        } else if (size < KB_SIZE) {
            fileSizeString = dFormat.format((double) size / BYTE_SIZE) + "KB";
        } else if (size < MB_SIZE) {
            fileSizeString = dFormat.format((double) size / KB_SIZE) + "MB";
        } else {
            fileSizeString = dFormat.format((double) size / MB_SIZE) + "GB";
        }
        return fileSizeString;
    }

    /**
     * 格式化文件时间
     * @param time
     * @return
     */
    public static String formatFileTime(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String formatTime = sdf.format(time);
        return formatTime;
    }

    /**
     * 查询所有txt文件
     * @return
     */
    public static List<File> getLocalTxt(File file) {
        List<File> txtList = new ArrayList<>();
        try {
            File[] files = file.listFiles();
            if (files.length > 0) {
                for (int i = 0; i < files.length; i++) {
                    if (!files[i].isDirectory()) {
                        if (files[i].getName().endsWith(".txt")) {
                            txtList.add(files[i]);
                        }
                    } else {
                        getLocalTxt(files[i]);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return txtList;
    }

    /**
     * 通过name查找文件
     * @param name
     * @return
     */
    public static File getFileByName(String name) {
        String path = Environment.getExternalStorageDirectory().toString();
        List<File> txtFileList = FileUtil.getLocalTxt(new File(path));
        for (File file : txtFileList) {
            if (name.equals(file.getName())) {
                return file;
            }
        }
        return null;
    }

    /**
     * 查询name文件是否被选中
     * @param checkMap
     * @return
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
