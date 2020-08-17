package com.sachtech.stadia.utils;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.sachtech.stadia.BaseActivity;
import com.sachtech.stadia.NextViewListener;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BluetoothConnector {
    public static final  String bluetooth_receiver = "BLUETOOTH_RECEIVER";

    private Context context;
    private BluetoothSocketWrapper bluetoothSocket;
    private BluetoothSocket mBluetoothSocket;
    private BluetoothDevice device;
    private boolean secure;
    private BluetoothAdapter adapter;
    private List<UUID> uuidCandidates;
    private int candidate;
    ParcelUuid[] uuids;
    private NextViewListener viewListener;


    /**
     * @param device         the device
     * @param secure         if connection should be done via a secure socket
     * @param adapter        the Android BT adapter
     * @param uuidCandidates a list of UUIDs. if null or empty, the Serial PP id is used
     */
    public BluetoothConnector(BluetoothDevice device, boolean secure, BluetoothAdapter adapter,
                              List<UUID> uuidCandidates, NextViewListener viewListener, Context context) {
        this.viewListener = viewListener;
        this.device = device;
        this.context = context;
        this.secure = secure;
        this.adapter = adapter;
        this.uuidCandidates = uuidCandidates;
        uuids = device.getUuids();

        if (this.uuidCandidates == null || this.uuidCandidates.isEmpty()) {
            this.uuidCandidates = new ArrayList<>();
            this.uuidCandidates.add(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
        }
    }

    private byte[] mmBuffer; // mmBuffer store for the stream
    int numBytes;
    ConnectedThread connectedThread;
    private Handler handler; // handler that gets info from Bluetooth service
    InputThread inputThread = new InputThread();

    public BluetoothSocketWrapper connect() throws IOException {
        boolean success = false;
        if (selectSocket()) {
            adapter.cancelDiscovery();
            try {
                // bluetoothSocket.connect();
                if (adapter.isEnabled()) {
                    if (uuids != null) {
                        mBluetoothSocket = device.createRfcommSocketToServiceRecord(uuids[0].getUuid());
                        mBluetoothSocket.connect();
                        mHandler1.sendEmptyMessage(0);
                    }
                }
                success = true;
                viewListener.moveToNextFragment(device);
            } catch (IOException e) {
                //try the fallback
                try {
                    bluetoothSocket = new FallbackBluetoothSocket(bluetoothSocket.getUnderlyingSocket());
                    Thread.sleep(500);
                    bluetoothSocket.connect();



                    inputThread = new InputThread();
                    inputThread.start();
                    inputThread.receiveData(bluetoothSocket);

                    OutputThread outputThread = new OutputThread();
                    outputThread.start();
                    outputThread.sendData(bluetoothSocket, "0".getBytes());

                   /* mmBuffer = new byte[1024];
                    while (true) {
                        try {
                            numBytes = bluetoothSocket.getInputStream().read(mmBuffer);
                            Message readMsg = handler.obtainMessage(
                                    0, numBytes, -1,
                                    mmBuffer);
                            readMsg.sendToTarget();
                        } catch (IOException ex) {
                            Log.d(" ", "Input stream was disconnected", ex);
                            break;
                        }
                    }*/


                    success = true;
                    viewListener.moveToNextFragment(device);
                } catch (FallbackException e1) {
                    Log.w("BT", "Could not initialize FallbackBluetoothSocket classes.", e);
                    viewListener.bluetoothPairError(e1, device);
                } catch (InterruptedException e1) {
                    Log.w("BT", e1.getMessage(), e1);
                    viewListener.bluetoothPairError(e1, device);
                } catch (IOException e1) {
                    Log.w("BT", "Fallback failed. Cancelling.", e1);
                    viewListener.bluetoothPairError(e1, device);
                }
            }
        }
/*
        if (!success) {
            throw new IOException("Could not connect to device: "+ device.getAddress());
        }*/
        return bluetoothSocket;
    }

    private boolean selectSocket() throws IOException {
        if (candidate >= uuidCandidates.size()) {
            return false;
        }

        BluetoothSocket tmp;
        //   UUID uuid = uuidCandidates.get(candidate++);

        // Log.i("BT", "Attempting to connect to Protocol: "+ Arrays.toString(uuids));

        if (uuids != null) {
            if (secure) {
                tmp = device.createRfcommSocketToServiceRecord(uuids[0].getUuid());
            } else {
                tmp = device.createInsecureRfcommSocketToServiceRecord(uuids[0].getUuid());
            }
            bluetoothSocket = new NativeBluetoothSocket(tmp);
        }
        return true;
    }

    public interface BluetoothSocketWrapper {

        InputStream getInputStream() throws IOException;

        OutputStream getOutputStream() throws IOException;

        String getRemoteDeviceName();

        void connect() throws IOException;

        String getRemoteDeviceAddress();

        void close() throws IOException;

        BluetoothSocket getUnderlyingSocket();

    }


    public static class NativeBluetoothSocket implements BluetoothSocketWrapper {

        private BluetoothSocket socket;

        public NativeBluetoothSocket(BluetoothSocket tmp) {
            this.socket = tmp;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return socket.getInputStream();
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return socket.getOutputStream();
        }

        @Override
        public String getRemoteDeviceName() {
            return socket.getRemoteDevice().getName();
        }

        @Override
        public void connect() throws IOException {
            socket.connect();
        }

        @Override
        public String getRemoteDeviceAddress() {
            return socket.getRemoteDevice().getAddress();
        }

        @Override
        public void close() throws IOException {
            socket.close();
        }

        @Override
        public BluetoothSocket getUnderlyingSocket() {
            return socket;
        }

    }


    public static class FallbackBluetoothSocket extends NativeBluetoothSocket {

        private BluetoothSocket fallbackSocket;

        public FallbackBluetoothSocket(BluetoothSocket tmp) throws FallbackException {
            super(tmp);
            try {
                Class<?> clazz = tmp.getRemoteDevice().getClass();
                Class<?>[] paramTypes = new Class<?>[]{Integer.TYPE};
                Method m = clazz.getMethod("createRfcommSocket", paramTypes);
                Object[] params = new Object[]{1};
                fallbackSocket = (BluetoothSocket) m.invoke(tmp.getRemoteDevice(), params);
            } catch (Exception e) {
                throw new FallbackException(e);
            }
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return fallbackSocket.getInputStream();
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return fallbackSocket.getOutputStream();
        }


        @Override
        public void connect() throws IOException {
            fallbackSocket.connect();
        }


        @Override
        public void close() throws IOException {
            fallbackSocket.close();
        }

    }

    public static class FallbackException extends Exception {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        public FallbackException(Exception e) {
            super(e);
        }

    }

    class InputThread extends Thread {
        public void receiveData(BluetoothSocketWrapper socket) throws IOException {
            InputStream socketInputStream = socket.getInputStream();
            byte[] buffer = new byte[256];
            int bytes;

            // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = socketInputStream.read(buffer);
                    //read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    if (readMessage.contains("|")) {
                        String[] arrayString = readMessage.split("|");
                        String distance = arrayString[0];
                        String battery = arrayString[1];

                        Intent intent = new Intent(bluetooth_receiver);
                        intent.putExtra("distance", distance);
                        intent.putExtra("battery", battery);
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

                    }
                    // Send the obtained bytes to the UI Activity via handler
                    Log.i("logging", readMessage + "");
                } catch (IOException e) {
                    break;
                }
            }

        }
    }

    class OutputThread extends Thread {

        public void sendData(BluetoothSocketWrapper socket, byte[] data) throws IOException {

            OutputStream outputStream = socket.getOutputStream();
            if (outputStream != null)
                outputStream.write(data);

        }
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler1 = new Handler() {
        @Override
        public void handleMessage(@NotNull Message msg) {
          /*  if(mBluetoothConnectProgressDialog!=null){
            mBluetoothConnectProgressDialog.dismiss();
            }*/
            //  Toast.makeText(, "Device now Connected", Toast.LENGTH_SHORT).show();
        }
    };
}
