package com.example.estia.AccountScreen.MainScreen

import android.content.Context
import android.media.MediaMetadataRetriever
import android.os.Environment
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.estia.LikedSongFile
import com.example.estia.MusicFile
import com.example.estia.EstiaDownloadFile
import com.example.estia.MusicDataBase
import com.example.estia.downloader.DownloaderObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import java.io.File
import androidx.compose.runtime.mutableStateMapOf
import kotlinx.coroutines.withContext

class AccountScreenViewModel() : ViewModel() {

    val likedSongs = MutableStateFlow(listOf<LikedSongFile>())
    val estiaDownloads = MutableStateFlow(listOf<EstiaDownloadFile>())
    lateinit var context: Context
    val isLikedSong = mutableStateOf(false)
    val isDownloadedSong = mutableStateOf(false)
    private var nowPlaying: MusicFile? = null

    val downloadIdToMusicFileIDMap = mutableStateMapOf<Long, String>()
    val musicFileIDMapToDownloadProgress = mutableStateMapOf<String, Int>()
    fun setNowPlaying(musicFile: MusicFile){
        this.nowPlaying = musicFile
        isLikedSong.value = likedSongs.value.any{ it.id == mfToLSF().id }
        isDownloadedSong.value = estiaDownloads.value.any { it.id == mfToEDF().id }
    }

    private lateinit var db: MusicDataBase
    fun initializeContextAndDB(c: Context){
        this.context = c
        db = MusicDataBase.Companion.getInstance(c)
        likedSongsLoadFromDB()
        estiaDownloadsLoadFromDB()
    }

    fun addSongToLikedSongs(){
        viewModelScope.launch{
            val file = mfToLSF()
            likedSongs.value += file
            isLikedSong.value = true
            likedSongSaveToDB(file)
        }
    }

    fun removeSongFromLikedSongs(){
        viewModelScope.launch {
            val file = mfToLSF()
            db.likedSongsDao().deleteMusicFile(file)
            likedSongs.value = likedSongs.value.drop(likedSongs.value.indexOf(file))
            isLikedSong.value = false
        }
    }

    fun isSongDownloadCompleted(musicFileID: String): Boolean{
        val key = downloadIdToMusicFileIDMap.entries
            .find { it.value == musicFileID }
            ?.key
        if (key != null){
            return DownloaderObject.isDownloadCompleted(context, key)
        }
        else{
            return true
        }
    }

    fun addSongToEstiaDownloads(){
        viewModelScope.launch{
            if(nowPlaying != null){

                val downloadID = DownloaderObject.downloadFile(context, nowPlaying!!)

                isDownloadedSong.value = true

                val path = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                    "Estia/${nowPlaying!!.id}.estia"
                ).absolutePath

                val file = mfToEDF().copy(
                    duration = getMetadataDuration(nowPlaying?.filePath.toString()).toLong(),
                    filePath = path
                )
                estiaDownloads.value += file

                downloadIdToMusicFileIDMap.put(downloadID, nowPlaying!!.id)
                musicFileIDMapToDownloadProgress.put(nowPlaying!!.id, 0)
            }
        }
    }

    fun removeSongFromEstiaDownloads(){
        viewModelScope.launch {
            val file = mfToEDF()
            db.estiaDownloadsDao().deleteMusicFile(file)
            estiaDownloads.value = estiaDownloads.value.drop(estiaDownloads.value.indexOf(file))
            isDownloadedSong.value = false
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

    fun mfToEDF(): EstiaDownloadFile{
        val filePath = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
            "Estia/${nowPlaying?.id}.estia"
        ).absolutePath
        return EstiaDownloadFile(
            name = nowPlaying?.name ?: "",
            id = nowPlaying?.id ?: "",
            artist = nowPlaying?.artist ?: "Unknown Artist",
            album = nowPlaying?.album ?: "Unknown Album",
            duration = nowPlaying?.duration ?: 0L,
            coverArtUri = nowPlaying?.coverArtUri,
            filePath = filePath,
        )
    }

    fun EDFToMF(f: EstiaDownloadFile): MusicFile{
        return MusicFile(
            name = f.name,
            id = f.id,
            artist = f.artist,
            album = f.album,
            duration = f.duration,
            filePath = f.filePath,
            coverArtUri = f.coverArtUri,
            source = f.source,
        )
    }

    suspend private fun likedSongSaveToDB(file: LikedSongFile){
        db.likedSongsDao().upsertMusicFile(file)
    }

    suspend private fun estiaDownloadSaveToDB(file: EstiaDownloadFile){
        db.estiaDownloadsDao().upsertMusicFile(file)
    }

    private fun likedSongsLoadFromDB(){
        viewModelScope.launch(Dispatchers.IO) {
            likedSongs.value = db.likedSongsDao().getAllMusic()
        }
    }

    private fun estiaDownloadsLoadFromDB(){
        viewModelScope.launch(Dispatchers.IO){
            estiaDownloads.value = db.estiaDownloadsDao().getAllMusic()
        }
    }

    suspend fun observe(){
        val done = true
        while(done){
            if(DownloaderObject.downloaderIsActive.value){
                downloadIdToMusicFileIDMap.forEach { it ->
                    if(isSongDownloadCompleted(it.value)){
                        downloadIdToMusicFileIDMap.remove(it.key)
                        musicFileIDMapToDownloadProgress.remove(it.value)

                        val songID = it.value
                        val fileToDB = estiaDownloads.value.find{
                            it.id == songID
                        }
                        if(fileToDB != null){
                            estiaDownloadSaveToDB(fileToDB)
                        }
                    }
                    else{
                        musicFileIDMapToDownloadProgress[it.value] =
                            DownloaderObject.getDownloadProgress(context, it.key)
                    }
                }
            }
            delay(2000)

        }
    }

    suspend fun getMetadataDuration(url: String): Long = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(url, HashMap())
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            durationStr?.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            Log.e("Metadata", "Error retrieving metadata", e)
            0L
        } finally {
            retriever.release()
        }
    }

    private fun fileExists(path: String): Boolean {
        return try {
            val file = File(path)
            file.exists() && file.isFile && file.length() > 0
        } catch (e: Exception) {
            false
        }
    }

}
