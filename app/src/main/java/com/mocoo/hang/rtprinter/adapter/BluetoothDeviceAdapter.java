package com.mocoo.hang.rtprinter.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.text.TextUtils;
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
public class BluetoothDeviceAdapter extends BaseAdapter{

    private Context mContext;
    private List<BluetoothDevice> mList;
    private LayoutInflater mInflater;

    public BluetoothDeviceAdapter(Context context, List<BluetoothDevice> list) {
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
            convertView = mInflater.inflate(R.layout.bluetooth_device_item,null);
            holder = new ViewHolder();
            holder.tvName = (TextView) convertView.findViewById(R.id.tv_bluetooth_device_name);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }
        BluetoothDevice bluetoothDevice = mList.get(position);
        if(TextUtils.isEmpty(bluetoothDevice.getName())){
            holder.tvName.setText(bluetoothDevice.getAddress());
        }else {
            holder.tvName.setText(mList.get(position).getName());
        }
        return convertView;
    }
}
