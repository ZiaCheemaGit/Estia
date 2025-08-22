package com.example.estia.SearchScreen.DeepSearch

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.estia.AudioFetcher
import com.example.estia.SearchScreen.CoverArtService
import com.example.estia.SearchScreen.DeezerTrack
import com.example.estia.SearchScreen.MusicBrainzService
import com.example.estia.SearchScreen.MusicBrainzTrack
import com.example.estia.SearchScreen.MusicBrainzTrackDetails
import com.example.estia.YTMusicSong
import kotlinx.coroutines.launch

class DeepSearchScreenViewModel: ViewModel(){

    val songName = mutableStateOf("")
    val artistName = mutableStateOf("")

    val isLoadingSongSearchResults = mutableStateOf(false)

    val songSearchResults = mutableStateOf<List<YTMusicSong>>(emptyList())

    fun search() {
        isLoadingSongSearchResults.value = true
        viewModelScope.launch {
            songSearchResults.value = emptyList()
            try {
                songSearchResults.value = AudioFetcher().searchYTMusic(songName.value + artistName.value)

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoadingSongSearchResults.value = false
            }
        }
    }

    fun durationToMillis(duration: String?): Long {
        if (duration.isNullOrBlank()) return 0L

        val parts = duration.split(":").mapNotNull { it.toIntOrNull() }
        return when (parts.size) {
            2 -> { // mm:ss
                val minutes = parts[0]
                val seconds = parts[1]
                (minutes * 60 + seconds) * 1000L
            }
            3 -> { // hh:mm:ss (sometimes YouTube gives this format)
                val hours = parts[0]
                val minutes = parts[1]
                val seconds = parts[2]
                (hours * 3600 + minutes * 60 + seconds) * 1000L
            }
            else -> 0L
        }
    }

}