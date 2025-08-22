package com.example.estia.MainAppScreen

import android.content.Context
import android.graphics.drawable.BitmapDrawable
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
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.os.Build
import androidx.compose.runtime.mutableLongStateOf
import androidx.core.graphics.ColorUtils
import com.example.estia.AudioFetcher
import com.example.estia.MusicPlaybackService
import com.example.estia.MusicServiceController
import com.example.estia.SearchScreen.ArtistFullData
import com.example.estia.SearchScreen.DeezerAlbum
import com.example.estia.SearchScreen.DeezerAlbumDetails
import com.example.estia.SearchScreen.DeezerArtist
import com.example.estia.SearchScreen.DeezerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import okhttp3.Request
import org.schabi.newpipe.extractor.timeago.patterns.no
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.coroutines.cancellation.CancellationException

class MainAppScreenViewModel : ViewModel(){

    // Nested nav screens Logic
    val selectedAlbum = mutableStateOf<DeezerAlbum?>(null)
    val selectedAlbumDetails = mutableStateOf<DeezerAlbumDetails?>(null)
    val isLoadingSelectedAlbumDetails = mutableStateOf(false)

    val selectedArtist = mutableStateOf<DeezerArtist?>(null)
    val selectedArtistDetails = mutableStateOf<ArtistFullData?>(null)
    val isLoadingSelectedArtistDetails = mutableStateOf(false)

    fun getSelectedAlbumTracks(){
        viewModelScope.launch {
            isLoadingSelectedAlbumDetails.value = true
            try {
                if(selectedAlbum.value?.id != null){
                    selectedAlbumDetails.value = DeezerService.api.getAlbumDetails(selectedAlbum.value?.id!!)
                }
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
            finally {
                isLoadingSelectedAlbumDetails.value = false
            }
        }
    }

    fun loadArtistData() {
        viewModelScope.launch{
            try {
                isLoadingSelectedArtistDetails.value = true
                selectedArtistDetails.value =
                    DeezerService.getArtistFullData(selectedArtist.value?.id ?: 0)
                // Update UI state with artistData
            } catch (e: Exception) {
                // Handle error
            } finally {
                isLoadingSelectedArtistDetails.value = false
            }
        }
    }

    //
    // Now Playing Logic
    //
    private val audioFetcher = AudioFetcher()

    private var loading = false

    val isLoadingSongURL = mutableStateOf(false)

    val dominantColor = mutableStateOf(Color.Gray)

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
                        if(musicFile.coverArtUri != null){
                            val imageUri = downloadAndCacheSingleImage(context, musicFile.coverArtUri!!)
                            _nowPlaying.value = nowPlaying.value?.copy(coverArtUri = imageUri)
                            dominantColor.value = getDominantColorFromUri(imageUri.toString())
                        }

                        _nowPlaying.value = nowPlaying.value?.copy(source = "Search")

                        // Fetch audio stream URL
                        val url = audioFetcher.fetchAudioStreamUrl_newpipe(
                            nowPlaying.value?.artist.orEmpty(),
                            nowPlaying.value?.name.orEmpty()
                        )

                        _nowPlaying.value = nowPlaying.value?.copy(
                            filePath = url.toString(), streamableURL = url.toString())
                        play()
                        isLoadingSongURL.value = false
                        val duration = getMetadataDuration(url.toString())
                        // Set duration
                        _nowPlaying.value = nowPlaying.value?.copy(duration = duration)
                    }
                    else if(musicFile.source == "Liked Songs"){
                        isLoadingSongURL.value = true
                        dominantColor.value = getDominantColorFromUri(musicFile.coverArtUri.toString())

                        // Fetch audio stream URL
                        val url = audioFetcher.fetchAudioStreamUrl_newpipe(
                            nowPlaying.value?.artist.orEmpty(),
                            nowPlaying.value?.name.orEmpty()
                        )
                        _nowPlaying.value = nowPlaying.value?.copy(
                            filePath = url.toString(), streamableURL = url.toString())
                        play()
                        isLoadingSongURL.value = false
                        val duration = getMetadataDuration(url.toString())
                        // Set duration
                        _nowPlaying.value = nowPlaying.value?.copy(duration = duration)
                    }
                    else if(musicFile.source == "YTMusicSearch"){
                        isLoadingSongURL.value = true

                        // Download image and cache it
                        if(musicFile.coverArtUri != null){
                            val imageUri = downloadAndCacheSingleImage(context, musicFile.coverArtUri!!)
                            _nowPlaying.value = nowPlaying.value?.copy(coverArtUri = imageUri)
                            dominantColor.value = getDominantColorFromUri(imageUri.toString())
                        }

                        _nowPlaying.value = nowPlaying.value?.copy(source = "Search")

                        // Fetch audio stream URL
                        val url = audioFetcher.fetchAudioStreamUrl_newpipe_by_VideoID(
                            nowPlaying.value?.id.toString()
                        )

                        _nowPlaying.value = nowPlaying.value?.copy(
                            filePath = url.toString(), streamableURL = url.toString())
                        play()
                        isLoadingSongURL.value = false
                    }
                    else{
                        dominantColor.value = getDominantColorFromUri(nowPlaying.value?.coverArtUri.toString())
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
                    id = it.id.toString(),
                    artist = it.artist,
                    album = it.album,
                    duration = it.duration,
                    filePath = it.filePath,
                    coverArtUri = it.coverArtUri,
                    source = it.source
                ))
                if (it.coverArtUri != null) {
                    dominantColor.value = getDominantColorFromUri(it.coverArtUri)
                }
                else{
                    dominantColor.value = Color(0xFFFFC0CB)
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
                        color = colorToInt(dominantColor.value)
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

    var currentScreen by mutableStateOf("SearchScreen")
    var selectedIcon by mutableStateOf("searchIcon")

    // Login options and their mapping with their icons in res/drawable/
    val unselectedBottomBarIcons = mapOf(
        //"exploreIcon" to R.drawable.home_icon_unselected,
        "searchIcon" to R.drawable.search_icon_unselected,
        "accountIcon" to R.drawable.library_icon_unselected,
    )

    val selectedBottomBarIcons = mapOf(
        //"exploreIcon" to R.drawable.home_icon_selected_icon,
        "searchIcon" to R.drawable.search_icon_selected,
        "accountIcon" to R.drawable.library_icon_selected,
    )

    val screenMapping = mapOf(
        //"exploreIcon" to "ExploreScreen",
        "searchIcon" to "SearchScreen",
        "accountIcon" to "AccountScreen",
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
                    var dominantColorInt = palette.getDominantColor(android.graphics.Color.BLACK)

                    Color(dominantColorInt)
                } ?: Color(0xFFFFC0CB) // Fallback
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
    fun copyImageToInternalStorage(context: Context, sourcePath: String, id: String): String? {
        return try {
            val sourceFile = File(sourcePath)
            if (!sourceFile.exists()) return null

            // Create destination directory inside internal storage
            val destDir = File(context.filesDir, "cover_images")
            if (!destDir.exists()) destDir.mkdirs()

            // Define the destination file
            val destFile = File(destDir, sourceFile.name)

            if (destFile.exists()) {
                return destFile.absolutePath
            }

            // Copy file
            FileInputStream(sourceFile).use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

            // Return absolute path of copied file
            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun isColorCloseToWhite(color: Color, threshold: Float = 0.4f): Boolean {
        val argb = color.toArgb()
        val luminance = ColorUtils.calculateLuminance(argb) // 0 = black, 1 = white
        return luminance > threshold
    }
}