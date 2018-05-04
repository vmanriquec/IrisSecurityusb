package com.apolomultimedia.guardify.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.apolomultimedia.guardify.model.BluetoothDeviceModel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class BlueetoothConnectionService extends Service {

    private String TAG = getClass().getSimpleName();

    BluetoothSocket bluetoothSocket;
    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice bluetoothDevice;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(TAG, "onCreate creando las conexiones a dispositivos blotoo");

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            Log.i(TAG, "hay device...");
            for (BluetoothDevice device : pairedDevices) {
                bluetoothDevice = device;
            }

            Log.i(TAG, "device: " + bluetoothDevice.getAddress());
            new ConnectThread(bluetoothDevice).execute();

        }

    }
/*hilo de comunicacion asincrona con el dispositivo blotoo*/
    private class CommunicationThread extends AsyncTask<Void, Void, Void> {

        private BluetoothSocket mmSocket;
        private InputStream mmInStream;
        private OutputStream mmOutStream;

        public CommunicationThread(BluetoothSocket mmSocket) {
            this.mmSocket = mmSocket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = this.mmSocket.getInputStream();
                tmpOut = this.mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;

        }

        @Override
        protected Void doInBackground(Void... params) {
            byte[] buffer = new byte[1024];
            int begin = 0;
            int bytes = 0;
            while (true) {
                try {
                    bytes += mmInStream.read(buffer, bytes, buffer.length - bytes);
                    for (int i = begin; i < bytes; i++) {
                        if (buffer[i] == "#".getBytes()[0]) {
                            mHandler.obtainMessage(1, begin, i, buffer).sendToTarget();
                            begin = i + 1;
                            if (i == bytes - 1) {
                                bytes = 0;
                                begin = 0;
                            }
                        }
                    }

                    bytes = mmInStream.read(buffer);
                    String dataRecibida = new String(buffer, 0, bytes);
                    final String message = String.valueOf(bytes) + " bytes received:\n" + dataRecibida;

                    Log.i(TAG, "message: " + message);

                } catch (IOException e) {
                    break;
                }
            }


            return null;
        }
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            Log.i(TAG, "handling msg....");

            byte[] writeBuf = (byte[]) msg.obj;
            int begin = (int) msg.arg1;
            int end = (int) msg.arg2;

            switch (msg.what) {
                case 1:
                    String writeMessage = new String(writeBuf);
                    writeMessage = writeMessage.substring(begin, end);
                    break;
            }
        }
    };

    private class ConnectThread extends AsyncTask<Void, Void, Void> {

        private BluetoothDevice mmDevice;

        private final UUID MY_UUID = UUID.fromString(String.valueOf(BluetoothDeviceModel.getUUID()));
        boolean success = false;

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            mmDevice = device;
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            bluetoothSocket = tmp;
        }

        @Override
        protected Void doInBackground(Void... params) {
            bluetoothAdapter.cancelDiscovery();

            try {
                success = true;
                bluetoothSocket.connect();
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    bluetoothSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                    return null;
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (success) {
                Log.i(TAG, "conectado");
                new CommunicationThread(bluetoothSocket).execute();
            }


        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
