package com.example.estia.MainAppScreen

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.estia.MusicDataBase
import com.example.estia.MusicFile
import com.example.estia.PlayBackMusicFile
import com.example.estia.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Build
import android.widget.RemoteViews
import androidx.compose.runtime.mutableLongStateOf
import androidx.core.app.NotificationCompat
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.example.estia.AudioFetcher
import com.example.estia.MainActivity
import com.example.estia.MusicPlaybackService
import com.example.estia.MusicServiceController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import okhttp3.Request
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Response
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.stream.StreamInfo
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.regex.Pattern
import java.util.zip.GZIPInputStream
import kotlin.coroutines.cancellation.CancellationException

class MainAppScreenViewModel : ViewModel(){

    //
    // Now Playing Logic
    //
    private val audioFetcher = AudioFetcher()

    private var loading = false

    val isLoadingSongURL = mutableStateOf(false)

    private val _dominantColor = MutableStateFlow(Color.Gray)
    val dominantColor: StateFlow<Color> = _dominantColor

    val isPaused = MusicServiceController.isPaused

    private val _nowPlaying = MutableStateFlow<MusicFile?>(null)
    val nowPlaying: StateFlow<MusicFile?> get() = _nowPlaying

    fun isNewSong(song : MusicFile) : Boolean{
        return nowPlaying.value?.id != song.id
    }

    val currentPosition = mutableLongStateOf(0L)
    
    fun startUpdatingProgress() {
        viewModelScope.launch {
            while (true) {

                currentPosition.longValue = MusicServiceController.getCurrentPosition()

                delay(400) // update every 0.5 seconds

            }
        }
    }

    fun setProgress(pos : Long){
        MusicServiceController.seekToPosition(pos)
    }

    fun play(){
        if (nowPlaying.value != null) {
            MusicServiceController.playFile(nowPlaying.value!!)
        } else {
            MusicServiceController.stop()
        }
        startUpdatingProgress()
    }

    fun resume(){
        if(MusicServiceController.noMediaSet()){
            play()
        }
        else{
            MusicServiceController.resume()
        }
    }

    fun pause(){
        MusicServiceController.pause()
    }

    fun clearPlayer(){
        MusicServiceController.clearPlayer()
    }

    fun initService(context: Context) {
        startMusicService(context.applicationContext)
    }

    fun startMusicService(context: Context) {
        val intent = Intent(context, MusicPlaybackService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    private var nowPlayingJob: Job? = null

    fun setNowPlaying(musicFile: MusicFile) {
        if (isNewSong(musicFile)) {

            // Cancel previous job if running
            nowPlayingJob?.cancel()

            nowPlayingJob = viewModelScope.launch {
                try {
                    clearPlayer()
                    _nowPlaying.value = musicFile
                    savePlayBackState(musicFile)

                    if (musicFile.source == "....") {
                        isLoadingSongURL.value = true

                        // Download image and cache it
                        val imageUri = downloadAndCacheSingleImage(context, musicFile.coverArtUri!!)
                        _nowPlaying.value = nowPlaying.value?.copy(coverArtUri = imageUri)

                        // Get dominant color
                        _dominantColor.value = getDominantColorFromUri(imageUri.toString())

                        _nowPlaying.value = nowPlaying.value?.copy(source = "Search")

                        // Fetch audio stream URL
                        val url = audioFetcher.fetchAudioStreamUrl(
                            nowPlaying.value?.artist.orEmpty(),
                            nowPlaying.value?.name.orEmpty()
                        )

                        _nowPlaying.value = nowPlaying.value?.copy(filePath = url.toString())
                        if(!loading){
                            play()
                        }
                        val duration = getMetadataDuration(url.toString())
                        isLoadingSongURL.value = false
                        // Set duration
                        _nowPlaying.value = nowPlaying.value?.copy(duration = duration)

                    }
                    else{
                        _dominantColor.value = getDominantColorFromUri(nowPlaying.value?.coverArtUri.toString())
                        if(!loading){
                            play()
                        }
                    }
                    loading = false
                    savePlayBackState(nowPlaying.value)
                } catch (e: CancellationException) {
                    Log.d("setNowPlaying", "Previous setNowPlaying job was cancelled")
                    isLoadingSongURL.value = false
                } catch (e: Exception) {
                    Log.e("setNowPlaying", "Error setting now playing", e)
                    isLoadingSongURL.value = false
                }
            }
        }
    }

    suspend fun getMetadataDuration(url: String): Long = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(url, HashMap())
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            durationStr?.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            Log.e("Metadata", "Error retrieving metadata", e)
            0L
        } finally {
            retriever.release()
        }
    }

    //
    // Music Data Base Logic
    //
    private lateinit var db: MusicDataBase
    internal lateinit var context : Context

    fun loadPlayBackState() {
        loading = true
        viewModelScope.launch {
            val tempMusicFile = db.playBackMusicFileDao().getState()
            tempMusicFile?.let {
                setNowPlaying(MusicFile(
                    name = it.name ?: "",
                    id = it.id,
                    artist = it.artist,
                    album = it.album,
                    duration = it.duration,
                    filePath = it.filePath,
                    coverArtUri = it.coverArtUri,
                    source = it.source
                ))
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
                        id = it.id,
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

    fun setContextandDB(context: Context){
        this.context = context
        db = MusicDataBase.Companion.getInstance(this.context)
    }

    //
    // Screen Change Logic
    //

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

    private var downloadImageJob: Deferred<String?>? = null

    suspend fun downloadAndCacheSingleImage(context: Context, url: String): String? {
        // Cancel previous job if any
        downloadImageJob?.cancelAndJoin()

        // Launch new job
        downloadImageJob = CoroutineScope(Dispatchers.IO).async {
            try {
                // ✅ Create (or reference) dedicated subfolder in cache
                val imageCacheDir = File(context.cacheDir, "cover_images")
                if (!imageCacheDir.exists()) {
                    imageCacheDir.mkdirs()
                }

                // ✅ Delete all existing files in that folder
                imageCacheDir.listFiles()?.forEach { it.delete() }

                // ✅ Create a new unique file name
                val fileName = "cover_${System.currentTimeMillis()}.jpg"
                val file = File(imageCacheDir, fileName)

                Log.d("DownloadImage", "Starting image download from: $url")

                // ✅ Download the image
                val client = OkHttpClient()
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    response.body?.byteStream()?.use { inputStream ->
                        FileOutputStream(file).use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                        Log.d("DownloadImage", "Image saved to: ${file.absolutePath}")
                        return@async file.absolutePath
                    } ?: run {
                        Log.e("DownloadImage", "Input stream is null")
                        return@async null
                    }
                } else {
                    Log.e("DownloadImage", "Request failed with code: ${response.code}")
                    return@async null
                }

            } catch (e: Exception) {
                if (e is CancellationException) {
                    Log.d("DownloadImage", "Download was cancelled")
                } else {
                    Log.e("DownloadImage", "Error downloading image", e)
                }
                return@async null
            }
        }

        return downloadImageJob?.await()
    }
}