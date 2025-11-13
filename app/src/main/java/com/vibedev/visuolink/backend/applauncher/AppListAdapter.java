package com.vibedev.visuolink.backend.applauncher;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.vibedev.visuolink.R;

import java.util.List;

/**
 * Custom adapter to show installed apps with icon, name, and package.
 */
public class AppListAdapter extends BaseAdapter {

    private Context context;
    private List<AppInstalledInfo> appList;
    private LayoutInflater inflater;

    public AppListAdapter(Context context, List<AppInstalledInfo> appList) {
        this.context = context;
        this.appList = appList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return appList.size();
    }

    @Override
    public Object getItem(int position) {
        return appList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_app, parent, false);
            holder = new ViewHolder();
            holder.icon = convertView.findViewById(R.id.appIcon);
            holder.name = convertView.findViewById(R.id.appName);
            holder.packageName = convertView.findViewById(R.id.appPackage);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        AppInstalledInfo appInfo = appList.get(position);

        holder.icon.setImageDrawable(appInfo.getIcon());
        holder.name.setText(appInfo.getAppName());
        holder.packageName.setText(appInfo.getPackageName());

        return convertView;
    }

    static class ViewHolder {
        ImageView icon;
        TextView name;
        TextView packageName;
    }
}


