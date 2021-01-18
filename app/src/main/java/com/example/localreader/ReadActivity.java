package com.example.localreader;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.SQLException;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.localreader.entity.Book;
import com.example.localreader.entity.Bookmark;
import com.example.localreader.entity.Config;
import com.example.localreader.util.PageFactory;
import com.example.localreader.view.PageWidget;
import com.example.localreader.view.SettingDialog;
import com.google.android.material.appbar.AppBarLayout;

import org.litepal.LitePal;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ReadActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "ReadActivity";
    private final static String EXTRA_BOOK = "book";
    private final static int MESSAGE_CHANGEPROGRESS = 1;

    private Config config;
    private Book book;
    private PageFactory pageFactory;
    private int screenWidth, screenHeight;
    // popwindow是否显示
    private Boolean isShow = false;
    private Boolean mDayOrNight;
    private SettingDialog settingsDetail;
    private PageWidget bookPage;
    private TextView showProgressTv;
    private RelativeLayout showProgressRl;
    private TextView preTv;
    private SeekBar chapterProgressSb;
    private TextView nextTv;
    private LinearLayout catalogTv;
    private LinearLayout dayornightLayout;
    private TextView dayornightTv;
    private LinearLayout settingLayout;
    private RelativeLayout readBottomRl;
    private Toolbar toolbar;
    private AppBarLayout appbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);

        initView();
        initData();
        initListener();
    }

    private void initView() {
        bookPage = findViewById(R.id.bookPage);
        showProgressTv = findViewById(R.id.tv_show_progress);
        showProgressRl = findViewById(R.id.rl_show_progress);
        preTv = findViewById(R.id.tv_read_pre);
        chapterProgressSb = findViewById(R.id.sb_chapter_progress);
        nextTv = findViewById(R.id.tv_read_next);
        catalogTv = findViewById(R.id.tv_read_catalog);
        dayornightLayout = findViewById(R.id.ll_read_day_or_night);
        dayornightTv = findViewById(R.id.tv_read_day_or_night);
        settingLayout = findViewById(R.id.ll_read_setting);
        readBottomRl = findViewById(R.id.rl_read_bottom);
        toolbar = findViewById(R.id.toolbar);
        appbar = findViewById(R.id.appbar);

        preTv.setOnClickListener(this);
        nextTv.setOnClickListener(this);
        catalogTv.setOnClickListener(this);
        dayornightLayout.setOnClickListener(this);
        settingLayout.setOnClickListener(this);
    }

    private void initData() {
        Intent intent = getIntent();
        book = (Book) intent.getSerializableExtra("book_data");

        toolbar.setTitle(book.getBookName().split(".txt")[0]);
        setSupportActionBar(toolbar);//一定要放在setNavigationOnClickListener的前面，否则点击事件不会被响应
        toolbar.setNavigationIcon(R.drawable.ic_title_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        config = Config.getInstance();
        pageFactory = PageFactory.getInstance();

        settingsDetail = new SettingDialog(this);
        //获取屏幕宽高
        WindowManager manage = getWindowManager();
        Display display = manage.getDefaultDisplay();
        Point displaySize = new Point();
        display.getSize(displaySize);
        screenWidth = displaySize.x;
        screenHeight = displaySize.y;
        //保持屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //初始化屏幕亮度
        pageFactory.changeBrightness(this, config.getLight());
        //隐藏
        hideSystemUI();

        pageFactory.setPageWidget(bookPage);

        try {
            pageFactory.openBook(book);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "打开电子书失败", Toast.LENGTH_SHORT).show();
        }

        initDayOrNight();
    }

    private void initListener() {
        chapterProgressSb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            float pro;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                pro = (float) (progress / 10000.0);
                showProgress(pro);
            }

            //开始拖动进度条监听
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            //停止拖动监听
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                pageFactory.changeProgress(pro);
            }
        });

        settingsDetail.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                hideSystemUI();
            }
        });

        settingsDetail.setSettingListener(new SettingDialog.SettingListener() {
            @Override
            public void changeSystemBright(float brightness) {
                pageFactory.changeBrightness(ReadActivity.this, brightness);
                Log.d("brightness",brightness+" "+pageFactory.getBrightness(ReadActivity.this));
            }

            @Override
            public void changeFontSize(int fontSize) {
                pageFactory.changeFontSize(fontSize);
            }

            @Override
            public void changeBookBg(int type) {
                pageFactory.changeBookBg(type);
            }
        });

        pageFactory.setPageEvent(new PageFactory.PageEvent() {
            @Override
            public void changeProgress(float progress) {
                Message message = new Message();
                message.what = MESSAGE_CHANGEPROGRESS;
                message.obj = progress;
                mHandler.sendMessage(message);
            }
        });

        bookPage.setTouchListener(new PageWidget.TouchListener() {
            @Override
            public void center() {
                if (isShow) {
                    hideReadSetting();
                } else {
                    showReadSetting();
                }
            }

            @Override
            public Boolean prePage() {
                if (isShow) {
                    return false;
                }

                pageFactory.prePage();
                if (pageFactory.isFirstPage()) {
                    return false;
                }
                return true;
            }

            @Override
            public Boolean nextPage() {
                if (isShow) {
                    return false;
                }

                pageFactory.nextPage();
                if (pageFactory.isLastPage()) {
                    return false;
                }
                return true;
            }

            @Override
            public void cancel() {
                pageFactory.cancelPage();
            }
        });

    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MESSAGE_CHANGEPROGRESS:
                    float progress = (float) msg.obj;
                    setSeekBarProgress(progress);
                    break;
            }
        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        if (!isShow) {
            hideSystemUI();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pageFactory.clear();
        bookPage = null;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.read_title_add_bookmark, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add_bookmark) {//添加书签
            if (pageFactory.getCurrentPage() != null) {
                List<Bookmark> bookMarksList = LitePal.where("bookpath = ? and begin = ?", pageFactory.getBookPath(), pageFactory.getCurrentPage().getBegin() + "").find(Bookmark.class);

                if (!bookMarksList.isEmpty()) {
                    Toast.makeText(ReadActivity.this, "该书签已存在", Toast.LENGTH_SHORT).show();
                } else {
//                    item.setIcon(R.drawable.selected_bookmark);
                    Bookmark bookMarks = new Bookmark();
                    String word = "";
                    for (String line : pageFactory.getCurrentPage().getLines()) {
                        word += line;
                    }
                    try {
                        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm ss");
                        String time = sf.format(new Date());
                        bookMarks.setTime(time);
                        bookMarks.setBegin(pageFactory.getCurrentPage().getBegin());
                        bookMarks.setText(word);
                        bookMarks.setBookPath(pageFactory.getBookPath());
                        bookMarks.save();

                        Toast.makeText(ReadActivity.this, "书签添加成功", Toast.LENGTH_SHORT).show();
                    } catch (SQLException e) {
                        Toast.makeText(ReadActivity.this, "该书签已存在", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(ReadActivity.this, "添加书签失败", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 隐藏菜单。沉浸式阅读
     */
    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }

    private void showSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }

    //拖动进度条时显示书本进度
    public void showProgress(float progress) {
        if (showProgressRl.getVisibility() != View.VISIBLE) {
            showProgressRl.setVisibility(View.VISIBLE);
        }
        setProgress(progress);
    }

    public void initDayOrNight() {
        mDayOrNight = config.getDayOrNight();
        if (mDayOrNight) {
            dayornightTv.setText("日间");
        } else {
            dayornightTv.setText("夜间");
        }
    }

    //改变显示模式
    public void changeDayOrNight() {
        if (mDayOrNight) {
            mDayOrNight = false;
            dayornightTv.setText("夜间");
        } else {
            mDayOrNight = true;
            dayornightTv.setText("日间");
        }
        config.setDayOrNight(mDayOrNight);
        pageFactory.setDayOrNight(mDayOrNight);
    }

    private void setProgress(float progress) {
        DecimalFormat decimalFormat = new DecimalFormat("00.00");//构造方法的字符格式这里如果小数不足2位,会以0补足.
        String p = decimalFormat.format(progress * 100.0);//format 返回的是字符串
        showProgressTv.setText(p + "%");
    }

    public void setSeekBarProgress(float progress) {
        chapterProgressSb.setProgress((int) (progress * 10000));
    }

    private void showReadSetting() {
        isShow = true;
        showProgressRl.setVisibility(View.GONE);
        showSystemUI();

        Animation bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_in);
        Animation topAnim = AnimationUtils.loadAnimation(this, R.anim.top_in);
        readBottomRl.startAnimation(bottomAnim);
        appbar.startAnimation(topAnim);
        readBottomRl.setVisibility(View.VISIBLE);
        appbar.setVisibility(View.VISIBLE);
    }

    private void hideReadSetting() {
        isShow = false;
        Animation bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_out);
        Animation topAnim = AnimationUtils.loadAnimation(this, R.anim.top_out);
        if (readBottomRl.getVisibility() == View.VISIBLE) {
            readBottomRl.startAnimation(bottomAnim);
        }
        if (appbar.getVisibility() == View.VISIBLE) {
            appbar.startAnimation(topAnim);
        }
        readBottomRl.setVisibility(View.GONE);
        appbar.setVisibility(View.GONE);
        hideSystemUI();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_read_pre:
                pageFactory.preChapter();
                break;
            case R.id.tv_read_next:
                pageFactory.nextChapter();
                break;
            case R.id.tv_read_catalog:
                Intent intent = new Intent(ReadActivity.this, CatalogActivity.class);
                intent.putExtra("book_data", book);
                startActivity(intent);
                break;
            case R.id.ll_read_day_or_night:
                changeDayOrNight();
                break;
            case R.id.ll_read_setting:
                hideReadSetting();
                settingsDetail.show();
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isShow) {
                hideReadSetting();
                return true;
            }
            if (settingsDetail.isShowing()) {
                settingsDetail.hide();
                return true;
            }
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}