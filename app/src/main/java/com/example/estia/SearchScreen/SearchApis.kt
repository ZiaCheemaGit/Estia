package com.example.estia.SearchScreen

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// Deezer Api

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

data class DSearchResponse<T>(
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
    suspend fun searchAlbums(@Query("q") query: String): DSearchResponse<DeezerAlbum>

    @GET("search/artist")
    suspend fun searchArtists(@Query("q") query: String): DSearchResponse<DeezerArtist>
}

object DeezerService {
    private val retrofit = retrofit2.Retrofit.Builder()
        .baseUrl("https://api.deezer.com/")
        .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
        .build()

    val api: DeezerApi = retrofit.create(DeezerApi::class.java)
}


// Music Brainz Api

data class MusicBrainzSearchResponse(
    val created: String,
    val count: Int,
    val offset: Int,
    @SerializedName("recordings")
    val recordings: List<MusicBrainzTrack>
)

data class MusicBrainzTrack(
    val id: String, // UUID, not Long
    val score: Int,
    val title: String,
    val length: Int? = null, // MusicBrainz uses "length"
    val video: Boolean? = null,
    @SerializedName("artist-credit")
    val artistCredit: List<ArtistCredit>
)

data class ArtistCredit(
    val name: String,
    val artist: Artist
)

data class Artist(
    val id: String,
    val name: String,
    @SerializedName("sort-name")
    val sortName: String
)

data class MusicBrainzTrackDetails(
    val id: Long,
    val title: String,
    val contributors: List<DeezerArtist>,
    val album: DeezerAlbum,
    val duration: Int,
    val preview: String
)


data class MusicBrainzArtist(
    val id: Long,
    val name: String,
    val link: String?,
    val picture: String?,
    val picture_medium: String?,
    val picture_big: String?,
    val picture_xl: String?
)

data class MBSearchResponse<T>(
    val data: List<T>
)

data class MusicBrainzAlbum(
    val id: Long,
    val title: String,
    val cover_small: String,
    val cover_medium: String,
    val cover_big: String,
    val cover_xl: String,
    val artist: DeezerArtist
)

interface MusicBrainzApi {
    @GET("recording")
    suspend fun searchTracks(
        @Query("query") query: String,
        @Query("fmt") format: String = "json",
        @Query("limit") limit: Int = 30
    ): MusicBrainzSearchResponse
}

object MusicBrainzService {
    private val retrofit = retrofit2.Retrofit.Builder()
        .baseUrl("https://musicbrainz.org/ws/2/")
        .client(
            OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .header("User-Agent", "Estia/1.0 (cheemazia863@gmail.com)")
                        .build()
                    val response = chain.proceed(request)
                    val bodyString = response.body?.string()
                    println("RAW JSON: $bodyString")
                    response.newBuilder()
                        .body(ResponseBody.create(response.body?.contentType(), bodyString!!))
                        .build()
                }
                .build()
        )
        .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
        .build()

    val api: MusicBrainzApi = retrofit.create(MusicBrainzApi::class.java)
}

