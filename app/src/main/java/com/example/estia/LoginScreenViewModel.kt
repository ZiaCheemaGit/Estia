package com.example.estia

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class LoginScreenViewModel : ViewModel(){
    // Login option selected by user
    var clickedLoginOption by mutableStateOf("")
        private set

    // Login options and their mapping with their icons in res/drawable/
    val socialIcons = mapOf(
        "Google" to R.drawable.google_logo,
        "Spotify" to R.drawable.spotify_logo
    )
}