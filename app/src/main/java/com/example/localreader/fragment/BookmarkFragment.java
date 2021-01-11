package com.example.localreader.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.localreader.R;
import com.example.localreader.adapter.BookmarkAdapter;
import com.example.localreader.entity.Bookmark;
import com.example.localreader.util.PageFactory;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * @author xialijuan
 * @date 2021/1/10
 */
public class BookmarkFragment extends BaseFragment{

    public static final String ARGUMENT = "argument";

    @BindView(R.id.rv_mark) ListView lv_bookmark;

    private String bookpath;
    private String mArgument;
    private List<Bookmark> bookMarksList;
    private BookmarkAdapter markAdapter;
    private PageFactory pageFactory;

    @Override
    protected int getLayoutRes() {
        return R.layout.view_mark;
    }

    @Override
    protected void initData(View view) {
        pageFactory = PageFactory.getInstance();
        Bundle bundle = getArguments();
        if (bundle != null) {
            bookpath = bundle.getString(ARGUMENT);
        }
        bookMarksList = new ArrayList<>();
        bookMarksList = LitePal.where("bookpath = ?", bookpath).find(Bookmark.class);
        markAdapter = new BookmarkAdapter(getActivity(), bookMarksList);
        lv_bookmark.setAdapter(markAdapter);
    }

    @Override
    protected void initListener() {
        lv_bookmark.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pageFactory.changeChapter(bookMarksList.get(position).getBegin());
                getActivity().finish();
            }
        });
        lv_bookmark.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("提示")
                        .setMessage("是否删除书签？")
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                LitePal.delete(Bookmark.class,bookMarksList.get(position).getId());
                                bookMarksList.clear();
                                bookMarksList.addAll(LitePal.where("bookpath = ?", bookpath).find(Bookmark.class));
                                markAdapter.notifyDataSetChanged();
                            }
                        }).setCancelable(true).show();
                return false;
            }
        });
    }

    /**
     * 用于从Activity传递数据到Fragment
     * @param bookpath
     * @return
     */
    public static BookmarkFragment newInstance(String bookpath) {
        Bundle bundle = new Bundle();
        bundle.putString(ARGUMENT, bookpath);
        BookmarkFragment bookMarkFragment = new BookmarkFragment();
        bookMarkFragment.setArguments(bundle);
        return bookMarkFragment;
    }
}
