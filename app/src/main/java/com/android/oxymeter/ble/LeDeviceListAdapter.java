package com.android.oxymeter.ble;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import androidx.appcompat.widget.AppCompatTextView;

import com.android.oxymeter.R;
import com.android.oxymeter.utilities.CommonUtils;

import java.util.ArrayList;

public class LeDeviceListAdapter extends BaseAdapter {

    private ArrayList<BluetoothDevice> mLeDevicesList;
    private Context mContext;

    public LeDeviceListAdapter(Context context, ArrayList<BluetoothDevice> devices) {
        this.mContext = context;
        this.mLeDevicesList = devices;
    }

    @Override
    public int getCount() {
        return mLeDevicesList == null ? 0 : mLeDevicesList.size();
    }

    @Override
    public Object getItem(int position) {
        return mLeDevicesList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;

        if (view == null) {

            viewHolder = new ViewHolder();

            view = LayoutInflater.from(mContext).inflate(R.layout.listitem_device, null);

            viewHolder.mDeviceNameTextView = view.findViewById(R.id.device_name);
            viewHolder.mDeviceAddressTextView = view.findViewById(R.id.device_address);
            view.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) view.getTag();
        }


        String deviceName = CommonUtils.checkData(mLeDevicesList.get(position).getName());

        deviceName = deviceName.isEmpty() ? mContext.getResources().getString(R.string.unknown_device) : deviceName;

        viewHolder.mDeviceNameTextView.setText(deviceName);
        viewHolder.mDeviceAddressTextView.setText(CommonUtils.checkData(mLeDevicesList.get(position).getAddress()));

        return view;
    }

    private class ViewHolder {

        AppCompatTextView mDeviceNameTextView, mDeviceAddressTextView;
    }
}
