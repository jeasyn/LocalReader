package com.example.localreader.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.localreader.R;
import com.example.localreader.entity.Book;
import com.example.localreader.listener.CheckedChangeListener;
import com.example.localreader.util.FileUtil;
import com.example.localreader.viewholder.ImportViewHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by xialijuan on 06/12/2020.
 */
public class ImportAdapter extends RecyclerView.Adapter<ImportViewHolder> {

    /**
     * 扫描到的sd卡中的所有txt文件
     */
    private List<File> sdCardFiles;
    /**
     * 选中的txt文件和对应状态集合
     */
    private HashMap<File, Boolean> selectMap = new HashMap<>();
    /**
     * 已导入书架的书籍集合
     */
    private List<Book> importedBooks;

    private CheckedChangeListener mCheckedChangeListener;

    public ImportAdapter(List<File> sdCardFiles) {
        this.sdCardFiles = sdCardFiles;
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
        for (File file : sdCardFiles) {
            boolean flag = true;
            for (Book book : importedBooks) {
                if (book.getBookName().equals(file.getName())) {
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
            if (importedBooks.size() != 0) {
                for (Book book : importedBooks) {
                    if (!selectMap.get(file) || book.getBookName().equals(file.getName())) {
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
     * 获取已添加到书架的书名集合
     *
     * @param importedBooks 已添加到书架的书名集合
     */
    public void getBookShelfNames(List<Book> importedBooks) {
        this.importedBooks = importedBooks;
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
        final File file = sdCardFiles.get(position);
        holder.fileView.setTag(file);
        holder.fileView.setOnClickListener(mOnItemClickListener);

        // 将导入书架的状态改变
        for (Book book : importedBooks) {
            if (book.getBookName().equals(file.getName())) {
                holder.isSelectCb.setVisibility(View.GONE);
                holder.importedTv.setVisibility(View.VISIBLE);
            }
        }
        holder.fileNameTv.setText(file.getName());
        holder.fileSizeTv.setText(FileUtil.formatFileSize(file.length()));
        holder.fileTimeTv.setText(FileUtil.formatFileTime(file.lastModified()));
        holder.isSelectCb.setChecked(FileUtil.isChecked(selectMap, file.getName()));

        // 给CheckBox设置监听
        holder.isSelectCb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            selectMap.put(file, isChecked);
            if (mCheckedChangeListener != null) {
                mCheckedChangeListener.onCheckedChanged(position, buttonView, isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return sdCardFiles.size();
    }

    private View.OnClickListener mOnItemClickListener = v -> {
        File file = (File) v.getTag();
        if (!FileUtil.isChecked(selectMap, file.getName())) {
            // 不是选中状态
            selectMap.put(file, true);
        } else {
            selectMap.put(file, false);
        }
        notifyDataSetChanged();
    };

    public void setCheckedChangeListener(CheckedChangeListener checkedChangeListener) {
        mCheckedChangeListener = checkedChangeListener;
    }
}
