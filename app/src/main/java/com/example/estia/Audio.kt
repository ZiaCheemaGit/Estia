package com.example.estia

import android.util.Log
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.*
import org.schabi.newpipe.extractor.stream.AudioStream
import org.schabi.newpipe.extractor.stream.StreamInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.search.SearchInfo
import org.schabi.newpipe.extractor.stream.StreamInfoItem

class AudioFetcher {

    private var fetchAudioJob: Job? = null
    private var pyModule: PyObject? = null

    init {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val py = Python.getInstance()
                pyModule = py.getModule("ytdlp_wrapper")
                Log.d("PythonInit", "Python module loaded")
            } catch (e: Exception) {
                Log.e("PythonInit", "Failed to load Python module", e)
            }
        }
    }

    // yt-dlp
    suspend fun fetchAudioStreamUrl(
        artist: String,
        songName: String
    ): String? = withContext(Dispatchers.IO) {
        // Cancel and wait for previous job if it exists
        fetchAudioJob?.cancelAndJoin()

        var resultUrl: String? = null

        fetchAudioJob = launch {
            try {
                val module = pyModule
                if (module == null) {
                    Log.e("AudioFetcher", "Python module not ready yet")
                    return@launch
                }

                val result = module.callAttr("get_song_audio_url", songName, artist)
                val url = result.callAttr("get", "url")?.toString()
                val error = result.callAttr("get", "error")?.toString()

                if (!error.isNullOrBlank()) {
                    Log.e("AudioFetcher", "Error from Python: $error")
                    resultUrl = null
                } else {
                    Log.d("AudioFetcher", "Fetched audio URL: $url")
                    resultUrl = url
                }

            } catch (e: CancellationException) {
                Log.d("AudioFetcher", "Job cancelled")
            } catch (e: Exception) {
                Log.e("AudioFetcher", "Exception during fetch", e)
            }
        }

        fetchAudioJob?.join()
        return@withContext resultUrl
    }

    fun fetchYouTubePlayList(
        playlistUrl: String
    ): MutableList<MusicFile>? {
        val module = pyModule
        if (module == null) {
            Log.e("AudioFetcher", "Python module not ready yet")
            return null
        }
        val result: PyObject = module.callAttr("get_playlist_song_info", playlistUrl)
        val list = result.callAttr("get", "result").asList()


        val songList = mutableListOf<MusicFile>()

        for (item in list) {
            val map = item.asMap()

            val title = map[PyObject.fromJava("title")]?.toString() ?: "Unknown"
            val artistsRaw = map[PyObject.fromJava("artists")]?.toString() ?: ""
            val artists = artistsRaw.split(",").map { it.trim() }
            val url = map[PyObject.fromJava("url")]?.toString() ?: ""
            val duration = map[PyObject.fromJava("duration")]?.toLong() ?: 0L
            val thumbnail = map[PyObject.fromJava("thumbnail")]?.toString() ?: ""
            val album = map[PyObject.fromJava("album")]?.toString() ?: ""
            val id = map[PyObject.fromJava("id")]?.toLong() ?: 0L
            val playlistTitle = map[PyObject.fromJava("playlist_title")]?.toString() ?: ""

            val song = MusicFile(
                name = title,
                artist = artists.joinToString(", "),
                source = "$playlistTitle - YouTube Playlist",
                filePath = url,
                id = id,
                album = album,
                duration = duration,
                coverArtUri = thumbnail
            )
            songList.add(song)
        }

        return songList
    }


    // new pipe extractor
    suspend fun getYouTubeStreamUrl(
        artist: String,
        songName: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            val module = pyModule
            if (module == null) {
                Log.e("AudioFetcher", "Python module not ready yet")
                return@withContext null
            }

            val videoId = module.callAttr("get_official_youtube_video_id", songName, artist).toString()
            //val videoId = getOfficialYouTubeVideoId(songName, artist)

            Log.d("", "Found Video ID:${videoId}")
            val url = "https://www.youtube.com/watch?v=$videoId"
            val service = ServiceList.YouTube
            val streamInfo = StreamInfo.getInfo(service, url)

            val videoStreams = streamInfo.videoStreams
            val audioStreams = streamInfo.audioStreams

            // You can select based on bitrate or quality here
            val selectedStream: AudioStream = audioStreams.firstOrNull()
                ?: return@withContext null

            return@withContext selectedStream.url
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }

    // use new pipe extractor to get youtube video id
    suspend fun getOfficialYouTubeVideoId(songName: String, artistName: String): String? = withContext(Dispatchers.IO) {
        try {
            val query = "$songName $artistName"
            val service = ServiceList.YouTube
            val handlerFactory = service.searchQHFactory

            // have to fix to properly get songs official id
            val queryHandler = handlerFactory.fromQuery(query, emptyList(), "relevance")

            // Fetch search info using SearchInfo.getInfo
            val searchInfo = SearchInfo.getInfo(service, queryHandler)

            val artistCandidates = artistName.lowercase().split(",").map { it.trim() }

            for (item in searchInfo.relatedItems) {
                if (item is StreamInfoItem) {
                    val uploader = item.uploaderName?.lowercase() ?: continue
                    if (artistCandidates.any { it in uploader }) {
                        return@withContext item.url.substringAfter("v=")
                    }
                }
            }

            // fallback: return first StreamInfoItem videoId
            for (item in searchInfo.relatedItems) {
                if (item is StreamInfoItem) {
                    return@withContext item.url.substringAfter("v=")
                }
            }

            return@withContext null

        } catch (e: ExtractionException) {
            e.printStackTrace()
            return@withContext null
        }
    }

}

