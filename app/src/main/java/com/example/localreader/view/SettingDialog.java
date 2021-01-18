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
 * @date 2021/1/10
 */
public class SettingDialog extends Dialog implements View.OnClickListener {

    private Config config;
    private int FONT_SIZE_MIN;
    private int FONT_SIZE_MAX;
    private int currentFontSize;
    private SeekBar brightnessSB;
    private TextView showSizeTv;
    private TextView moreSizeTv;
    private FloatingActionButton iv_bg_default;
    private FloatingActionButton iv_bg1;
    private FloatingActionButton iv_bg2;
    private FloatingActionButton iv_bg3;
    private FloatingActionButton iv_bg4;
    private TextView lessSizeTv;
    private SettingListener mSettingListener;

    public SettingDialog(@NonNull Context context) {
        this(context,R.style.setting_dialog);
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
        getWindow().setAttributes(p);

        FONT_SIZE_MIN = (int) getContext().getResources().getDimension(R.dimen.reading_min_text_size);
        FONT_SIZE_MAX = (int) getContext().getResources().getDimension(R.dimen.reading_max_text_size);

        config = Config.getInstance();

        initView();

        //初始化进度条的位置
        setBrightness(config.getLight());

        //初始化字体大小
        currentFontSize = (int) config.getFontSize();
        showSizeTv.setText(currentFontSize + "");

        //拖动亮度进度条使数据和进度条位置一样
        brightnessSB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                changeBrightnessProgress(progress);
                Log.d("progress",progress+"");
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
        brightnessSB = findViewById(R.id.sb_brightness);
        showSizeTv = findViewById(R.id.tv_show_font_size);
        lessSizeTv = findViewById(R.id.tv_less_font_size);
        moreSizeTv = findViewById(R.id.tv_more_font_size);
        iv_bg_default = findViewById(R.id.iv_bg_default);
        iv_bg1 = findViewById(R.id.iv_bg_1);
        iv_bg2 = findViewById(R.id.iv_bg_2);
        iv_bg3 = findViewById(R.id.iv_bg_3);
        iv_bg4 = findViewById(R.id.iv_bg_4);

        lessSizeTv.setOnClickListener(this);
        moreSizeTv.setOnClickListener(this);
        iv_bg_default.setOnClickListener(this);
        iv_bg1.setOnClickListener(this);
        iv_bg2.setOnClickListener(this);
        iv_bg3.setOnClickListener(this);
        iv_bg4.setOnClickListener(this);
    }

    //设置字体
    public void setBookBg(int type) {
        config.setBookBg(type);
        if (mSettingListener != null) {
            mSettingListener.changeBookBg(type);
        }
    }

    //设置亮度
    public void setBrightness(float brightness) {
        brightnessSB.setProgress((int) (brightness * 100));
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
            case R.id.iv_bg_default:
                setBookBg(Config.BOOK_BG_DEFAULT);
                break;
            case R.id.iv_bg_1:
                setBookBg(Config.BOOK_BG_1);
                break;
            case R.id.iv_bg_2:
                setBookBg(Config.BOOK_BG_2);
                break;
            case R.id.iv_bg_3:
                setBookBg(Config.BOOK_BG_3);
                break;
            case R.id.iv_bg_4:
                setBookBg(Config.BOOK_BG_4);
                break;
        }
    }

    //变大书本字体
    private void addFontSize() {
        if (currentFontSize < FONT_SIZE_MAX) {
            currentFontSize += 1;
            showSizeTv.setText(currentFontSize + "");
            config.setFontSize(currentFontSize);
            if (mSettingListener != null) {
                mSettingListener.changeFontSize(currentFontSize);
            }
        }
    }

    //变小书本字体
    private void lessFontSize() {
        if (currentFontSize > FONT_SIZE_MIN) {
            currentFontSize -= 1;
            showSizeTv.setText(currentFontSize + "");
            config.setFontSize(currentFontSize);
            if (mSettingListener != null) {
                mSettingListener.changeFontSize(currentFontSize);
            }
        }
    }

    //改变亮度进度条位置
    private void changeBrightnessProgress(int brightness) {
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
        void changeSystemBright(float brightness);

        void changeFontSize(int fontSize);

        void changeBookBg(int type);
    }
}
