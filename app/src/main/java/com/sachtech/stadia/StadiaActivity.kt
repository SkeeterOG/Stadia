package com.sachtech.stadia

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.musify.audioplayer.AudioPlayerManager
import com.sachtech.stadia.utils.PrefKey
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
    override fun onReceivedData(
        height: String,
        battery: String,
        alert: Boolean
    ) {
        if (height.isNotEmpty()) {
            val devicestatus =
                if (sharedPreference.getString(PrefKey.DATA_COMMAND, "0") == "0") "48" else "49"
            tv_heightbatt.text = "$height $battery $devicestatus"

            if (height.contains("STANDBY", true)) {
                tv_heightftvalue.text = "" + height
                tv_warning.visibility = View.GONE
            } else {

                if (alert) {
                    if (sharedPreference?.getBoolean(PrefKey.VisualAlert, false)) {
                        tv_warning.visibility = View.VISIBLE
                    }
                } else {
                    tv_warning.visibility = View.GONE
                }

                tv_heightftvalue.text = "" + height
            }


        } else {
            val devicestatus =
                if (sharedPreference.getString(PrefKey.DATA_COMMAND, "0") == "0") "48" else "49"
            tv_heightbatt.text = "$height $battery $devicestatus"
        }

    }


    override fun onConnect() {


    }

    override fun onDisconnect() {
        Toast.makeText(this, "Device disconnected", Toast.LENGTH_SHORT).show()
        finish()
    }


    fun mute_unmute() {
        val audioManager: AudioManager =
            this@StadiaActivity.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (audioManager.isStreamMute(AudioManager.STREAM_MUSIC)) {
                //mute
                btn_mute.isEnabled = false
                btn_unMute.isEnabled = true
            } else {
                btn_mute.isEnabled = true
                btn_unMute.isEnabled = false
            }
        } else {
            btn_mute.isEnabled = true
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
                val toneG = ToneGenerator(AudioManager.STREAM_MUSIC, 300)
                toneG?.startTone(ToneGenerator.TONE_DTMF_1, 1000)
            }
            R.id.btn_mute -> {

                //mute
                audioPlayerManager.mute()
                btn_mute.isEnabled = false
                btn_unMute.isEnabled = true


            }
            R.id.btn_unMute -> {

                audioPlayerManager.unMute()
                btn_mute.isEnabled = true
                btn_unMute.isEnabled = false

            }

        }

    }


}