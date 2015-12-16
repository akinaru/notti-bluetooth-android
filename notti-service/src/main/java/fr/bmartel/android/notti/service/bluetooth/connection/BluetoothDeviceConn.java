/**
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2015 Bertrand Martel
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package fr.bmartel.android.notti.service.bluetooth.connection;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

import fr.bmartel.android.notti.service.bluetooth.BluetoothConst;
import fr.bmartel.android.notti.service.bluetooth.IBluetoothCustomManager;
import fr.bmartel.android.notti.service.bluetooth.IDevice;
import fr.bmartel.android.notti.service.bluetooth.events.BluetoothEvents;
import fr.bmartel.android.notti.service.bluetooth.listener.IDeviceInitListener;
import fr.bmartel.android.notti.service.bluetooth.listener.IPushListener;
import fr.bmartel.android.notti.service.bluetooth.notti.NottiDevice;

/**
 * Bluetooth device connection management
 *
 * @author Bertrand Martel
 */
public class BluetoothDeviceConn implements IBluetoothDeviceConn {

    private final static String TAG = BluetoothDeviceConn.class.getName();

    /**
     * Bluetooth callback for gatt layer interaction
     */
    private BluetoothGattCallback gattCallback = null;

    /**
     * bluetooth gatt connection object
     */
    private BluetoothGatt gatt = null;

    /**
     * device address
     */
    private String deviceAddr = "";

    private String deviceName = "";

    private IBluetoothCustomManager manager = null;

    private IDevice device = null;

    private boolean connected = false;

    /**
     * Build Bluetooth device connection
     *
     * @param address
     */
    @SuppressLint("NewApi")
    public BluetoothDeviceConn(String address, String deviceName, final IBluetoothCustomManager manager) {
        this.deviceAddr = address;
        this.deviceName = deviceName;
        this.manager = manager;

        gattCallback = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                                int newState) {

                if (newState == BluetoothProfile.STATE_CONNECTED) {

                    Log.i(TAG, "Connected to GATT server.");
                    Log.i(TAG, "Attempting to start service discovery:" + gatt.discoverServices());

                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {

                    connected = false;
                    Log.i(TAG, "Disconnected from GATT server.");

                    try {
                        JSONObject object = new JSONObject();
                        object.put(BluetoothConst.DEVICE_ADDRESS, getAddress());
                        object.put(BluetoothConst.DEVICE_NAME, getDeviceName());

                        ArrayList<String> values = new ArrayList<String>();
                        values.add(object.toString());

                        //when device is fully intitialized broadcast service discovery
                        manager.broadcastUpdateStringList(BluetoothEvents.BT_EVENT_DEVICE_DISCONNECTED, values);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    
                    if (BluetoothDeviceConn.this.gatt != null) {
                        BluetoothDeviceConn.this.gatt.close();
                    }
                }
            }

            @Override
            // New services discovered
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {

                    Runnable test = new Runnable() {
                        @Override
                        public void run() {

                            //you can improve this by using reflection
                            device = new NottiDevice(BluetoothDeviceConn.this);

                            device.addInitListener(new IDeviceInitListener() {
                                @Override
                                public void onInit() {
                                    try {
                                        JSONObject object = new JSONObject();
                                        object.put(BluetoothConst.DEVICE_ADDRESS, getAddress());
                                        object.put(BluetoothConst.DEVICE_NAME, getDeviceName());

                                        ArrayList<String> values = new ArrayList<String>();
                                        values.add(object.toString());

                                        connected = true;
                                        //when device is fully intitialized broadcast service discovery
                                        manager.broadcastUpdateStringList(BluetoothEvents.BT_EVENT_DEVICE_CONNECTED, values);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            device.init();
                        }
                    };
                    Thread testThread = new Thread(test);
                    testThread.start();

                } else {
                    Log.w(TAG, "onServicesDiscovered received: " + status);
                }
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                manager.getEventManager().set();
                if (device != null) {
                    device.notifyCharacteristicWriteReceived(characteristic);
                }
            }

            @Override
            // Result of a characteristic read operation
            public void onCharacteristicRead(BluetoothGatt gatt,
                                             BluetoothGattCharacteristic characteristic,
                                             int status) {
                manager.getEventManager().set();
                if (device != null) {
                    device.notifyCharacteristicReadReceived(characteristic);
                }
            }

            @Override
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                manager.getEventManager().set();
            }

            @Override
            // Characteristic notification
            public void onCharacteristicChanged(BluetoothGatt gatt,
                                                BluetoothGattCharacteristic characteristic) {
                if (device != null) {
                    device.notifyCharacteristicChangeReceived(characteristic);
                }
            }
        };
    }

    public BluetoothGattCallback getGattCallback() {
        return gattCallback;
    }

    @Override
    public String getAddress() {
        return this.deviceAddr;
    }

    @Override
    public String getDeviceName() {
        return deviceName;
    }

    @Override
    public BluetoothGatt getBluetoothGatt() {
        return gatt;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @SuppressLint("NewApi")
    @Override
    public void writeCharacteristic(String service, String charac, byte[] value, IPushListener listener) {
        manager.writeCharacteristic(charac, value, gatt, listener);
    }

    @SuppressLint("NewApi")
    @Override
    public void readCharacteristic(String service, String charac) {
        manager.readCharacteristic(charac, gatt);
    }

    @SuppressLint("NewApi")
    @Override
    public void enableDisableNotification(UUID service, UUID charac, boolean enable) {

        if (gatt.getService(service) != null &&
                gatt.getService(service).getCharacteristic(charac) != null)
            gatt.setCharacteristicNotification(gatt.getService(service).getCharacteristic(charac), enable);
        else {
            Log.e(TAG, "error inconsistent service or characteristic");
        }
    }

    @SuppressLint("NewApi")
    @Override
    public void enableGattNotifications(String serviceUid, String characUid) {

        String descriptorStr = BluetoothConst.CLIENT_CHARACTERISTIC_CONFIG;
        manager.writeDescriptor(descriptorStr, gatt, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE, serviceUid, characUid);
    }

    public BluetoothGatt getGatt() {
        return gatt;
    }

    public void setGatt(BluetoothGatt gatt) {
        this.gatt = gatt;
    }

    @Override
    public IBluetoothCustomManager getManager() {
        return manager;
    }

    @Override
    public IDevice getDevice() {
        return device;
    }

    @SuppressLint("NewApi")
    @Override
    public void disconnect() {
        if (gatt != null) {
            gatt.disconnect();
        }
    }
}
