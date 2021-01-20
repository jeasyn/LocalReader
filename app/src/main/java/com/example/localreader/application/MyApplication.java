package com.example.localreader.application;

import android.content.Context;
import android.os.Environment;

import com.example.localreader.entity.Config;
import com.example.localreader.util.PageFactory;

import org.litepal.LitePal;
import org.litepal.LitePalApplication;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author xialijuan
 * @date 2021/1/10
 */
public class MyApplication extends LitePalApplication {
    public static volatile Context applicationContext = null;
    private String mSampleDirPath;
    public static final String SAMPLE_DIR_NAME = "baiduTTS";
    public static final String TEXT_MODEL_NAME = "bd_etts_text.dat";

    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = getApplicationContext();

        LitePal.initialize(this);
        Config.createConfig(this);
        PageFactory.createPageFactory(this);

        initialEnv();
    }

    private void initialEnv() {
        if (mSampleDirPath == null) {
            String sdcardPath = Environment.getExternalStorageDirectory().toString();
            mSampleDirPath = sdcardPath + "/" + SAMPLE_DIR_NAME;
        }
        makeDir(mSampleDirPath);
        copyFromAssetsToSdcard(false, TEXT_MODEL_NAME, mSampleDirPath + "/" + TEXT_MODEL_NAME);
    }

    private void makeDir(String dirPath) {
        File file = new File(dirPath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    /**
     * 将sample工程需要的资源文件拷贝到SD卡中使用（授权文件为临时授权文件，请注册正式授权）
     *
     * @param isCover 是否覆盖已存在的目标文件
     * @param source
     * @param dest
     */
    private void copyFromAssetsToSdcard(boolean isCover, String source, String dest) {
        File file = new File(dest);
        if (isCover || !file.exists()) {
            InputStream is = null;
            FileOutputStream fos = null;
            try {
                is = getResources().getAssets().open(source);
                String path = dest;
                fos = new FileOutputStream(path);
                byte[] buffer = new byte[1024];
                int size;
                while ((size = is.read(buffer, 0, 1024)) >= 0) {
                    fos.write(buffer, 0, size);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fos != null) fos.close();
                    if (is != null) is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
