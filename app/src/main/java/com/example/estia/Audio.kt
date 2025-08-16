package com.example.estia

import android.util.Log
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException
import org.schabi.newpipe.extractor.stream.AudioStream
import org.schabi.newpipe.extractor.stream.StreamInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.search.SearchInfo
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import java.util.Locale

class AudioFetcher {
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

    // new pipe extractor
    suspend fun fetchAudioStreamUrl_newpipe(
        artist: String,
        songName: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            val module = pyModule
            if (module == null) {
                Log.e("AudioFetcher", "Python module not ready yet")
                return@withContext null
            }

            val cleanedSongName = songName.trim()

            val videoId = module.callAttr("get_official_youtube_video_id", cleanedSongName, artist)


            Log.d("", "Found Video ID:${videoId}")
            val url = "https://www.youtube.com/watch?v=$videoId"
            val service = ServiceList.YouTube
            val streamInfo = StreamInfo.getInfo(service, url)

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

}

