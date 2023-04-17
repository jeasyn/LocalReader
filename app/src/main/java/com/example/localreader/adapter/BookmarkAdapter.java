package com.example.localreader.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.example.localreader.R;
import com.example.localreader.entity.Bookmark;
import com.example.localreader.util.PageFactory;
import com.example.localreader.viewholder.BookmarkViewHolder;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by xialijuan on 2021/01/02.
 */
public class BookmarkAdapter extends BaseAdapter {

    private Context context;
    /**
     * 书签集合
     */
    private List<Bookmark> bookmarks;
    private PageFactory pageFactory;

    public BookmarkAdapter(Context context, List<Bookmark> bookmarks) {
        this.context = context;
        this.bookmarks = bookmarks;
        pageFactory = PageFactory.getInstance();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Bookmark bookmark = bookmarks.get(position);
        View view;
        final BookmarkViewHolder bookmarkViewHolder;
        if (convertView == null){
            view = LayoutInflater.from(context).inflate(R.layout.item_bookmark,parent,false);
            bookmarkViewHolder = new BookmarkViewHolder();
            bookmarkViewHolder.markContentTv = view.findViewById(R.id.tv_mark_content);
            bookmarkViewHolder.markProgressTv = view.findViewById(R.id.tv_mark_progress);
            bookmarkViewHolder.markTimeTv = view.findViewById(R.id.tv_mark_time);
            // 将ViewHolder存储在View中
            view.setTag(bookmarkViewHolder);
        }else {
            view = convertView;
            // 重新获取ViewHolder
            bookmarkViewHolder = (BookmarkViewHolder) view.getTag();
        }
        bookmarkViewHolder.markContentTv.setText(bookmark.getPartContent());

        // 格式化显示书签进度
        long begin = bookmark.getFirstIndex();
        float percent = (float) (begin * 1.0 / pageFactory.getBookLen());
        DecimalFormat df = new DecimalFormat("#0.0");
        String progress = df.format(percent * 100) + "%";
        bookmarkViewHolder.markProgressTv.setText(progress);

        bookmarkViewHolder.markTimeTv.setText(bookmark.getTime());
        return view;
    }

    @Override
    public int getCount() {
        return bookmarks.size();
    }

    @Override
    public Object getItem(int position) {
        return bookmarks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
