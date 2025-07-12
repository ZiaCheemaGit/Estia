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
    val name: String,
    val link: String?,
    val picture: String?,
    val picture_medium: String?,
    val picture_big: String?,
    val picture_xl: String?
)

data class SearchResponse<T>(
    val data: List<T>
)

data class DeezerAlbum(
    val id: Long,
    val title: String,
    val cover_small: String,
    val cover_medium: String,
    val cover_big: String,
    val cover_xl: String,
    val artist: DeezerArtist
)


interface DeezerApi {
    @GET("search")
    suspend fun searchTracks(@Query("q") query: String): DeezerSearchResponse

    @GET("track/{id}")
    suspend fun getTrackDetails(@Path("id") id: Long): DeezerTrackDetails

    @GET("search/album")
    suspend fun searchAlbums(@Query("q") query: String): SearchResponse<DeezerAlbum>

    @GET("search/artist")
    suspend fun searchArtists(@Query("q") query: String): SearchResponse<DeezerArtist>
}

object DeezerService {
    private val retrofit = retrofit2.Retrofit.Builder()
        .baseUrl("https://api.deezer.com/")
        .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
        .build()

    val api: DeezerApi = retrofit.create(DeezerApi::class.java)
}
