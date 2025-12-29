package com.example.estia.HomeScreen.MainScreen

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.estia.DeezerArtist
import com.example.estia.DeezerChartResponse
import com.example.estia.DeezerPlaylist
import com.example.estia.DeezerService
import com.example.estia.DeezerTrack
import kotlinx.coroutines.launch

class HomeScreenViewModel: ViewModel(){
    val deezerCharts = mutableStateOf<DeezerChartResponse?>(null)
    val globalChartsSongs = mutableStateOf(listOf<DeezerTrack>())
    val globalChartsArtists = mutableStateOf(listOf<DeezerArtist>())
    val globalChartsPlaylists = mutableStateOf(listOf<DeezerPlaylist>())

    val isLoadingGlobalCharts = mutableStateOf(false)
    val isLoadingGlobalChartsSongs = mutableStateOf(false)
    val isLoadingGlobalChartsArtists = mutableStateOf(false)
    val isLoadingGlobalChartsPlayLists = mutableStateOf(false)

    init{
        viewModelScope.launch{
            getDeezerCharts()
            if(deezerCharts.value != null){
                getGlobalChartsSongs()
                getGlobalChartsArtists()
                getGlobalChartsPlayLists()
            }
        }
    }

    suspend fun getDeezerCharts(){
        isLoadingGlobalCharts.value = true
        deezerCharts.value = DeezerService.getGlobalCharts()
        isLoadingGlobalCharts.value = false
    }
    fun getGlobalChartsSongs(){
        isLoadingGlobalChartsSongs.value = true
        globalChartsSongs.value = deezerCharts.value?.tracks?.data!!
        isLoadingGlobalChartsSongs.value = false
    }
    fun getGlobalChartsArtists(){
        isLoadingGlobalChartsArtists.value = true
        globalChartsArtists.value = deezerCharts.value?.artists?.data!!
        isLoadingGlobalChartsArtists.value = false
    }
    fun getGlobalChartsPlayLists(){
        isLoadingGlobalChartsPlayLists.value = true
        globalChartsPlaylists.value = deezerCharts.value?.playlists?.data!!
        isLoadingGlobalChartsPlayLists.value = false
    }
}


