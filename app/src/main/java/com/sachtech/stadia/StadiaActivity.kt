package com.sachtech.stadia

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.musify.audioplayer.AudioPlayerManager
import com.sachtech.stadia.utils.*
import kotlinx.android.synthetic.main.activity_description.*

class StadiaActivity : BaseActivity(), View.OnClickListener {
    val audioPlayerManager by lazy { AudioPlayerManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_description)

        btn_activeMode.setOnClickListener(this)
        btn_standBy.setOnClickListener(this)
        btn_beep.setOnClickListener(this)
        btn_mute.setOnClickListener(this)
        btn_unMute.setOnClickListener(this)
        mute_unmute()


    }


    @SuppressLint("SetTextI18n")
    override fun onReceivedData(height: String, battery: String) {
        if (height.isNotEmpty()) {
            if (height.contains("STANDBY", true)) {
                tv_heightftvalue.text = "" + height
                tv_warning.visibility = View.GONE
            } else {
                var heightInt = height.toInt()
                if (sharedPreference?.getBoolean(PrefKey.isMetricMeasurement, false)) {
                    heightInt -= sharedPreference.getInt(PrefKey.HEIGHT_OFFSET, 0)
                    if (heightInt <= 0) {
                        tv_heightftvalue.text = "0"
                    } else
                        tv_heightftvalue.text =
                            "" + (heightInt.cmtoMeters()).toString().uptoTwoDecimal()
                } else {
                    heightInt =
                        heightInt.cmtoInches() - sharedPreference.getInt(PrefKey.HEIGHT_OFFSET, 0)
                    if (heightInt <= 0) {
                        tv_heightftvalue.text = "0"
                    } else
                        tv_heightftvalue.text =
                            "" + (heightInt.inchestoFeet()).toString().uptoTwoDecimal()
                }




                if (isHeightAllert(heightInt)) {
                    if (sharedPreference?.getBoolean(PrefKey.VisualAlert, false)) {
                        tv_warning.visibility = View.VISIBLE
                    }
                } else {
                    tv_warning.visibility = View.GONE
                }

            }

        }

    }


    override fun onConnect() {


    }

    override fun onDisconnect() {
        Toast.makeText(this, "Device disconnected", Toast.LENGTH_SHORT).show()
        finish()
    }


    fun mute_unmute() {
        if (sharedPreference?.getBoolean(
                PrefKey.VoiceAlert,
                false
            ) == true || sharedPreference?.getBoolean(PrefKey.SoundAlert, false) == true
        ) {
            btn_mute.isEnabled = true
            btn_unMute.isEnabled = true
            if (sharedPreference?.getBoolean(PrefKey.isMute, false)) {
                //mute
                btn_mute.isEnabled = false
                btn_unMute.isEnabled = true
            } else {
                btn_mute.isEnabled = true
                btn_unMute.isEnabled = false
            }
        } else {
            btn_mute.isEnabled = false
            btn_unMute.isEnabled = false
        }

    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.btn_activeMode -> {
                sharedPreference.edit().putString(PrefKey.DATA_COMMAND, "0").apply()
            }
            R.id.btn_standBy -> {
                sharedPreference.edit().putString(PrefKey.DATA_COMMAND, "1").apply()

            }
            R.id.btn_beep -> {
                audioPlayerManager.startMediaPlayer(R.raw.beep2, true)
            }
            R.id.btn_mute -> {

                //mute
                audioPlayerManager.mute()
                sharedPreference.edit()?.putBoolean(PrefKey.isMute, true)?.apply()
                btn_mute.isEnabled = false
                btn_unMute.isEnabled = true


            }
            R.id.btn_unMute -> {

                audioPlayerManager.unMute()
                sharedPreference.edit()?.putBoolean(PrefKey.isMute, false)?.apply()
                btn_mute.isEnabled = true
                btn_unMute.isEnabled = false
            }

        }

    }


}