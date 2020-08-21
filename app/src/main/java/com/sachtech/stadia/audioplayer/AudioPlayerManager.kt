package com.musify.audioplayer

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.util.Log

class AudioPlayerManager(val context: Context) : AudioManager.OnAudioFocusChangeListener {
     var maudioPlayerListener :AudioPlayerListener?=null
    override fun onAudioFocusChange(focusState: Int) {
        when (focusState) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                // resume playback
                if (mediaPlayer != null) {
                    if (mediaPlayer?.isPlaying == false) mediaPlayer?.start()
                    mediaPlayer?.setVolume(1.0f, 1.0f)
                }
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (mediaPlayer?.isPlaying == true) mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ->
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (mediaPlayer?.isPlaying == true) mediaPlayer?.pause()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK ->
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mediaPlayer?.isPlaying == false) mediaPlayer?.setVolume(0.1f, 0.1f)
        }
    }

    private var mediaPlayer: MediaPlayer? = null

    init {
        mediaPlayer = MediaPlayer()
    }

    private var audioManager: AudioManager? = null
    fun startMediaPlayer(url: String) {
        //mediaPlayer = MediaPlayer.create(context, Uri.parse(url))
        if (mediaPlayer == null) mediaPlayer = MediaPlayer()
        if (mediaPlayer?.isPlaying == true) mediaPlayer?.stop()
        if (requestAudioFocus() == true) {
            mediaPlayer?.setAudioAttributes(getAudioAttributes())
            mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
            mediaPlayer?.setDataSource(context, Uri.parse(url))
            //mediaPlayer?.start()
            mediaPlayer?.prepareAsync()

            mediaPlayer?.setOnCompletionListener {
                releaseMediaPlayer()
                maudioPlayerListener?.onFinish()
            }
            mediaPlayer?.setOnPreparedListener {
                if (mediaPlayer?.isPlaying == false)
                    mediaPlayer?.start()
            }
            mediaPlayer?.setOnErrorListener(object : MediaPlayer.OnErrorListener {
                override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
                    maudioPlayerListener?.onError()
                    when (what) {
                        MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK ->
                            Log.d(
                            "MediaPlayer Error",
                            "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK $extra")
                        MediaPlayer.MEDIA_ERROR_SERVER_DIED ->
                            Log.d(
                            "MediaPlayer Error",
                            "MEDIA ERROR SERVER DIED $extra")
                        MediaPlayer.MEDIA_ERROR_UNKNOWN ->
                            Log.d(
                            "MediaPlayer Error",
                            "MEDIA ERROR UNKNOWN $extra")
                    }
                    return false
                }
            })
            mediaPlayer?.setOnBufferingUpdateListener { mp, percent ->

            }


        }
    }
    fun startMediaPlayer(url: Int) {
        //mediaPlayer = MediaPlayer.create(context, Uri.parse(url))
        mediaPlayer = MediaPlayer.create(context,url)
        if (mediaPlayer?.isPlaying == true) mediaPlayer?.stop()
        if (requestAudioFocus() == true) {
            mediaPlayer?.setAudioAttributes(getAudioAttributes())

            //mediaPlayer?.start()
            mediaPlayer?.prepareAsync()

            mediaPlayer?.setOnCompletionListener {
                releaseMediaPlayer()
                maudioPlayerListener?.onFinish()
            }
            mediaPlayer?.setOnPreparedListener {
                if (mediaPlayer?.isPlaying == false)
                    mediaPlayer?.start()
            }
            mediaPlayer?.setOnErrorListener(object : MediaPlayer.OnErrorListener {
                override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
                    maudioPlayerListener?.onError()
                    when (what) {
                        MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK ->
                            Log.d(
                            "MediaPlayer Error",
                            "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK $extra")
                        MediaPlayer.MEDIA_ERROR_SERVER_DIED ->
                            Log.d(
                            "MediaPlayer Error",
                            "MEDIA ERROR SERVER DIED $extra")
                        MediaPlayer.MEDIA_ERROR_UNKNOWN ->
                            Log.d(
                            "MediaPlayer Error",
                            "MEDIA ERROR UNKNOWN $extra")
                    }
                    return false
                }
            })
            mediaPlayer?.setOnBufferingUpdateListener { mp, percent ->

            }


        }
    }

    fun setAudioPlayerListener(audioPlayerListener: AudioPlayerListener){
        this.maudioPlayerListener=audioPlayerListener
    }
    fun stopMedaiPlayer() {
        //mediaPlayer = MediaPlayer.create(context, Uri.parse(url))
        if (mediaPlayer?.isPlaying == true) mediaPlayer?.stop()
          releaseMediaPlayer()
    }

    fun releaseMediaPlayer() {
        mediaPlayer?.release()
        removeAudioFocus()
        mediaPlayer = null

    }

    /**
     * AudioFocus
     */
    private fun requestAudioFocus(): Boolean {
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager?


        val mAudioAttributes = getAudioAttributes()
        val mAudioFocusRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(mAudioAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(this)
                .build()
        } else {
            null
        }
        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager?.requestAudioFocus(mAudioFocusRequest!!)
        } else {
            audioManager?.requestAudioFocus(
                this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED


    }

    private fun getAudioAttributes(): AudioAttributes {
        return AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
    }

    private fun removeAudioFocus(): Boolean {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager?.abandonAudioFocus(this)
        } else {
            audioManager?.abandonAudioFocus(this)

        }
    }
    fun isPlaying():Boolean{
        return mediaPlayer?.isPlaying==true
    }

    interface AudioPlayerListener{
        fun onFinish(){}
        fun onError(){}
    }
}