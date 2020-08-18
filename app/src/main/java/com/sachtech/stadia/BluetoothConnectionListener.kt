package com.sachtech.stadia

import android.bluetooth.BluetoothDevice

interface BluetoothConnectionListener {
    fun onDeviceConnect(device: BluetoothDevice?)
    fun onDIsconnect(error:String)
    fun bluetoothPairError(
        eConnectException: Exception?,
        device: BluetoothDevice?
    )
}