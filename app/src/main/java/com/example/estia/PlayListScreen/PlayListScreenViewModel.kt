package com.example.estia.PlayListScreen

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.estia.MusicFile

class PlayListScreenViewModel : ViewModel() {

    var playList = mutableStateOf(listOf<MusicFile>())

    fun enqueueMusicFile(musicFile: MusicFile) {
        val currentList = playList.value.toMutableList()
        currentList.add(musicFile)
        playList.value = currentList
    }

    fun addNowPlayingMusicFile(musicFile: MusicFile){
        val currentList = playList.value.toMutableList()
        currentList.add(0, musicFile)
        playList.value = currentList
    }

    fun replaceNowPlayingMusicFile(musicFile: MusicFile){
        val currentList = playList.value.toMutableList()
        currentList.removeAt(0)
        currentList.add(0, musicFile)
        playList.value = currentList
    }

    fun dequeueMusicFile() {
        val currentList = playList.value.toMutableList()
        if (currentList.isNotEmpty()) {
            currentList.removeAt(0)
            playList.value = currentList
        }
    }

}