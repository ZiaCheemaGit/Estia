package com.example.estia.SearchScreen

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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

data class DeezerAlbum(
    val id: Long,
    val title: String,
    val cover_small: String,
    val cover_medium: String,
    val cover_big: String,
    val cover_xl: String,
    val artist: DeezerArtist
)

data class DeezerAlbumDetails(
    val id: Long,
    val title: String,
    val release_date: String?,
    val cover_small: String,
    val cover_medium: String,
    val cover_big: String,
    val cover_xl: String,
    val artist: DeezerArtist,
    val tracks: DSearchResponse<DeezerTrack>   // nested tracks
)

data class DeezerArtistDetails(
    val id: Long,
    val name: String,
    val link: String?,
    val picture: String?,
    val picture_medium: String?,
    val picture_big: String?,
    val picture_xl: String?,
    val nb_album: Int,
    val nb_fan: Int,
    val radio: Boolean,
    val tracklist: String
)

data class DeezerArtistAlbums(
    val data: List<DeezerAlbumSummary>,
    val next: String? = null // for pagination
)

data class DeezerAlbumSummary(
    val id: Long,
    val title: String,
    val cover_small: String,
    val cover_medium: String,
    val cover_big: String,
    val cover_xl: String,
    val record_type: String,
    val tracklist: String,
    val type: String
)

data class ArtistFullData(
    val artist: DeezerArtistDetails,
    val albums: List<DeezerAlbumDetails>,
    val albumIds: List<Long>,
    val topTracks: List<DeezerTrack>
)

data class DSearchResponse<T>(
    val data: List<T>,
    val total: Int,
    val next: String?
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

    @GET("album/{id}")
    suspend fun getAlbumDetails(@Path("id") id: Long): DeezerAlbumDetails

    // Artist basic details
    @GET("artist/{id}")
    suspend fun getArtistDetails(@Path("id") id: Long): DeezerArtistDetails

    // Artist albums
    @GET("artist/{id}/albums")
    suspend fun getArtistAlbums(@Path("id") id: Long): DeezerArtistAlbums

    @GET("artist/{id}/top")
    suspend fun getArtistTopTracks(@Path("id") artistId: Long): DSearchResponse<DeezerTrack>
}

object DeezerService {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.deezer.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: DeezerApi = retrofit.create(DeezerApi::class.java)

    suspend fun getArtistFullData(artistId: Long): ArtistFullData {
        // 1. Get artist details
        val artistDetails = api.getArtistDetails(artistId)

        // 2. Get artist albums (first page)
        val albumsResponse = api.getArtistAlbums(artistId)
        val albumIds = albumsResponse.data.map { it.id }

        // 3. Fetch details for each album
        val albums = albumIds.mapNotNull { id ->
            try {
                api.getAlbumDetails(id)
            } catch (e: Exception) {
                null // Skip failed album fetches
            }
        }

        // 4. Get top tracks
        val topTracks = try {
            api.getArtistTopTracks(artistId).data
        } catch (e: Exception) {
            emptyList() // Fallback if top tracks fail
        }

        return ArtistFullData(
            artist = artistDetails,
            albums = albums,
            albumIds = albumIds,
            topTracks = topTracks
        )
    }
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
    val id: String,
    val score: Int,
    val title: String,
    val length: Int? = null,
    val video: Boolean? = null,
    @SerializedName("artist-credit")
    val artistCredit: List<ArtistCredit>,
    val releases: List<Release>?
)

data class Release(
    val id: String,
    val title: String?
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
    val track: MusicBrainzTrack,
    val coverArt: String
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

    suspend fun searchExactTrack(title: String, artist: String): MusicBrainzSearchResponse {
        val query = """recording:"$title" AND artist:"$artist""""
        return api.searchTracks(query)
    }
}

interface CoverArtApi {
    @GET("release/{mbid}/")
    suspend fun getCoverArt(@Path("mbid") releaseId: String): CoverArtResponse
}

data class CoverArtResponse(
    val images: List<CoverImage>
)

data class CoverImage(
    val front: Boolean,
    val image: String
)

object CoverArtService {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://coverartarchive.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: CoverArtApi = retrofit.create(CoverArtApi::class.java)
}
