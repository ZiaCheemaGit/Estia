package com.example.estia.SearchScreen

import retrofit2.http.GET
import retrofit2.http.Query

data class DeezerSearchResponse(
    val data: List<DeezerTrack>
)

data class DeezerTrack(
    val title: String,
    val artist: DeezerArtist,
    val album: DeezerAlbum,
    val duration: Int,
    val preview: String
)

data class DeezerArtist(
    val name: String
)

data class DeezerAlbum(
    val title: String,
    val cover_small: String,
    val cover_medium: String,
    val cover_big: String,
    val cover_xl: String
)


interface DeezerApi {
    @GET("search")
    suspend fun searchTracks(@Query("q") query: String): DeezerSearchResponse
}

object DeezerService {
    private val retrofit = retrofit2.Retrofit.Builder()
        .baseUrl("https://api.deezer.com/")
        .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
        .build()

    val api: DeezerApi = retrofit.create(DeezerApi::class.java)
}
