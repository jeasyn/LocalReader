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
 * @date 2020/12/09
 */
public class CatalogFragment extends Fragment {

    private static final String ARGUMENT = "argument";
    private PageFactory pageFactory;
    ArrayList<BookCatalog> catalogList = new ArrayList<>();
    private ListView catalogLv;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_catalog, container, false);
        init(view);
        initListener();
        return view;
    }

    private void init(View v) {
        catalogLv = v.findViewById(R.id.rv_catalog);

        pageFactory = PageFactory.getInstance();
        catalogList.addAll(pageFactory.getDirectoryList());
        CatalogAdapter catalogAdapter = new CatalogAdapter(getContext(), catalogList);
        catalogAdapter.setCharter(pageFactory.getCurrentCharter());
        catalogLv.setAdapter(catalogAdapter);
        catalogAdapter.notifyDataSetChanged();
    }

    private void initListener() {
        catalogLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pageFactory.changeChapter(catalogList.get(position).getPosition());
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
