package com.samsung.pusdemo;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;


/**
 * Created by samsung on 2017/2/22.
 */

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.ViewHolder> {

    private List<App> mAppList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView appImage;
        TextView appInfo;

        public ViewHolder(View view) {
            super(view);
            appImage = (ImageView) view.findViewById(R.id.app_icon);
            appInfo = (TextView) view.findViewById(R.id.app_info);
        }
    }

    public AppAdapter(List<App> appList) {
        mAppList = appList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        App app = mAppList.get(position);
        holder.appImage.setImageDrawable(app.getIcon());
        holder.appInfo.setText(app.getInfo());
    }

    @Override
    public int getItemCount() {
        return mAppList.size();
    }
}
