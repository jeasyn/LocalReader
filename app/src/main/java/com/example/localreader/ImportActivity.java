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
import com.example.localreader.util.BookShelfUtil;
import com.example.localreader.util.FileUtil;

import org.litepal.LitePal;

import java.io.File;
import java.util.List;

public class ImportActivity extends AppCompatActivity {

    public static List<File> sdCardFiles;//存放sd卡所有小说文件
    private RecyclerView recyclerView;
    private ImportAdapter adapter;
    private TextView noFilePointTv;
    private List<String> bookShelfNames;
    private int actualSize; //减去书架上的个数（最多选中文件个数）
    private Button importBookshelfBtn;

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
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void initView() {
        recyclerView = findViewById(R.id.rv_import_book);
        noFilePointTv = findViewById(R.id.tv_no_file);
        importBookshelfBtn = findViewById(R.id.btn_import_book_self);
    }

    public void initData() {
        //获取所有txt文件
        String path = Environment.getExternalStorageDirectory().toString();
        sdCardFiles = FileUtil.getLocalTXT(new File(path));

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ImportAdapter(sdCardFiles);
        recyclerView.setAdapter(adapter);

        adapter.setCheckedChangeListener(mCheckedChangeListener);

        //将导入到书架上的书状态变成"已导入"
        bookShelfNames = BookShelfUtil.getBookShelfName();
        adapter.getBookShelfNames(bookShelfNames);

        actualSize = sdCardFiles.size() - bookShelfNames.size();

        showNoFilePoint();
    }

    public void showNoFilePoint() {
        noFilePointTv.setVisibility(sdCardFiles.size() == 0 ? View.VISIBLE : View.GONE);
    }

    ImportAdapter.CheckedChangeListener mCheckedChangeListener = new ImportAdapter.CheckedChangeListener() {
        @Override
        public void onCheckedChanged(int position, CompoundButton buttonView, boolean isChecked) {
            int selectedSize = adapter.getSelectNum();
            importBookshelfBtn.setText("导入书架(" + selectedSize + ")");
        }
    };

    //导入书架按钮监听
    public void importBookShelf(View v) {
        if (adapter.getSelectNum() != 0) {
            List<File> selectFiles = adapter.getSelectFile();
            BookShelfUtil.importBooks(selectFiles);
            initData();
            refreshData();
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    //全选按钮监听（全选or全不选）
    public void selectFiles(View v) {
        if (LitePal.findAll(Book.class).size() == sdCardFiles.size()) return;
        if (adapter.getSelectNum() != actualSize) {
            adapter.selectAll();
        } else {
            adapter.unSelectAll();
        }
        refreshData();
    }

    //通知adapter数据已发生变化
    public void refreshData() {
        adapter.notifyDataSetChanged();
    }
}
