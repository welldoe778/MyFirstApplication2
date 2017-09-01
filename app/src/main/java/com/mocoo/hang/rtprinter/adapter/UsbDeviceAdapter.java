package com.mocoo.hang.rtprinter.adapter;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mocoo.hang.rtprinter.R;

import java.util.List;

/**
 * Created by Administrator on 2015/7/1.
 */
public class UsbDeviceAdapter extends BaseAdapter{

    private Context mContext;
    private List<UsbDevice> mList;
    private LayoutInflater mInflater;

    public UsbDeviceAdapter(Context context, List<UsbDevice> list) {
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
        TextView tvText;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null){
            convertView = mInflater.inflate(R.layout.basic_dialog_item,null);
            holder = new ViewHolder();
            holder.tvText = (TextView) convertView.findViewById(R.id.tv_basic_dialog_item_text);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tvText.setText(mContext.getString(R.string.print_device)+(mList.get(position).getDeviceId()));
        return convertView;
    }

}
