package com.sachtech.stadia.base

import android.bluetooth.BluetoothDevice

interface BaseInteractionListener {
    fun connectBt(device: BluetoothDevice)
    fun writeDat(data :String)
}