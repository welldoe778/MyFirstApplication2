package com.mocoo.hang.rtprinter.adapter;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mocoo.hang.rtprinter.R;

import java.util.List;

public class WifiListAdapter extends BaseAdapter {

    private Context mContext;
    private List<ScanResult> mScanResults;

    public WifiListAdapter(Context context, List<ScanResult> scanResults) {
        // TODO Auto-generated constructor stub
        this.mContext = context;
        this.mScanResults = scanResults;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mScanResults.size();
    }

    @Override
    public ScanResult getItem(int position) {
        // TODO Auto-generated method stub
        return mScanResults.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext.getApplicationContext())
                    .inflate(R.layout.wifi_listview_item, null);
        }
        System.out.println("?????????");
        TextView wifi_set_wifiName = ViewHolder.get(convertView,
                R.id.wifi_set_wifiName);
        ImageView wifi_set_wifiLevel = ViewHolder.get(convertView,
                R.id.wifi_set_wifiLevel);
        wifi_set_wifiName.setText(mScanResults.get(position).SSID);
        if (Math.abs(mScanResults.get(position).level) > 100) {
            wifi_set_wifiLevel.setImageDrawable(mContext.getResources().getDrawable(R.mipmap.signal_wifi_1));
        } else if (Math.abs(mScanResults.get(position).level) > 80) {
            wifi_set_wifiLevel.setImageDrawable(mContext.getResources()
                    .getDrawable(R.mipmap.signal_wifi_2));
        } else if (Math.abs(mScanResults.get(position).level) > 70) {
            wifi_set_wifiLevel.setImageDrawable(mContext.getResources()
                    .getDrawable(R.mipmap.signal_wifi_3));
        } else if (Math.abs(mScanResults.get(position).level) > 60) {
            wifi_set_wifiLevel.setImageDrawable(mContext.getResources()
                    .getDrawable(R.mipmap.signal_wifi_4));
        } else if (Math.abs(mScanResults.get(position).level) > 50) {
            wifi_set_wifiLevel.setImageDrawable(mContext.getResources()
                    .getDrawable(R.mipmap.signal_wifi_5));
        } else {
            wifi_set_wifiLevel.setImageDrawable(mContext.getResources()
                    .getDrawable(R.mipmap.signal_wifi_5));
        }
        return convertView;
    }

}
