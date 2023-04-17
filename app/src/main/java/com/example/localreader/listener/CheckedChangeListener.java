package com.example.localreader.listener;

import android.widget.CompoundButton;

/**
 * Created by xialijuan on 06/12/2020.
 */
public interface CheckedChangeListener {
    /**
     * checkbox状态监听
     *
     * @param position   当前位置
     * @param buttonView 状态已更改的复合按钮视图
     * @param isChecked  单选按钮状态
     */
    void onCheckedChanged(int position, CompoundButton buttonView, boolean isChecked);
}
