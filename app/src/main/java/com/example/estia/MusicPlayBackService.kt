package com.example.estia

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.core.net.toUri
import androidx.media3.common.Player
import kotlinx.coroutines.flow.MutableStateFlow
import android.app.*
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Handler
import android.os.IBinder
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.RemoteViews
import androidx.annotation.RequiresPermission
import java.io.File

class MusicPlaybackService : Service() {

    companion object {
        private const val NOTIF_ID = 1
        private const val CHANNEL_ID = "media_playback_channel"
    }

    lateinit var player: ExoPlayer

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this).build()
        MusicServiceController.connect(this)

        // TEMP foreground dummy notification to satisfy Android requirements
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Media Playback",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }

        val notif = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Music service is running")
            .setSmallIcon(R.drawable.main_logo)
            .build()

        startForeground(NOTIF_ID, notif)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            stopForeground(true)
        }
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        player.pause()
        player.stop()
        player.release()
        MusicServiceController.disconnect()
        super.onDestroy()
    }

    // Return null since you're not binding
    override fun onBind(intent: Intent?): IBinder? = null

    fun loadImageFromPath(path: String, context: Context): Bitmap? {
        return if (path == "null") {
            BitmapFactory.decodeResource(context.resources, R.drawable.music_icon_compressed)
        } else {
            try {
                val file = File(path.removePrefix("file:"))
                if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}

object MusicServiceController {

    private var service: MusicPlaybackService? = null

    val isPaused = MutableStateFlow(true)

    // Called by the service itself to register
    fun connect(service: MusicPlaybackService) {
        this.service = service

        this.service?.player?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                this@MusicServiceController.isPaused.value = !isPlaying
            }
        })
    }

    fun getCurrentPosition() : Long{
        return service?.player?.currentPosition ?: 0L
    }

    fun seekToPosition(pos : Long){
        service?.player?.seekTo(pos)
    }

    fun resume(){
        service?.player?.play()
    }

    fun playFile(file: MusicFile) {
        val uri = file.filePath?.toUri()
        if (uri != null) {
            val mediaItem = MediaItem.fromUri(uri)
            service?.apply {
                player.stop()
                player.clearMediaItems()
                player.setMediaItem(mediaItem)
                player.prepare()
                player.play()
            }
        } else {
            service?.player?.apply {
                stop()
                clearMediaItems()
                seekTo(0)
                Log.d("Player", "Cleared media items due to null file path.")
            }
        }
    }

    fun noMediaSet(): Boolean {
        return service?.player?.mediaItemCount == 0
    }

    fun clearPlayer(){
        service?.player?.clearMediaItems()
    }

    fun pause(){
        service?.player?.pause()
    }

    fun stop(){
        service?.player?.stop()
    }

    // Cleanup
    fun disconnect() {
        service = null
    }
}

class NotificationActionReceiver : android.content.BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            "ACTION_TOGGLE_PLAY" -> {
                if (MusicServiceController.isPaused.value) {
                    MusicServiceController.resume()
                } else {
                    MusicServiceController.pause()
                }
            }
            "ACTION_NEXT" -> {
                PlaybackController.onNext?.invoke()
            }
            "ACTION_PREVIOUS" -> {
                PlaybackController.onPrevious?.invoke()
            }
        }
    }
}

object PlaybackController {
    var onNext: (() -> Unit)? = null
    var onPrevious: (() -> Unit)? = null
}

