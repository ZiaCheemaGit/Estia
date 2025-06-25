package com.example.estia

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import com.example.estia.MainAppScreen.PlayerNotificationService


// This is now moved to MusicPlaybackService.kt

class MusicPlaybackService : LifecycleService() {

    internal lateinit var player: ExoPlayer
    private val queue = mutableListOf<MusicFile>()
    private var currentIndex = 0

    var nowPlayingPaused = mutableStateOf(true)

    private val _nowPlaying = MutableStateFlow<MusicFile?>(null)
    val nowPlaying: StateFlow<MusicFile?> get() = _nowPlaying

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition

    private lateinit var notificationManager: PlayerNotificationService

    override fun onCreate() {
        super.onCreate()

        player = ExoPlayer.Builder(this).build()
        MusicServiceController.connect(this)

        startForegroundServiceWithNotification()
        startMonitoringNotification()
        startProgressUpdater()
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

    private fun startMonitoringNotification() {
        notificationManager = PlayerNotificationService(this)
        lifecycleScope.launch {
            while (true) {
                val song = _nowPlaying.value
                if (song != null && player.isPlaying) {
                    notificationManager.showNotification(
                        song.coverArtUri ?: "",
                        song.artist ?: "Unknown Artist",
                        song.name ?: "Unknown Title"
                    )
                }
                delay(1000)
            }
        }
    }

    private fun startProgressUpdater() {
        lifecycleScope.launch {
            while (true) {
                _currentPosition.value = player.currentPosition
                delay(500)
            }
        }
    }

    fun setPlaylist(songs: List<MusicFile>, startIndex: Int = 0) {
        queue.clear()
        queue.addAll(songs)
        currentIndex = startIndex
        playCurrent()
    }

    fun playCurrent() {
        val song = queue.getOrNull(currentIndex) ?: return
        val mediaItem = MediaItem.fromUri(song.filePath?.toUri()!!)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
        _nowPlaying.value = song
    }

    fun playNext() {
        if (currentIndex < queue.lastIndex) {
            currentIndex++
            playCurrent()
        }
    }

    fun playPrevious() {
        if (currentIndex > 0) {
            currentIndex--
            playCurrent()
        }
    }

    fun pause() {
        player.pause()
    }

    fun resume() {
        player.play()
    }

    override fun onDestroy() {
        player.release()
        super.onDestroy()
    }

    fun togglePlayback() {
        if (player.isPlaying) {
            pause()
        } else {
            resume()
        }
    }


}



object MusicServiceController {

    private var service: MusicPlaybackService? = null

    // Called by the service itself to register
    fun connect(service: MusicPlaybackService) {
        this.service = service
    }

    // get status
    fun isPlaying(): Boolean = service?.player?.isPlaying ?: false

    // Control playback
    fun play() = service?.resume()
    fun pause() = service?.pause()
    fun togglePlayback() = service?.togglePlayback()
    fun playNext() = service?.playNext()
    fun playPrevious() = service?.playPrevious()

    // Set playlist
    fun setPlaylist(songs: List<MusicFile>, startIndex: Int = 0) {
        service?.setPlaylist(songs, startIndex)
    }

    // Access current state
    val nowPlaying: StateFlow<MusicFile?>?
        get() = service?.nowPlaying

    val currentPosition: StateFlow<Long>?
        get() = service?.currentPosition

    // Cleanup
    fun disconnect() {
        service = null
    }
}


