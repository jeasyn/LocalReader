package com.example.localreader.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.localreader.R;

/**
 * Create by xlj on 2020/11/17
 */
public class BookShelfViewHolder extends RecyclerView.ViewHolder {

    public View bookView;
    public TextView bookItem;
    public ImageView bookSelect;
    public TextView bookName;
    public TextView bookProgress;

    public BookShelfViewHolder(@NonNull View itemView) {
        super(itemView);
        bookView = itemView;
        bookItem = itemView.findViewById(R.id.tv_book);
        bookSelect = itemView.findViewById(R.id.iv_book_select);
        bookName = itemView.findViewById(R.id.tv_book_name);
        bookProgress = itemView.findViewById(R.id.tv_show_progress);
    }
}