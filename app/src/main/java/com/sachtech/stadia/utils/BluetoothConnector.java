package com.sachtech.stadia.utils;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.sachtech.stadia.BluetoothConnectionListener;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BluetoothConnector {

    public static final String bluetooth_receiver = "BLUETOOTH_RECEIVER";
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


    static BluetoothConnector bluetoothConnector;

    public static BluetoothConnector getInstance(Context context, BluetoothConnectionListener viewListener) {
        if (bluetoothConnector == null)
            bluetoothConnector = new BluetoothConnector(context, viewListener);
        return bluetoothConnector;
    }

    private BluetoothConnector(Context context, BluetoothConnectionListener viewListener) {
        this.context = context;
        this.viewListener = viewListener;

        // uuids = device.getUuids();

        if (this.uuidCandidates == null || this.uuidCandidates.isEmpty()) {
            this.uuidCandidates = new ArrayList<>();
            this.uuidCandidates.add(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
        }
    }

    InputThread inputThread;

    public BluetoothSocket connect(BluetoothDevice device) throws IOException {
        this.device = device;
        uuids = device.getUuids();
        boolean success = false;
        //if (selectSocket()) {
        // adapter.cancelDiscovery();
        try {
            //bluetoothSocket.connect();
            // if (adapter.isEnabled()) {
            if (uuids != null) {
                mBluetoothSocket = device.createRfcommSocketToServiceRecord(uuids[0].getUuid());
                mBluetoothSocket.connect();
                mHandler1.sendEmptyMessage(0);
                inputThread = new InputThread(mBluetoothSocket);
                inputThread.start();
                Thread.sleep(150);
                sendData("0");
            }

            // }
            success = true;
            viewListener.onDeviceConnect(device);
        } catch (Exception e) {
            mBluetoothSocket = null;
            viewListener.onDIsconnect(e.getLocalizedMessage());
        }
/*
        if (!success) {
            throw new IOException("Could not connect to device: "+ device.getAddress());
        }*/
        return mBluetoothSocket;
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
            //   bluetoothSocket = new NativeBluetoothSocket(tmp);
        }
        return true;
    }

    public void sendData(@NotNull String data) {
        OutputThread outputThread = new OutputThread();
        outputThread.start();
        try {
            outputThread.sendData(mBluetoothSocket, data.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("send data ex>> ", e.getLocalizedMessage() + "");
        }
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
        BluetoothSocket socket;

        public InputThread(BluetoothSocket bluetoothSocket) {
            this.socket = bluetoothSocket;
        }

        @Override
        public void run() {
            super.run();

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
                    if(socketInputStream.available()>0) {
                        while (isContinue) {
                            try {
                                bytes = socketInputStream.read(buffer);
                                if (bytes != -1) {
                                    String readMessage = new String(buffer, 0, bytes);
                                    if (readMessage.contains("|")) {
                                        String[] arrayString = readMessage.replace("\r\n","").split("\\|");
                                        String distance = arrayString[0];
                                        String battery = arrayString[1];

                                        Intent intent = new Intent(bluetooth_receiver);
                                        intent.putExtra("distance", distance);
                                        intent.putExtra("battery", battery);
                                       context.sendBroadcast(intent);

                                    }
                                    Log.e("Reading >> ", readMessage + "");
                                }
                                // Send the obtained bytes to the UI Activity via handler
                                Log.e("Reading >> ", "no data");
                            } catch (IOException e) {
                                isContinue = false;
                                if (e != null)
                                    viewListener.onDIsconnect(e.getLocalizedMessage());
                                else viewListener.onDIsconnect("Input Socket null");
                                e.printStackTrace();
                            }
                            //read bytes from input buffer

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
