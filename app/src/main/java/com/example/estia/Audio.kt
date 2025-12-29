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
import org.json.JSONObject
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.playlist.PlaylistInfo
import org.schabi.newpipe.extractor.search.SearchInfo
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import org.schabi.newpipe.extractor.stream.StreamType
import java.net.URL
import java.net.URLEncoder
import java.util.Locale
import kotlinx.coroutines.withContext
import java.text.Normalizer
import kotlin.math.abs
import kotlin.math.max

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

    suspend fun searchYTMusic(query: String): List<YTMusicSong> = withContext(Dispatchers.IO) {
        try {
            val module = pyModule ?: return@withContext emptyList()

            val result: PyObject = module.callAttr("yt_music_search", query)

            // Convert Python list → Kotlin list of YTMusicSong
            result.asList().map { item ->
                val map = item.asMap().mapKeys { it.key.toString() }

                val title = map["title"]?.toString().orEmpty()
                val videoId = map["videoId"]?.toString().orEmpty()
                val duration = map["duration"]?.toString()

                val album = when (val albumObj = map["album"]) {
                    is PyObject -> {
                        val albumMap = albumObj.asMap().mapKeys { it.key.toString() }
                        albumMap["name"]?.toString()
                    }
                    is Map<*, *> -> albumObj["name"]?.toString()
                    else -> null
                }

                val artists = (map["artists"] as? PyObject)
                    ?.asList()
                    ?.mapNotNull { artistObj ->
                        val artistMap = (artistObj as PyObject).asMap().mapKeys { it.key.toString() }
                        artistMap["name"]?.toString()
                    }
                    ?: emptyList()

                val thumbUrl = (map["thumbnails"] as? PyObject)
                    ?.asList()
                    ?.mapNotNull { thumbObj ->
                        val thumbMap = (thumbObj as PyObject).asMap().mapKeys { it.key.toString() }
                        val url = thumbMap["url"]?.toString()
                        val width = thumbMap["width"]?.toString()?.toIntOrNull() ?: 0
                        val height = thumbMap["height"]?.toString()?.toIntOrNull() ?: 0
                        if (url != null) Triple(url, width, height) else null
                    }
                    ?.maxByOrNull { it.second * it.third } // pick by largest area
                    ?.first

//                val thumbUrl = fetchHighResCoverArtFromITunes(
//                    title = title,
//                    artist = artists.joinToString(","),
//                    album = album,
//                    durationMs = durationStringToMillis(duration.toString()),
//                    minScore = 0.8
//                )

                YTMusicSong(
                    title = title,
                    videoId = videoId,
                    artists = artists as List<String>,
                    album = album,
                    duration = duration,
                    thumbnailUrl = thumbUrl
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
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

    suspend fun fetchAudioStreamUrl_newpipe_by_VideoID(
        videoId: String
    ): String? = withContext(Dispatchers.IO) {
        try {
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

    suspend fun fetchHighResCoverArtFromITunes(
        title: String,
        artist: String,
        album: String? = null,
        durationMs: Long? = null,         // optional, in milliseconds
        minScore: Double = 0.80           // raise to be more strict
    ): String? {
        return withContext(Dispatchers.IO) {
            try {
                val query = URLEncoder.encode(listOf(title, artist, album ?: "").joinToString(" "), "UTF-8")
                val url = "https://itunes.apple.com/search?term=$query&entity=song&limit=50"
                val json = URL(url).readText()
                val root = JSONObject(json)
                val results = root.optJSONArray("results") ?: return@withContext null

                val normTitle = normalize(title)
                val normArtist = normalize(artist)
                val normAlbum = album?.let { normalize(it) }

                var bestScore = 0.0
                var bestArtworkUrl: String? = null

                for (i in 0 until results.length()) {
                    val track = results.getJSONObject(i)
                    val trackName = track.optString("trackName", "")
                    val artistName = track.optString("artistName", "")
                    val collectionName = track.optString("collectionName", "")
                    val trackTime = track.optLong("trackTimeMillis", -1L)
                    val artwork = track.optString("artworkUrl100", null) ?: continue

                    val titleScore = computeTitleSimilarity(normTitle, normalize(trackName))
                    val artistScore = computeStringSimilarity(normArtist, normalize(artistName))
                    val albumScore = if (normAlbum != null) computeStringSimilarity(normAlbum, normalize(collectionName)) else 0.0
                    val durationScore = if (durationMs != null && trackTime > 0) {
                        val diff = abs(durationMs - trackTime)
                        when {
                            diff <= 1000 -> 1.0
                            diff <= 3000 -> 0.85
                            diff <= 7000 -> 0.6
                            else -> 0.0
                        }
                    } else 0.0

                    // Weighted scoring (tweak weights if you like)
                    val score = titleScore * 0.55 + artistScore * 0.35 + albumScore * 0.08 + durationScore * 0.02

                    if (score > bestScore) {
                        bestScore = score
                        bestArtworkUrl = artwork
                    }
                }

                if (bestArtworkUrl != null && bestScore >= minScore) {
                    // convert to high resolution (safe regex replace)
                    return@withContext bestArtworkUrl.replace(Regex("\\d+x\\d+bb\\.jpg$"), "1000x1000bb.jpg")
                }

                null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

}

data class YTMusicSong(
    val title: String,
    val videoId: String,
    val artists: List<String>,
    val album: String?,
    val duration: String?,
    val thumbnailUrl: String?
)

// Helper Function for itunes function

fun durationStringToMillis(duration: String): Long {
    val parts = duration.split(":").map { it.trim() }

    return when (parts.size) {
        1 -> { // only seconds, e.g. "45"
            val seconds = parts[0].toLongOrNull() ?: 0L
            seconds * 1000
        }
        2 -> { // mm:ss
            val minutes = parts[0].toLongOrNull() ?: 0L
            val seconds = parts[1].toLongOrNull() ?: 0L
            (minutes * 60 + seconds) * 1000
        }
        3 -> { // hh:mm:ss
            val hours = parts[0].toLongOrNull() ?: 0L
            val minutes = parts[1].toLongOrNull() ?: 0L
            val seconds = parts[2].toLongOrNull() ?: 0L
            (hours * 3600 + minutes * 60 + seconds) * 1000
        }
        else -> 0L // invalid format
    }
}

private fun normalize(s: String): String {
    // lowercase, remove diacritics, non-alphanumerics -> space, collapse spaces
    val noDiacritics = Normalizer.normalize(s.lowercase(Locale.ROOT), Normalizer.Form.NFD)
        .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
    return noDiacritics.replace("[^a-z0-9\\s]".toRegex(), " ").replace("\\s+".toRegex(), " ").trim()
}

private fun tokensSet(s: String): Set<String> =
    if (s.isBlank()) emptySet() else s.split(" ").filter { it.isNotBlank() }.toSet()

private fun jaccard(a: Set<String>, b: Set<String>): Double {
    if (a.isEmpty() && b.isEmpty()) return 1.0
    if (a.isEmpty() || b.isEmpty()) return 0.0
    val inter = a.intersect(b).size.toDouble()
    val union = a.union(b).size.toDouble()
    return inter / union
}

private fun levenshteinDistance(a: String, b: String): Int {
    if (a == b) return 0
    if (a.isEmpty()) return b.length
    if (b.isEmpty()) return a.length

    val prev = IntArray(b.length + 1) { it }
    val cur = IntArray(b.length + 1)
    for (i in 1..a.length) {
        cur[0] = i
        for (j in 1..b.length) {
            val cost = if (a[i - 1] == b[j - 1]) 0 else 1
            cur[j] = minOf(
                prev[j] + 1,          // deletion
                cur[j - 1] + 1,       // insertion
                prev[j - 1] + cost    // substitution
            )
        }
        System.arraycopy(cur, 0, prev, 0, prev.size)
    }
    return prev[b.length]
}

private fun levenshteinSimilarity(a: String, b: String): Double {
    val maxLen = max(a.length, b.length)
    if (maxLen == 0) return 1.0
    val dist = levenshteinDistance(a, b)
    return 1.0 - (dist.toDouble() / maxLen.toDouble()).coerceIn(0.0, 1.0)
}

private fun computeStringSimilarity(a: String, b: String): Double {
    if (a.isBlank() && b.isBlank()) return 1.0
    if (a == b) return 1.0
    val tokensA = tokensSet(a)
    val tokensB = tokensSet(b)
    val j = jaccard(tokensA, tokensB)
    val lev = levenshteinSimilarity(a, b)
    return max(j, lev) // prefer the stronger signal
}

private fun computeTitleSimilarity(a: String, b: String): Double {
    // combine token overlap and levenshtein for robustness
    val j = jaccard(tokensSet(a), tokensSet(b))
    val lev = levenshteinSimilarity(a, b)
    // heavier weight on levenshtein for exact reading order, but keep both
    return (0.6 * lev + 0.4 * j).coerceIn(0.0, 1.0)
}