package com.example.localreader.adapter;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.localreader.R;
import com.example.localreader.entity.Book;
import com.example.localreader.viewholder.BaseViewHolder;
import com.example.localreader.viewholder.BookShelfViewHolder;
import com.example.localreader.viewholder.ImportViewHolder;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by xialijuan on 14/12/2020.
 */
public class BookShelfAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    private Context context;
    private List<Book> books;
    /**
     * 可以打开图书或显示图书
     */
    private final static int OPEN_BOOK = 0;
    /**
     * 导入图书到书架
     */
    private final static int IMPORT_BOOK = 1;
    /**
     * 是否显示底部选项和checkbox
     */
    private boolean showItem = false;
    /**
     * 存储选中的图书，类似HashMap
     */
    private SparseBooleanArray booleanArray;

    private int[] bg = new int[]{R.drawable.book_shelf_cover_bg_1,
            R.drawable.book_shelf_cover_bg_2, R.drawable.book_shelf_cover_bg_3};

    private View.OnClickListener mOnItemClickListener;
    private View.OnLongClickListener mItemLongClickListener;
    private View.OnClickListener mItemSelectedClickListener;
    private View.OnClickListener mImportListener;

    public BookShelfAdapter(List<Book> books, Context context) {
        booleanArray = new SparseBooleanArray();
        this.context = context;
        this.books = books;
    }

    /**
     * 执行删除操作后，调用该方法
     *
     * @param books 被更新后的books
     */
    public void setBookList(List<Book> books) {
        this.books = books;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (books == null || books.size() == 0 || position == books.size()) {
            return IMPORT_BOOK;
        }
        return OPEN_BOOK;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case IMPORT_BOOK: {
                View view = LayoutInflater.from(context).inflate(R.layout.import_book, parent, false);
                return new ImportViewHolder(view);
            }
            case OPEN_BOOK: {
                View view = LayoutInflater.from(context).inflate(R.layout.recycleview_book_shelf, parent, false);
                return new BookShelfViewHolder(view);
            }
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final BaseViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case OPEN_BOOK:
                BookShelfViewHolder viewHolder = (BookShelfViewHolder) holder;
                Book book = books.get(position);
                String bookName = book.getBookName().split(".txt")[0];
                viewHolder.bookItemTv.setText(bookName);

                if (book.getBookBg() == 0) {
                    // 如果没有封面，则设置封面
                    int random = new Random().nextInt(3);
                    book.setBookBg(random);
                    book.save();
                }
                viewHolder.bookItemTv.setBackgroundResource(bg[book.getBookBg()]);
                viewHolder.bookNameTv.setText(bookName);

                String progress = book.getProgress();
                String readed = "100.0%";
                String noRead = "未读";
                if (progress.equals(readed)) {
                    progress = "已读完";
                } else if (!progress.equals(noRead)) {
                    progress = "已读" + progress;
                }
                viewHolder.bookProgressTv.setText(progress);

                viewHolder.bookView.setTag(book.getId());
                viewHolder.bookView.setOnLongClickListener(mItemLongClickListener);
                if (showItem) {
                    // 可以显示选中的图书
                    viewHolder.bookSelectIv.setVisibility(View.VISIBLE);
                    viewHolder.bookSelectIv.setSelected(getItemSelected(book.getId()));
                    viewHolder.bookView.setOnClickListener(mItemSelectedClickListener);
                } else {
                    // 不能显示选中的图书，并清空选中集合中的书架
                    booleanArray.clear();
                    viewHolder.bookSelectIv.setVisibility(View.GONE);
                    viewHolder.bookView.setOnClickListener(mOnItemClickListener);
                }
                break;
            case IMPORT_BOOK:
                holder.itemView.setOnClickListener(mImportListener);
                break;
            default:
                break;
        }
    }

    @Override
    public int getItemCount() {
        return books == null ? 1 : books.size() + 1;
    }

    /**
     * 导入图书监听
     *
     * @param mImportListener
     */
    public void setImportListener(View.OnClickListener mImportListener) {
        this.mImportListener = mImportListener;
    }

    /**
     * 点击图书监听
     *
     * @param mOnItemClickListener
     */
    public void setOnItemClickListener(View.OnClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
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

    private void initCheck(boolean states) {
        if (books != null) {
            for (Book book : books) {
                setItemSelected(book.getId(), states);
            }
            notifyDataSetChanged();
        }
    }

    /**
     * 选中图书
     *
     * @param bookId 图书id
     */
    public void selectBook(int bookId) {
        final Book book = LitePal.findAll(Book.class, bookId).get(0);

        boolean isSelected = getItemSelected(book.getId());
        setItemSelected(book.getId(), !isSelected);
        notifyDataSetChanged();
    }

    /**
     * 长按图书后自动点击图书监听
     *
     * @param onItemClickListener
     */
    public void setOnItemClickSelectListener(View.OnClickListener onItemClickListener) {
        this.mItemSelectedClickListener = onItemClickListener;
    }

    public void setItemLongClickListener(View.OnLongClickListener itemLongClickListener) {
        this.mItemLongClickListener = itemLongClickListener;
    }

    /**
     * 底部菜单栏显示状态，并初始化选中的集合数据
     *
     * @param state 显示状态
     */
    public void showState(boolean state) {
        showItem = state;
        booleanArray.clear();
        notifyDataSetChanged();
    }

    /**
     * 长按后选中当前图书
     *
     * @param id 长按当前图书的id
     */
    public void bookSelect(int id) {
        showItem = true;
        setItemSelected(id, true);
        notifyDataSetChanged();
    }

    /**
     * 设置id的图书选中状态
     *
     * @param id       为id的图书
     * @param selected 是否被选中
     */
    private void setItemSelected(int id, boolean selected) {
        if (booleanArray == null) {
            booleanArray = new SparseBooleanArray();
        }
        booleanArray.put(id, selected);
    }

    /**
     * 返回图书id是否被选中
     *
     * @param id 图书id
     * @return 图书是否被选中
     */
    private boolean getItemSelected(int id) {
        if (booleanArray == null) {
            return false;
        }
        return booleanArray.get(id, false);
    }

    /**
     * @return 被选中的图书集合
     */
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

    public boolean isShowItem() {
        return showItem;
    }
}
