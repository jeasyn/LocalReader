package com.example.localreader.entity;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.localreader.R;

/**
 * @author xialijuan
 * @date 2021/1/9
 */
public class Config {
    private final static String SP_NAME = "config";
    private final static String READ_BG_KEY = "readbg";
    private final static String FONT_SIZE_KEY = "fontsize";
    private final static String NIGHT_KEY = "night";
    private final static String LIGHT_KEY = "light";

    public final static int BOOK_BG_DEFAULT = 0;
    public final static int BOOK_BG_1 = 1;
    public final static int BOOK_BG_2 = 2;
    public final static int BOOK_BG_3 = 3;
    public final static int BOOK_BG_4 = 4;

    private Context context;
    private static Config config;
    private SharedPreferences sp;

    //字体大小
    private float fontSize = 0;
    //亮度值
    private float light = 0;
    private int bookBG;

    private Config(Context context){
        this.context = context.getApplicationContext();
        sp = this.context.getSharedPreferences(SP_NAME,Context.MODE_PRIVATE);
    }

    public static synchronized Config getInstance(){
        return config;
    }

    public static synchronized Config createConfig(Context context){
        if (config == null){
            config = new Config(context);
        }

        return config;
    }

    public int getBookBgType(){
        return sp.getInt(READ_BG_KEY,BOOK_BG_DEFAULT);
    }

    public void setBookBg(int type){
        sp.edit().putInt(READ_BG_KEY,type).commit();
    }


    public float getFontSize(){
        if (fontSize == 0){
            fontSize = sp.getFloat(FONT_SIZE_KEY, context.getResources().getDimension(R.dimen.reading_default_text_size));
        }
        return fontSize;
    }

    public void setFontSize(float fontSize){
        this.fontSize = fontSize;
        sp.edit().putFloat(FONT_SIZE_KEY,fontSize).commit();
    }

    /**
     * 获取夜间还是白天阅读模式,true为夜晚，false为白天
     */
    public boolean getDayOrNight() {
        return sp.getBoolean(NIGHT_KEY, false);
    }

    public void setDayOrNight(boolean isNight){
        sp.edit().putBoolean(NIGHT_KEY,isNight).commit();
    }

    public float getLight(){
        if (light == 0){
            light = sp.getFloat(LIGHT_KEY,0.1f);
        }
        return light;
    }
    /**
     * 记录配置文件中亮度值
     */
    public void setLight(float light) {
        this.light = light;
        sp.edit().putFloat(LIGHT_KEY,light).commit();
    }
}
