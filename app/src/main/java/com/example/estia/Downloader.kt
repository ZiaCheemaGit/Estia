package com.example.estia.downloader

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.os.Environment
import android.content.Intent
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.FileObserver
import android.util.Log
import androidx.core.net.toUri
import com.example.estia.MusicFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import android.content.Context
import com.example.estia.AudioFetcher
import kotlinx.coroutines.Delay
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

interface Downloader{
    fun downloadFile(musicFile: MusicFile): Long
}

class AndroidDownloader(private val context: Context): Downloader{

    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as android.app.DownloadManager

    override fun downloadFile(musicFile: MusicFile): Long {
        val request = DownloadManager.Request(musicFile.streamableURL?.toUri())
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setTitle("${musicFile.name} - ${musicFile.artist.toString()}")
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_MUSIC,
                "Estia/${musicFile.id}" + ".estia"
            )
        return downloadManager.enqueue(request)
    }
}

object DownloaderObject {
    val downloadIdToFileMap = mutableMapOf<Long, MusicFile>()
    lateinit var downloader: Downloader

    fun initialize(context: Context) {
        downloader = AndroidDownloader(context)
    }

    fun downloadFile(musicFile: MusicFile) {
        val id = downloader.downloadFile(musicFile)
        downloadIdToFileMap[id] = musicFile
    }
}

