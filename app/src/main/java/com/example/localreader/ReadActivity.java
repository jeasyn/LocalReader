package com.example.localreader;

import android.app.Activity;
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
    private SettingDialog mSettingDialog;
    private PageWidget bookPage;
    private TextView tv_progress;
    private RelativeLayout rl_progress;
    private TextView tv_pre;
    private SeekBar sb_progress;
    private TextView tv_next;
    private LinearLayout tv_directory;
    private LinearLayout ll_dayornight;
    private TextView tv_dayornight;
    private LinearLayout tv_setting;
    private LinearLayout bookpop_bottom;
    private RelativeLayout rl_bottom;
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

    private void initView(){
        bookPage = findViewById(R.id.bookpage);
        tv_progress = findViewById(R.id.tv_progress);
        rl_progress = findViewById(R.id.rl_progress);
        tv_pre = findViewById(R.id.tv_pre);
        sb_progress = findViewById(R.id.sb_progress);
        tv_next = findViewById(R.id.tv_next);
        tv_directory = findViewById(R.id.tv_directory);
        ll_dayornight = findViewById(R.id.ll_dayornight);
        tv_dayornight = findViewById(R.id.tv_dayornight);
        tv_setting = findViewById(R.id.tv_setting);
        bookpop_bottom = findViewById(R.id.bookpop_bottom);
        rl_bottom = findViewById(R.id.rl_bottom);
        toolbar = findViewById(R.id.toolbar);
        appbar = findViewById(R.id.appbar);

        tv_pre.setOnClickListener(this);
        tv_next.setOnClickListener(this);
        tv_directory.setOnClickListener(this);
        ll_dayornight.setOnClickListener(this);
        tv_setting.setOnClickListener(this);
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

        mSettingDialog = new SettingDialog(this);
        //获取屏幕宽高
        WindowManager manage = getWindowManager();
        Display display = manage.getDefaultDisplay();
        Point displaySize = new Point();
        display.getSize(displaySize);
        screenWidth = displaySize.x;
        screenHeight = displaySize.y;
        //保持屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //隐藏
        hideSystemUI();
        //改变屏幕亮度
//        if (!config.isSystemLight()) {
//            BrightnessUtil.setBrightness(this, config.getLight());
//        }

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
        sb_progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            float pro;
            // 触发操作，拖动
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                pro = (float) (progress / 10000.0);
                showProgress(pro);
            }

            // 表示进度条刚开始拖动，开始拖动时候触发的操作
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            // 停止拖动时候
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                pageFactory.changeProgress(pro);
            }
        });

        mSettingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                hideSystemUI();
            }
        });

        mSettingDialog.setSettingListener(new SettingDialog.SettingListener() {
            @Override
            public void changeSystemBright(Boolean isSystem, float brightness) {
//                if (!isSystem) {
//                    BrightnessUtil.setBrightness(ReadActivity.this, brightness);
//                } else {
//                    int bh = BrightnessUtil.getScreenBrightness(ReadActivity.this);
//                    BrightnessUtil.setBrightness(ReadActivity.this, bh);
//                }
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
                if (isShow){
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
                Log.e("setTouchListener", "nextPage");
                if (isShow){
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
    protected void onResume(){
        super.onResume();
        if (!isShow){
            hideSystemUI();
        }
    }

    @Override
    protected void onStop(){
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
        if (id == R.id.action_add_bookmark){//添加书签
            if (pageFactory.getCurrentPage() != null) {
                List<Bookmark> bookMarksList = LitePal.where("bookpath = ? and begin = ?", pageFactory.getBookPath(),pageFactory.getCurrentPage().getBegin() + "").find(Bookmark.class);

                if (!bookMarksList.isEmpty()){
                    Toast.makeText(ReadActivity.this, "该书签已存在", Toast.LENGTH_SHORT).show();
                }else {
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


    public static boolean openBook(final Book bookList, Activity context) {
        if (bookList == null){
            throw new NullPointerException("BookList can not be null");
        }

        Intent intent = new Intent(context, ReadActivity.class);
        intent.putExtra(EXTRA_BOOK, bookList);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        context.startActivity(intent);
        return true;
    }


    /**
     * 隐藏菜单。沉浸式阅读
     */
    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        //  | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }

    private void showSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }

    //显示书本进度
    public void showProgress(float progress){
        if (rl_progress.getVisibility() != View.VISIBLE) {
            rl_progress.setVisibility(View.VISIBLE);
        }
        setProgress(progress);
    }

    //隐藏书本进度
    public void hideProgress(){
        rl_progress.setVisibility(View.GONE);
    }

    public void initDayOrNight(){
        mDayOrNight = config.getDayOrNight();
        if (mDayOrNight){
            tv_dayornight.setText("日间");
        }else{
            tv_dayornight.setText("夜间");
        }
    }

    //改变显示模式
    public void changeDayOrNight(){
        if (mDayOrNight){
            mDayOrNight = false;
            tv_dayornight.setText("夜间");
        }else{
            mDayOrNight = true;
            tv_dayornight.setText("日间");
        }
        config.setDayOrNight(mDayOrNight);
        pageFactory.setDayOrNight(mDayOrNight);
    }

    private void setProgress(float progress){
        DecimalFormat decimalFormat=new DecimalFormat("00.00");//构造方法的字符格式这里如果小数不足2位,会以0补足.
        String p=decimalFormat.format(progress * 100.0);//format 返回的是字符串
        tv_progress.setText(p + "%");
    }

    public void setSeekBarProgress(float progress){
        sb_progress.setProgress((int) (progress * 10000));
    }

    private void showReadSetting(){
        isShow = true;
        rl_progress.setVisibility(View.GONE);
        showSystemUI();

        Animation bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_in);
        Animation topAnim = AnimationUtils.loadAnimation(this, R.anim.top_in);
        rl_bottom.startAnimation(bottomAnim);
        appbar.startAnimation(topAnim);
        rl_bottom.setVisibility(View.VISIBLE);
        appbar.setVisibility(View.VISIBLE);
    }

    private void hideReadSetting() {
        isShow = false;
        Animation bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_out);
        Animation topAnim = AnimationUtils.loadAnimation(this, R.anim.top_out);
        if (rl_bottom.getVisibility() == View.VISIBLE) {
            rl_bottom.startAnimation(bottomAnim);
        }
        if (appbar.getVisibility() == View.VISIBLE) {
            appbar.startAnimation(topAnim);
        }
        rl_bottom.setVisibility(View.GONE);
        appbar.setVisibility(View.GONE);
        hideSystemUI();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_pre:
                pageFactory.preChapter();
                break;
            case R.id.tv_next:
                pageFactory.nextChapter();
                break;
            case R.id.tv_directory:
                Intent intent = new Intent(ReadActivity.this, CatalogActivity.class);
                intent.putExtra("book_data", book);
                startActivity(intent);
                break;
            case R.id.ll_dayornight:
                changeDayOrNight();
                break;
            case R.id.tv_setting:
                hideReadSetting();
                mSettingDialog.show();
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isShow){
                hideReadSetting();
                return true;
            }
            if (mSettingDialog.isShowing()){
                mSettingDialog.hide();
                return true;
            }
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}