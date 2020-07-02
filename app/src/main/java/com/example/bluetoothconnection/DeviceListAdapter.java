package com.example.bluetoothconnection;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class DeviceListAdapter extends ArrayAdapter<BluetoothDevice> {
    private LayoutInflater mLayoutInFlater;
    private ArrayList<BluetoothDevice> mDevices;
    private int mViewResourceId;

    public DeviceListAdapter(Context context, int tvResourceID, ArrayList<BluetoothDevice>devices)
    {
        super(context,tvResourceID,devices);
        this.mDevices=devices;
        mLayoutInFlater=(LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        mViewResourceId=tvResourceID;

    }
    public View getView(int position, View convertView, ViewGroup parent)
    {
        convertView = mLayoutInFlater.inflate(mViewResourceId,null);
        BluetoothDevice device=mDevices.get(position);
        if(device!=null)
        {
            TextView deviceName=(TextView)convertView.findViewById(R.id.tvDeviceName);
            TextView deviceAddress=(TextView)convertView.findViewById(R.id.tvDeviceAddress);
            if(deviceName!=null)
            {
                deviceName.setText(device.getName());
            }
            if(deviceAddress!=null)
            {
               deviceAddress.setText(device.getAddress());
            }

        }

     return convertView;
    }


}
