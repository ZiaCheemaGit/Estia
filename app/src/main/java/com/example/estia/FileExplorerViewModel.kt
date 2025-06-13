package com.example.estia

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.media.MediaMetadataRetriever
import android.provider.MediaStore
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.drawable.toBitmap
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Upsert
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

class FileExplorerViewModel : ViewModel() {
    private lateinit var db: MusicDataBase
    private lateinit var context : Context

    fun setContentResolverAndInitDB(resolver: ContentResolver, context: Context) {
        contentResolver = resolver
        this.context = context
        db = MusicDataBase.getInstance(context)
        Log.d("DB_INIT", "Database initialized: $db")
    }

    private var contentResolver : ContentResolver? = null

    val _musicList = MutableStateFlow(listOf<MusicFile>())
    val musicList = _musicList

    private val _nowPlaying = MutableStateFlow<MusicFile?>(null)
    val nowPlaying: StateFlow<MusicFile?> = _nowPlaying

    private val _dominantColor = MutableStateFlow(Color.Gray)
    val dominantColor: StateFlow<Color> = _dominantColor

    fun setNowPlaying(musicFile: MusicFile) {
        _nowPlaying.value = musicFile
        viewModelScope.launch {
            _dominantColor.value = getDominantColorFromUri(musicFile.coverArtUri ?: "")
        }
    }

    private val _permissionGranted = MutableStateFlow(false)
    val permissionGranted : StateFlow<Boolean> = _permissionGranted

    private val _isLoading = mutableStateOf(true)
    val isLoading = _isLoading

    fun loadMusicFiles() = viewModelScope.launch(Dispatchers.IO) {

        val existingSongs = db.musicDao().getAllMusic().firstOrNull()
        if (!existingSongs.isNullOrEmpty()) {
            _musicList.value = existingSongs
            _isLoading.value = false
            return@launch
        }

        isLoading.value = true
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DATA,
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

        contentResolver?.let{ consRes ->
            val cursor = consRes.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
            )
            val songs = mutableListOf<MusicFile>()
            cursor?.use {
                val nameColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val filePathColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

                while (it.moveToNext()) {
                    var name = it.getString(nameColumn)
                    val id = it.getLong(idColumn)
                    val artist = it.getString(artistColumn)
                    val album = it.getString(albumColumn)
                    val duration = it.getLong(durationColumn)
                    val filePath = it.getString(filePathColumn)
                    val coverArtUri = extractCoverArtUri(filePath)

                    name = name.substringBeforeLast(".")

                    songs.add(MusicFile(name, id, artist, album, duration,
                        filePath, coverArtUri))
                }

                db.musicDao().upsertAll(songs)

                musicList.value = songs
                isLoading.value = false
            }
        }
    }

    fun refreshMusicFiles() = viewModelScope.launch(Dispatchers.IO) {
        db.musicDao().deleteAll()
        loadMusicFiles()
    }

    fun grantPermission(){
        _permissionGranted.value = true
    }

    fun isGranted() : Boolean{
        return permissionGranted.value
    }

    fun extractCoverArtUri(filePath: String): String? {
        val mmr = MediaMetadataRetriever()
        return try {
            mmr.setDataSource(filePath)
            val artBytes = mmr.embeddedPicture
            if (artBytes != null) {
                // Save to internal cache
                val file = File(context.cacheDir, "${filePath.hashCode()}.jpg")
                file.writeBytes(artBytes)
                file.toURI().toString()
            } else null
        } catch (e: Exception) {
            null
        } finally {
            mmr.release()
        }
    }

    suspend fun getDominantColorFromUri(imageUri: String): Color {
        return withContext(Dispatchers.IO) {
            try {
                val loader = ImageLoader(context)
                val request = ImageRequest.Builder(context)
                    .data(imageUri)
                    .allowHardware(false)
                    .build()
                val result = (loader.execute(request) as? SuccessResult)?.drawable
                val bitmap = (result as? BitmapDrawable)?.bitmap

                bitmap?.let {
                    val palette = Palette.from(it).generate()
                    val dominantColor = palette.getDominantColor(android.graphics.Color.BLACK)
                    Color(dominantColor)
                } ?: Color(0xFFFFC0CB)
            } catch (e: Exception) {
                Log.e("DominantColor", "Error extracting color", e)
                Color(0xFFFFC0CB)
            }
        }
    }




}

@Entity
data class MusicFile(
    val name : String? = null,
    @PrimaryKey(autoGenerate = false)
    val id : Long = 0,
    val artist : String? = null,
    val album : String? = null,
    val duration: Long? = null,
    val filePath : String? = null,
    val coverArtUri: String? = null
)

@Dao
interface MusicFileDao {
    @Query("SELECT * FROM musicfile ORDER BY name COLLATE NOCASE ASC")
    fun getAllMusic(): Flow<List<MusicFile>>

    @Upsert
    suspend fun upsertAll(musicFiles: List<MusicFile>)

    @Upsert
    suspend fun upsertMusicFile(musicFile: MusicFile)

    @Delete
    suspend fun deleteMusicFile(musicFile: MusicFile)

    @Query("DELETE FROM MusicFile")
    suspend fun deleteAll()
}

@Database(
    entities = [MusicFile::class], version = 1
)
abstract class MusicDataBase : RoomDatabase()
{
    abstract fun musicDao(): MusicFileDao

    companion object {
        @Volatile
        private var INSTANCE: MusicDataBase? = null

        fun getInstance(context: Context): MusicDataBase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MusicDataBase::class.java,
                    "music_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}