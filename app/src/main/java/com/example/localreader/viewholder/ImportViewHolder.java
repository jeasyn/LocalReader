package com.example.localreader.viewholder;

import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.localreader.R;

/**
 * Create by xlj on 2020/11/16
 */
public class ImportViewHolder extends RecyclerView.ViewHolder {

    public View fileView;
    public TextView nameFirstWord;
    public TextView fileName;
    public TextView fileSize;
    public TextView fileTime;
    public CheckBox isSelect;
    public TextView imported;

    public ImportViewHolder(@NonNull View itemView) {

        super(itemView);
        fileView = itemView;
        nameFirstWord = itemView.findViewById(R.id.tv_add_file_bg_name);
        fileName = itemView.findViewById(R.id.tv_add_file_name);
        fileSize = itemView.findViewById(R.id.tv_add_file_size);
        fileTime = itemView.findViewById(R.id.tv_add_file_time);
        isSelect = itemView.findViewById(R.id.cb_selected_book);
        imported = itemView.findViewById(R.id.tv_imported_book);
    }
}
