package com.example.localreader;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.localreader.adapter.BookShelfAdapter;
import com.example.localreader.entity.Book;
import com.example.localreader.entity.Bookmark;
import com.example.localreader.entity.Config;
import com.example.localreader.util.BookUtil;
import com.example.localreader.util.FileUtil;
import com.example.localreader.util.PageFactory;

import org.litepal.LitePal;

import java.io.File;
import java.util.List;

/**
 * Created by xialijuan on 2020/12/05.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private List<Book> books;
    private BookShelfAdapter adapter;
    private RecyclerView bookShelfRv;
    private LinearLayout bottomLayout;
    private ImageView deleteImg;
    private TextView deleteTv;
    private ImageView selectImg;
    private TextView selectTv;
    private ImageView detailImg;
    private TextView detailTv;
    private TextView filePathTv;
    private TextView fileSizeTv;
    private TextView fileTimeTv;
    private View view;
    private LayoutInflater inflater;
    private boolean hideBottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //创建LitePal数据库
        LitePal.getDatabase();
        Config.createConfig(this);
        PageFactory.createPageFactory(this);

        //申请读取文件权限
        verifyStoragePermissions(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //引入popup_window_book_detail布局，否则弹出详细信息时，会找不到子控件
        inflater = getLayoutInflater();
        view = inflater.inflate(R.layout.popup_window_book_detail, null);

        setTitle(getString(R.string.main_toolbar_title));

        initView();
        initData();
    }

    private void initData() {
        books = LitePal.findAll(Book.class);
        bookShelfRv.setLayoutManager(new GridLayoutManager(MainActivity.this, 3));
        adapter = new BookShelfAdapter(books, this);

        bookShelfRv.setAdapter(adapter);

        adapter.setOnItemClickListener(mOnItemClickListener);
        adapter.setItemLongClickListener(mItemLongClickListener);
        adapter.setOnItemClickSelectListener(mItemSelectedClickListener);
        adapter.setImportListener(mImportListener);
    }

    private void changeBtnState() {
        // 选中个数
        int selectSize = adapter.getSelectSize();
        // 可选的最大个数
        int maxSize = books.size();
        if (selectSize == maxSize && selectSize != 0) {
            selectTv.setText(getResources().getString(R.string.main_no_select_all));
            selectImg.setImageResource(R.drawable.main_bottom_select_all);
        } else if (books.size() == 0) {
            selectImg.setImageResource(R.drawable.main_bottom_no_select);
            selectTv.setTextColor(ContextCompat.getColor(this, R.color.main_bottom_freeze_color));
        } else {
            selectTv.setText(getResources().getString(R.string.main_select_all));
            selectImg.setImageResource(R.drawable.main_bottom_select_all_cancel);
        }

        if (selectSize == 0) {
            deleteImg.setImageResource(R.drawable.main_bottom_no_delete);
            deleteTv.setTextColor(ContextCompat.getColor(this, R.color.main_bottom_freeze_color));
        } else {
            deleteImg.setImageResource(R.drawable.main_bottom_delete);
            deleteTv.setTextColor(Color.BLACK);
        }

        if (selectSize == 1) {
            detailImg.setImageResource(R.drawable.main_bottom_detail);
            detailTv.setTextColor(Color.BLACK);
        } else {
            detailImg.setImageResource(R.drawable.main_bottom_no_detail);
            detailTv.setTextColor(ContextCompat.getColor(this, R.color.main_bottom_freeze_color));
        }
    }

    private View.OnClickListener mDeleteBookListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (adapter.getSelectSize() == 0) {
                return;
            }
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("警告")
                    .setMessage("确认删除选中的书籍？")
                    .setNegativeButton("取消", null)
                    .setPositiveButton("确定", (dialog, which) -> {
                        List<Book> selectBooks = adapter.getSelectBook();
                        books.removeAll(selectBooks);
                        for (Book book : selectBooks) {
                            LitePal.delete(Book.class, book.getId());
                        }
                        BookUtil.deleteBookmarks(selectBooks);
                        if (books.size() == 0) {
                            hideBottomLayout();
                        }
                        changeBtnState();
                        adapter.setBookList(books);
                    }).show();
        }
    };

    private View.OnClickListener mCancelListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            hideBottomLayout();
            //设置为默认状态
            selectTv.setText(getResources().getString(R.string.main_select_all));
            selectImg.setImageResource(R.drawable.main_bottom_select_all_cancel);
            detailImg.setImageResource(R.drawable.main_bottom_detail);
            detailTv.setTextColor(Color.BLACK);
        }
    };
    private View.OnClickListener mSelectListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (getResources().getString(R.string.main_select_all).equals(selectTv.getText())) {
                adapter.selectAll();
            } else {
                adapter.unSelectAll();
            }
            changeBtnState();
        }
    };
    private View.OnClickListener mDetailListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (adapter.getSelectSize() == 1) {
                Book book = adapter.getSelectBook().get(0);
                File file = FileUtil.getFileByName(book.getBookName());
                filePathTv.setText(file.getPath());
                fileSizeTv.setText(FileUtil.formatFileSize(file.length()));
                fileTimeTv.setText(FileUtil.formatFileTime(file.lastModified()));
                popupWindow();
            }
        }
    };
    /**
     * 打开图书
     */
    private View.OnClickListener mOnItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            final int bookId = (Integer) v.getTag();
            final Book book = LitePal.findAll(Book.class, bookId).get(0);

            if (new File(book.getBookPath()).exists()) {
                Intent intent = new Intent(MainActivity.this, ReadActivity.class);
                intent.putExtra("book_data", book);
                startActivity(intent);
            } else {
                // 当已放入书架的书被删除时，弹出此对话框
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("警告")
                        .setMessage("书籍不存在，是否删除该书籍")
                        .setNegativeButton("取消", null)
                        .setPositiveButton("确定", (dialog, which) -> {
                            LitePal.delete(Book.class, bookId);
                            List<Book> all = LitePal.findAll(Book.class);
                            LitePal.deleteAll(Bookmark.class, "bookPath = ?", book.getBookPath());
                            adapter.setBookList(all);
                        }).show();
            }
        }
    };

    /**
     * 导入图书
     */
    private View.OnClickListener mImportListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!adapter.isShowItem()) {//底部菜单弹出来后，不能添加图书
                startActivity(new Intent(MainActivity.this, ImportActivity.class));
            }
        }
    };

    /**
     * 长按图书监听
     */
    private View.OnLongClickListener mItemLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            int bookId = (Integer) v.getTag();
            showBottomLayout();
            adapter.bookSelect(bookId);
            changeBtnState();
            // 返回false：执行完长按事件后，还要执行单击事件
            return false;
        }
    };

    /**
     * 监听选中图书个数
     */
    private View.OnClickListener mItemSelectedClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            adapter.selectBook((Integer) v.getTag());
            changeBtnState();
        }
    };

    /**
     * 显示底部菜单
     */
    private void showBottomLayout() {
        hideBottom = true;
        Animation mAnimationBottomIn = AnimationUtils.loadAnimation(MainActivity.this, R.anim.bottom_in);
        bottomLayout.setVisibility(View.VISIBLE);
        bottomLayout.startAnimation(mAnimationBottomIn);
    }

    /**
     * 隐藏底部菜单
     */
    private void hideBottomLayout() {
        hideBottom = false;
        // 隐藏复选框
        adapter.showState(false);
        Animation mAnimationBottomOut = AnimationUtils.loadAnimation(MainActivity.this, R.anim.bottom_out);
        bottomLayout.setVisibility(View.GONE);
        bottomLayout.startAnimation(mAnimationBottomOut);
    }

    /**
     * 导入时直接隐藏，无需动画
     */
    public void directHideBottom() {
        adapter.showState(false);
        bottomLayout.setVisibility(View.GONE);
    }

    /**
     * 弹出详细信息菜单
     */
    private void popupWindow() {
        // 获取屏幕宽高
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int width = displayMetrics.widthPixels;
        PopupWindow popupWindow = new PopupWindow(view, 2 * width / 3, WindowManager.LayoutParams.WRAP_CONTENT);
        // 点击空白处，隐藏popup窗口
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
        // 创建当前界面的一个参数对象
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.alpha = 0.3f;
        getWindow().setAttributes(params);
        popupWindow.setOnDismissListener(() -> {
            WindowManager.LayoutParams params1 = getWindow().getAttributes();
            params1.alpha = 1.0f;
            getWindow().setAttributes(params1);
        });
        popupWindow.showAtLocation(inflater.inflate(R.layout.activity_main, null), Gravity.CENTER, 0, 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.title_import_book, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.add) {
            startActivity(new Intent(this, ImportActivity.class));
            directHideBottom();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (hideBottom) {
                hideBottomLayout();
            } else {
                exit();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private long exitTime = 0;

    private void exit() {
        long endTime = 2000;
        //两秒内如果没有再次按下，则不会退出
        if ((System.currentTimeMillis() - exitTime) > endTime) {
            Toast.makeText(MainActivity.this, getResources().getString(R.string.back_app), Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
            System.exit(0);
        }
    }

    /**
     * 动态申请SD卡读写的权限，Android6.0之后系统对权限的管理更加严格了，不但要在AndroidManifest中添加，还要在应用运行的时候动态申请
     */
    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    private static String[] PERMISSION_STORAGE = {"android.permission.READ_EXTERNAL_STORAGE"};

    public static void verifyStoragePermissions(Activity activity) {
        try {
            int permission = ActivityCompat.checkSelfPermission(activity, "android.permission.WRITE_EXTERNAL_STORAGE");
            // 判断是否已经授予权限
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, PERMISSION_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                // 拒绝授予权限，弹出框，让用户去应用详情页手动设置权限
                new AlertDialog.Builder(this)
                        .setTitle("警告")
                        .setMessage("存储权限是必须的，若拒绝，则部分功能无法正常运行！")
                        .setPositiveButton("确定", (dialog, which) -> {
                            Uri uri = Uri.parse("package:" + getPackageName());
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri);
                            startActivity(intent);
                        }).show();
            }
        }
    }

    private void initView() {
        LinearLayout deleteLayout = findViewById(R.id.ll_main_bottom_delete);
        LinearLayout cancelLayout = findViewById(R.id.ll_main_bottom_cancel);
        LinearLayout selectLayout = findViewById(R.id.ll_main_bottom_select_all);
        LinearLayout detailLayout = findViewById(R.id.ll_main_bottom_detail);
        bookShelfRv = findViewById(R.id.rv_book_shelf);
        bottomLayout = findViewById(R.id.ll_main_bottom);
        deleteImg = findViewById(R.id.iv_main_bottom_delete);
        deleteTv = findViewById(R.id.tv_main_bottom_delete);
        selectImg = findViewById(R.id.iv_main_bottom_select_all);
        selectTv = findViewById(R.id.tv_main_bottom_select_all);
        detailImg = findViewById(R.id.iv_main_bottom_detail);
        detailTv = findViewById(R.id.tv_main_bottom_detail);
        filePathTv = view.findViewById(R.id.tv_file_path);
        fileSizeTv = view.findViewById(R.id.tv_file_size);
        fileTimeTv = view.findViewById(R.id.tv_file_time);
        deleteLayout.setOnClickListener(mDeleteBookListener);
        cancelLayout.setOnClickListener(mCancelListener);
        selectLayout.setOnClickListener(mSelectListener);
        detailLayout.setOnClickListener(mDetailListener);
    }
}
