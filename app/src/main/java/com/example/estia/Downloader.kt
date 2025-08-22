package com.example.estia.downloader


import android.app.DownloadManager
import android.os.Environment
import androidx.core.net.toUri
import com.example.estia.MusicFile
import android.content.Context
import androidx.compose.runtime.mutableStateOf

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
    val downloaderIsActive = mutableStateOf(false)

    fun initialize(context: Context) {
        downloader = AndroidDownloader(context)
    }

    fun downloadFile(context: Context, musicFile: MusicFile) : Long{
        val id = downloader.downloadFile(musicFile)
        downloadIdToFileMap[id] = musicFile
        updateDownloaderIsActive(context)
        return id
    }

    fun isDownloadCompleted(context: Context, downloadId: Long): Boolean {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor = downloadManager.query(query)

        cursor?.use {
            if (it.moveToFirst()) {
                val status = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                return status == DownloadManager.STATUS_SUCCESSFUL
            }
        }
        return false
    }

    fun getDownloadProgress(context: Context, downloadId: Long): Int {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor = downloadManager.query(query)

        cursor?.use {
            if (it.moveToFirst()) {
                val bytesDownloaded = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                val totalBytes = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))

                if (totalBytes > 0) {
                    return ((bytesDownloaded * 100L) / totalBytes).toInt()
                }
            }
        }
        return -1 // -1 means unknown/not found
    }

    fun updateDownloaderIsActive(context: Context) {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query()
        val cursor = downloadManager.query(query)

        var active = false
        cursor?.use {
            val idIndex = it.getColumnIndexOrThrow(DownloadManager.COLUMN_ID)
            val statusIndex = it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)

            while (it.moveToNext()) {
                val id = it.getLong(idIndex)
                val status = it.getInt(statusIndex)

                if ((status == DownloadManager.STATUS_RUNNING || status == DownloadManager.STATUS_PENDING)
                    && downloadIdToFileMap.containsKey(id)
                ) {
                    active = true
                    break
                }
            }
        }
        downloaderIsActive.value = active
    }
}