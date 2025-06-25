package com.example.estia.MainAppScreen

import android.app.Notification
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.estia.MusicDataBase
import com.example.estia.MusicFile
import com.example.estia.PlayBackMusicFile
import com.example.estia.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat.startForeground
import androidx.core.content.ContextCompat
import com.example.estia.MainActivity


class MainAppScreenViewModel : ViewModel(){

    // ExoPlayer
//    fun monitorAndShowNotification() {
//        viewModelScope.launch {
//            while (true) {
//                val song = nowPlaying.value
//                if (song != null && exoPlayer.isPlaying) {
//                    PlayerNotificationService(context).showNotification(
//                        song.coverArtUri ?: "",
//                        song.artist ?: "Unknown Artist",
//                        song.name ?: "Unknown Title"
//                    )
//                }
//                delay(1000) // Check every 1 second
//            }
//        }
//    }

//    fun createNotificationChannel() {
//
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
//            val channel = NotificationChannel(
//                "media_playback_channel",
//                "Media Playback",
//                NotificationManager.IMPORTANCE_LOW
//            ).apply {
//                description = "Used to control music playback from notification"
//                setSound(null, null)
//                enableVibration(false)
//                setShowBadge(false)
//                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
//            }
//
//
//            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            notificationManager.createNotificationChannel(channel)
//            while(nowPlaying.value != null && exoPlayer.isPlaying){
//
//                PlayerNotificationService(context).showNotification(
//                    nowPlaying.value?.coverArtUri!!,
//                    nowPlaying.value?.artist!!,
//                    nowPlaying.value?.name!!
//                )
//            }
//        }
//    }

    var nowPlayingPaused = mutableStateOf(true)

    val currentPosition = mutableStateOf(0L)

    fun startUpdatingProgress() {
        viewModelScope.launch {
            while (true) {
                currentPosition.value = exoPlayer.currentPosition
                delay(500) // update every 0.5 seconds
            }
        }
    }

    private var _exoPlayer: ExoPlayer? = null
    val exoPlayer: ExoPlayer get() = _exoPlayer!!

    fun initExoPlayer(context: Context) {
        if (_exoPlayer == null) {
            this.context = context.applicationContext
            _exoPlayer = ExoPlayer.Builder(context).build()
        }
    }

    fun resume() {
        nowPlayingPaused.value = false
        if (exoPlayer.mediaItemCount == 0) {
            val file = nowPlaying.value?.filePath
            if(file != null){
                val uri = file.toUri()
                play(uri)
            }
        }
        if (!exoPlayer.isPlaying) {
            exoPlayer.play()
        }
    }

    fun seekTo(pos : Long){
        exoPlayer.seekTo(pos)
    }

