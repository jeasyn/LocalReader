package com.example.localreader.fragment;

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
import com.example.localreader.adapter.CatalogAdapter;
import com.example.localreader.entity.BookCatalog;
import com.example.localreader.util.PageFactory;

import java.util.List;

/**
 * @author xialijuan
 * @date 2020/12/30
 */
public class CatalogFragment extends Fragment {

    private static final String ARGUMENT = "argument";
    private PageFactory pageFactory;
    private List<BookCatalog> catalogs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_catalog, container, false);
        init(view);
        return view;
    }

    private void init(View v) {
        ListView catalogLv = v.findViewById(R.id.rv_catalog);
        pageFactory = PageFactory.getInstance();
        // 获取章节目录
        catalogs = pageFactory.getDirectoryList();

        CatalogAdapter catalogAdapter = new CatalogAdapter(getContext(), catalogs);
        catalogAdapter.setCharter(pageFactory.getCurrentCharter());
        catalogLv.setAdapter(catalogAdapter);
        catalogAdapter.notifyDataSetChanged();

        catalogLv.setOnItemClickListener(mOnItemClickListener);
    }

    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            pageFactory.changeChapter(catalogs.get(position).getFirstIndex());
            getActivity().finish();
        }
    };

    /**
     * 从activity传值到fragment
     *
     * @param bookPath 图书路径
     * @return
     */
    public static CatalogFragment newInstance(String bookPath) {
        Bundle bundle = new Bundle();
        bundle.putString(ARGUMENT, bookPath);
        CatalogFragment catalogFragment = new CatalogFragment();
        catalogFragment.setArguments(bundle);
        return catalogFragment;
    }
}
