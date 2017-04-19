package com.example.zx.bluetooth_radio.BlueToothService;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.zx.bluetooth_radio.DeviceScanActivity;
import com.example.zx.bluetooth_radio.List.ScanListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * Created by zx on 2017/2/5.
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class BluetoothService{
    public               Context                                            context;
    private static final long                                               SCAN_PERIOD = 1000;
    public final static  UUID                                               UUID_NOTIFY = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    public final static  UUID                                               UUID_SERVICE = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    public final static  UUID                                               UUID_KEY_DATA = UUID.fromString("0000fff6-0000-1000-8000-00805f9b34fb");
    private              BluetoothManager                                   bluetoothManager;
    public               BluetoothAdapter                                   bluetoothAdapter;
    private              BluetoothGatt                                      bluetoothGatt ;
    private              BluetoothGattService                               bluetoothGattServer;
    private              BluetoothGattCharacteristic                        bluetoothGattCharacteristic;
    private              BluetoothLeScanner                                 bluetoothLeScanner;
    private              BluetoothLeAdvertiser                              bluetoothLeAdvertiser;
    private              HashMap<BluetoothDevice, BluetoothGatt>                     connectedService;
    private              ArrayList<BluetoothDevice>                         connectedDevice;
    private              HashMap<BluetoothDevice,BluetoothGattCharacteristic>        connectedCharacteristic;

    private              String                            bluetoothDeviceAddress;

    public               ArrayList<BluetoothDevice>        list_LeDevices;
    private              List<BluetoothGattService>        bluetoothGattServices;
    private              boolean                           mScanning;
    private              Handler                           handler;

    public               DeviceScanActivity                deviceScanActivity;

    private              BluetoothGattCallback             bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt bluetoothGatt, int state, int newState){ // 连接成功后启动服务发现
            if(newState == BluetoothProfile.STATE_CONNECTED){
               Log.i("A","启动服务发现：" + bluetoothGatt.discoverServices());
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status){// 发现服务的回调
            if(status == BluetoothGatt.GATT_SUCCESS){
                Log.i("A", "成功发现服务");

                //成功发现服务后可以调用相应方法得到该BLE设备的所有服务，并且打印每一个服务的UUID和每个服务下各个特征的UUID
                List<BluetoothGattService> supportedGattServices = bluetoothGatt.getServices();
                for(int i = 0; i < supportedGattServices.size(); i++){
                    Log.i("A", "BluetoothGattService UUID = " + supportedGattServices.get(i).getUuid());

                    List<BluetoothGattCharacteristic> listGattCharacteristic = supportedGattServices.get(i).getCharacteristics();
                    for(int j = 0; j < listGattCharacteristic.size(); j++){
                        Log.i("A", "BluetoothGattCharacteristic UUID = " + listGattCharacteristic.get(j).getUuid());
                    }
                }
            } else {
                Log.e("E", "服务发现失败，错误代码为：" + status);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status){// 写操作
            if(status == BluetoothGatt.GATT_SUCCESS){
                Log.i("A", "写入成功" + characteristic.getValue());
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status){// 读操作
            if(status == BluetoothGatt.GATT_SUCCESS){
                Log.i("A", "读取成功" + characteristic.getValue());
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic){// 数据返回的回调（此处接受BLE设备返回的数据）

        }
    };


    public BluetoothService(Object bluetoothManager, Handler newHandler, DeviceScanActivity newDeviceScanActivity){
        this.bluetoothManager = (BluetoothManager)bluetoothManager;
        handler = newHandler;
        deviceScanActivity = newDeviceScanActivity;

        connectedDevice = new ArrayList();

        list_LeDevices = new ArrayList<>();
        bluetoothAdapter = ((BluetoothManager) bluetoothManager).getAdapter();
        bluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
        connectedService = new HashMap<>();
        connectedCharacteristic = new HashMap<>();
    }

    public void scanLeDevice(ScanListAdapter adapter){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

            final mScanCallback mLeScanCallback = new mScanCallback(adapter) {
                @Override
                public void onScanResult(int callbackType, ScanResult result){
                    /*deviceScanActivity.makeToast(result.getDevice().getName());*/
                    deviceScanActivity.makeToast("scanning");

                    BluetoothDevice device = result.getDevice();
                    addDevice(device,listAdapter);
                }
            };

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    bluetoothLeScanner.stopScan(mLeScanCallback);
                    Toast.makeText(DeviceScanActivity.context, "stopScan", Toast.LENGTH_SHORT).show();
                }

            }, SCAN_PERIOD);

            bluetoothLeScanner.startScan(mLeScanCallback);
            Toast.makeText(DeviceScanActivity.context, "startScan",Toast.LENGTH_LONG ).show();
        } else {

        }
    }

    public void addDevice(BluetoothDevice bluetoothDevice, ScanListAdapter adapter){
        if(!list_LeDevices.contains(bluetoothDevice)){
            list_LeDevices.add(bluetoothDevice);

            adapter.getDataFromDevice(bluetoothDevice);
        }
    }

    public BluetoothDevice getDeviceByNumber(int number){
        return list_LeDevices.get(number);
    }

    public ArrayList getBluetoothDeviceList(){
        return list_LeDevices;
    }

    public boolean connectDevice(final String address){
        if(bluetoothAdapter == null || address == null){
            Log.w("hello","BluetoothAdapter not initialized or unspecified address.");

            return false;
        }

        if(bluetoothDeviceAddress != null && address.equals(bluetoothDeviceAddress)
                && bluetoothGatt != null){
            Log.d("hello", "Trying to use an existing mBluetoothGatt for connection.");

            if(bluetoothGatt.connect()) {
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        if(device == null){
            Log.w("hello", "Device not found.  Unable to connect.");
            return false;
        }

        bluetoothGatt = device.connectGatt(context ,false, bluetoothGattCallback);
        Log.d("hello", "Trying to create a new connection.");

        bluetoothDeviceAddress = address;
        bluetoothGatt.discoverServices();
        return true;
    }
    public boolean connectDevice(BluetoothDevice device){
        if(device == null){
            Log.e("E", "remote bolutooth device is null");
            return false;
        }

        if( !connectedDevice.contains(device) ){
            bluetoothGatt = device.connectGatt(context, false, bluetoothGattCallback);

            bluetoothGattServer = bluetoothGatt.getService(UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"));
            bluetoothGattCharacteristic = bluetoothGattServer.getCharacteristic(UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb"));

            //bluetoothGatt.discoverServices() ;
            connectedDevice.add(device);
            connectedService.put(device, bluetoothGatt);
            connectedCharacteristic.put(device, bluetoothGattCharacteristic);
        }



        return true;
    }

    public void sendMessage(byte[] message, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic){
        characteristic.setValue(message);
        gatt.writeCharacteristic(characteristic);

    }
    public void sendMessage(byte[] message1, byte[] message2){
        BluetoothDevice device1 = connectedDevice.get(0);
        BluetoothDevice device2 = connectedDevice.get(1);

        BluetoothGatt gatt1 = connectedService.get(device1);
        BluetoothGatt gatt2 = connectedService.get(device2);

       sendMessage(message1, gatt1, connectedCharacteristic.get(device1));
       sendMessage(message2, gatt2, connectedCharacteristic.get(device2));
    }

    public void findService(List<BluetoothGattService> gattServices){
        Log.i("a", "Count is:" + gattServices.size());
        for (BluetoothGattService gattService : gattServices) {
            Log.i("a", gattService.getUuid().toString());
            Log.i("a", UUID_SERVICE.toString());
            Log.i("a", String.valueOf(gattService.getType()));
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            Log.i("a", "Count is:" + gattCharacteristics.size());
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                if(gattCharacteristic.getUuid().toString().equalsIgnoreCase(UUID_SERVICE.toString())) {
                    Log.i("a", gattCharacteristic.getUuid().toString());
                    Log.i("a", UUID_NOTIFY.toString());
                    bluetoothGattCharacteristic = gattCharacteristic;
                    setCharacteristicNotification(gattCharacteristic, true);
                }
            }
        }
    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (bluetoothAdapter == null || bluetoothGatt == null) {
            Log.w("a", "BluetoothAdapter not initialized");
            return;
        }
        bluetoothGatt.setCharacteristicNotification(characteristic, enabled);
    }

    public static AdvertiseSettings createAdvSettings(boolean connectable, int timeoutMillis) {
        AdvertiseSettings.Builder mSettingsbuilder = new AdvertiseSettings.Builder();
        mSettingsbuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED);
        mSettingsbuilder.setConnectable(connectable);
        mSettingsbuilder.setTimeout(timeoutMillis);
        mSettingsbuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);
        AdvertiseSettings mAdvertiseSettings = mSettingsbuilder.build();

        return mAdvertiseSettings;
    }

    public static AdvertiseData createAdvertiseData(){
        byte[] a = {'a', 'b', 'c', 'd'};

        AdvertiseData.Builder    mDataBuilder = new AdvertiseData.Builder();
        mDataBuilder.addServiceUuid(ParcelUuid.fromString("00001101-0000-1000-8000-00805F9B34FB"));
        mDataBuilder.addServiceData(ParcelUuid.fromString("00001101-0000-1000-8000-00805F9B34FB"),a);
        AdvertiseData mAdvertiseData = mDataBuilder.build();

        return mAdvertiseData;
    }

    public AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            Toast.makeText(DeviceScanActivity.context, "sucess", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);

            Toast.makeText(DeviceScanActivity.context, "fail", Toast.LENGTH_SHORT).show();
            Toast.makeText(DeviceScanActivity.context, String.valueOf(errorCode), Toast.LENGTH_SHORT).show();
        }
    };
}

class mScanCallback extends ScanCallback{
    public ScanListAdapter listAdapter;

    public mScanCallback(ScanListAdapter newAdapter){
        listAdapter = newAdapter;
    }
}
