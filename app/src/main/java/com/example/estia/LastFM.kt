package com.example.estia

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import org.json.JSONObject
import java.security.MessageDigest
import androidx.core.content.edit

object LastFmService {

    private const val API_KEY = "21264443388760c5b92dbd41a077b26f"
    private const val SHARED_SECRET = "e0241ad852f50ddf1211a7b10a69c725"
    private const val BASE_URL = "https://ws.audioscrobbler.com/2.0/"

    // Login Status
    fun isLoggedIn(context: Context): Boolean {
        val prefs = context.getSharedPreferences("lastfm", Context.MODE_PRIVATE)
        return prefs.getString("session_key", null) != null
    }

    // Used to generate the required api_sig hash
    private fun generateApiSig(params: Map<String, String>, secret: String): String {
        val sorted = params.toSortedMap()
        val raw = sorted.entries.joinToString("") { it.key + it.value } + secret
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(raw.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    // Log in, get sessionKey, store in SharedPreferences
    suspend fun loginAndGetSessionKey(
        context: Context,
        username: String,
        password: String
    ): String? = withContext(Dispatchers.IO) {

        val params = mapOf(
            "method" to "auth.getMobileSession",
            "username" to username,
            "password" to password,
            "api_key" to API_KEY
        )

        val apiSig = generateApiSig(params, SHARED_SECRET)

        val body = FormBody.Builder()
            .add("method", "auth.getMobileSession")
            .add("username", username)
            .add("password", password)
            .add("api_key", API_KEY)
            .add("api_sig", apiSig)
            .add("format", "json")
            .build()

        val request = Request.Builder()
            .url(BASE_URL)
            .post(body)
            .build()

        val client = OkHttpClient()
        try {
            client.newCall(request).execute().use { response ->
                val json = JSONObject(response.body?.string() ?: "")
                val sessionKey = json.optJSONObject("session")?.optString("key")

                sessionKey?.let {
                    // Save in shared preferences
                    context.getSharedPreferences("lastfm", Context.MODE_PRIVATE)
                        .edit { putString("session_key", it) }
                }

                return@withContext sessionKey
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }

    // Retrieve the saved sessionKey from SharedPreferences
    fun getStoredSessionKey(context: Context): String? {
        return context.getSharedPreferences("lastfm", Context.MODE_PRIVATE)
            .getString("session_key", null)
    }

    // Helper to post a signed request
    private suspend fun postSignedRequest(
        method: String,
        context: Context,
        extraParams: Map<String, String>
    ): JSONObject? = withContext(Dispatchers.IO) {
        val sessionKey = getStoredSessionKey(context) ?: return@withContext null

        val params = mutableMapOf(
            "method" to method,
            "api_key" to API_KEY,
            "sk" to sessionKey,
            "format" to "json"
        )
        params.putAll(extraParams)

        val apiSig = generateApiSig(params, SHARED_SECRET)

        val bodyBuilder = FormBody.Builder()
        for ((key, value) in params) {
            bodyBuilder.add(key, value)
        }
        bodyBuilder.add("api_sig", apiSig)

        val request = Request.Builder()
            .url(BASE_URL)
            .post(bodyBuilder.build())
            .build()

        return@withContext try {
            val response = OkHttpClient().newCall(request).execute()
            val json = JSONObject(response.body?.string() ?: "")
            json
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Like A Song
    suspend fun likeTrack(context: Context, artist: String, track: String): Boolean {
        val json = postSignedRequest(
            method = "track.love",
            context = context,
            extraParams = mapOf("artist" to artist, "track" to track)
        )
        return json?.has("error") == false
    }

    // Unlike a track
    suspend fun unlikeTrack(context: Context, artist: String, track: String): Boolean {
        val json = postSignedRequest(
            method = "track.unlove",
            context = context,
            extraParams = mapOf("artist" to artist, "track" to track)
        )
        return json?.has("error") == false
    }

    // Add song to history
    suspend fun scrobbleTrack(
        context: Context,
        artist: String,
        track: String,
        timestamp: Long = System.currentTimeMillis() / 1000
    ): Boolean {
        val json = postSignedRequest(
            method = "track.scrobble",
            context = context,
            extraParams = mapOf(
                "artist" to artist,
                "track" to track,
                "timestamp" to timestamp.toString()
            )
        )
        return json?.has("error") == false
    }

    // Log Out
    fun logout(context: Context) {
        context.getSharedPreferences("lastfm", Context.MODE_PRIVATE).edit {
            remove("session_key")
            remove("username")
        }
    }

    suspend fun fetchUserInfo(context: Context): JSONObject? = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences("lastfm", Context.MODE_PRIVATE)
        val sessionKey = prefs.getString("session_key", null)
        val username = prefs.getString("username", null)

        if (sessionKey == null || username == null) return@withContext null

        val params = mapOf(
            "method" to "user.getInfo",
            "user" to username,
            "api_key" to API_KEY,
            "sk" to sessionKey
        )

        val apiSig = generateApiSig(params, SHARED_SECRET)

        val url = HttpUrl.Builder()
            .scheme("https")
            .host("ws.audioscrobbler.com")
            .addPathSegment("2.0")
            .addQueryParameter("method", "user.getInfo")
            .addQueryParameter("user", username)
            .addQueryParameter("api_key", API_KEY)
            .addQueryParameter("sk", sessionKey)
            .addQueryParameter("api_sig", apiSig)
            .addQueryParameter("format", "json")
            .build()

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        try {
            OkHttpClient().newCall(request).execute().use { response ->
                val json = JSONObject(response.body?.string() ?: "")
                return@withContext json.optJSONObject("user")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }

}
