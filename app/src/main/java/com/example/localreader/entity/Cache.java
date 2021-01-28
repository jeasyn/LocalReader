package com.example.localreader.entity;

import java.lang.ref.WeakReference;

/**
 * @author xialijuan
 * @date 2021/01/11
 */
public class Cache {
    /**
     * 存储的字符长度
     */
    private long size;
    /**
     * 存储的字符数组（小说的部分内容）
     */
    private WeakReference<char[]> data;

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public WeakReference<char[]> getData() {
        return data;
    }

    public void setData(WeakReference<char[]> data) {
        this.data = data;
    }
}
