package top.autoget.autokit

import android.Manifest.permission.VIBRATE
import android.app.Activity
import android.media.AudioManager
import android.media.MediaPlayer
import androidx.annotation.RequiresPermission
import top.autoget.autokit.VibrateKit.vibrateOnce
import java.io.IOException

object BeepKit {
    private const val BEEP_VOLUME = 0.5f
    private const val VIBRATE_DURATION = 200
    private var mediaPlayer: MediaPlayer? = null

    @RequiresPermission(VIBRATE)
    fun playBeep(activity: Activity, isVibrate: Boolean = true) = when {
        activity.audioManager.ringerMode == AudioManager.RINGER_MODE_NORMAL && mediaPlayer != null -> {
            mediaPlayer?.start()
            if (isVibrate) vibrateOnce(VIBRATE_DURATION) else Unit
        }
        else -> {
            activity.volumeControlStream = AudioManager.STREAM_MUSIC
            mediaPlayer = MediaPlayer().apply {
                setAudioStreamType(AudioManager.STREAM_MUSIC)
                setOnCompletionListener { seekTo(0) }
                try {
                    activity.resources.openRawResourceFd(R.raw.beep).use {
                        setDataSource(it.fileDescriptor, it.startOffset, it.length)
                    }
                    setVolume(BEEP_VOLUME, BEEP_VOLUME)
                    prepare()
                } catch (e: IOException) {
                    mediaPlayer = null
                }
            }
        }
    }
}