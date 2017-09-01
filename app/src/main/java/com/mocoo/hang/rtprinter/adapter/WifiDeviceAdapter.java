package com.mocoo.hang.rtprinter.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mocoo.hang.rtprinter.R;

import java.util.List;

/**
 * Created by Administrator on 2015/6/9.
 */
public class WifiDeviceAdapter extends BaseAdapter{

    private Context mContext;
    private List<String> mList;
    private LayoutInflater mInflater;

    public WifiDeviceAdapter(Context context, List<String> list) {
        this.mContext = context;
        this.mList = list;
        mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class ViewHolder{
        TextView tvName;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null){
            convertView = mInflater.inflate(R.layout.wifi_device_item,null);
            holder = new ViewHolder();
            holder.tvName = (TextView) convertView.findViewById(R.id.tv_wifi_device_name);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tvName.setText(mList.get(position));
        return convertView;
    }
}
