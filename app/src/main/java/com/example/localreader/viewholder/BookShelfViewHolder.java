package com.example.localreader.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.localreader.R;

/**
 * @author xialijuan
 * @date 2020/11/17
 */
public class BookShelfViewHolder extends RecyclerView.ViewHolder {

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
        bookProgressTv = itemView.findViewById(R.id.tv_show_progress);
    }
}