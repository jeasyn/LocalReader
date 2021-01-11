package com.example.localreader.entity;

import java.lang.ref.WeakReference;

/**
 * @author xialijuan
 * @date 2021/1/9
 */
public class Cache {
    private long size;
    private WeakReference<char[]> data;

    public WeakReference<char[]> getData() {
        return data;
    }

    public void setData(WeakReference<char[]> data) {
        this.data = data;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
