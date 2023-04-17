package com.example.localreader.listener;

/**
 * Created by xialijuan on 2021/01/27.
 */
public interface PageListener {
    /**
     * 读书进度监听
     *
     * @param progress 进度
     */
    void changeProgress(float progress);
}
