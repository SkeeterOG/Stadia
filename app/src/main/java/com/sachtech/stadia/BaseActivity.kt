package com.sachtech.stadia

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.*
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.sachtech.stadia.utils.*
import com.sachtech.stadia.utils.BluetoothConnector.BROADCAST_CONNECT_DEVICE

abstract class BaseActivity : AppCompatActivity() {
    val sharedPreference: SharedPreferences by lazy {
        getSharedPreferences(
            "PREFERENCE_NAME",
            Context.MODE_PRIVATE
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }



    override fun onResume() {

        super.onResume()

        val filter1 = IntentFilter()
        filter1.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        filter1.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        registerReceiver(pairedBluetoothReceiver, filter1)
        val intentFilter = IntentFilter()
        intentFilter.addAction(BluetoothConnector.BROADCAST_DEVICE_CONNECTED)
        intentFilter.addAction(BluetoothConnector.BROADCAST_DEVICE_DISCONNECTED)
        intentFilter.addAction(BluetoothConnector.BROADCAST_CALCULATED_DATA)
        registerReceiver(broadCastReceiver, intentFilter)

    }

    val broadCastReceiver = object : BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {


            runOnUiThread {
                when (intent?.action!!) {
                    BluetoothConnector.BROADCAST_DEVICE_CONNECTED->{
                        onConnect()
                    }
                    BluetoothConnector.BROADCAST_DEVICE_DISCONNECTED->{
                        onDisconnect()
                    }

                    BluetoothConnector.BROADCAST_CALCULATED_DATA -> {
                        val distance = intent?.getStringExtra("distance") ?: "0"
                        val battery = intent?.getStringExtra("battery") ?: "0"
                        val isAlert = intent?.getBooleanExtra("isAlert",false) ?: false
                        onReceivedData(distance, battery,isAlert)

                    }

                }
            }

        }
    }
  // method to reflect ui when data received
    abstract fun onReceivedData(
        height: String,
        battery: String,
        alert: Boolean
    )
    abstract fun onConnect()
    abstract fun onDisconnect()

    override fun onPause() {
        super.onPause()
        // register receiver to handle the response from device
        unregisterReceiver(pairedBluetoothReceiver)
        unregisterReceiver(broadCastReceiver)
    }

    fun connectBt(device: BluetoothDevice) {
        // send broad cast tos service to connect device
       val intent=Intent(BROADCAST_CONNECT_DEVICE)
        intent.putExtra("device",device)
       sendBroadcast(intent)
    }



   /* override fun onDeviceConnect(device: BluetoothDevice?) {
        runOnUiThread {
            onConnect()
        }


    }

    override fun bluetoothPairError(eConnectException: Exception?, device: BluetoothDevice?) {
        onDisconnect()
    }*/

    private val pairedBluetoothReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
                val mDevice =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                //3 cases:
                //case1: bonded already
                if (mDevice?.bondState == BluetoothDevice.BOND_BONDED) {
                    Log.d("", "BroadcastReceiver: BOND_BONDED.")
                    // bleUtils.run(mDevice,getActivity());
                    connectBt(mDevice)
                }
                //case2: creating a bone
                if (mDevice?.bondState == BluetoothDevice.BOND_BONDING) {
                    Log.d("", "BroadcastReceiver: BOND_BONDING.")
                }
                //case3: breaking a bond
                if (mDevice?.bondState == BluetoothDevice.BOND_NONE) {
                    Log.d("", "BroadcastReceiver: BOND_NONE.")
                    onDisconnect()
                }
            } else if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(
                    BluetoothAdapter.EXTRA_STATE,
                    BluetoothAdapter.ERROR
                )
                when (state) {
                    BluetoothAdapter.STATE_OFF -> onDisconnect()
                    BluetoothAdapter.STATE_TURNING_OFF -> onDisconnect()
                }
            }
        }
    }

   /* override fun onDIsconnect(error: String?) {
        onDisconnect()

    }*/
    override fun onDestroy() {
        super.onDestroy()

    }

}