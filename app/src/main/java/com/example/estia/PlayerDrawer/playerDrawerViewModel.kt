package com.example.estia.PlayerDrawer

import android.R
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.estia.LyricsEntry
import com.example.estia.MusicDataBase
import com.example.estia.MusicFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.jsoup.Jsoup
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

class PlayerDrawerViewModel : ViewModel() {

    val libraryManagerDialogShown = mutableStateOf(false)

    //
    // Lyrics Logic
    //
    var lyrics by mutableStateOf("")
    var isLyricsLoading by mutableStateOf(false)
    var lyricsError by mutableStateOf<String?>(null)

    var loadedLyricsSongName by mutableStateOf("")
    var loadedLyricsSongArtistName by mutableStateOf("")

    private lateinit var db: MusicDataBase
    lateinit var context : Context

    fun setContentResolverAndInitDB(context: Context) {
        this.context = context
        db = MusicDataBase.Companion.getInstance(context)
        Log.d("DB_INIT", "Database initialized: $db")
    }

    suspend fun getLyricsFromDb(title: String, artist: String) : String{
        val db = MusicDataBase.getInstance(context)
        val lyricsDao = db.lyricsDao()

        return lyricsDao.getLyrics(title, artist)?.lyrics.toString()
    }
    fun saveLyricsToDb(title: String, artist: String, lyrics: String) {
        val lyricsDao = db.lyricsDao()

        viewModelScope.launch(Dispatchers.IO) {
            val entry = LyricsEntry(
                songName = title,
                artistName = artist,
                lyrics = lyrics
            )
            lyricsDao.insertLyrics(entry)
        }
    }

    fun fetchLyrics(artist: String, title: String) {
        isLyricsLoading = true
        viewModelScope.launch(Dispatchers.IO) {
            isLyricsLoading = true
            val cleanedTitle = title
                .lowercase()
                .substringBefore("-")                          // Remove text after "-"
                .replace(Regex("\\(.*?\\)|\\[.*?\\]"), "")     // Remove anything in the () or []
                .replace("radio edit", "", ignoreCase = true)  // Remove "radio edit"
                .replace(".", "")                              // Remove dots
                .replace(Regex("[^a-z0-9 ]"), "")              // Remove special characters
                .replace(Regex("\\s+"), "-")                   // Convert spaces to dashes
                .trim()


            lyricsError = null

            // Check DB first
            lyrics = getLyricsFromDb(title, artist).toString()

            if (lyrics != "null" && lyrics != "") {
                if (loadedLyricsSongName == title && loadedLyricsSongArtistName == artist) {
                    isLyricsLoading = false
                    return@launch
                } else {
                    loadedLyricsSongName = title
                    loadedLyricsSongArtistName = artist
                    isLyricsLoading = false
                    return@launch
                }
            }

            // Call the unified fetcher
            val result = fetchLyricsFromAllSources(artist, cleanedTitle)

            if (result != "null" && result != null && result != "") {
                lyrics = result
                loadedLyricsSongName = title
                loadedLyricsSongArtistName = artist
                saveLyricsToDb(title, artist, result)
            } else {
                lyricsError = "No lyrics found from any source"
                var r = "No lyrics found from any source on Server"
                saveLyricsToDb(title, artist, r)
            }

            isLyricsLoading = false
        }
    }
    fun refreshLyrics(artist: String, title: String) {
        isLyricsLoading = true
        viewModelScope.launch(Dispatchers.IO) {
            // Clear existing cache from memory
            lyrics = ""
            lyricsError = null

            // Optionally clear from database too
            db.lyricsDao().deleteLyrics(title, artist)

            val result = fetchLyricsFromAllSources(artist, title)

            if (!result.isNullOrEmpty() && result != "null") {
                lyrics = result
                loadedLyricsSongName = title
                loadedLyricsSongArtistName = artist
                saveLyricsToDb(title, artist, result)
            } else {
                lyricsError = "No fresh lyrics found"
                val fallback = "No lyrics found on Server"
                saveLyricsToDb(title, artist, fallback)
            }

            isLyricsLoading = false
        }
    }

