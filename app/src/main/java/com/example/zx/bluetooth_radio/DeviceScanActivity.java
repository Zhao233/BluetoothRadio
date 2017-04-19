package com.example.zx.bluetooth_radio;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.example.zx.bluetooth_radio.BlueToothService.BluetoothService;
import com.example.zx.bluetooth_radio.List.ScanListAdapter;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by zx on 2017/2/22.
 */

public class DeviceScanActivity extends Activity{
    public        ListView                          bluetoothDeviceList;
    public        Button                            scanDeviceButton;
    public        Button                            sendMessage;

    public        BluetoothService                  bluetoothService;

    public        ScanListAdapter                   listAdapter;

    public        Handler                           handler;
    public static Context                           context;
    /*Test
    *public        ArrayList<String>                 names_test;
    *public        ArrayList<String>                 address_test;
    */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetoothbevicelist);

        handler = new Handler();
        context = this;

        bluetoothDeviceList = (ListView) findViewById(R.id.BluetoothDeviceList);
        scanDeviceButton = (Button) findViewById(R.id.ScanDeviceButton);
        sendMessage = (Button) findViewById(R.id.sendMessage);

        bluetoothService = new BluetoothService(getSystemService(BLUETOOTH_SERVICE), handler, this);
        listAdapter = new ScanListAdapter(context, R.id.nameOfDevice, R.id.address);

        bluetoothDeviceList.setAdapter(listAdapter);
        /*names = new ArrayList<>();
        address = new ArrayList<>();*/

        /*Test start
        names_test = new ArrayList<>();
        address_test = new ArrayList<>();

        *names_test.add("A");
        *address_test.add("B");
        *names_test.add("A");
        *address_test.add("B");
        *names_test.add("A");
        *address_test.add("B");
        *names_test.add("A");
        *address_test.add("B");

        *adapter = new ScanListAdapter(context, R.id.nameOfDevice, R.id.address);
        *adapter.setData(names_test, address_test);
        */

        scanDeviceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( !bluetoothService.bluetoothAdapter.isEnabled() ){
                    Log.e("error", "the bluetooth is close");
                    makeToast("the bluetooth is close");

                    bluetoothService.bluetoothAdapter.enable();
                } else {
                        Log.i("A", "List get succeed");
                        bluetoothService.scanLeDevice(listAdapter);
                }

                /*Test start
                *bluetoothDeviceList.setAdapter(adapter);
                *Test end*/
            }
        });

        setListItemClickLisener();

        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] a = {'b', 'b', 'c', 'd', 'e'};
                byte[] b = {'2', '2', '3', '4', '5'};
                bluetoothService.sendMessage(a,b);
            }
        });
    }

    public void setListItemClickLisener(){
        AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                bluetoothService.connectDevice(bluetoothService.getDeviceByNumber(position));
            }
        };

        bluetoothDeviceList.setOnItemClickListener(listener);
    }


    public void makeToast(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }
}
