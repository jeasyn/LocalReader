package com.example.localreader.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.localreader.R;
import com.example.localreader.adapter.BookmarkAdapter;
import com.example.localreader.entity.Bookmark;
import com.example.localreader.util.PageFactory;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xialijuan
 * @date 2021/1/10
 */
public class BookmarkFragment extends Fragment {

    public static final String ARGUMENT = "argument";
    private String bookPath;
    private List<Bookmark> bookmarkList;
    private BookmarkAdapter bookmarkAdapter;
    private PageFactory pageFactory;
    private ListView bookmarkLv;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_mark, container, false);
        init(view);
        initListener();
        return view;
    }

    private void init(View v) {
        bookmarkLv = v.findViewById(R.id.rv_bookmark);

        pageFactory = PageFactory.getInstance();
        Bundle bundle = getArguments();
        if (bundle != null) {
            bookPath = bundle.getString(ARGUMENT);
        }
        bookmarkList = new ArrayList<>();
        bookmarkList = LitePal.where("bookPath = ?", bookPath).find(Bookmark.class);
        bookmarkAdapter = new BookmarkAdapter(getActivity(), bookmarkList);
        bookmarkLv.setAdapter(bookmarkAdapter);
    }

    private void initListener() {
        bookmarkLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pageFactory.changeChapter(bookmarkList.get(position).getBegin());
                getActivity().finish();
            }
        });
        bookmarkLv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("提示")
                        .setMessage("确定删除书签？")
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                LitePal.delete(Bookmark.class, bookmarkList.get(position).getId());
                                bookmarkList.clear();
                                bookmarkList.addAll(LitePal.where("bookPath = ?", bookPath).find(Bookmark.class));
                                bookmarkAdapter.notifyDataSetChanged();
                            }
                        }).setCancelable(true).show();
                return false;
            }
        });
    }

    /**
     * 用于从Activity传递数据到Fragment
     * @param bookPath
     * @return
     */
    public static BookmarkFragment newInstance(String bookPath) {
        Bundle bundle = new Bundle();
        bundle.putString(ARGUMENT, bookPath);
        BookmarkFragment bookmarkFragment = new BookmarkFragment();
        bookmarkFragment.setArguments(bundle);
        return bookmarkFragment;
    }
}
