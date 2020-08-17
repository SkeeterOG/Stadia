package com.sachtech.stadia

import android.content.*
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.sachtech.stadia.utils.BluetoothConnector
import com.sachtech.stadia.utils.PrefKey
import kotlinx.android.synthetic.main.activity_description.*

open class BaseActivity : AppCompatActivity() {
    var sharedPreference: SharedPreferences? = null
    var mp: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreference = getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        var editor = sharedPreference?.edit()

    }

    override fun onResume() {

        super.onResume()
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadCastReceiver, IntentFilter("receive broadcast"))

    }

    val broadCastReceiver = object : BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {

           var distance= intent?.getStringExtra("distance")
            var battery =intent?.getStringExtra("battery")
            
            runOnUiThread {
                when (intent?.action!!) {
                    BluetoothConnector.bluetooth_receiver -> {
                        if (isAlertDistance(distance!!.toInt())) {
                           /* val isAlertbeep=true
                            if(isAlertbeep) {

                            }*/

                            if (sharedPreference!!.getBoolean(PrefKey.VisualAlert, false) == true) {
                                tv_warning.setText("Warning: Low Altitude!")

                            }
                            if (sharedPreference!!.getBoolean(PrefKey.VoiceAlert, false) == true) {
                                mp = MediaPlayer.create(contxt, R.raw.beep)
                                mp?.isLooping = true
                                mp?.start();
                            }
                            if (sharedPreference!!.getBoolean(PrefKey.SoundAlert, false) == true) {
                                mp = MediaPlayer.create(contxt, R.raw.beep2)
                                mp?.isLooping = true
                                mp?.start();
                            }


                        } else {

                        }
                    }

                }
            }

        }
    }

    private fun isAlertDistance(int: Int): Boolean {
        if(sharedPreference!!.getString(PrefKey.seekbarValue," ")==int.toString()){
            return true
        }
        return true
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this)
            .unregisterReceiver(broadCastReceiver)
    }

}