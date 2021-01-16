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

import java.util.ArrayList;

/**
 * @author xialijuan
 * @date 2021/1/10
 */
public class CatalogFragment extends Fragment {

    public static final String ARGUMENT = "argument";
    private PageFactory pageFactory;
    ArrayList<BookCatalog> catalogueList = new ArrayList<>();
    private ListView lv_catalogue;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_catalog, container, false);
        init(view);
        initListener();
        return view;
    }

    private void init(View v) {
        lv_catalogue = v.findViewById(R.id.rv_catalog);

        pageFactory = PageFactory.getInstance();
        catalogueList.addAll(pageFactory.getDirectoryList());
        CatalogAdapter catalogueAdapter = new CatalogAdapter(getContext(), catalogueList);
        catalogueAdapter.setCharter(pageFactory.getCurrentCharter());
        lv_catalogue.setAdapter(catalogueAdapter);
        catalogueAdapter.notifyDataSetChanged();
    }

    private void initListener() {
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
     * @param bookPath
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
