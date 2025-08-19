package com.example.estia.AccountScreen.MainScreen

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.estia.LikedSongFile
import com.example.estia.MusicFile
import com.example.estia.EstiaDownloadFile
import com.example.estia.EstiaDownloadsDao
import com.example.estia.MusicDataBase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import okhttp3.Dispatcher
import androidx.compose.runtime.State
import org.schabi.newpipe.extractor.timeago.patterns.no

class AccountScreenViewModel() : ViewModel() {

    val likedSongs = MutableStateFlow(listOf<LikedSongFile>())
    val estiaDownloads = MutableStateFlow(listOf<EstiaDownloadFile>())

    val isLikedSong = mutableStateOf(false)

    private var nowPlaying: MusicFile? = null

    fun setNowPlaying(musicFile: MusicFile){
        this.nowPlaying = musicFile
        isLikedSong.value = likedSongs.value.contains(mfToLSF())
    }

    private lateinit var db: MusicDataBase
    fun initializeContextAndDB(c: Context){
        db = MusicDataBase.Companion.getInstance(c)
    }

    fun addSongToLikedSongs(){
        viewModelScope.launch{
            val file = mfToLSF()
            likedSongSaveToDB(file)
            likedSongs.value += file
            isLikedSong.value = true
        }
    }

    fun removeSongFromLikedSongs(){
        viewModelScope.launch {
            val file = mfToLSF()
            db.likedSongsDao().deleteMusicFile(file)
            likedSongs.value.drop(likedSongs.value.indexOf(file))
            isLikedSong.value = false
        }
    }

    fun mfToLSF(): LikedSongFile{
        return LikedSongFile(
            name = nowPlaying?.name ?: "",
            id = nowPlaying?.id ?: "",
            artist = nowPlaying?.artist ?: "Unknown Artist",
            album = nowPlaying?.album ?: "Unknown Album",
            duration = nowPlaying?.duration ?: 0L,
            coverArtUri = nowPlaying?.coverArtUri,
        )
    }

    suspend private fun likedSongSaveToDB(file: LikedSongFile){
        db.likedSongsDao().upsertMusicFile(file)
    }

    suspend private fun EstiaDownloadSaveToDB(file: EstiaDownloadFile){
        db.estiaDownloadsDao().upsertMusicFile(file)
    }

    private fun likedSongsLoadFromDB(){
        likedSongs.value = db.likedSongsDao().getAllMusic()
    }

    private fun EstiaDownloadsLoadFromDB(){
        estiaDownloads.value = db.estiaDownloadsDao().getAllMusic()
    }
}