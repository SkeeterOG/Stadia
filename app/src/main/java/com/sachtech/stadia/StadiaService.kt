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


class StadiaService : Service(), BluetoothConnectionListener {
    val audioPlayerManager by lazy { AudioPlayerManager(this) }
    val bluetoothConnector by lazy { BluetoothConnector.getInstance(this) }
    val sharedPreference: SharedPreferences by lazy {
        getSharedPreferences(
            "PREFERENCE_NAME",
            Context.MODE_PRIVATE
        )
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

            }


        }
    }

    override fun onBind(p0: Intent?): IBinder? {

        return null
    }

    override fun onCreate() {
        super.onCreate()
        bluetoothConnector.setBluetoothConnetionListener(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val intentFilter = IntentFilter()
        intentFilter.addAction(BluetoothConnector.BROADCAST_CONNECT_DEVICE)
        intentFilter.addAction(BluetoothConnector.bluetooth_receiver)
        registerReceiver(broadCastReceiver, intentFilter)
        //startNotification()
        return Service.START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadCastReceiver)
    }

    override fun onDeviceConnect(device: BluetoothDevice?) {

        sendBroadcastAction(BluetoothConnector.BROADCAST_DEVICE_CONNECTED)
    }

    override fun onDIsconnect(error: String?) {
        sendBroadcastAction(BluetoothConnector.BROADCAST_DEVICE_DISCONNECTED)
    }

    fun sendBroadcastAction(action: String?) {
        val intent = Intent(action)
        sendBroadcast(intent)
    }

    fun sendBroadcastAction(action: String?, data: Bundle) {
        val intent = Intent(action)
        intent.putExtras(data)
        sendBroadcast(intent)
    }

    override fun bluetoothPairError(eConnectException: Exception?, device: BluetoothDevice?) {
        sendBroadcastAction(BluetoothConnector.BROADCAST_DEVICE_DISCONNECTED)
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
    fun playHeightAlert(distanceInCm: Int) {
        // check if voice alert is on
        if (sharedPreference.getBoolean(PrefKey.VoiceAlert, false)) {
            if (!isSpeeking) {
                isSpeeking = true;
                var tts: TextToSpeech? = null
                val onInitListener = TextToSpeech.OnInitListener { status ->
                    if (status != TextToSpeech.ERROR) {
                        tts?.language = Locale.UK
                        val warning = "Warning: Low Altitude"
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            tts?.speak(warning, TextToSpeech.QUEUE_FLUSH, null, null);

                        } else {
                            tts?.speak(warning, TextToSpeech.QUEUE_FLUSH, null);
                        }
                        Handler(this.mainLooper).postDelayed({
                            isSpeeking = false
                        }, 500)

                    }
                }
                tts = TextToSpeech(
                    applicationContext,
                    onInitListener
                )
            }

        }
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