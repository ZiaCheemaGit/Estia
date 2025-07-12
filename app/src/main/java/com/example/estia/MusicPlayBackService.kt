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
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.RemoteViews
import androidx.annotation.RequiresPermission
import java.io.File

class MusicPlaybackService : LifecycleService() {
    private lateinit var notifBuilder: NotificationCompat.Builder

    companion object {
        private const val NOTIF_ID = 1        // EDIT: use a constant everywhere
        private const val CHANNEL_ID = "media_playback_channel"
    }

    lateinit var player: ExoPlayer
    lateinit var mediaSession: MediaSessionCompat
    lateinit var notificationManager: NotificationManagerCompat

    private var currentTitle: String = "Unknown Title"
    private var currentArtist: String = "Unknown Artist"
    private var currentAlbumArt: Bitmap? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        // Build and start a temporary notification
        val initialNotification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Preparing playback…")
            .setSmallIcon(R.drawable.main_logo)
            .setOngoing(true)
            .build()

        startForeground(NOTIF_ID, initialNotification)

        // You already startForeground again later with the real notif — that's fine
        return START_STICKY
    }
    
    override fun onCreate() {
        super.onCreate()

        player = ExoPlayer.Builder(this).build()
        notificationManager = NotificationManagerCompat.from(this)

        mediaSession = MediaSessionCompat(this, "MusicServiceSession")
        mediaSession.isActive = true

        MusicServiceController.connect(this)

        player.addListener(object : Player.Listener {
             override fun onPlaybackStateChanged(state: Int) {
                   if (state == Player.STATE_READY) {
                         // first time the duration is known
                         startForegroundServiceWithNotification()
                         // now start the 1‑sec update loop
                         progressUpdateHandler = Handler(mainLooper)
                         progressUpdateHandler?.post(progressRunnable)
                   }
             }
        })
    }

    override fun onDestroy() {
        player.pause()
        player.stop()
        progressUpdateHandler?.removeCallbacks(progressRunnable)
        player.release()
        mediaSession.release()
        MusicServiceController.disconnect()
        super.onDestroy()
    }

    fun updateMediaSession() {
        mediaSession.setMetadata(
            MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, currentTitle)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, currentArtist)
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, currentAlbumArt)
                .build()
        )

        mediaSession.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setState(
                    if (player.isPlaying) PlaybackStateCompat.STATE_PLAYING
                    else PlaybackStateCompat.STATE_PAUSED,
                    player.currentPosition,
                    1f
                )
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY or
                            PlaybackStateCompat.ACTION_PAUSE or
                            PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                            PlaybackStateCompat.ACTION_SEEK_TO
                )
                .build()
        )

    }

    fun startForegroundServiceWithNotification() {
        val channelId = CHANNEL_ID
        val channelName = "Media Playback"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Used to control music playback from notification"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        // Determine correct play/pause icon
        val playPauseIconRes = if (player.isPlaying) R.drawable.pause_icon else R.drawable.play_icon
        val playPauseTitle = if (player.isPlaying) "Pause" else "Play"

        // --- PendingIntents for actions ---
        val playPauseIntent = PendingIntent.getBroadcast(
            this, 0,
            Intent(this, NotificationActionReceiver::class.java).apply { action = "ACTION_TOGGLE_PLAY" },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val prevIntent = PendingIntent.getBroadcast(
            this, 1,
            Intent(this, NotificationActionReceiver::class.java).apply { action = "ACTION_PREVIOUS" },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nextIntent = PendingIntent.getBroadcast(
            this, 2,
            Intent(this, NotificationActionReceiver::class.java).apply { action = "ACTION_NEXT" },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        // --- Build notification ---
        notifBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(currentTitle)
            .setContentText(currentArtist)
            .setLargeIcon(currentAlbumArt)
            .setSmallIcon(R.drawable.main_logo)
            .addAction(R.drawable.play_previous_icon, "Previous", prevIntent)
            .addAction(playPauseIconRes, playPauseTitle, playPauseIntent)
            .addAction(R.drawable.play_next_icon, "Next", nextIntent)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2) // shows prev, play/pause, next

            )
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setOngoing(player.isPlaying)

        // now compute a safe max/pos
        val duration = player.duration.coerceAtLeast(1L).toInt()
        val position = player.currentPosition.coerceIn(0L, duration.toLong()).toInt()
        notifBuilder.setProgress(duration, position, false)

        startForeground(NOTIF_ID, notifBuilder.build())
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

    fun updateSongInfo(title: String, artist: String, albumArt: Bitmap?) {
        currentTitle = title
        currentArtist = artist
        currentAlbumArt = albumArt
        updateMediaSession()
        startForegroundServiceWithNotification()
    }

    fun loadImageFromPath(path: String, context: Context): Bitmap? {
        if(path == "null"){
            return BitmapFactory.decodeResource(context.resources, R.drawable.music_icon_compressed)

        }
        else{
            return try {
                val file = File(path.removePrefix("file:"))
                if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private var progressUpdateHandler: Handler? = null
    private val progressRunnable = object : Runnable {
        @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
        override fun run() {
            updateMediaSessionPlaybackState()
            updateNotificationProgress()
            progressUpdateHandler?.postDelayed(this, 1000)
        }
    }
    fun updateMediaSessionPlaybackState() {
        val playbackState = PlaybackStateCompat.Builder()
            .setState(
                if (player.isPlaying) PlaybackStateCompat.STATE_PLAYING
                else PlaybackStateCompat.STATE_PAUSED,
                player.currentPosition,
                1f // playback speed
            )
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackStateCompat.ACTION_SEEK_TO
            )
            .build()

        mediaSession.setPlaybackState(playbackState)
    }
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun updateNotificationProgress() {
        // recompute safe max & pos every tick
        val duration = player.duration.coerceAtLeast(1L).toInt()
        val position = player.currentPosition.coerceIn(0L, duration.toLong()).toInt()

        notifBuilder
            .setProgress(duration, position, false)
            .setOngoing(player.isPlaying)   // update play/pause state if needed

        // and re‑notify the same ID
        (getSystemService(NotificationManager::class.java))
            .notify(NOTIF_ID, notifBuilder.build())
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
                service.startForegroundServiceWithNotification()
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

                val albumArt = loadImageFromPath(file.coverArtUri.toString(), service!!)
                updateSongInfo(
                    title = file.name ?: "Unknown Title",
                    artist = file.artist ?: "Unknown Artist",
                    albumArt = albumArt
                )
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

