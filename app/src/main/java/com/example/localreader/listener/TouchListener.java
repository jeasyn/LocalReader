package com.example.localreader.listener;

/**
 * Created by xialijuan on 25/12/2020.
 */
public interface TouchListener {
    /**
     * 触摸中间
     */
    void center();
    /**
     * 触摸左边
     * @return
     */
    boolean upPage();
    /**
     * 触摸右边
     * @return
     */
    boolean nextPage();
    /**
     * 取消触摸
     */
    void cancel();
}
