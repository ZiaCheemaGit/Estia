package com.example.estia.LoginScreen

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.estia.R
import androidx.core.content.edit
import androidx.lifecycle.viewModelScope
import com.example.estia.LastFmService
import kotlinx.coroutines.launch

class LoginScreenViewModel : ViewModel(){

    lateinit var context: Context

    // Login option selected by user
    var clickedLoginOption by mutableStateOf("")
        private set

    // Login options and their mapping with their icons in res/drawable/
    val socialIcons = mapOf(
        "Google" to R.drawable.google_logo,
        "Spotify" to R.drawable.spotify_logo
    )

    fun saveSessionKey(context: Context, sessionKey: String) {
        context.getSharedPreferences("lastfm", Context.MODE_PRIVATE)
            .edit() {
                putString("session_key", sessionKey)
            }
    }
    fun loadSessionKey(context: Context): String? {
        return context.getSharedPreferences("lastfm", Context.MODE_PRIVATE)
            .getString("session_key", null)
    }

    fun loginLastFM(username: String, password: String){
        viewModelScope.launch {
            val sessionKey = LastFmService.loginAndGetSessionKey(context, username, password)

            if (sessionKey != null) {
                Log.d("LastFM", "Login successful: $sessionKey")
            } else {
                Log.e("LastFM", "Login failed")
            }
        }

    }
}