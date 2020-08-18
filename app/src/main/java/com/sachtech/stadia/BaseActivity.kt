package com.sachtech.stadia

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.*
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.sachtech.stadia.utils.BluetoothConnector
import com.sachtech.stadia.utils.PrefKey

abstract class BaseActivity : AppCompatActivity(), BluetoothConnectionListener {
    var sharedPreference: SharedPreferences? = null
    var mpVoice: MediaPlayer? = null
    var mpSound: MediaPlayer? = null
    val bluetoothConnector by lazy { BluetoothConnector.getInstance(this, this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreference = getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        var editor = sharedPreference?.edit()

    }

    override fun onResume() {

        super.onResume()
        val filter1 = IntentFilter()
        filter1.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        filter1.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        registerReceiver(pairedBluetoothReceiver,filter1)
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadCastReceiver, IntentFilter("receive broadcast"))

    }

    val broadCastReceiver = object : BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {

            var distance = intent?.getStringExtra("distance") ?: ""
            var battery = intent?.getStringExtra("battery") ?: ""


            runOnUiThread {
                when (intent?.action!!) {
                    BluetoothConnector.bluetooth_receiver -> {
                        onReceivedData(distance, battery)
                        if (isAlertDistance(distance!!.toInt())) {

                            /* val isAlertbeep=true
                             if(isAlertbeep) {

                             }*/

                            if (sharedPreference!!.getBoolean(PrefKey.VisualAlert, false) == true) {
                                // tv_warning.setText("Warning: Low Altitude!")
                                onHeightAlert()
                            }
                            if (sharedPreference!!.getBoolean(PrefKey.VoiceAlert, false) == true) {
                                mpVoice = MediaPlayer.create(contxt, R.raw.beep)
                                mpVoice?.isLooping = true
                                mpVoice?.start();
                            }
                            if (sharedPreference!!.getBoolean(PrefKey.SoundAlert, false) == true) {
                                mpSound = MediaPlayer.create(contxt, R.raw.beep2)
                                mpSound?.isLooping = true
                                mpSound?.start();
                            }


                        } else {

                        }
                    }

                }
            }

        }
    }

    abstract fun onHeightAlert()
    abstract fun onReceivedData(height: String, battery: String)
    abstract fun onConnect()
    abstract fun onDisconnect()

    private fun isAlertDistance(int: Int): Boolean {
        if (sharedPreference!!.getString(PrefKey.seekbarValue, " ") == int.toString()) {
            return true
        }
        return true
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(pairedBluetoothReceiver)
        LocalBroadcastManager.getInstance(this)
            .unregisterReceiver(broadCastReceiver)
    }

    fun onConnectBt(device: BluetoothDevice) {

        bluetoothConnector.connect(device)
    }

    fun writeData(data: String) {
        bluetoothConnector.sendData(data)
    }

    override fun onDeviceConnect(device: BluetoothDevice?) {
        onConnect()

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
                      onConnectBt(mDevice)
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
            }  else if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
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
    override fun onDIsconnect(error: String) {
onDisconnect()

    }


}