package com.example.zx.bluetooth_radio.List;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.zx.bluetooth_radio.BlueToothService.BluetoothService;
import com.example.zx.bluetooth_radio.R;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/3/20.
 */

public class ScanListAdapter extends BaseAdapter {
    public ArrayList<String> names = new ArrayList();
    public ArrayList<String> address =new ArrayList<>();

    public int               names_id; //the id of the names(View)(R.id.)
    public int               address_id; //the id of the address(View)(R.id.)

    private LayoutInflater inflater;

    public Context context;

    public ScanListAdapter(Context newContext, int newNames_id, int newAddress_id) {
        context = newContext;
        names_id = newNames_id;
        address_id = newAddress_id;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addItem(final String newNames, final String newAddress){
        names.add(newNames);
        address.add(newAddress);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return names.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if(convertView == null) {
            convertView = inflater.inflate(R.layout.item_bluetoothdevicelist, parent, false);
            holder = new ViewHolder();

            holder.names = (TextView) convertView.findViewById(names_id);
            holder.address = (TextView) convertView.findViewById(address_id);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.names.setText(names.get(position));
        holder.address.setText(address.get(position));

        return convertView;
    }


    public void addDate(String newName, String newAddress){
        names.add(newName);
        address.add(newAddress);
        notifyDataSetChanged();
    }

    public void setData(ArrayList<String> newNames, ArrayList<String> newAddress){
        names = newNames;
        address = newAddress;
    }

    public void getDataFromDevice(BluetoothDevice device){
        addDate(device.getName(), device.getAddress());
        notifyDataSetChanged();
    }
    

    public static class ViewHolder {
        public TextView names;
        public TextView address;
    }
}


