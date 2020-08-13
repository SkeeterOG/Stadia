package com.sachtech.stadia

import android.bluetooth.BluetoothDevice

interface NextViewListener {
    fun moveToNextFragment(device: BluetoothDevice?)
    fun bluetoothPairError(
        eConnectException: Exception?,
        device: BluetoothDevice?
    )
}