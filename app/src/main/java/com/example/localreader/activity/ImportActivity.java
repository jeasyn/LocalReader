package com.example.localreader.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.localreader.R;
import com.example.localreader.adapter.ImportAdapter;
import com.example.localreader.util.BookShelfUtil;
import com.example.localreader.util.FileUtil;

import java.io.File;
import java.util.List;

public class ImportActivity extends AppCompatActivity {

    public static List<File> txts;//存放sd卡所有小说文件
    private RecyclerView recyclerView;
    private ImportAdapter adapter;
    private ImageView importImg;
    private TextView importTxt;
    private ImageView deleteImg;
    private TextView deleteTxt;
    private ImageView selectImg;
    private TextView selectTxt;
    private TextView noFilePointTv;
    private List<String> bookShelfNames;
    private int actualSize; //减去书架上的个数（最多选中文件个数）
    private boolean isDeleteAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);


        initView();
        initData();

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("本地扫描");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_title_back);
        toolbar.setNavigationOnClickListener((v)->finish());
    }

    public void initView() {

        recyclerView = findViewById(R.id.rv_import_book);

        importImg = findViewById(R.id.iv_import_bottom_import);
        importTxt = findViewById(R.id.tv_import_bottom_import);

        deleteImg = findViewById(R.id.iv_import_bottom_delete);
        deleteTxt = findViewById(R.id.tv_import_bottom_delete);

        selectImg = findViewById(R.id.iv_import_bottom_select);
        selectTxt = findViewById(R.id.tv_import_bottom_select);

        noFilePointTv = findViewById(R.id.tv_no_file);
    }

    public void initData() {
        //获取所有txt文件
        String path = Environment.getExternalStorageDirectory().toString();
        txts = FileUtil.getLocalTXT(new File(path));

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ImportAdapter(txts);
        recyclerView.setAdapter(adapter);

        adapter.setCheckedChangeListener(mCheckedChangeListener);

        //将导入到书架上的书状态变成"已导入"
        bookShelfNames = BookShelfUtil.getBookShelfName();
        adapter.getBookShelfNames(bookShelfNames);

        showNoFilePoint();

        changeBtnState();
    }

    public void showNoFilePoint() {
        noFilePointTv.setVisibility(txts.size() == 0 ? View.VISIBLE : View.GONE);
    }

    private void changeBtnState() {
        actualSize = txts.size() - bookShelfNames.size();
        int selectSize = adapter.getSelectNum();
        if (selectSize == actualSize && selectSize != 0) {
            selectTxt.setText("全不选");
            selectImg.setImageResource(R.drawable.ic_bottom_select_all);
        } else if (BookShelfUtil.queryBooks().size() == txts.size()) {
            selectTxt.setText("全选");
            selectImg.setImageResource(R.drawable.ic_bottom_no_select);
            selectTxt.setTextColor(getResources().getColor(R.color.freeze_color));
        } else {
            selectTxt.setText("全选");
            selectImg.setImageResource(R.drawable.ic_bottom_select_all_cancel);
        }

        if (selectSize != 0 && !isDeleteAll) {
            importImg.setSelected(true);
            importTxt.setTextColor(getResources().getColor(R.color.black_color));
            deleteImg.setSelected(true);
            deleteTxt.setTextColor(getResources().getColor(R.color.black_color));
        } else {//冻结状态
            importImg.setSelected(false);
            importTxt.setTextColor(getResources().getColor(R.color.freeze_color));
            deleteImg.setSelected(false);
            deleteTxt.setTextColor(getResources().getColor(R.color.freeze_color));
        }
    }

    ImportAdapter.CheckedChangeListener mCheckedChangeListener = new ImportAdapter.CheckedChangeListener() {
        @Override
        public void onCheckedChanged(int position, CompoundButton buttonView, boolean isChecked) {
            changeBtnState();
        }
    };

    //导入书架按钮监听
    public void importListener(View v) {
        if (adapter.getSelectNum() != 0) {
            List<File> selectFiles = adapter.getSelectFile();
            BookShelfUtil.importBooks(selectFiles);
            initData();
            refreshData();
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    //删除按钮监听（删除时也要删除书架上的）
    public void deleteListener(View v) {
        if (adapter.getSelectNum() != 0) {
            //删除前弹出确认对话框
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("警告");
            dialog.setMessage("确定删除选中的文件？");
            dialog.setCancelable(false);//设置为false时，点击返回键无效
            dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    List<File> deleteFileList = adapter.getSelectFile();
                    txts.addAll(FileUtil.deleteTXT(txts, deleteFileList));
                    if (txts.size() == 0) {
                        isDeleteAll = true;
                        showNoFilePoint();
                    }
                    changeBtnState();
                    refreshData();
                }
            });
            dialog.setNegativeButton("取消", null);
            dialog.show();
        }
    }

    //全选按钮监听（全选or全不选）
    public void selectListener(View v) {
        if (BookShelfUtil.queryBooks().size() == txts.size()) return;
        if (adapter.getSelectNum() != actualSize) {
            adapter.selectAll();
        } else {
            adapter.unSelectAll();
        }
        changeBtnState();
        refreshData();
    }


    //通知adapter数据已发生变化
    public void refreshData() {
        adapter.notifyDataSetChanged();
    }
}
