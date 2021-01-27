package com.example.localreader.listener;

/**
 * @author xialijuan
 * @date 2021/1/27
 */
public interface PageListener {
    /**
     * 读书进度监听
     *
     * @param progress 进度
     */
    void changeProgress(float progress);
}
