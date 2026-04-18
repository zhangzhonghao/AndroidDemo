package com.example.androiddemo.tools;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.androiddemo.R;
import java.util.List;

public class AppCloneAdapter extends BaseAdapter {
    private android.content.Context context;
    private List<AppCloneActivity.AppInfo> list;
    AppCloneAdapter(android.content.Context context, List<AppCloneActivity.AppInfo> list) {
        this.context = context;
        this.list = list;
    }
    @Override
    public int getCount() { return list.size(); }
    @Override
    public Object getItem(int position) { return list.get(position); }
    @Override
    public long getItemId(int position) { return position; }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(context, android.R.layout.simple_list_item_2, null);
        }
        AppCloneActivity.AppInfo app = list.get(position);
        ((ImageView) convertView.findViewById(android.R.id.icon)).setImageDrawable(app.icon);
        ((TextView) convertView.findViewById(android.R.id.text1)).setText(app.label);
        ((TextView) convertView.findViewById(android.R.id.text2)).setText(app.packageName);
        return convertView;
    }
}