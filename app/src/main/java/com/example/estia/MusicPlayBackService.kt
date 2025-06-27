package com.example.estia

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.core.net.toUri
import kotlinx.coroutines.flow.MutableStateFlow


// This is now moved to MusicPlaybackService.kt

class MusicPlaybackService : LifecycleService() {
    lateinit var player: ExoPlayer

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this).build()
        MusicServiceController.connect(this)

        startForegroundServiceWithNotification()
    }

    override fun onDestroy() {
        player.release()
        MusicServiceController.disconnect()
        super.onDestroy()
    }

    private fun startForegroundServiceWithNotification() {
        val channelId = "media_playback_channel"
        val channelName = "Media Playback"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Used to control music playback from notification"
                setSound(null, null)
                enableVibration(false)
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Music Playback")
            .setSmallIcon(R.drawable.music_logo)
            .setOngoing(true)
            .build()

        startForeground(1, notification)
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

}




object MusicServiceController {

    private var service: MusicPlaybackService? = null

    // status
    var isPaused = MutableStateFlow<Boolean>(true)

    // Called by the service itself to register
    fun connect(service: MusicPlaybackService) {
        this.service = service
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
        if(uri != null){
            val mediaItem = MediaItem.fromUri(uri)
            service?.player?.apply {
                stop()
                clearMediaItems()
                setMediaItem(mediaItem)
                prepare()
                play()
            }
        }
    }

    fun noMediaSet(): Boolean {
        return service?.player?.mediaItemCount == 0
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


