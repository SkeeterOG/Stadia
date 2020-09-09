package com.musify.audioplayer

import android.content.Context
import android.content.SharedPreferences
import android.media.*
import android.media.MediaPlayer.*
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.util.Log
import com.sachtech.stadia.utils.PrefKey


class AudioPlayerManager(val context: Context) {





    val handler by lazy { Handler(context.mainLooper) }
    var duration = 1000L;
    var frequency = 500;
    var toneG: ToneGenerator? = null
    val runnable = Runnable {
        if (isToneStarted) {
            toneG?.release()
            toneG = ToneGenerator(AudioManager.STREAM_MUSIC, frequency)
            toneG?.startTone(ToneGenerator.TONE_DTMF_1, duration.toInt())
            restart()
        }
    }
   private fun restart() {
       if(isToneStarted) {
           if (duration == 60000L) {
               handler.postDelayed(runnable, duration + 10)
           } else {
               handler.postDelayed(runnable, (duration + 10) * 2)
           }
       }
    }

    var isToneStarted = false;
    fun startMediaPlayer(height: Int = 150) {
        //mediaPlayer = MediaPlayer.create(context, Uri.parse(url))

        calculateToneProps(height)

        if (!isToneStarted) {
            handler.removeCallbacks(runnable)
            isToneStarted = true
            handler.postDelayed(runnable, 0)
        }

    }

    //Range = 1524 – 1219 cm (50 – 40 ft): F = 270Hz, D = 400ms
    // Range = 1219 – 914 cm (40 – 30 ft): F = 510Hz, D = 400ms
    // Range = 914 – 610 cm (30 – 20 ft): F = 760Hz, D = 300ms
    // Range = 610 – 305 cm (20 – 10 ft): F = 1020Hz, D = 300ms
    // Range = 305 – 152 cm (10 – 5 ft): F = 1220Hz, D = 200ms
    // Range = 152 – 30 cm (5 – 1 ft): F = 1350Hz, D = 100ms
    // Range < 30 cm (1 ft): F = 1400, D = Constant
    var lastDuration=0L
    private fun calculateToneProps(height: Int) {
        lastDuration=duration
        if (height > 1219) {
            frequency = 270
            duration = 600
        } else if (height in 914..1218) {
            frequency = 510
            duration = 400

        } else if (height in 610..914) {
            frequency = 760
            duration = 300

        } else if (height in 305..610) {
            frequency = 1020
            duration = 300

        } else if (height in 152..305) {
            frequency = 1220
            duration = 200

        } else if (height in 30..152) {
            frequency = 1350
            duration = 100

        } else if (height < 30) {
            frequency = 1400
            duration = 60000

        }

        if(lastDuration==60000L){
            if(lastDuration!=duration){
                handler.removeCallbacks(runnable)
                isToneStarted=false
                toneG?.release()
            }
        }

    }

    fun mute() {
        val audioManager: AudioManager =
            context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_MUTE,
                0
            )
        } else {
            audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true)
        };

    }

    fun unMute() {
        val audioManager: AudioManager =
            context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_UNMUTE,
                0
            )
        } else {
            audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false)
        };
    }

    fun stopMedaiPlayer() {
        handler.removeCallbacks(runnable)
        isToneStarted = false
        toneG?.release()
    }





}