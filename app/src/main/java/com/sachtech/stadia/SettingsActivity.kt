package com.sachtech.stadia

import android.os.Bundle
import android.util.Log
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SeekBar
import com.musify.audioplayer.AudioPlayerManager
import com.sachtech.stadia.utils.PrefKey
import kotlinx.android.synthetic.main.activity_settings.*


class SettingsActivity : BaseActivity() {
    private lateinit var selectedRadioButton: RadioButton

    val audioPlayerManager by lazy { AudioPlayerManager(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        if (sharedPreference?.getBoolean(PrefKey.isMetricMeasurement, false)) {
            radioMetric.isChecked = true
            radioImperical.isChecked = false
        } else {
            radioMetric.isChecked = false
            radioImperical.isChecked = true

        }
        setUpScrollerTitle();

        radioGroup.setOnCheckedChangeListener { radioGroup, i ->
            val selectedRadioButtonId: Int = radioGroup.checkedRadioButtonId
            when (radioGroup.checkedRadioButtonId) {
                R.id.radioMetric -> {
                    sharedPreference?.edit().putBoolean(PrefKey.isMetricMeasurement, false).apply()
                }
                R.id.radioImperical -> {
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

        btn_play.setOnClickListener {
            audioPlayerManager.startMediaPlayer(R.raw.beep2, false)
        }

    }

    private fun setUpScrollerTitle() {
        if (sharedPreference.getBoolean(PrefKey.isMetricMeasurement, false)) {
            tv_alertHeight.text = "Alert Height Adjustment(meter)"

        } else {
            tv_alertHeight.text = "Alert Height Adjustment(inches)"
        }
    }

    override fun onResume() {
        super.onResume()
        showSelectedValues()
    }


    override fun onReceivedData(height: String, battery: String) {

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
        audioPlayerManager.releaseMediaPlayer()
    }

}