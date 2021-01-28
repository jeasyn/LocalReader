package com.example.localreader.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.localreader.R;
import com.example.localreader.adapter.BookmarkAdapter;
import com.example.localreader.entity.Bookmark;
import com.example.localreader.util.PageFactory;

import org.litepal.LitePal;

import java.util.List;

/**
 * @author xialijuan
 * @date 2021/01/02
 */
public class BookmarkFragment extends Fragment {

    private static final String ARGUMENT = "argument";
    private String bookPath;
    private List<Bookmark> bookmarkList;
    private BookmarkAdapter bookmarkAdapter;
    private PageFactory pageFactory;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_mark, container, false);
        init(view);
        return view;
    }

    private void init(View v) {
        ListView bookmarkLv = v.findViewById(R.id.rv_bookmark);

        pageFactory = PageFactory.getInstance();
        Bundle bundle = getArguments();
        if (bundle != null) {
            bookPath = bundle.getString(ARGUMENT);
        }
        bookmarkList = LitePal.where("bookPath = ?", bookPath).find(Bookmark.class);
        bookmarkAdapter = new BookmarkAdapter(getActivity(), bookmarkList);
        bookmarkLv.setAdapter(bookmarkAdapter);

        bookmarkLv.setOnItemClickListener(mOnItemClickListener);
        bookmarkLv.setOnItemLongClickListener(mOnItemLongClickListener);
    }

    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            pageFactory.changeChapter(bookmarkList.get(position).getFirstIndex());
            getActivity().finish();
        }
    };

    private AdapterView.OnItemLongClickListener mOnItemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
            new AlertDialog.Builder(getActivity())
                    .setTitle("提示")
                    .setMessage("确定删除书签？")
                    .setNegativeButton("取消", null)
                    .setPositiveButton("确定", (dialog, which) -> {
                        LitePal.delete(Bookmark.class, bookmarkList.get(position).getId());
                        bookmarkList.clear();
                        bookmarkList.addAll(LitePal.where("bookPath = ?", bookPath).find(Bookmark.class));
                        bookmarkAdapter.notifyDataSetChanged();
                    }).show();
            //设置为true，可避免长按事件与点击事件冲突
            return true;
        }
    };

    /**
     * 从activity传值到fragment
     *
     * @param bookPath 图书路径
     * @return BookmarkFragment
     */
    public static BookmarkFragment newInstance(String bookPath) {
        Bundle bundle = new Bundle();
        bundle.putString(ARGUMENT, bookPath);
        BookmarkFragment bookmarkFragment = new BookmarkFragment();
        bookmarkFragment.setArguments(bundle);
        return bookmarkFragment;
    }
}
