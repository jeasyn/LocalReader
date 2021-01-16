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

    private Context mContext;
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
        LayoutInflater inflater = LayoutInflater.from(mContext);

        final ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.item_bookmark, null);
            viewHolder.text_mark = convertView.findViewById(R.id.text_mark);
            viewHolder.progress1 = convertView.findViewById(R.id.progress1);
            viewHolder.mark_time = convertView.findViewById(R.id.mark_time);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.text_mark.setText(list.get(position).getText());
        long begin = list.get(position).getBegin();
        float fPercent = (float) (begin * 1.0 / pageFactory.getBookLen());
        DecimalFormat df = new DecimalFormat("#0.0");
        String strPercent = df.format(fPercent * 100) + "%";
        viewHolder.progress1.setText(strPercent);
        viewHolder.mark_time.setText(list.get(position).getTime().substring(0, 16));
        return convertView;
    }

    public BookmarkAdapter(Context context, List<Bookmark> list) {
        mContext = context;
        this.list = list;
        pageFactory = PageFactory.getInstance();
        config = config.getInstance();
    }

    class ViewHolder {
        TextView text_mark, progress1, mark_time;
    }
}
