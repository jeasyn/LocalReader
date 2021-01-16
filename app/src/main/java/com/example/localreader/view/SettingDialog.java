package com.example.localreader.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.localreader.R;
import com.example.localreader.entity.Config;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * @author xialijuan
 * @date 2021/1/10
 */
public class SettingDialog extends Dialog implements View.OnClickListener {


    private Config config;
    private Boolean isSystem;
    private SettingListener mSettingListener;
    private int FONT_SIZE_MIN;
    private int FONT_SIZE_MAX;
    private int currentFontSize;
    private SeekBar sb_brightness;
    private TextView tv_size;
    private TextView tv_add;
    private FloatingActionButton iv_bg_default;
    private FloatingActionButton iv_bg1;
    private FloatingActionButton iv_bg2;
    private FloatingActionButton iv_bg3;
    private FloatingActionButton iv_bg4;
    private TextView tv_subtract;

    private SettingDialog(Context context, boolean flag, DialogInterface.OnCancelListener listener) {
        super(context, flag, listener);
    }

    public SettingDialog(Context context) {
        this(context, R.style.setting_dialog);
    }

    public SettingDialog(Context context, int themeResId) {
        super(context, themeResId);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setGravity(Gravity.BOTTOM);
        setContentView(R.layout.popup_settings_layout);

        initView();

        WindowManager m = getWindow().getWindowManager();
        Display d = m.getDefaultDisplay();
        WindowManager.LayoutParams p = getWindow().getAttributes();
        p.width = d.getWidth();
        getWindow().setAttributes(p);

        FONT_SIZE_MIN = (int) getContext().getResources().getDimension(R.dimen.reading_min_text_size);
        FONT_SIZE_MAX = (int) getContext().getResources().getDimension(R.dimen.reading_max_text_size);

        config = Config.getInstance();

        //初始化亮度
        isSystem = config.isSystemLight();
        setBrightness(config.getLight());

        //初始化字体大小
        currentFontSize = (int) config.getFontSize();
        tv_size.setText(currentFontSize + "");

        sb_brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress > 10) {
                    changeBright(false, progress);
                }
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
        sb_brightness = findViewById(R.id.sb_brightness);
        tv_size = findViewById(R.id.tv_size);
        tv_subtract = findViewById(R.id.tv_subtract);
        tv_add = findViewById(R.id.tv_add);
        iv_bg_default = findViewById(R.id.iv_bg_default);
        iv_bg1 = findViewById(R.id.iv_bg_1);
        iv_bg2 = findViewById(R.id.iv_bg_2);
        iv_bg3 = findViewById(R.id.iv_bg_3);
        iv_bg4 = findViewById(R.id.iv_bg_4);

        tv_subtract.setOnClickListener(this);
        tv_add.setOnClickListener(this);
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
        sb_brightness.setProgress((int) (brightness * 100));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_subtract:
                subtractFontSize();
                break;
            case R.id.tv_add:
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
            tv_size.setText(currentFontSize + "");
            config.setFontSize(currentFontSize);
            if (mSettingListener != null) {
                mSettingListener.changeFontSize(currentFontSize);
            }
        }
    }

    //变小书本字体
    private void subtractFontSize() {
        if (currentFontSize > FONT_SIZE_MIN) {
            currentFontSize -= 1;
            tv_size.setText(currentFontSize + "");
            config.setFontSize(currentFontSize);
            if (mSettingListener != null) {
                mSettingListener.changeFontSize(currentFontSize);
            }
        }
    }

    //改变亮度
    public void changeBright(Boolean isSystem, int brightness) {
        float light = (float) (brightness / 100.0);
        config.setSystemLight(isSystem);
        config.setLight(light);
        if (mSettingListener != null) {
            mSettingListener.changeSystemBright(isSystem, light);
        }
    }

    public void setSettingListener(SettingListener settingListener) {
        this.mSettingListener = settingListener;
    }

    public interface SettingListener {
        void changeSystemBright(Boolean isSystem, float brightness);

        void changeFontSize(int fontSize);

        void changeBookBg(int type);
    }
}
