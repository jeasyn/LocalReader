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
 * @author xialijuan
 * @date 2020/11/07
 */
public class ImportAdapter extends RecyclerView.Adapter<ImportViewHolder> {

    /**
     * 扫描到的sd卡中的所有txt文件
     */
    private List<File> files;
    /**
     * 选中的txt文件和对应状态集合
     */
    private HashMap<File, Boolean> selectMap = new HashMap<>();
    /**
     * 已导入书架的名字集合
     */
    private List<String> names;

    private CheckedChangeListener mCheckedChangeListener;


    public ImportAdapter(List<File> files) {
        this.files = files;
    }

    /**
     * 全选
     */
    public void selectAll() {
        initCheck(true);
    }

    /**
     * 全不选
     */
    public void unSelectAll() {
        initCheck(false);
    }

    /**
     * 更改集合存储状态（排除已导入的）
     *
     * @param states 选中状态
     */
    private void initCheck(boolean states) {
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
        // 集合元素改变后，可以动态更新
        notifyDataSetChanged();
    }

    /**
     * @return 被选中的个数
     */
    public int getSelectNum() {
        return getSelectFile().size();
    }

    /**
     * @return 被选中的文件集合
     */
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

    /**
     * 获取添加到书架的书名集合
     *
     * @param names 书名集合
     */
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

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(@NonNull ImportViewHolder holder, final int position) {
        final File file = files.get(position);
        holder.fileView.setTag(file);
        holder.fileView.setOnClickListener(mOnItemClickListener);

        // 将导入书架的状态改变
        for (String name : names) {
            if (name.equals(file.getName())) {
                holder.isSelectCb.setVisibility(View.GONE);
                holder.importedTv.setVisibility(View.VISIBLE);
            }
        }
        holder.fileNameTv.setText(file.getName());
        holder.fileSizeTv.setText(FileUtil.formatFileSize(file.length()));
        holder.fileTimeTv.setText(FileUtil.formatFileTime(file.lastModified()));
        holder.isSelectCb.setChecked(FileUtil.isChecked(selectMap, file.getName()));

        // 给单选按钮设置监听
        holder.isSelectCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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

    private View.OnClickListener mOnItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            File file = (File) v.getTag();
            if (!FileUtil.isChecked(selectMap, file.getName())) {
                // 不是选中状态
                selectMap.put(file, true);
            } else {
                selectMap.put(file, false);
            }
            notifyDataSetChanged();
        }
    };

    public interface CheckedChangeListener {
        /**
         * checkbox状态监听
         *
         * @param position   当前位置
         * @param buttonView 状态已更改的复合按钮视图
         * @param isChecked  单选按钮状态
         */
        void onCheckedChanged(int position, CompoundButton buttonView, boolean isChecked);
    }

    public void setCheckedChangeListener(CheckedChangeListener checkedChangeListener) {
        mCheckedChangeListener = checkedChangeListener;
    }
}
