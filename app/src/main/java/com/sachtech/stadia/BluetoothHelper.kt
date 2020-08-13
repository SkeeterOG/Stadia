package com.sachtech.stadia

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import java.io.IOException
import java.util.*


object BluetoothHelper {
     val  bluetoothAdapter by lazy { BluetoothAdapter.getDefaultAdapter() }
   private val  connectThread by lazy {ConnectThread() }
    var  onDeviceScan:((BluetoothDevice)->Unit)? = null

    fun isEnabled():Boolean{
        return bluetoothAdapter.isEnabled
    }

    fun getPairedDevice(): Array<BluetoothDevice> {
        return bluetoothAdapter.bondedDevices.toTypedArray()
    }
    fun startScan(onDeviceScan:(BluetoothDevice)->Unit){
        this.onDeviceScan=onDeviceScan
        bluetoothAdapter.startDiscovery()
    }
    fun cancelSacn(){
        onDeviceScan=null
        bluetoothAdapter.cancelDiscovery()
    }
    fun connectDevice(bluetoothDevice: BluetoothDevice){
        connectThread.start()
        connectThread.connect(bluetoothDevice, UUID.randomUUID())
    }


     val bReciever: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                // Create a new device item
                onDeviceScan?.invoke(device!!)

            }
        }
    }


    class ConnectThread : Thread() {
        private val bTSocket: BluetoothSocket? = null



        fun connect(bTDevice: BluetoothDevice, mUUID: UUID?): Boolean {
            var bTSocket: BluetoothSocket? = null
            bTSocket = try {
                bTDevice.createRfcommSocketToServiceRecord(mUUID)
            } catch (e: IOException) {
                Log.e("CONNECTTHREAD", "Could not create RFCOMM socket:" + e.toString())
                return false
            }

            try {
                bTSocket!!.connect()
            } catch (e: IOException) {
                Log.e("CONNECTTHREAD", "Could not connect: " + e.toString())
                try {
                    bTSocket!!.close()
                } catch (close: IOException) {
                    Log.e("CONNECTTHREAD", "Could not close connection:" + e.toString())
                    return false
                }
            }
            return true
        }

        fun cancel(): Boolean {
            try {
                bTSocket!!.close()
            } catch (e: IOException) {
                Log.e("CONNECTTHREAD", "Could not close connection:" + e.toString())
                return false
            }
            return true
        }
    }

}