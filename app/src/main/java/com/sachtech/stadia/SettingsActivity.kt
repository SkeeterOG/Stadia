package com.sachtech.stadia

import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.media.MediaPlayer
import android.os.Handler
import android.widget.RadioButton
import android.widget.SeekBar
import android.widget.Toast
import com.musify.audioplayer.AudioPlayerManager
import com.sachtech.stadia.utils.BluetoothConnector
import com.sachtech.stadia.utils.PrefKey
import kotlinx.android.synthetic.main.activity_settings.*


class SettingsActivity : BaseActivity() {
    private var mediaPlayer: MediaPlayer? = null
    val audioPlayerManager by lazy { AudioPlayerManager(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        if (sharedPreference?.getBoolean(PrefKey.isMetricMeasurement, false)) {
            radioMetric.isChecked = true
            radioImperial.isChecked = false
        } else {
            radioMetric.isChecked = false
            radioImperial.isChecked = true

        }
        setUpScrollerTitle();

        radioGroup.setOnCheckedChangeListener { radioGroup, i ->
            val selectedRadioButtonId: Int = radioGroup.checkedRadioButtonId
            when (radioGroup.checkedRadioButtonId) {
                R.id.radioMetric -> {
                    sharedPreference?.edit().putBoolean(PrefKey.isMetricMeasurement, true).apply()
                }
                R.id.radioImperial -> {
                    sharedPreference?.edit().putBoolean(PrefKey.isMetricMeasurement, false).apply()
                }
            }
            setUpScrollerTitle();




        }



        checkBox_VisualAlert.setOnCheckedChangeListener { compoundButton, isChecked ->

            if (isChecked) {
                sharedPreference.edit()?.putBoolean(PrefKey.VisualAlert, true)?.apply()

            } else {
                sharedPreference.edit()?.putBoolean(PrefKey.VisualAlert, false)?.apply()
            }
        }

        checkBox_soundAlert.setOnCheckedChangeListener { compoundButton, isChecked ->
            if (isChecked) {
                sharedPreference.edit()?.putBoolean(PrefKey.SoundAlert, true)?.apply()

            } else {
                sharedPreference.edit()?.putBoolean(PrefKey.SoundAlert, false)?.apply()
               sendBroadcast(Intent(BluetoothConnector.STOPSOUND))

            }
        }

        checkBox_voiceAlert.setOnCheckedChangeListener { compoundButton, isChecked ->
            if (isChecked) {
                sharedPreference.edit()?.putBoolean(PrefKey.VoiceAlert, true)?.apply()
            } else {
                sharedPreference.edit()?.putBoolean(PrefKey.VoiceAlert, false)?.apply()
            }
        }

        seek_bar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar, progress: Int,
                fromUser: Boolean
            ) {
                seekbar_Value.text = " " + progress.toString()
                sharedPreference.edit()?.putInt(PrefKey.seekbarValue, progress)?.apply()
                //Toast.makeText(applicationContext, "seekbar progress: $progress", Toast.LENGTH_SHORT).show()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                //Toast.makeText(applicationContext, "seekbar touch started!", Toast.LENGTH_SHORT).show()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                //Toast.makeText(applicationContext, "seekbar touch stopped!", Toast.LENGTH_SHORT).show()
            }
        }
        )

        //Example of Tones
        btn_play.setOnClickListener {
            mediaPlayer = MediaPlayer.create(this, R.raw.stadiaexample)
            mediaPlayer?.start()
            Toast.makeText(this, "50ft - 40 ft (15.2m - 12.2m)", Toast.LENGTH_SHORT).show()
            val handler = Handler()
            handler.postDelayed({
                Toast.makeText(this, "40ft - 30ft (12.2m - 9.1m)", Toast.LENGTH_SHORT).show()
            }, 10)
            handler.postDelayed({
                Toast.makeText(this, "30ft - 20ft (9.1m - 6.1m)", Toast.LENGTH_SHORT).show()
            }, 10)
            handler.postDelayed({
                Toast.makeText(this, "20ft - 10ft (6.1m - 3m)", Toast.LENGTH_SHORT).show()
            }, 10)
            handler.postDelayed({
                Toast.makeText(this, "10ft - 3ft (3m - 0.9m)", Toast.LENGTH_SHORT).show()
            }, 10)
            handler.postDelayed({
                Toast.makeText(this, "3ft - 1ft (0.9m - 0.3m)", Toast.LENGTH_SHORT).show()
            }, 10)
            handler.postDelayed({
                Toast.makeText(this, "Less than 1ft (0.3m)", Toast.LENGTH_SHORT).show()
            }, 10)






            //val toneG = ToneGenerator(AudioManager.STREAM_MUSIC, 300)
            //toneG?.startTone(ToneGenerator.TONE_DTMF_1, 10000)
            /*audioPlayerManager.stopMedaiPlayer()
            audioPlayerManager.startMediaPlayer(20)*/
        }

    }

    private fun setUpScrollerTitle() {
        if (sharedPreference.getBoolean(PrefKey.isMetricMeasurement, false)) {
            tv_alertHeight.text = "Alert Height Adjustment(meter)"

        } else {
            tv_alertHeight.text = "Alert Height Adjustment(feet)"
        }
    }

    override fun onResume() {
        super.onResume()
        showSelectedValues()
    }


    override fun onReceivedData(
        height: String,
        battery: String,
        alert: Boolean
    ) {

    }

    override fun onConnect() {
    }

    override fun onDisconnect() {

    }


    fun showSelectedValues() {
        if (sharedPreference?.getBoolean(PrefKey.VoiceAlert, false) == true) {
            checkBox_voiceAlert.isChecked = true
        }
        if (sharedPreference?.getBoolean(PrefKey.VisualAlert, false) == true) {
            checkBox_VisualAlert.isChecked = true
        }
        if (sharedPreference?.getBoolean(PrefKey.SoundAlert, false) == true) {
            checkBox_soundAlert.isChecked = true
        }











        seek_bar.setProgress(sharedPreference.getInt(PrefKey.seekbarValue, 0))
        seekbar_Value.setText(
            (sharedPreference.getInt(PrefKey.seekbarValue, 0)).toString()
        )


    }

    override fun onDestroy() {
        super.onDestroy()

    }

}