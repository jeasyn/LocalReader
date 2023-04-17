package com.example.localreader.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.localreader.R;

/**
 * Created by xialijuan on 14/12/2020.
 */
public class BookShelfViewHolder extends BaseViewHolder {

    public View bookView;
    public TextView bookItemTv;
    public ImageView bookSelectIv;
    public TextView bookNameTv;
    public TextView bookProgressTv;

    public BookShelfViewHolder(@NonNull View itemView) {
        super(itemView);
        bookView = itemView;
        bookItemTv = itemView.findViewById(R.id.tv_book);
        bookSelectIv = itemView.findViewById(R.id.iv_book_select);
        bookNameTv = itemView.findViewById(R.id.tv_book_name);
        bookProgressTv = itemView.findViewById(R.id.tv_main_show_progress);
    }
}