    suspend fun fetchLyricsFromAllSources(artist: String, title: String): String? {
        val cleanedArtist = artist.trim()
        val cleanedTitle = title.trim()

        // First try lyrics.ovh
//        try {
//            val ovhLyrics = fetchLyricsFromOvh(cleanedArtist, cleanedTitle)
//            if (ovhLyrics != null) return ovhLyrics.toString()
//        } catch (_: Exception) {
//
//        }
//
        // Then try Genius
//        try {
//            val geniusLyrics = getLyricsFromGenius(artist, title)
//            if (geniusLyrics != null) return geniusLyrics.toString()
//        } catch (_: Exception) {
//
//        }

        return null
    }
    suspend fun getLyricsFromGenius(
        artistString: String,
        title: String,
        apiKey:String = "dSyCsv6bUVP9cpcKxhrAncBFxqoK6CmrxyrNfqeTuuT5lbZwGoHKlk7wJbYX2Zxg"
    ): String {
        return try {
            if(artistString == "Unknown Artist") { return "No Lyrics Found"}

            val inputArtists = artistString.split(",", "&").map { it.trim().lowercase(Locale.getDefault()) }

            val query = title
            val searchUrl = "https://api.genius.com/search?q=${query.replace(" ", "%20")}"
            val searchConnection = withContext(Dispatchers.IO) {
                val url = URL(searchUrl)
                val conn = url.openConnection() as HttpURLConnection
                conn.setRequestProperty("Authorization", "Bearer $apiKey")
                conn.connectTimeout = 10_000
                conn.readTimeout = 10_000
                conn
            }

            for(artist in inputArtists) {

                val responseCode = searchConnection.responseCode

                if (responseCode != 200) {
                    return "[ERROR] Genius API error: ${
                        searchConnection.inputStream.bufferedReader().readText()
                    }"
                }

                val json = searchConnection.inputStream.bufferedReader().readText()

                val root = JSONObject(json)
                val hits = root.getJSONObject("response").getJSONArray("hits")
                if (hits.length() == 0) {
                    println("[WARN] No song hits found on Genius.")
                    return "❌ Could not find song on Genius."
                }

                var matchedSongUrl: String? = null
                var r = ""

                for (i in 0 until hits.length()) {
                    val result = hits.getJSONObject(i).getJSONObject("result")
                    val geniusArtists = result.getJSONObject("primary_artist").getString("name")
                        .split(",", "&").map { it.trim().lowercase(Locale.getDefault()) }

                    for (geniusArtist in geniusArtists){

                        val cleanedGeniusArtist = geniusArtist.replace(Regex("\\s*\\(.*?\\)"), "").trim()
                        val geniusUrl = result.getString("url")

                        if (cleanedGeniusArtist.equals(artist, ignoreCase = true)) {
                            matchedSongUrl = geniusUrl
                            break
                        }
                        else{
                            r += cleanedGeniusArtist + ", "
                        }
                    }
                }

                if (matchedSongUrl == null) {
                    return "returned Artists ${r}"
                }


                val regex = Regex("\"url\":\"(https://genius\\.com[^\"]+)\"")
                val songUrl = regex.find(json)?.groupValues?.get(1)?.replace("\\/", "/")

                if (songUrl == null) {
                    return "Could not find SOng"
                }


                // Now fetch and scrape the lyrics from the song page
                val doc = withContext(Dispatchers.IO) {
                    Jsoup.connect(songUrl)
                        .userAgent(
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                                    "(KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36"
                        )
                        .header(
                            "Accept",
                            "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"
                        )
                        .header("Accept-Language", "en-US,en;q=0.5")
                        .header("Connection", "keep-alive")
                        .timeout(10_000)
                        .get()
                }

                val containers = doc.select("div[class^=Lyrics__Container]")

                if (containers.isEmpty()) return "No lyrics found."

                val rawHtml = containers.joinToString("\n") { it.html() }
                val rawLyrics = Jsoup.parse(rawHtml).wholeText()

                // Filter out non-lyric lines
                val cleanedLyrics = rawLyrics
                    .lines()
                    .map { it.trim() }
                    .filter { line ->
                        line.isNotEmpty() &&
                                !line.contains("you might also like", true) &&
                                !line.contains("Embed", true) &&
                                !line.contains("Translations", true) &&
                                !line.contains("contributor", true) &&
                                !line.contains("Lyrics", true) &&
                                !line.contains("Read More", true)
                    }
                    .dropWhile { !it.startsWith("[") && it.length < 30 } // start from first verse marker
                    .joinToString("\n")

                println("[DEBUG] Final lyrics length: ${cleanedLyrics.length}")
                return cleanedLyrics.ifBlank { rawLyrics }
            }

        } catch (e: Exception) {
            "[ERROR] Exception: ${e.message}"
        }.toString()
    }
    suspend fun fetchLyricsFromOvh(artist: String, title: String): String? = withContext(Dispatchers.IO) {
        val artistList = artist.split(",").map { it.trim() }

        for (a in artistList) {
            if (a == "Unknown Artist"){
                break
            }
            try {
                val url = "https://api.lyrics.ovh/v1/${a}/${title}"
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5_000
                connection.readTimeout = 5_000

                if (connection.responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(response)
                    var rawLyrics = json.getString("lyrics")

                    return@withContext rawLyrics
                        .lines()
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                        .joinToString("\n")
                }
            } catch (e: Exception) {
                // Ignore and try next artist
                Log.d("LyricsFetcher", "Error fetching lyrics from OVH: ${e.message}")
            }
        }

        return@withContext null
    }
    suspend fun fetchLyricsFromGenius(artist: String, title: String): String? {
        val baseUrl = "https://genius.com/"

        // Split multiple artists by common delimiters like ",", "&", "feat", etc.
        val artistOptions = artist
            .lowercase()
            .split(Regex("[,|&]|feat|ft\\.?")) // handles multiple formats
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        // Prepare a cleaned list of artist names
        val cleanedArtists = artistOptions.map {
            it.replace(Regex("[^a-z0-9 ]"), "") // remove non-alphanumerics
                .replace(Regex("\\s+"), "-")     // replace spaces with dashes
                .trim()
        }

        val cleanedTitle = title
            .lowercase()
            .replace(Regex("[^a-z0-9 ]"), "")
            .replace(Regex("\\s+"), "-")
            .trim()

        // Try each artist variant
        for (cleanedArtist in cleanedArtists) {
            if (cleanedArtist == "Unknown Artist"){
                break
            }
            val url = "$baseUrl$cleanedArtist-$cleanedTitle-lyrics"
            try {
                val doc = withContext(Dispatchers.IO) {
                    Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                        .header("Accept-Language", "en-US,en;q=0.5")
                        .header("Connection", "keep-alive")
                        .followRedirects(true)
                        .timeout(10_000)
                        .get()
                }

                val lyricsElements = doc.select("div[class^=Lyrics__Container]")
                val rawHtmlLyrics = lyricsElements.joinToString("\n") { it.html() }
                var rawLyrics = Jsoup.parse(rawHtmlLyrics).wholeText()


                return rawLyrics
                    .lines()
                    .map { it.trim() }
                    .filter { line ->
                        line.isNotEmpty() &&
                                !line.contains("You might also like", true) &&
                                !line.contains("Embed", true) &&
                                !line.matches(Regex("^\\d+ contributors$", RegexOption.IGNORE_CASE)) &&
                                !line.endsWith("contributor", true) &&
                                !line.endsWith("contributors", true)
                    }
                    .dropWhile { line -> !line.contains("[verse 1]", true) }
                    .flatMap { line ->
                        if (line.matches(Regex("\\[.*?\\]", RegexOption.IGNORE_CASE))) {
                            listOf("", ":-:", "")
                        } else listOf(line)
                    }
                    .joinToString("\n")
            } catch (e: Exception) {
                // Try next artist variant
                Log.d("LyricsFetcher", "Error fetching lyrics from Genius: ${e.message}")
            }
        }

        return null // If no variant worked
    }
    suspend fun getLyricsLRCLIB(
        trackName: String,
        artistName: String,
        albumName: String,
        durationSeconds: Int
    ): String? = withContext(Dispatchers.IO) {
        isLyricsLoading = true
        try{
            val client = OkHttpClient()

            val url = "https://lrclib.net/api/get" +
                    "?track_name=${trackName.replace(" ", "+")}" +
                    "&artist_name=${artistName.replace(" ", "+")}" +
                    "&album_name=${albumName.replace(" ", "+")}" +
                    "&duration=$durationSeconds"

            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use null
                val body = response.body?.string() ?: return@use null

                val json = JSONObject(body)
                lyrics = json.optString("syncedLyrics", json.optString("plainLyrics", null))
                loadedLyricsSongName = trackName
                loadedLyricsSongArtistName = artistName
                return@use lyrics
            }
        } catch (e: Exception){
            e.message
        } finally{
            isLyricsLoading = false
        }.toString()

    }


    //
    // expand and collapse Logic
    //

    var collapsedHeightDp = 0.dp
    private var expandedHeightDp = 0.dp

    fun setExpandedAndCollapsedHeight(expandedHeight : Dp, collapsedHeight : Dp){
        expandedHeightDp = expandedHeight
        collapsedHeightDp = collapsedHeight
    }

    var collapsedHeightPx by mutableStateOf(0f)
    var expandedHeightPx by mutableStateOf(0f)

    var heightPx by mutableStateOf(0f)
    var wasExpanded by mutableStateOf(false)

    val thresholdDistance get() = (expandedHeightPx - collapsedHeightPx) * 0.02f

    fun initializeHeights(density: Density) {
        collapsedHeightPx = with(density) { collapsedHeightDp.toPx() }
        expandedHeightPx = with(density) { expandedHeightDp.toPx() }

        if (heightPx == 0f) heightPx = collapsedHeightPx
    }

    fun handleDrag(delta: Float) {
        heightPx = (heightPx - delta).coerceIn(collapsedHeightPx, expandedHeightPx)
        wasExpanded = heightPx > (collapsedHeightPx + expandedHeightPx) / 2
    }

    fun handleDragStopped() {
        heightPx = if (wasExpanded) {
            if (expandedHeightPx - heightPx > thresholdDistance) collapsedHeightPx else expandedHeightPx
        } else {
            if (heightPx - collapsedHeightPx > thresholdDistance) expandedHeightPx else collapsedHeightPx
        }
    }

    fun collapse() {
        if (isExpanded) {
            heightPx = collapsedHeightPx
        }
    }

    fun expandOnClick() {
        if (heightPx == collapsedHeightPx) heightPx = expandedHeightPx
    }

    val isExpanded : Boolean
        get() = heightPx >= expandedHeightPx * 0.9f
}