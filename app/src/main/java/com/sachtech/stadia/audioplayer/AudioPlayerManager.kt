package com.musify.audioplayer

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.os.Handler
import com.sachtech.stadia.utils.PrefKey


class AudioPlayerManager(val context: Context) {

    val sharedPreference: SharedPreferences by lazy {
        context.getSharedPreferences(
            "PREFERENCE_NAME",
            Context.MODE_PRIVATE
        )
    }


    val handler by lazy { Handler(context.mainLooper) }
    var duration = 1000L
    var frequency = 500

    //var toneG: ToneGenerator? = null
    var toneG: AudioTrack? = null
    val runnable = Runnable {
        if (sharedPreference.getBoolean(PrefKey.SoundAlert, false)) {
            if (isToneStarted) {
                toneG?.release()
                /*toneG = ToneGenerator(AudioManager.STREAM_MUSIC, frequency)
            toneG?.startTone(ToneGenerator.TONE_DTMF_1, duration.toInt())
            */


                toneG = generateTone(frequency.toDouble(), duration.toInt())

                toneG?.play()

                restart()

            }
        }
    }

    private fun generateTone(freqHz: Double, durationMs: Int): AudioTrack? {
        val count = (44100.0 * 2.0 * (durationMs / 1000.0)).toInt() and 1.inv()
        val samples = ShortArray(count)
        var i = 0
        while (i < count) {
            val sample =
                (Math.sin(2 * Math.PI * i / (44100.0 / freqHz)) * 0x7FFF).toShort()
            samples[i + 0] = sample
            samples[i + 1] = sample
            i += 2
        }

        val track = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioTrack(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build(),
                AudioFormat.Builder()
                    .setSampleRate(44100)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO).build(),
                count * (java.lang.Short.SIZE / 8),
                AudioTrack.MODE_STATIC,
                AudioManager.AUDIO_SESSION_ID_GENERATE
            )
        } else {
            AudioTrack(
                AudioManager.STREAM_MUSIC,
                44100,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT,
                count * (java.lang.Short.SIZE / 8),
                AudioTrack.MODE_STATIC
            )
        }



        track.write(samples, 0, count)
        return track
    }

    private fun restart() {
        if (isToneStarted) {
            if (duration == 60000L) {
                handler.postDelayed(runnable, duration + 10)
            } else {
                handler.postDelayed(runnable, (duration + 10) * 2)
            }
        }
    }

    var isToneStarted = false
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
    var lastDuration = 0L
    private fun calculateToneProps(height: Int) {
        lastDuration = duration
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

        if (lastDuration == 60000L) {
            if (lastDuration != duration) {
                handler.removeCallbacks(runnable)
                isToneStarted = false
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
        }

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
        }
    }

    fun setVolumeFull() {
        val audioManager: AudioManager =
            context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
            0
        )
    }

    fun stopMedaiPlayer() {
        isToneStarted = false
        handler.removeCallbacks(runnable)
        toneG?.release()

    }


}