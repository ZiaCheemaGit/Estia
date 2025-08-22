package com.example.estia.AccountScreen.LocalFilesScreen

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.util.Patterns
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.estia.AudioFetcher
import com.example.estia.MusicDataBase
import com.example.estia.MusicFile
import com.example.estia.downloader.DownloaderObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class FileExplorerViewModel : ViewModel() {

    // search Logic
    val searchQuery = mutableStateOf("")
    val showSearchBar = mutableStateOf(false)
    val permanentAllSongsList = MutableStateFlow(listOf<MusicFile>())

    fun search() {
        viewModelScope.launch(Dispatchers.IO) {
            val originalList = permanentAllSongsList.value

            val query = searchQuery.value.trim()
            val filtered = if (query.isNotEmpty()) {
                originalList
                    .map { song ->
                        val nameMatch = song.name.contains(query, ignoreCase = true)
                        val artistMatch = song.artist?.contains(query, ignoreCase = true) ?: false
                        val albumMatch = song.album?.contains(query, ignoreCase = true) ?: false

                        val score = when {
                            nameMatch -> 3
                            artistMatch -> 2
                            albumMatch -> 1
                            else -> 0
                        }

                        song to score
                    }
                    .filter { it.second > 0 }
                    .sortedByDescending { it.second }
                    .map { it.first }
            } else {
                originalList
            }

            if (filtered.isNotEmpty()) {
                musicList.value = filtered
            }
        }
    }

    fun stopSearch(){
        viewModelScope.launch(Dispatchers.IO) {
            musicList.value = permanentAllSongsList.value
        }
    }

    private lateinit var db: MusicDataBase
    lateinit var context : Context

    fun setContentResolverAndInitDB(resolver: ContentResolver, context: Context) {
        contentResolver = resolver
        this.context = context
        db = MusicDataBase.Companion.getInstance(context)
        Log.d("DB_INIT", "Database initialized: $db")
    }

    private var contentResolver : ContentResolver? = null

    val _musicList = MutableStateFlow(listOf<MusicFile>())
    val musicList = _musicList

    private val _permissionGranted = MutableStateFlow(false)
    val permissionGranted : StateFlow<Boolean> = _permissionGranted

    private val _isLoading = mutableStateOf(true)
    val isLoading = _isLoading

    fun loadMusicFiles() = viewModelScope.launch(Dispatchers.IO) {

        var existingSongs = db.musicDao().getAllMusic().firstOrNull()
        if (!existingSongs.isNullOrEmpty()) {
            permanentAllSongsList.value = existingSongs
            _musicList.value = existingSongs
            _isLoading.value = false
            return@launch
        }

        isLoading.value = true
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DATA,
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

        contentResolver?.let{ consRes ->
            val cursor = consRes.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
            )
            val songs = mutableListOf<MusicFile>()
            cursor?.use {
                val nameColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val filePathColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

                while (it.moveToNext()) {
                    var name = it.getString(nameColumn)
                    val id = it.getLong(idColumn)
                    var artist = it.getString(artistColumn)
                    val album = it.getString(albumColumn)
                    val duration = it.getLong(durationColumn)
                    val filePath = it.getString(filePathColumn)
                    val coverArtUri = extractCoverArtUri(filePath)

                    name = name.substringBeforeLast("-")

                    if (artist == "<unknown>") {artist = "Unknown Artist"}

                    val song = MusicFile(
                        name, id.toString(), artist, album, duration,
                        filePath, coverArtUri, "Local Storage"
                    )

                    if(!permanentAllSongsList.value.contains(song)){
                        songs.add(song)
                    }
                }

                db.musicDao().upsertAll(songs)

                musicList.value += songs
                permanentAllSongsList.value += songs
                isLoading.value = false

                var existingSongs = db.musicDao().getAllMusic().firstOrNull()
                if (!existingSongs.isNullOrEmpty()) {
                    _musicList.value = existingSongs
                    permanentAllSongsList.value = existingSongs
                    _isLoading.value = false
                    return@launch
                }
            }
        }
    }

    fun refreshMusicFiles() = viewModelScope.launch(Dispatchers.IO) {
        db.musicDao().deleteAll()
        loadMusicFiles()
    }

    fun grantPermission(){
        _permissionGranted.value = true
    }

    fun isGranted() : Boolean{
        return permissionGranted.value
    }

    fun extractCoverArtUri(filePath: String): String? {
        val mmr = MediaMetadataRetriever()
        return try {
            mmr.setDataSource(filePath)
            val artBytes = mmr.embeddedPicture
            if (artBytes != null) {
                // Save to internal cache
                val file = File(context.cacheDir, "${filePath.hashCode()}.jpg")
                file.writeBytes(artBytes)
                file.toURI().toString()
            } else null
        } catch (e: Exception) {
            null
        } finally {
            mmr.release()
        }
    }

    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
