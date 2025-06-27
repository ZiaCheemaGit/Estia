package com.example.estia.FileExplorerScreen

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.estia.MusicDataBase
import com.example.estia.MusicFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

class FileExplorerViewModel : ViewModel() {

    // Filter Logic
    val filterOptionsList = arrayOf("Songs", "Albums", "Artists")

    var selectedFilter = mutableStateOf(filterOptionsList[0])

    fun applyFilter(newFilter: String) {
        if(selectedFilter.value != newFilter){
            viewModelScope.launch {
                isLoading.value = true
                delay(100) // simulate/allow recomposition/render prep
                if (newFilter == filterOptionsList[0]) {
                    showSongs()
                } else if (newFilter == filterOptionsList[1]) {
                    showAlbums()
                } else if (newFilter == filterOptionsList[2]) {
                    showArtists()
                }
                isLoading.value = false
                selectedFilter.value = newFilter
            }
        }
    }

    // search Logic
    val searchQuery = mutableStateOf("")
    val showSearchBar = mutableStateOf(false)
    val permanentAllSongsList = MutableStateFlow(listOf<MusicFile>())

    fun search() {
        viewModelScope.launch(Dispatchers.IO) {
            val originalList = permanentAllSongsList.value

            val filtered = if (searchQuery.value.isNotEmpty()) {
                originalList.filter {
                    it.name?.contains(searchQuery.value, ignoreCase = true) == true ||
                    it.artist?.contains(searchQuery.value, ignoreCase = true) == true ||
                    it.album?.contains(searchQuery.value, ignoreCase = true) == true
                }
            } else {
                originalList
            }
            if(filtered.size != 0){
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

    fun showSongs() {
        _musicList.value = permanentAllSongsList.value // original song list
    }

    fun showAlbums() {
        _musicList.value = permanentAllSongsList.value
            .distinctBy { it.album?.lowercase(Locale.ROOT)?.trim() } // Normalize album
            .sortedBy { it.album?.lowercase(Locale.ROOT)?.trim() ?: "" }
    }

    fun showArtists() {
        val expanded = permanentAllSongsList.value.flatMap { music ->
            val artists = music.artist
                ?.split(",")
                ?.map { it.trim() }
                ?: listOf("Unknown Artist")

            artists.map { artist -> music.copy(artist = artist) }
        }

        _musicList.value = expanded
            .distinctBy { it.artist?.lowercase(Locale.ROOT)?.trim() }
            .sortedBy { it.artist?.lowercase(Locale.ROOT)?.trim() }
    }

    private val _permissionGranted = MutableStateFlow(false)
    val permissionGranted : StateFlow<Boolean> = _permissionGranted

    private val _isLoading = mutableStateOf(true)
    val isLoading = _isLoading

    fun loadMusicFiles() = viewModelScope.launch(Dispatchers.IO) {

        if(permanentAllSongsList.value.isNotEmpty()){
            isLoading.value = false
            return@launch
        }

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

                    songs.add(
                        MusicFile(
                            name, id, artist, album, duration,
                            filePath, coverArtUri, "Local Storage"
                        )
                    )
                }

                db.musicDao().upsertAll(songs)

                musicList.value = songs
                permanentAllSongsList.value = songs
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