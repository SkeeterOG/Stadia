package com.sachtech.stadia

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothDevice
import android.content.*
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import com.musify.audioplayer.AudioPlayerManager
import com.sachtech.stadia.utils.*
import kotlin.math.roundToInt

class StadiaService :Service(), BluetoothConnectionListener {
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
                BluetoothConnector.BROADCAST_CONNECT_DEVICE->{
                    val device = intent.getParcelableExtra<BluetoothDevice>("device")
                    if(device!=null)
                    connectBt(device)
                }
                BluetoothConnector.bluetooth_receiver -> {
                        if(!distance.isEmpty()&& !distance.contains("STANDBY")) {

                            if (isHeightAllert(distance.toInt())) {
                                playHeightAlert()
                            } else {
                                audioPlayerManager.stopMedaiPlayer()
                            }
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
    override fun bluetoothPairError(eConnectException: Exception?, device: BluetoothDevice?) {
        sendBroadcastAction(BluetoothConnector.BROADCAST_DEVICE_DISCONNECTED)
    }
    fun connectBt(device: BluetoothDevice) {
        bluetoothConnector.connect(device)
    }
    fun isHeightAllert(heightInt: Int): Boolean {
        if(sharedPreference?.getBoolean(PrefKey.isMetricMeasurement, false)){
            val i = (heightInt - sharedPreference.getInt(PrefKey.HEIGHT_OFFSET, 0)).toDouble().cmtoMeters()
            if(i<=0)
                return false
            return i <=sharedPreference.getInt(PrefKey.seekbarValue,0)
        } else{
            val i = (heightInt.toDouble().cmtoInches() - sharedPreference.getInt(PrefKey.HEIGHT_OFFSET, 0)).inchestoFeet()
            if(i<=0)
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
    fun playHeightAlert(){

        if (sharedPreference.getBoolean(PrefKey.VoiceAlert, false)) {
            audioPlayerManager.startMediaPlayer(R.raw.beep,true)
        }else if (sharedPreference.getBoolean(PrefKey.SoundAlert, false)) {
            audioPlayerManager.startMediaPlayer(R.raw.beep2,true)
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