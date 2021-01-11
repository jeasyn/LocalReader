package com.example.localreader.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.localreader.R;
import com.example.localreader.adapter.CatalogAdapter;
import com.example.localreader.entity.BookCatalog;
import com.example.localreader.util.PageFactory;

import java.util.ArrayList;

import butterknife.BindView;

/**
 * @author xialijuan
 * @date 2021/1/10
 */
public class CatalogFragment extends BaseFragment{

    public static final String ARGUMENT = "argument";

    @BindView(R.id.rv_catalog) ListView lv_catalogue;
    private PageFactory pageFactory;
    ArrayList<BookCatalog> catalogueList = new ArrayList<>();


    @Override
    protected int getLayoutRes() {
        return R.layout.view_catalog;
    }

    @Override
    protected void initData(View view) {
        pageFactory = PageFactory.getInstance();
        catalogueList.addAll(pageFactory.getDirectoryList());
        CatalogAdapter catalogueAdapter = new CatalogAdapter(getContext(), catalogueList);
        catalogueAdapter.setCharter(pageFactory.getCurrentCharter());
        lv_catalogue.setAdapter(catalogueAdapter);
        catalogueAdapter.notifyDataSetChanged();
    }

    @Override
    protected void initListener() {
        lv_catalogue.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pageFactory.changeChapter(catalogueList.get(position).getBookCatalogueStartPos());
                getActivity().finish();
            }
        });
    }

    /**
     * 用于从Activity传递数据到Fragment
     * @param bookpath
     * @return
     */
    public static CatalogFragment newInstance(String bookpath) {
        Bundle bundle = new Bundle();
        bundle.putString(ARGUMENT, bookpath);
        CatalogFragment catalogFragment = new CatalogFragment();
        catalogFragment.setArguments(bundle);
        return catalogFragment;
    }
}
