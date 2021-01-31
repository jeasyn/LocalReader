package com.example.localreader;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.localreader.adapter.ImportAdapter;
import com.example.localreader.entity.Book;
import com.example.localreader.listener.CheckedChangeListener;
import com.example.localreader.util.FileUtil;

import org.litepal.LitePal;

import java.io.File;
import java.util.List;

/**
 * @author xialijuan
 * @date 2020/12/06
 */
public class ImportActivity extends AppCompatActivity {

    /**
     * 存放sd卡所有小说文件
     */
    public static List<File> sdCardFiles;
    private RecyclerView recyclerView;
    private ImportAdapter adapter;
    private TextView noFilePointTv;
    /**
     * 减去书架的个数（最多选中文件个数）
     */
    private int actualSize;
    private Button importBookshelfBtn;
    private Button selectStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);

        initView();
        initData();

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.import_toolbar_title));
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_title_back);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    public void initData() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //先清空一下，否则会有重复的，或者可以用Set集合存储
            FileUtil.txtList.clear();
            File sdCardTxt = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
            FileUtil.getLocalTxt(sdCardTxt);
        }
        // 获取所有txt文件
        sdCardFiles = FileUtil.txtList;

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ImportAdapter(sdCardFiles);
        recyclerView.setAdapter(adapter);

        adapter.setCheckedChangeListener(mCheckedChangeListener);

        // 将导入到书架上的书状态变成"已导入"
        List<Book> bookShelf = LitePal.findAll(Book.class);
        adapter.getBookShelfNames(bookShelf);

        actualSize = sdCardFiles.size() - bookShelf.size();

        showNoFilePoint();
    }

    public void showNoFilePoint() {
        noFilePointTv.setVisibility(sdCardFiles.size() == 0 ? View.VISIBLE : View.GONE);
    }

    CheckedChangeListener mCheckedChangeListener = new CheckedChangeListener() {
        @Override
        public void onCheckedChanged(int position, CompoundButton buttonView, boolean isChecked) {
            int selectedSize = adapter.getSelectNum();
            importBookshelfBtn.setText(String.format(getString(R.string.import_import_bookshelf), String.valueOf(selectedSize)));
        }
    };

    /**
     * 导入书架按钮监听
     *
     * @param v
     */
    public void importBookShelf(View v) {
        if (adapter.getSelectNum() != 0) {
            List<File> selectFiles = adapter.getSelectFile();

            if (selectFiles.size() != 0) {
                for (File file : selectFiles) {
                    Book book = new Book();
                    book.setBookName(file.getName());
                    book.setBookPath(file.getPath());
                    book.setProgress("未读");
                    book.save();
                }
            }

            initData();
            adapter.notifyDataSetChanged();//通知adapter数据已发生变化
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    /**
     * 全选按钮监听（全选or全不选）
     *
     * @param v
     */
    public void selectFiles(View v) {
        if (LitePal.findAll(Book.class).size() == sdCardFiles.size()) {
            return;
        }
        if (adapter.getSelectNum() != actualSize) {
            selectStatus.setText(getString(R.string.main_no_select_all));
            adapter.selectAll();
        } else {
            selectStatus.setText(getString(R.string.main_select_all));
            adapter.unSelectAll();
        }
        adapter.notifyDataSetChanged();
    }

    public void initView() {
        recyclerView = findViewById(R.id.rv_import_book);
        noFilePointTv = findViewById(R.id.tv_no_file);
        importBookshelfBtn = findViewById(R.id.btn_import_book_self);
        selectStatus = findViewById(R.id.btn_select_all_or_not);
    }
}
