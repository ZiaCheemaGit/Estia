package com.example.estia.SearchScreen.MainScreen

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.example.estia.MusicDataBase
import com.example.estia.MusicFile
import com.example.estia.SearchHistoryEntry
import com.example.estia.SearchScreen.DeezerAlbum
import com.example.estia.SearchScreen.DeezerArtist
import com.example.estia.SearchScreen.DeezerService
import com.example.estia.SearchScreen.DeezerTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.Locale
import kotlin.coroutines.cancellation.CancellationException

class SearchScreenViewModel : ViewModel() {

    private lateinit var db: MusicDataBase

    fun initializeDataBAse(context: Context){
        db = MusicDataBase.Companion.getInstance(context)
    }

    private var country : String = ""

    val filterOptionsList = listOf("Songs", "Albums", "Artists")
    val selectedFilter = mutableStateOf(filterOptionsList[0])
    val searchQuery = mutableStateOf("")

    var history = mutableStateOf(emptyList<SearchHistoryEntry>())
        private set
    private val maxHistorySize = 20

    fun addToHistory(musicFile: MusicFile) {
        val entry = SearchHistoryEntry(
            id = musicFile.id,
            name = musicFile.name,
            artist = musicFile.artist,
            album = musicFile.album,
            duration = musicFile.duration,
            filePath = musicFile.filePath,
            coverArtUri = musicFile.coverArtUri,
            source = musicFile.source,
            timeStamp = System.currentTimeMillis()
        )

        // Remove any old instance with same ID
        val updated = listOf(entry) + history.value.filterNot { it.id == entry.id }

        // Limit to last MAX_HISTORY_SIZE entries
        history.value = updated.take(maxHistorySize)

        saveSearchHistoryToDB()
    }

    fun saveSearchHistoryToDB(){
        viewModelScope.launch(Dispatchers.IO) {
            db.searchHistoryDao().clearHistory()
            db.searchHistoryDao().upsertAll(history.value)
        }
    }

    fun loadSearchHistoryFromDB() {
        viewModelScope.launch(Dispatchers.IO) {
            val initial = db.searchHistoryDao().getHistory().firstOrNull()
            if (initial != null) {
                history.value = initial
            }
        }
    }

    fun removeFromHistory(musicFile: MusicFile) {
        history.value = history.value.filterNot { it.id == musicFile.id }
        saveSearchHistoryToDB()
    }

    private var searchJob: Job? = null

    val songSearchResults = mutableStateOf<List<DeezerTrack>>(emptyList())
    val albumSearchResults = mutableStateOf<List<DeezerAlbum>>(emptyList())
    val artistSearchResults = mutableStateOf<List<DeezerArtist>>(emptyList())

    fun clearSearchResults(){
        songSearchResults.value = emptyList()
        albumSearchResults.value = emptyList()
        artistSearchResults.value = emptyList()
    }

    var isLoading = mutableStateOf(false)
        private set

    fun fillSearchResultsBySongs(query: String) {
        if (query.isBlank()) {
            songSearchResults.value = emptyList()
            return
        }

        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            songSearchResults.value = emptyList()
            try {
                isLoading.value = true
                val searchResponse = DeezerService.api.searchTracks(query)
                songSearchResults.value = searchResponse.data


            } catch (e: Exception) {
                if (e !is CancellationException) {
                    Log.d("Search Songs", e.toString())
                    songSearchResults.value = emptyList()
                }
            } finally {
                isLoading.value = false
            }
        }
    }

    fun fillSearchResultsByAlbums(query: String){
        if (query.isBlank()) {
            albumSearchResults.value = emptyList()
            return
        }

        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            albumSearchResults.value = emptyList()
            try {
                isLoading.value = true
                val searchResponse = DeezerService.api.searchAlbums(query)
                albumSearchResults.value = searchResponse.data
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    albumSearchResults.value = emptyList()
                }
            } finally {
                isLoading.value = false
            }
        }
    }

    fun fillSearchResultsByArtists(query: String){
        if (query.isBlank()) {
            artistSearchResults.value = emptyList()
            return
        }

        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            artistSearchResults.value = emptyList()
            try {
                isLoading.value = true
                val searchResponse = DeezerService.api.searchArtists(query)
                artistSearchResults.value = searchResponse.data
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    artistSearchResults.value = emptyList()
                }
            } finally {
                isLoading.value = false
            }
        }
    }

    suspend fun getAllArtists(id: String): String = withContext(Dispatchers.IO) {
        val artists = DeezerService.api.getTrackDetails(id.toLong()).contributors
        artists
            .distinctBy { it.name }
            .joinToString(separator = ", ") { it.name }
    }

    fun applyFilter(){
        when(selectedFilter.value){
            filterOptionsList[0] -> fillSearchResultsBySongs(searchQuery.value)
            filterOptionsList[1] -> fillSearchResultsByAlbums(searchQuery.value)
            filterOptionsList[2] -> fillSearchResultsByArtists(searchQuery.value)
        }
    }

    suspend fun getCachedSongPath(
        context: Context,
        artist: String,
        songName: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            val py = Python.getInstance()
            val pyModule = py.getModule("ytdlp_wrapper")
            val fullQuery = "$songName $artist"
            Log.d("Downloader", "Querying: $fullQuery")

            val result =
                pyModule.callAttr("get_song_info", fullQuery, context.cacheDir.absolutePath)

            val resultMap = result.asMap()
            val error = resultMap[PyObject.fromJava("error")]?.toString()

            if (error != null) {
                Log.e("Downloader", "Error from Python: $error")
                return@withContext null
            }

            val audioPath = resultMap[PyObject.fromJava("path")]?.toString()
            val ext = resultMap[PyObject.fromJava("ext")]?.toString()
            Log.d("Downloader", "Downloaded audio path: $audioPath")
            Log.d("", "Downloaded audio Extension: $ext")
            return@withContext audioPath
        } catch (e: Exception) {
            Log.e("Downloader", "Exception while calling Python", e)
            null
        }
    }

    fun getCountryFromIP() {
        country = try {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("https://ipapi.co/json/")
                .build()

            val response = client.newCall(request).execute()
            val json = JSONObject(response.body?.string() ?: "")
            val countryCode = json.optString("country_code", "")
            if (countryCode.isNotBlank()) Locale("", countryCode).displayCountry else ""
        } catch (e: Exception) {
            ""
        }
    }

}