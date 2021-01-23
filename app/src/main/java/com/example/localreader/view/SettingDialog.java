package com.example.localreader.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.localreader.R;
import com.example.localreader.entity.Config;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * @author xialijuan
 * @date 2020/12/18
 */
public class SettingDialog extends Dialog implements View.OnClickListener {

    private Config config;
    private Context context;
    private int fontSizeMin;
    private int fontSizeMax;
    private int currentFontSize;
    private SeekBar brightnessSb;
    private TextView showSizeTv;
    private TextView moreSizeTv;
    private FloatingActionButton whiteBgFb;
    private FloatingActionButton yellowBgFb;
    private FloatingActionButton grayBgFb;
    private FloatingActionButton greenBgFb;
    private FloatingActionButton blueBgFb;
    private TextView lessSizeTv;
    private SettingListener mSettingListener;

    public SettingDialog(@NonNull Context context) {
        this(context, R.style.read_setting_popup);
    }

    public SettingDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setGravity(Gravity.BOTTOM);
        setContentView(R.layout.popup_settings_layout);

        WindowManager m = getWindow().getWindowManager();
        Display d = m.getDefaultDisplay();
        WindowManager.LayoutParams p = getWindow().getAttributes();
        p.width = d.getWidth();

//        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();

        getWindow().setAttributes(p);

        fontSizeMin = (int) getContext().getResources().getDimension(R.dimen.read_min_text_size);
        fontSizeMax = (int) getContext().getResources().getDimension(R.dimen.read_max_text_size);

        config = Config.getInstance();

        initView();

        // 初始化进度条的位置
        changeBrightnessProgress((int) (config.getLight() * 100));

        // 初始化字体大小
        currentFontSize = (int) config.getFontSize();
        showSizeTv.setText(currentFontSize + "");

        // 拖动亮度进度条使数据和进度条位置一样
        brightnessSb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                changeBrightnessProgress(progress);
                Log.d("progress", progress + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void initView() {
        brightnessSb = findViewById(R.id.sb_brightness);
        showSizeTv = findViewById(R.id.tv_show_font_size);
        lessSizeTv = findViewById(R.id.tv_less_font_size);
        moreSizeTv = findViewById(R.id.tv_more_font_size);
        whiteBgFb = findViewById(R.id.fb_bg_white);
        yellowBgFb = findViewById(R.id.fb_bg_yellow);
        grayBgFb = findViewById(R.id.fb_bg_gray);
        greenBgFb = findViewById(R.id.fb_bg_green);
        blueBgFb = findViewById(R.id.fb_bg_blue);

        lessSizeTv.setOnClickListener(this);
        moreSizeTv.setOnClickListener(this);
        whiteBgFb.setOnClickListener(this);
        yellowBgFb.setOnClickListener(this);
        grayBgFb.setOnClickListener(this);
        greenBgFb.setOnClickListener(this);
        blueBgFb.setOnClickListener(this);
    }

    /**
     * 设置字体
     *
     * @param bg
     */
    public void setBookBg(int bg) {
        config.setBookBg(bg);
        if (mSettingListener != null) {
            mSettingListener.changeBookBg(bg);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_less_font_size:
                lessFontSize();
                break;
            case R.id.tv_more_font_size:
                addFontSize();
                break;
            case R.id.fb_bg_white:
                setBookBg(Config.BOOK_BG_WHITE);
                break;
            case R.id.fb_bg_yellow:
                setBookBg(Config.BOOK_BG_YELLOW);
                break;
            case R.id.fb_bg_gray:
                setBookBg(Config.BOOK_BG_GRAY);
                break;
            case R.id.fb_bg_green:
                setBookBg(Config.BOOK_BG_GREEN);
                break;
            case R.id.fb_bg_blue:
                setBookBg(Config.BOOK_BG_BLUE);
                break;
            default:
                break;
        }
    }

    /**
     * 变大书本字号
     */
    private void addFontSize() {
        if (currentFontSize < fontSizeMax) {
            currentFontSize += 1;
            showSizeTv.setText(currentFontSize + "");
            config.setFontSize(currentFontSize);
            if (mSettingListener != null) {
                mSettingListener.changeFontSize(currentFontSize);
            }
        }
    }

    /**
     * 变小书本字号
     */
    private void lessFontSize() {
        if (currentFontSize > fontSizeMin) {
            currentFontSize -= 1;
            showSizeTv.setText(currentFontSize + "");
            config.setFontSize(currentFontSize);
            if (mSettingListener != null) {
                mSettingListener.changeFontSize(currentFontSize);
            }
        }
    }

    /**
     * 改变亮度进度条位置
     * @param brightness
     */
    private void changeBrightnessProgress(int brightness) {
        Log.d("brightness", brightness + "");
        brightnessSb.setProgress(brightness);
        float light = (float) (brightness / 100.0);
        config.setLight(light);
        if (mSettingListener != null) {
            mSettingListener.changeSystemBright(light);
        }
    }

    public void setSettingListener(SettingListener settingListener) {
        this.mSettingListener = settingListener;
    }

    public interface SettingListener {
        /**
         * 改变亮度
         * @param brightness
         */
        void changeSystemBright(float brightness);

        /**
         * 改变字号
         * @param fontSize
         */
        void changeFontSize(int fontSize);

        /**
         * 换读书背景
         * @param bg
         */
        void changeBookBg(int bg);
    }
}
