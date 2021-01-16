package com.example.localreader.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.localreader.R;
import com.example.localreader.util.FileUtil;
import com.example.localreader.viewholder.ImportViewHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Create by xlj on 2020/11/07
 */
public class ImportAdapter extends RecyclerView.Adapter<ImportViewHolder> {

    private List<File> files;
    private HashMap<File, Boolean> selectMap = new HashMap<>();
    private List<String> names;

    private CheckedChangeListener mCheckedChangeListener;

    public ImportAdapter(List<File> files) {
        this.files = files;
    }

    //全选
    public void selectAll() {
        initCheck(true);
    }

    //全不选
    public void unSelectAll() {
        initCheck(false);
    }

    //更改集合存储状态（排除已导入的）
    public void initCheck(boolean states) {
        for (File file : files) {
            boolean flag = true;
            for (String name : names) {
                if (name.equals(file.getName())) {
                    flag = false;
                }
            }
            if (flag) {
                selectMap.put(file, states);
            }
        }
        //集合元素改变后，可以动态更新
        notifyDataSetChanged();
    }

    //被选中的个数
    public int getSelectNum() {
        return getSelectFile().size();
    }

    //被选中的文件集合
    public List<File> getSelectFile() {
        List<File> fileList = new ArrayList<>();
        for (File file : selectMap.keySet()) {
            boolean flag = true;
            if (names.size() != 0) {
                for (String name : names) {
                    if (!selectMap.get(file) || name.equals(file.getName())) {
                        flag = false;
                    }
                }
            } else {
                if (!selectMap.get(file)) {
                    flag = false;
                }
            }
            if (flag) {
                fileList.add(file);
            }
        }
        return fileList;
    }

    //获取添加到书架的书名集合
    public void getBookShelfNames(List<String> names) {
        this.names = names;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ImportViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycleview_import_book, parent, false);
        return new ImportViewHolder(view);
    }

    /*
     由于RecyclerView的onBindViewHolder()方法，只有在getItemViewType()返回类型不同时才会调用，
     这点是跟ListView的getView()方法不同的地方，所以如果想要每次都调用onBindViewHolder()刷新item数据，
     就要重写getItemViewType()，让其返回position，否则很容易产生数据错乱的现象。
     */
    @Override
    public int getItemViewType(int position) {
        return position;
    }

    //每次滑动都会调用
    @Override
    public void onBindViewHolder(@NonNull ImportViewHolder holder, final int position) {
        final File file = files.get(position);
        holder.fileView.setTag(file);
        holder.fileView.setOnClickListener(mOnItemClickListener);

        //将导入书架的状态改变
        for (String name : names) {
            if (name.equals(file.getName())) {
                holder.isSelect.setVisibility(View.GONE);
                holder.imported.setVisibility(View.VISIBLE);
            }
        }

        holder.fileName.setText(file.getName());
        holder.fileSize.setText(FileUtil.formatFileSize(file.length()));
        holder.fileTime.setText(FileUtil.formatFileTime(file.lastModified()));
        holder.isSelect.setChecked(FileUtil.isChecked(selectMap, file.getName()));


        //给单选按钮设置监听
        holder.isSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                selectMap.put(file, isChecked);
                if (mCheckedChangeListener != null) {
                    mCheckedChangeListener.onCheckedChanged(position, buttonView, isChecked);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public View.OnClickListener mOnItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            File file = (File) v.getTag();
            if (!FileUtil.isChecked(selectMap, file.getName())) {//不是选中状态
                selectMap.put(file, true);
            } else {
                selectMap.put(file, false);
            }
            notifyDataSetChanged();
        }
    };

    public interface CheckedChangeListener {
        void onCheckedChanged(int position, CompoundButton buttonView, boolean isChecked);
    }

    public void setCheckedChangeListener(CheckedChangeListener checkedChangeListener) {
        mCheckedChangeListener = checkedChangeListener;
    }
}
