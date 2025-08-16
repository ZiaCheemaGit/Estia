package com.example.estia.AccountScreen.MainScreen

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.estia.MusicFile

class AccountScreenViewModel() : ViewModel() {

    val likedSongsPlaylist = mutableStateOf(listOf<MusicFile>())
}