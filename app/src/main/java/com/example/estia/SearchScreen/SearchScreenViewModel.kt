package com.example.estia.SearchScreen

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.cancellation.CancellationException
import okhttp3.Request
import org.json.JSONObject
import java.util.Locale

class SearchScreenViewModel : ViewModel() {

    private var country : String = ""

    val filterOptionsList = listOf("Songs", "Albums", "Artists")
    val selectedFilter = mutableStateOf(filterOptionsList[0])
    val searchQuery = mutableStateOf("")

    private var searchJob: Job? = null

    val songSearchResults = mutableStateOf<List<DeezerTrack>>(emptyList())
    val albumSearchResults = mutableStateOf<List<DeezerAlbum>>(emptyList())
    val artistSearchResults = mutableStateOf<List<DeezerArtist>>(emptyList())

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

    suspend fun getOtherArtists(id: Long): String = withContext(Dispatchers.IO) {
        val artists = DeezerService.api.getTrackDetails(id).contributors
        artists.joinToString(separator = ", ") { it.name }
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

            val result = pyModule.callAttr("get_song_info", fullQuery, context.cacheDir.absolutePath)

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
            val json = JSONObject(response.body?.string() ?:  "")
            val countryCode = json.optString("country_code", "")
            if (countryCode.isNotBlank()) Locale("", countryCode).displayCountry else ""
        } catch (e: Exception) {
            ""
        }
    }

}
