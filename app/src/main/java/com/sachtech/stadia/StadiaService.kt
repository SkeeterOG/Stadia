package com.sachtech.stadia

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothDevice
import android.content.*
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.speech.tts.TextToSpeech
import androidx.annotation.RequiresApi
import com.musify.audioplayer.AudioPlayerManager
import com.sachtech.stadia.utils.*
import java.util.*
import kotlin.coroutines.coroutineContext


class StadiaService(val context:Context): BluetoothConnectionListener {
    val audioPlayerManager by lazy { AudioPlayerManager(context) }
    val bluetoothConnector by lazy { BluetoothConnector.getInstance(context) }
    val sharedPreference: SharedPreferences by lazy {
        context. getSharedPreferences(
            "PREFERENCE_NAME",
            Context.MODE_PRIVATE
        )
    }

    companion object{
        var service:StadiaService?=null
        fun getInstance(context:Context):StadiaService
        {
            if(service==null)
                service= StadiaService(context)
            return service!!
        }

    }

    val broadCastReceiver = object : BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {

            val distance = intent?.getStringExtra("distance") ?: "0"
            val battery = intent?.getStringExtra("battery") ?: "0"

            when (intent?.action!!) {
                BluetoothConnector.BROADCAST_CONNECT_DEVICE -> {
                    val device = intent.getParcelableExtra<BluetoothDevice>("device")
                    if (device != null)
                        connectBt(device)
                }
                BluetoothConnector.bluetooth_receiver -> {
                    if (!distance.isEmpty() && !distance.contains("STANDBY")) {

                        val heightAllert = isHeightAllert(distance.toInt())
                        sendBroadcastAction(
                            BluetoothConnector.BROADCAST_CALCULATED_DATA,
                            Bundle().apply {
                                this.putBoolean("isAlert", heightAllert.first)
                                this.putString(
                                    "distance",
                                    heightAllert.second.toString().uptoTwoDecimal()
                                )
                                this.putString("battery", battery)
                            })
                        if (heightAllert.first) {
                            val distanceInCm = getHeightAfterCalibrate(distance.toInt())
                            playHeightAlert(distanceInCm)
                        } else {
                            audioPlayerManager.stopMedaiPlayer()
                        }
                    } else {
                        audioPlayerManager.stopMedaiPlayer()
                        sendBroadcastAction(
                            BluetoothConnector.BROADCAST_CALCULATED_DATA,
                            Bundle().apply {
                                this.putBoolean("isAlert", false)
                                this.putString("distance", distance)
                                this.putString("battery", battery)
                            })

                    }
                }
                BluetoothConnector.STOPSOUND->{
                    audioPlayerManager.stopMedaiPlayer()
                }

            }


        }
    }


   init{
        bluetoothConnector.setBluetoothConnetionListener(this)
        val intentFilter = IntentFilter()
        intentFilter.addAction(BluetoothConnector.BROADCAST_CONNECT_DEVICE)
        intentFilter.addAction(BluetoothConnector.bluetooth_receiver)
        intentFilter.addAction(BluetoothConnector.STOPSOUND)
       context. registerReceiver(broadCastReceiver, intentFilter)
        //startNotification()

    }



    override fun onDeviceConnect(device: BluetoothDevice?) {
        sharedPreference.edit().putBoolean(PrefKey.isDeviceConnected,true).apply()
        sendBroadcastAction(BluetoothConnector.BROADCAST_DEVICE_CONNECTED)
    }

    override fun onDIsconnect(error: String?) {
        sharedPreference.edit().putBoolean(PrefKey.isDeviceConnected,false).apply()
        sendBroadcastAction(BluetoothConnector.BROADCAST_DEVICE_DISCONNECTED)
        audioPlayerManager.stopMedaiPlayer()
    }

    fun sendBroadcastAction(action: String?) {
        val intent = Intent(action)
        context. sendBroadcast(intent)
    }

    fun sendBroadcastAction(action: String?, data: Bundle) {
        val intent = Intent(action)
        intent.putExtras(data)
        context.   sendBroadcast(intent)
    }

    override fun bluetoothPairError(eConnectException: Exception?, device: BluetoothDevice?) {
        sharedPreference.edit().putBoolean(PrefKey.isDeviceConnected,false).apply()
        sendBroadcastAction(BluetoothConnector.BROADCAST_DEVICE_DISCONNECTED)
        audioPlayerManager.stopMedaiPlayer()
    }

    // connect device
    fun connectBt(device: BluetoothDevice) {
        bluetoothConnector.connect(device)
    }

    fun getHeightAfterCalibrate(heightInt: Int): Int {
        if (sharedPreference?.getBoolean(PrefKey.isMetricMeasurement, false)) {
            val i = (heightInt - sharedPreference.getInt(PrefKey.HEIGHT_OFFSET, 0))

            return i
        } else {
            val i = (heightInt.toDouble().cmtoInches() - sharedPreference.getInt(
                PrefKey.HEIGHT_OFFSET,
                0
            )).toInt().inchestocm()


            return i

        }
    }

    fun isHeightAllert(heightInt: Int): Pair<Boolean, Double> {
        // check which measurement is selected and check height according to that
        if (sharedPreference?.getBoolean(PrefKey.isMetricMeasurement, false)) {
            val i = (heightInt - sharedPreference.getInt(PrefKey.HEIGHT_OFFSET, 0)).toDouble()
                .cmtoMeters()
            if (i < 0)
                return Pair(true, 0.0)
            return Pair(i <= sharedPreference.getInt(PrefKey.seekbarValue, 0), i)
        } else {
            val i = (heightInt.toDouble().cmtoInches() - sharedPreference.getInt(
                PrefKey.HEIGHT_OFFSET,
                0
            )).inchestoFeet()
            if (i < 0)
                return Pair(true, 0.0)
            return Pair(i <= sharedPreference.getInt(PrefKey.seekbarValue, 0), i)

        }

    }

    var isSpeeking = false
    var tts: TextToSpeech? = null
    fun playHeightAlert(distanceInCm: Int) {
        // check if voice alert is on

        if (sharedPreference.getBoolean(PrefKey.VoiceAlert, false)) {
            if (!isSpeeking) {
                isSpeeking = true;

                val onInitListener = TextToSpeech.OnInitListener { status ->
                    if (status != TextToSpeech.ERROR) {
                        tts?.language = Locale.UK
                        val warning = "Warning: Low Altitude"
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            tts?.speak(warning, TextToSpeech.QUEUE_FLUSH, null, null);

                        } else {
                            tts?.speak(warning, TextToSpeech.QUEUE_FLUSH, null);
                        }
                        Handler(context.mainLooper).postDelayed({
                            isSpeeking = false
                        }, 500)

                    }
                }
                tts = TextToSpeech(
                    context,
                    onInitListener
                )
            }

        }else tts?.stop()
        if (sharedPreference.getBoolean(PrefKey.SoundAlert, false)) {
            audioPlayerManager.startMediaPlayer(distanceInCm)
        }
    }
    /* private fun startNotification() {

         //Sets an ID for the notification
         val mNotificationId = 1

         // Build Notification , setOngoing keeps the notification always in status bar


         // Create the Foreground Service
         val notificationManager =
             getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
         val channelId =
             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createNotificationChannel(
                 notificationManager
             ) else ""
         val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this, channelId)
         val notification: Notification = notificationBuilder.setOngoing(true)
             .setSmallIcon(R.mipmap.ic_launcher)
             .setContentTitle(getString(R.string.app_name))
             .setContentText(getString(R.string.app_name))
             .setPriority(NotificationCompat.PRIORITY_HIGH)
             .setCategory(NotificationCompat.CATEGORY_SERVICE)
             .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
             .build()
         notification.flags = Notification.FLAG_ONGOING_EVENT
         startForeground(15101, notification)
     }*/

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager): String {
        val channelId = "15101"
        val channelName = "Stadia_"
        val channel =
            NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
        channel.importance = NotificationManager.IMPORTANCE_HIGH
        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        notificationManager.createNotificationChannel(channel)
        return channelId
    }
}