    fun pause() {
        nowPlayingPaused.value = true
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        }
    }

    override fun onCleared() {
        _exoPlayer?.release()
        _exoPlayer = null
        super.onCleared()
    }

    fun play(uri: Uri) {
        nowPlayingPaused.value = false
        _exoPlayer?.apply {
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
            playWhenReady = true
        }
        startUpdatingProgress()

    }

    // for now playing drawer
    private lateinit var db: MusicDataBase
    internal lateinit var context : Context

    private val _dominantColor = MutableStateFlow(Color.Gray)
    val dominantColor: StateFlow<Color> = _dominantColor

    private val _nowPlaying = MutableStateFlow<MusicFile?>(null)
    val nowPlaying: StateFlow<MusicFile?> = _nowPlaying

    fun loadPlayBackState() {
        viewModelScope.launch {
            val tempMusicFile = db.playBackMusicFileDao().getState()
            tempMusicFile?.let {
                _nowPlaying.value = MusicFile(
                    name = it.name ?: "",
                    id = it.id,
                    artist = it.artist,
                    album = it.album,
                    duration = it.duration,
                    filePath = it.filePath,
                    coverArtUri = it.coverArtUri,
                    source = it.source
                )
                if (it.coverArtUri != null) {
                    _dominantColor.value = getDominantColorFromUri(it.coverArtUri)
                }
                else{
                    _dominantColor.value = Color(0xFFFFC0CB)
                }
            }
        }
    }

    fun savePlayBackState(music: MusicFile?) {
        viewModelScope.launch {
            music?.let {
                db.playBackMusicFileDao().saveState(
                    PlayBackMusicFile(
                        id = 1, // fixed ID for single state entry
                        name = it.name,
                        artist = it.artist,
                        album = it.album,
                        duration = it.duration,
                        filePath = it.filePath,
                        coverArtUri = it.coverArtUri,
                        source = it.source,
                        color = colorToInt(_dominantColor.value)
                    )
                )
            }
        }
    }

    fun colorToInt(color: Color): Int {
        return color.toArgb()
    }

    fun setNowPlaying(musicFile: MusicFile) {
        if(musicFile != nowPlaying.value){
            _nowPlaying.value = musicFile
            viewModelScope.launch {
                _dominantColor.value = getDominantColorFromUri(musicFile.coverArtUri ?: "")
            }
            savePlayBackState(musicFile)
            val file = musicFile.filePath
            if (file != null) {
                val uri = file.toUri()
                play(uri)
            }
        }
    }

    fun setContextandDB(context: Context){
        this.context = context
        db = MusicDataBase.Companion.getInstance(this.context)
    }

    var isExpandedNowPlaying by mutableStateOf(false)

    var currentScreen by mutableStateOf("ExploreScreen")
    var selectedIcon by mutableStateOf("exploreIcon")

    // Login options and their mapping with their icons in res/drawable/
    val unselectedBottomBarIcons = mapOf(
        "exploreIcon" to R.drawable.explore_icon_unselected,
        "searchIcon" to R.drawable.search_icon_unselected,
        "fileExplorerIcon" to R.drawable.file_explorer_icon_unselected,
        "accountIcon" to R.drawable.account_icon_unselected,
    )

    val unselectedTopBarIcons = mapOf(
        "playListIcon" to R.drawable.playlist_icon_unselected,
        "settingsIcon" to R.drawable.settings_icon_unselected
    )

    val selectedBottomBarIcons = mapOf(
        "exploreIcon" to R.drawable.explore_icon_selected,
        "searchIcon" to R.drawable.search_icon_selected,
        "fileExplorerIcon" to R.drawable.file_explorer_icon_selected,
        "accountIcon" to R.drawable.account_icon_selected,
    )

    val selectedTopBarIcons = mapOf(
        "playListIcon" to R.drawable.playlist_icon_selected,
        "settingsIcon" to R.drawable.settings_icon_selected,
    )

    val screenMapping = mapOf(
        "playListIcon" to "PlayListScreen",
        "exploreIcon" to "ExploreScreen",
        "searchIcon" to "SearchScreen",
        "fileExplorerIcon" to "FileExplorerScreen",
        "accountIcon" to "AccountScreen",
        "settingsIcon" to "SettingsScreen"
    )

    fun changeScreen(icon : String){
        currentScreen = screenMapping[icon].toString()
    }

    suspend fun getDominantColorFromUri(imageUri: String): Color {
        return withContext(Dispatchers.IO) {
            try {
                val loader = ImageLoader(context)
                val request = ImageRequest.Builder(context)
                    .data(imageUri)
                    .allowHardware(false)
                    .build()
                val result = (loader.execute(request) as? SuccessResult)?.drawable
                val bitmap = (result as? BitmapDrawable)?.bitmap

                bitmap?.let {
                    val palette = Palette.from(it).generate()
                    val dominantColor = palette.getDominantColor(android.graphics.Color.BLACK)
                    Color(dominantColor)
                } ?: Color(0xFFFFC0CB)
            } catch (e: Exception) {
                Log.e("DominantColor", "Error extracting color", e)
                Color(0xFFFFC0CB)
            }
        }
    }

    fun formatDuration(durationMs: Long): String {
        val totalSeconds = durationMs / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return if (hours > 0)
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        else
            String.format("%d:%02d", minutes, seconds)
    }
}


class PlayerNotificationService(private val context: Context) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager

    fun showNotification(bitmap: String, title: String, artist: String){

        val remoteViews = RemoteViews(context.packageName, R.layout.custom_notification).apply{
            setTextViewText(R.id.notification_song_title, title)
            setTextViewText(R.id.notification_song_artist, artist)
            setImageViewBitmap(R.id.notification_cover, uriStringToBitmap(bitmap))
            // TODO: Add pending intent for play/pause
        }

        val activityIntent = Intent(context, MainActivity::class.java)
        val activityPendingIntent = PendingIntent.getActivity(
            context,
            1,
            activityIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "media_playback_channel")
            .setSmallIcon(R.drawable.main_logo)
            .setCustomContentView(remoteViews)
            .setOngoing(true)
            .setContentIntent(activityPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setDefaults(0)
            .build()

        notificationManager.notify(1, notification)
    }


    private fun uriStringToBitmap(uriString: String): Bitmap? {
        return try {
            val uri = Uri.parse(uriString)
            val inputStream = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    companion object{
        const val PLAYER_CHANNEL_ID = "player_channel"
    }
}


