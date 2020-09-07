package com.sachtech.stadia

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.*
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.sachtech.stadia.utils.*
import com.sachtech.stadia.utils.BluetoothConnector.BROADCAST_CONNECT_DEVICE
import kotlin.math.roundToInt

abstract class BaseActivity : AppCompatActivity(), BluetoothConnectionListener {
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
        intentFilter.addAction(BluetoothConnector.bluetooth_receiver)
        registerReceiver(broadCastReceiver, intentFilter)

    }

    val broadCastReceiver = object : BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {

            val distance = intent?.getStringExtra("distance") ?: "0"
            val battery = intent?.getStringExtra("battery") ?: "0"


            runOnUiThread {
                when (intent?.action!!) {
                    BluetoothConnector.BROADCAST_DEVICE_CONNECTED->{
                        onConnect()
                    }
                    BluetoothConnector.BROADCAST_DEVICE_DISCONNECTED->{
                        onDisconnect()
                    }

                    BluetoothConnector.bluetooth_receiver -> {
                        onReceivedData(distance, battery)
                        isHeightAllert(distance.toInt().toDouble())
                    }

                }
            }

        }
    }

    abstract fun onReceivedData(height: String, battery: String)
    abstract fun onConnect()
    abstract fun onDisconnect()

     fun isHeightAllert(heightInt: Double): Boolean {
         if(sharedPreference?.getBoolean(PrefKey.isMetricMeasurement, false)){
             val i = (heightInt - sharedPreference.getInt(PrefKey.HEIGHT_OFFSET, 0)).toDouble().cmtoMeters()
             if(i<0)
                 return false
             return i <=sharedPreference.getInt(PrefKey.seekbarValue,0)
         }
         else{
             val i = (heightInt.toDouble().cmtoInches() - sharedPreference.getInt(PrefKey.HEIGHT_OFFSET, 0)).inchestoFeet()
             if(i<0)
                 return false
             return i <=sharedPreference.getInt(PrefKey.seekbarValue,0)

         }

/*

        val i = (heightInt - sharedPreference.getInt(PrefKey.Height_Inches, 0))* 0.0328
         if(i.toInt()==0)
             return false
        return i <=sharedPreference.getInt(PrefKey.seekbarValue,0)
*/

    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(pairedBluetoothReceiver)
        unregisterReceiver(broadCastReceiver)
    }

    fun connectBt(device: BluetoothDevice) {
       val intent=Intent(BROADCAST_CONNECT_DEVICE)
        intent.putExtra("device",device)
       sendBroadcast(intent)
    }



    override fun onDeviceConnect(device: BluetoothDevice?) {
        runOnUiThread {
            onConnect()
        }


    }

    override fun bluetoothPairError(eConnectException: Exception?, device: BluetoothDevice?) {
        onDisconnect()
    }

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

    override fun onDIsconnect(error: String?) {
        onDisconnect()

    }

    override fun onDestroy() {
        super.onDestroy()

    }

}