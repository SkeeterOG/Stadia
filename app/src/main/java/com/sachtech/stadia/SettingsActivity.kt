package com.sachtech.stadia

import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import com.sachtech.stadia.utils.PrefKey
import kotlinx.android.synthetic.main.activity_settings.*


class SettingsActivity : BaseActivity() {

    var sharpref: SharedPreferences? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        sharpref = sharedPreference
        var editor = sharpref?.edit()



        checkBox_VisualAlert.setOnCheckedChangeListener { compoundButton, isChecked ->

            if (isChecked == true) {
                editor?.putBoolean(PrefKey.VisualAlert, true)
                editor?.apply()
            } else {
                editor?.putBoolean(PrefKey.VisualAlert, false)
                editor?.apply()
            }
        }

        checkBox_soundAlert.setOnCheckedChangeListener { compoundButton, isChecked ->
            if (isChecked == true) {
                editor?.putBoolean(PrefKey.SoundAlert, true)
                editor?.apply()
            } else {
                editor?.putBoolean(PrefKey.SoundAlert, false)
                editor?.apply()
                mpSound?.stop()
                mpSound = null
            }
        }

        checkBox_voiceAlert.setOnCheckedChangeListener { compoundButton, isChecked ->
            if (isChecked == true) {
                editor?.putBoolean(PrefKey.VoiceAlert, true)
                editor?.apply()

            } else {

                editor?.putBoolean(PrefKey.VoiceAlert, false)
                editor?.apply()
                mpVoice?.stop()
                mpVoice = null

            }
        }

        seek_bar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar, progress: Int,
                fromUser: Boolean
            ) {
                seekbar_Value.setText(" " + progress.toString())
                editor?.putInt(PrefKey.seekbarValue, progress)
                editor?.apply()
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

        btn_play.setOnClickListener {


            /* mp = MediaPlayer.create(this, R.raw.beep2)
             mp?.start();*/

             if (sharpref!!.getBoolean(PrefKey.VisualAlert, false) == true) {


             }
             if (sharpref!!.getBoolean(PrefKey.VoiceAlert, false) == true) {
                 mpVoice = MediaPlayer.create(this, R.raw.beep)
                 mpVoice?.start();
                 mpVoice?.isLooping=true
             }
             if (sharpref!!.getBoolean(PrefKey.SoundAlert, false) == true) {
                 mpSound = MediaPlayer.create(this, R.raw.beep2)
                 mpSound?.start();
                 mpSound?.isLooping=true
             }
        }

    }

    override fun onResume() {
        super.onResume()
        showSelectedValues()
    }

    override fun onHeightAlert() {

    }

    override fun onReceivedData(height: String, battery: String) {

    }

    override fun onConnect() {
    }
    override fun onDisconnect() {

    }


    fun showSelectedValues() {
        if (sharpref!!.getBoolean(PrefKey.VoiceAlert, false) == true) {
            checkBox_voiceAlert.isChecked = true
        }
        if (sharpref!!.getBoolean(PrefKey.VisualAlert, false) == true) {
            checkBox_VisualAlert.isChecked = true
        }
        if (sharpref!!.getBoolean(PrefKey.SoundAlert, false) == true) {
            checkBox_soundAlert.isChecked = true
        }

        if (sharpref!!.getInt(
                PrefKey.seekbarValue,
                0
            ) != null
        ) {

            seek_bar.setProgress(
                sharpref!!.getInt(
                    PrefKey.seekbarValue,
                    0
                )
            )
            seekbar_Value.setText(
                " " + sharpref!!.getInt(
                    PrefKey.seekbarValue,
                    0
                ).toString()
            )

        }

    }

}