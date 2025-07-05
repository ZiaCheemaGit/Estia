package com.example.estia.SearchScreen

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

data class DeezerSearchResponse(
    val data: List<DeezerTrack>
)

data class DeezerTrack(
    val id: Long,
    val title: String,
    val artist: DeezerArtist,
    val album: DeezerAlbum,
    val duration: Int,
    val preview: String
)

data class DeezerTrackDetails(
    val id: Long,
    val title: String,
    val contributors: List<DeezerArtist>,
    val album: DeezerAlbum,
    val duration: Int,
    val preview: String
)


data class DeezerArtist(
    val id: Long,
    val name: String
    // Add more fields if needed
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

    @GET("track/{id}")
    suspend fun getTrackDetails(@Path("id") id: Long): DeezerTrackDetails
}

object DeezerService {
    private val retrofit = retrofit2.Retrofit.Builder()
        .baseUrl("https://api.deezer.com/")
        .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
        .build()

    val api: DeezerApi = retrofit.create(DeezerApi::class.java)
}
