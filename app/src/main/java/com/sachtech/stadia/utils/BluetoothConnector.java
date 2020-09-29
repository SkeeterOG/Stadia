package com.sachtech.stadia.utils;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.sachtech.stadia.BluetoothConnectionListener;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BluetoothConnector {

    public static final String bluetooth_receiver = "BLUETOOTH_RECEIVER";
    public static final String BROADCAST_CONNECT_DEVICE = "BROADCAST_CONNECT_DEVICE";
    public static final String BROADCAST_DEVICE_CONNECTED = "BROADCAST_DEVICE_CONNECTED";
    public static final String BROADCAST_DEVICE_DISCONNECTED = "BROADCAST_DEVICE_DISCONNECTED";
    public static final String BROADCAST_CALCULATED_DATA="BROADCAST_CALCULATED_DATA";
    public static final String STOPSOUND="STOPSOUND";
    private Context context;
    // private BluetoothSocketWrapper bluetoothSocket;
    private BluetoothSocket mBluetoothSocket;
    private BluetoothDevice device;
    private boolean secure;
    private BluetoothAdapter adapter;
    private List<UUID> uuidCandidates;
    private int candidate;
    ParcelUuid[] uuids;
    private BluetoothConnectionListener viewListener;


   private static BluetoothConnector bluetoothConnector;


    public static BluetoothConnector getInstance(Context context) {
        if (bluetoothConnector == null)
            bluetoothConnector = new BluetoothConnector(context);
        return bluetoothConnector;
    }

    private BluetoothConnector(Context context) {
        this.context = context;

    }

    public void setBluetoothConnetionListener( BluetoothConnectionListener viewListener){
        this.viewListener= viewListener;
    }
    InputThread inputThread;

    public BluetoothSocket connect(BluetoothDevice device) throws IOException {
        this.device = device;
        uuids = device.getUuids();

        if (this.uuidCandidates == null || this.uuidCandidates.isEmpty()) {
            this.uuidCandidates = new ArrayList<>();
            this.uuidCandidates.add(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
        }
         try {
            if (uuids != null) {
                // create socket between phone and bluetooth device
                mBluetoothSocket = device.createRfcommSocketToServiceRecord(uuids[0].getUuid());
                // connect socket
                mBluetoothSocket.connect();

                // start input thread
                inputThread = new InputThread(mBluetoothSocket);
                inputThread.start();

                // add some delay and send first command to device
                Thread.sleep(150);
                sendInfoCommand();
            }

            // send response back that device is connected
            viewListener.onDeviceConnect(device);
        } catch (Exception e) {
             // send response back that device is not connected
            viewListener.onDIsconnect(e.getLocalizedMessage());
        }

        return mBluetoothSocket;
    }


    private void sendInfoCommand() {
        try {
            // add delay before send data to device


            // check the mode command and send to device
            SharedPreferences sharedPreferences = context.getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE);
            if(sharedPreferences!=null) {
                String command = sharedPreferences.getString(PrefKey.INSTANCE.getDATA_COMMAND(), "0");
                if(command=="0")
                    Thread.sleep(200);
                else Thread.sleep(500);
                sendData(command);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
       }

    public void sendData(@NotNull String data) {
        // create another thread to send command to device
        OutputThread outputThread = new OutputThread();
        outputThread.start();
        try {
            outputThread.sendData(mBluetoothSocket, data.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("send data ex>> ", e.getLocalizedMessage() + "");
        }
    }

    class InputThread extends Thread {
        BluetoothSocket socket;

        public InputThread(BluetoothSocket bluetoothSocket) {
            this.socket = bluetoothSocket;
        }

        @Override
        public void run() {
            super.run();
            // start read device data
            receiveData();

        }

        private void receiveData() {
            if (socket != null) {
                InputStream socketInputStream = null;
                try {
                    socketInputStream = socket.getInputStream();
                    byte[] buffer = new byte[256];
                    int bytes;

                    // Keep looping to listen for received messages
                    boolean isContinue = true;
                    while (isContinue) {
                            try {
                                //read bytes from input buffer
                                bytes = socketInputStream.read(buffer);
                                if (bytes != -1) {
                                    String readMessage = new String(buffer, 0, bytes);

                                    if (readMessage.contains("|")) {
                                        // parse the data received from device
                                        String[] arrayString = readMessage.replace("\r\n","").split("\\|");
                                        String distance = arrayString[0];
                                        String battery = arrayString[1];

                                        // prepare and Send the obtained data to the StadiaService
                                        Intent intent = new Intent(bluetooth_receiver);
                                        intent.putExtra("distance", distance);
                                        intent.putExtra("battery", battery);
                                        context.sendBroadcast(intent);

                                        // save the height of device
                                        SharedPreferences sharedPreferences = context.getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE);
                                        if(!distance.isEmpty()&&!distance.contains("STANDBY"))
                                        sharedPreferences.edit().putInt("Calibrate_value",Integer.parseInt(distance)).apply();

                                        // send command again to get data
                                        sendInfoCommand();
                                    }
                                    Log.e("Reading >> ", readMessage + "");
                                }


                            } catch (IOException e) {
                                isContinue = false;
                                if (e != null)
                                    viewListener.onDIsconnect(e.getLocalizedMessage());
                                else viewListener.onDIsconnect("Input Socket null");
                                e.printStackTrace();
                            }


                        }

                } catch (IOException e) {
                    e.printStackTrace();
                    if(e!=null)
                        viewListener.onDIsconnect(e.getLocalizedMessage());
                    else  viewListener.onDIsconnect("Input Socket null");
                    e.printStackTrace();
                }


            } else {

                viewListener.onDIsconnect("socket null");


            }
        }
    }

    class OutputThread extends Thread {

        public void sendData(BluetoothSocket socket, byte[] data) throws IOException {
            if (socket != null) {
                OutputStream outputStream = socket.getOutputStream();
                if (outputStream != null)
                    outputStream.write(data);
                outputStream.flush();
            } else {
                viewListener.onDIsconnect("socket null");
            }

        }
    }

}
