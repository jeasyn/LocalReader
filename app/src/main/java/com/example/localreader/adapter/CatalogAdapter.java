package com.example.localreader.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import androidx.core.content.ContextCompat;

import com.example.localreader.R;
import com.example.localreader.entity.BookCatalog;
import com.example.localreader.viewholder.CatalogViewHolder;

import java.util.List;

/**
 * @author xialijuan
 * @date 2020/12/30
 */
public class CatalogAdapter extends BaseAdapter {

    private Context context;
    private List<BookCatalog> catalogs;
    private int currentCharter = 0;

    public CatalogAdapter(Context context, List<BookCatalog> catalogs) {
        this.context = context;
        this.catalogs = catalogs;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        final CatalogViewHolder catalogViewHolder;
        if (convertView == null) {
            catalogViewHolder = new CatalogViewHolder();
            convertView = inflater.inflate(R.layout.item_catalog, parent, false);
            catalogViewHolder.catalogTv = convertView.findViewById(R.id.tv_catalog);
            convertView.setTag(catalogViewHolder);
        } else {
            catalogViewHolder = (CatalogViewHolder) convertView.getTag();
        }
        if (currentCharter == position) {
            // 正在阅读的当前章节
            catalogViewHolder.catalogTv.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
        } else {
            catalogViewHolder.catalogTv.setTextColor(ContextCompat.getColor(context, R.color.read_default_text));
        }
        catalogViewHolder.catalogTv.setText(catalogs.get(position).getCatalog());
        return convertView;
    }

    /**
     * 设置当前章节（正在阅读）的索引
     * @param charter 当前章节的索引
     */
    public void setCharter(int charter) {
        currentCharter = charter;
    }

    @Override
    public int getCount() {
        return catalogs.size();
    }

    @Override
    public Object getItem(int position) {
        return catalogs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
