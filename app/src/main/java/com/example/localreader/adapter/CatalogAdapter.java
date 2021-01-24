package com.example.localreader.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.localreader.R;
import com.example.localreader.entity.BookCatalog;

import java.util.List;

/**
 * @author xialijuan
 * @date 2020/12/10
 */
public class CatalogAdapter extends BaseAdapter {

    private Context context;
    private List<BookCatalog> bookCatalogueList;
    private int currentCharter = 0;

    @Override
    public int getCount() {
        return bookCatalogueList.size();
    }

    @Override
    public Object getItem(int position) {
        return bookCatalogueList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setCharter(int charter) {
        currentCharter = charter;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        final ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.item_catalog, parent, false);
            viewHolder.catalog = convertView.findViewById(R.id.tv_catalog);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (currentCharter == position) {
            viewHolder.catalog.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
        } else {
            viewHolder.catalog.setTextColor(ContextCompat.getColor(context, R.color.read_default_text));
        }
        viewHolder.catalog.setText(bookCatalogueList.get(position).getCatalog());
        return convertView;
    }

    public CatalogAdapter(Context context, List<BookCatalog> bookCatalogueList) {
        this.context = context;
        this.bookCatalogueList = bookCatalogueList;
    }

    class ViewHolder {
        TextView catalog;
    }
}
