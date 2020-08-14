package com.sachtech.stadia

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.sachtech.stadia.utils.PrefKey
import kotlinx.android.synthetic.main.activity_settings.*


class SettingsActivity : AppCompatActivity() {

    var sharedPreference: SharedPreferences? = null

    var mp: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        sharedPreference = getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        var editor = sharedPreference?.edit()


        checkBox_VisualAlert.setOnCheckedChangeListener { compoundButton, isChecked ->

            if (isChecked == true) {
                editor?.putBoolean(PrefKey.VisualAlert, true)
                editor?.apply()
            } else {
                editor?.putBoolean(PrefKey.VisualAlert, false)
                editor?.apply()
                mp?.isLooping = false
                mp?.stop()
                mp?.release();
                mp = null;

            }
        }

        checkBox_soundAlert.setOnCheckedChangeListener { compoundButton, isChecked ->
            if (isChecked == true) {
                editor?.putBoolean(PrefKey.SoundAlert, true)
                editor?.apply()
            } else {
                editor?.putBoolean(PrefKey.SoundAlert, false)
                editor?.apply()
                mp?.isLooping = false
                mp?.stop()
                mp?.release();
                mp = null;
            }
        }

        checkBox_voiceAlert.setOnCheckedChangeListener { compoundButton, isChecked ->
            if (isChecked == true) {
                editor?.putBoolean(PrefKey.VoiceAlert, true)
                editor?.apply()
            } else {
                editor?.putBoolean(PrefKey.VoiceAlert, false)
                editor?.apply()
                mp?.isLooping = false
                mp?.stop()
                mp?.release();
                mp = null;
            }
        }

        seek_bar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar, progress: Int,
                fromUser: Boolean
            ) {
                seekbar_Value.setText(" " + progress.toString())
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
            if (sharedPreference!!.getBoolean(PrefKey.VisualAlert, false) == true) {
                mp = MediaPlayer.create(this, R.raw.beep2)
                mp?.isLooping = true
                mp?.start();
            }
            if (sharedPreference!!.getBoolean(PrefKey.VoiceAlert, false) == true) {
                mp = MediaPlayer.create(this, R.raw.beep)
                mp?.isLooping = true
                mp?.start();
            }
            if (sharedPreference!!.getBoolean(PrefKey.SoundAlert, false) == true) {
                mp = MediaPlayer.create(this, R.raw.beep2)
                mp?.isLooping = true
                mp?.start();
            }
        }

    }


}