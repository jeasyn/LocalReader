package com.example.localreader.viewholder;

import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.localreader.R;

/**
 * @author xialijuan
 * @date 2020/11/07
 */
public class ImportViewHolder extends BaseViewHolder {

    public View fileView;
    public TextView fileNameTv;
    public TextView fileSizeTv;
    public TextView fileTimeTv;
    public CheckBox isSelectCb;
    public TextView importedTv;

    public ImportViewHolder(@NonNull View itemView) {

        super(itemView);
        fileView = itemView;
        fileNameTv = itemView.findViewById(R.id.tv_add_file_name);
        fileSizeTv = itemView.findViewById(R.id.tv_add_file_size);
        fileTimeTv = itemView.findViewById(R.id.tv_add_file_time);
        isSelectCb = itemView.findViewById(R.id.cb_selected_book);
        importedTv = itemView.findViewById(R.id.tv_imported_book);
    }
}
