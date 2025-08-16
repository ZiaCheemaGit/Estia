package com.example.estia.PlayListScreen

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.estia.AudioFetcher
import com.example.estia.MusicFile
import kotlinx.coroutines.launch

class PlayListScreenViewModel : ViewModel() {
    private val audioFetcher = AudioFetcher()

    private val playQueue = mutableStateOf(listOf<MusicFile>())
    private val localStorageQueue = mutableStateOf(listOf<MusicFile>())

    val visibleLocalStorageQueue = mutableStateOf(listOf<MusicFile>())
    val visiblePlayQueue = mutableStateOf(listOf<MusicFile>())

    private var playQueueIndex = 0
    private var localStorageQueueIndex = 0

    private var nowPlaying : MusicFile? = null

    fun setNowPlaying(musicFile: MusicFile?){
        if(musicFile != null){
            nowPlaying = musicFile
            localStorageQueueIndex = localStorageQueue.value.indexOf(musicFile) + 1
            updateVisibleLocalStorageQueue()
        }
    }

    fun setLocalStorageQueue(musicFiles: List<MusicFile>) {
        localStorageQueue.value = musicFiles
    }

    fun enqueueInPlayQueue(musicFile: MusicFile) {
        // From search but url has to load
        if(musicFile.source == "...."){
            viewModelScope.launch{
                val url = audioFetcher.fetchAudioStreamUrl_newpipe(
                    musicFile.artist.orEmpty(),
                    musicFile.name
                )
                musicFile.filePath = url
                musicFile.source = "Search"
            }
        }
        val currentList = playQueue.value.toMutableList()
        currentList.add(musicFile)
        playQueue.value = currentList
        visiblePlayQueue.value = currentList.subList(playQueueIndex, currentList.size)
    }

    fun getNextSongFromMainQueue(): MusicFile? {
        if (playQueue.value.isNotEmpty() && playQueueIndex < playQueue.value.size) {
            val nextSong = playQueue.value[playQueueIndex]
            playQueueIndex++
            updateVisiblePlayQueue()
            return nextSong
        }
        else if(nowPlaying?.source == "Local Storage" &&
            localStorageQueue.value.isNotEmpty() &&
            localStorageQueueIndex < localStorageQueue.value.size){
            val nextSong = localStorageQueue.value[localStorageQueueIndex]
            localStorageQueueIndex++
            updateVisibleLocalStorageQueue()
            return nextSong
        }
        else {
            return null
        }
    }

    // clear
    fun clearPlayQueue() {
        playQueue.value = emptyList()
        visiblePlayQueue.value = emptyList()
    }

    fun clearLocalStorageQueue() {
        localStorageQueue.value = emptyList()
        visibleLocalStorageQueue.value = emptyList()
    }

    // update
    fun updateVisibleLocalStorageQueue() {
        val newSubList = localStorageQueue.value.subList(localStorageQueueIndex, localStorageQueue.value.size)
        visibleLocalStorageQueue.value = newSubList
    }

    fun updateVisiblePlayQueue(){
        val newSubList = playQueue.value.subList(playQueueIndex, playQueue.value.size)
        visiblePlayQueue.value = newSubList
    }

    // revert
    fun revertToPreviousSong(): MusicFile? {
        if (playQueueIndex > 0) {
            playQueueIndex--
            updateVisiblePlayQueue()
            return playQueue.value[playQueueIndex]
        }
        else if(localStorageQueueIndex > 0){
            localStorageQueueIndex--
            updateVisibleLocalStorageQueue()
            return localStorageQueue.value[localStorageQueueIndex]
        }
        else {
            return null
        }
    }
}

