package com.example.estia.SearchScreen.DeepSearch

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.estia.SearchScreen.CoverArtService
import com.example.estia.SearchScreen.DeezerTrack
import com.example.estia.SearchScreen.MusicBrainzService
import com.example.estia.SearchScreen.MusicBrainzTrack
import com.example.estia.SearchScreen.MusicBrainzTrackDetails
import kotlinx.coroutines.launch

class DeepSearchScreenViewModel: ViewModel(){

    val songName = mutableStateOf("")
    val artistName = mutableStateOf("")

    val isLoadingSongSearchResults = mutableStateOf(false)

    val songSearchResults = mutableStateOf<List<MusicBrainzTrackDetails>>(emptyList())

    fun search() {
        isLoadingSongSearchResults.value = true
        viewModelScope.launch {
            songSearchResults.value = emptyList()
            try {
                val response = MusicBrainzService.searchExactTrack(
                    title = songName.value,
                    artist = artistName.value
                )
                val tempList = response.recordings

                tempList.forEach { song->

                    val releaseId = song.releases?.firstOrNull()?.id
                    if (releaseId != null) {
                        val coverArt = CoverArtService.api.getCoverArt(releaseId)
                        val coverUrl = coverArt.images
                            .firstOrNull { it.front }?.image
                            ?: coverArt.images.firstOrNull()?.image
                        songSearchResults.value += MusicBrainzTrackDetails(
                            track = song,
                            coverArt = coverUrl.orEmpty()
                        )
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoadingSongSearchResults.value = false
            }
        }
    }
}