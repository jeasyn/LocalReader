package com.example.localreader.entity;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.localreader.R;

/**
 * @author xialijuan
 * @date 2020/11/20
 */
public class Config {

    private Context context;
    private static Config config;
    private SharedPreferences sp;
    /**
     * 在shared_prefs生成的xml文件名
     */
    private final static String SP_NAME = "config";
    private final static String READ_BG = "read_bg";
    private final static String FONT_SIZE = "font_size";
    private final static String IS_NIGHT = "is_night";
    private final static String LIGHT = "light";
    public final static int BOOK_BG_WHITE = 0;
    public final static int BOOK_BG_YELLOW = 1;
    public final static int BOOK_BG_GRAY = 2;
    public final static int BOOK_BG_GREEN = 3;
    public final static int BOOK_BG_BLUE = 4;
    /**
     * 字体大小
     */
    private float fontSize = 0;
    /**
     * 亮度值
     */
    private float light = 0;

    private Config(Context context){
        this.context = context.getApplicationContext();
        sp = this.context.getSharedPreferences(SP_NAME,Context.MODE_PRIVATE);
    }

    public static synchronized Config getInstance(){
        return config;
    }

    public static synchronized void createConfig(Context context){
        if (config == null){
            config = new Config(context);
        }
    }

    public int getBookBg(){
        return sp.getInt(READ_BG,BOOK_BG_WHITE);
    }

    public void setBookBg(int bg){
        sp.edit().putInt(READ_BG,bg).commit();
    }

    public float getFontSize(){
        if (fontSize == 0){
            fontSize = sp.getFloat(FONT_SIZE, context.getResources().getDimension(R.dimen.read_default_text_size));
        }
        return fontSize;
    }

    public void setFontSize(float fontSize){
        this.fontSize = fontSize;
        sp.edit().putFloat(FONT_SIZE,fontSize).commit();
    }

    /**
     * @return true：夜间模式，false：日间模式
     */
    public boolean isNight() {
        return sp.getBoolean(IS_NIGHT, false);
    }

    public void setNight(boolean night) {
        sp.edit().putBoolean(IS_NIGHT,night).commit();
    }

    public float getLight(){
        return sp.getFloat(LIGHT,0);
    }

    public void setLight(float light) {
        this.light = light;
        sp.edit().putFloat(LIGHT,light).commit();
    }
}
