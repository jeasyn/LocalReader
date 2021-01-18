package com.example.localreader.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.localreader.R;
import com.example.localreader.entity.Bookmark;
import com.example.localreader.entity.Config;
import com.example.localreader.util.PageFactory;

import java.text.DecimalFormat;
import java.util.List;

/**
 * @author xialijuan
 * @date 2021/1/10
 */
public class BookmarkAdapter extends BaseAdapter {

    private Context context;
    private List<Bookmark> list;
    private Config config;
    private PageFactory pageFactory;

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);

        final ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.item_bookmark, null);
            viewHolder.markContent = convertView.findViewById(R.id.tv_mark_content);
            viewHolder.markProgress = convertView.findViewById(R.id.tv_mark_progress);
            viewHolder.markTime = convertView.findViewById(R.id.tv_mark_time);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.markContent.setText(list.get(position).getText());
        long begin = list.get(position).getBegin();
        float percent = (float) (begin * 1.0 / pageFactory.getBookLen());
        DecimalFormat df = new DecimalFormat("#0.0");
        String progress = df.format(percent * 100) + "%";
        viewHolder.markProgress.setText(progress);
        viewHolder.markTime.setText(list.get(position).getTime().substring(0, 16));
        return convertView;
    }

    public BookmarkAdapter(Context context, List<Bookmark> list) {
        this.context = context;
        this.list = list;
        pageFactory = PageFactory.getInstance();
        config = config.getInstance();
    }

    class ViewHolder {
        TextView markContent, markProgress, markTime;
    }
}
