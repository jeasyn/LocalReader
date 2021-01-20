package com.example.localreader.adapter;

import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.localreader.R;
import com.example.localreader.entity.Book;
import com.example.localreader.util.BookShelfUtil;
import com.example.localreader.viewholder.BookShelfViewHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author xialijuan
 * @date 2020/10/26
 */
public class BookShelfAdapter extends RecyclerView.Adapter<BookShelfViewHolder> {

    private List<Book> books;
    private int[] bg = new int[]{R.drawable.book_shelf_cover_bg_1, R.drawable.book_shelf_cover_bg_2, R.drawable.book_shelf_cover_bg_3};

    private View.OnClickListener mOnItemClickListener;
    private View.OnLongClickListener mItemLongClickListener;
    private View.OnClickListener mItemSelectedClickListener;

    private boolean isShowItem = false;//是否显示底部选项和checkbox

    private SparseBooleanArray mBooleanArray;//类似hashmap

    public BookShelfAdapter(List<Book> books) {
        mBooleanArray = new SparseBooleanArray();
        this.books = books;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @NonNull
    @Override
    public BookShelfViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycleview_book_shelf, parent, false);
        return new BookShelfViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final BookShelfViewHolder holder, int position) {
        Book book = books.get(position);
        String bookName = book.getBookName().split(".txt")[0];
        holder.bookItemTv.setText(bookName);

        if (book.getBookBg() == 0) {//如果没有封面，则设置封面
            int random = new Random().nextInt(3);
            book.setBookBg(random);
            book.save();
        }
        holder.bookItemTv.setBackgroundResource(bg[book.getBookBg()]);
        holder.bookNameTv.setText(bookName);

        String progress = book.getProgress();
        if (progress.equals("0.0%")) {
            progress = "未读";
        } else if (progress.equals("100.0%")) {
            progress = "已读完";
        } else {
            progress = "已读" + progress;
        }
        holder.bookProgressTv.setText(progress);

        holder.bookView.setTag(book.getId());
        holder.bookView.setOnLongClickListener(mItemLongClickListener);
        if (isShowItem) {
            holder.bookSelectIv.setVisibility(View.VISIBLE);
            holder.bookSelectIv.setSelected(getItemSelected(book.getId()));
            holder.bookView.setOnClickListener(mItemSelectedClickListener);
        } else {
            mBooleanArray.clear();
            holder.bookSelectIv.setVisibility(View.GONE);
            holder.bookView.setOnClickListener(mOnItemClickListener);
        }
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    //点击图书监听
    public void setOnItemClickListener(View.OnClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    //全选
    public void selectAll() {
        initCheck(true);
    }

    //全不选
    public void unSelectAll() {
        initCheck(false);
    }

    public void initCheck(boolean states) {
        if (books != null) {
            for (Book book : books) {
                setItemSelected(book.getId(), states);
            }
            notifyDataSetChanged();
        }
    }

    //选中图书
    public int selectBook(int id) {
        Book book = BookShelfUtil.queryBookById(id);
        boolean isSelected = getItemSelected(book.getId());
        setItemSelected(book.getId(), !isSelected);
        notifyDataSetChanged();
        return getSelectSize();
    }

    //长按后点击图书监听
    public void setOnItemClickSelectListener(View.OnClickListener onItemClickListener) {
        this.mItemSelectedClickListener = onItemClickListener;
    }

    public void setItemLongClickListener(View.OnLongClickListener itemLongClickListener) {
        this.mItemLongClickListener = itemLongClickListener;
    }

    public void animState(boolean state) {
        isShowItem = state;
        mBooleanArray.clear();
        notifyDataSetChanged();
    }

    public void bookSelect(int id) {
        isShowItem = true;
        setItemSelected(id, true);
        notifyDataSetChanged();
    }


    public void setItemSelected(int id, boolean selected) {
        if (mBooleanArray == null) mBooleanArray = new SparseBooleanArray();
        mBooleanArray.put(id, selected);
    }

    //返回图书id是否被选中
    public boolean getItemSelected(int id) {
        if (mBooleanArray == null) return false;
        return mBooleanArray.get(id, false);
    }

    //获取被选中的id集合
    public List<Book> getSelectBook() {
        List<Book> selectBook = new ArrayList<>();
        for (Book book : books) {
            if (getItemSelected(book.getId())) {
                selectBook.add(book);
            }
        }
        return selectBook;
    }

    public int getSelectSize() {
        return getSelectBook().size();
    }
}
