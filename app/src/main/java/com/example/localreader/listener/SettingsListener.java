package com.example.localreader.listener;

/**
 * Created by xialijuan on 05/01/2020.
 */
public interface SettingsListener {
    /**
     * 改变亮度
     *
     * @param light 亮度值
     */
    void changeLight(float light);

    /**
     * 改变字号
     *
     * @param fontSize 字号大小
     */
    void changeFontSize(int fontSize);

    /**
     * 换读书背景
     *
     * @param bg 背景颜色
     */
    void changeBookBg(int bg);
}
