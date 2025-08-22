package com.example.estia

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Database(
    entities = [MusicFile::class,
        PlayBackMusicFile::class,
        LyricsEntry::class,
        SearchHistoryEntry::class,
        LikedSongFile::class,
        EstiaDownloadFile::class
               ],
    version = 2
)
abstract class MusicDataBase : RoomDatabase() {
    abstract fun musicDao(): MusicFileDao
    abstract fun lyricsDao(): LyricsDao
    abstract fun playBackMusicFileDao(): PlayBackMusicFileDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun estiaDownloadsDao(): EstiaDownloadsDao
    abstract fun likedSongsDao(): LikedSongsDao

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

@Entity(tableName = "musicfile")
data class MusicFile(
    val name: String,
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val artist: String? = null,
    val album: String? = null,
    var duration: Long? = null,
    var filePath: String? = null,
    var coverArtUri: String? = null,
    var source: String?,
    var streamableURL: String? = null
)

@Entity
data class PlayBackMusicFile(
    @PrimaryKey
    val rowId: Int = 0,

    val id: String,
    val name: String? = null,
    val artist: String? = null,
    val album: String? = null,
    val duration: Long? = null,
    val filePath: String? = null,
    val coverArtUri: String? = null,
    val source: String? = null,
    val color: Int? = null
)

@Entity(tableName = "lyrics_table")
data class LyricsEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val songName: String,
    val artistName: String,
    val lyrics: String
)

@Entity(tableName = "search_history")
data class SearchHistoryEntry(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val name: String,
    val artist: String? = null,
    val album: String? = null,
    val duration: Long? = null,
    val filePath: String? = null,
    val coverArtUri: String? = null,
    val source: String? = null,
    val timeStamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "likedSong")
data class LikedSongFile(
    val name: String,
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val coverArtUri: String? = null,
    val source: String = "Liked Songs",
)

@Entity(tableName = "estiaDownload")
data class EstiaDownloadFile(
    val name: String,
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val filePath: String,
    val coverArtUri: String? = null,
    val source: String = "Estia Downloads",
)

@Dao
interface LyricsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLyrics(entry: LyricsEntry)

    @Query("SELECT * FROM lyrics_table WHERE songName = :title AND artistName = :artist LIMIT 1")
    suspend fun getLyrics(title: String, artist: String): LyricsEntry?

    @Query("DELETE FROM lyrics_table WHERE songName = :title AND artistName = :artist")
    suspend fun deleteLyrics(title: String, artist: String)

    @Query("DELETE FROM lyrics_table")
    suspend fun deleteAllLyrics()
}


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

@Dao
interface LikedSongsDao {
    @Query("SELECT * FROM likedSong ORDER BY name COLLATE NOCASE ASC")
    fun getAllMusic(): List<LikedSongFile>

    @Upsert
    suspend fun upsertAll(musicFiles: List<LikedSongFile>)

    @Upsert
    suspend fun upsertMusicFile(musicFile: LikedSongFile)

    @Delete
    suspend fun deleteMusicFile(musicFile: LikedSongFile)

    @Query("DELETE FROM likedSong")
    suspend fun deleteAll()
}

@Dao
interface EstiaDownloadsDao {
    @Query("SELECT * FROM estiaDownload ORDER BY name COLLATE NOCASE ASC")
    fun getAllMusic(): List<EstiaDownloadFile>

    @Upsert
    suspend fun upsertAll(musicFiles: List<EstiaDownloadFile>)

    @Upsert
    suspend fun upsertMusicFile(musicFile: EstiaDownloadFile)

    @Delete
    suspend fun deleteMusicFile(musicFile: EstiaDownloadFile)

    @Query("DELETE FROM estiaDownload")
    suspend fun deleteAll()
}

@Dao
interface PlayBackMusicFileDao {
    @Query("SELECT * FROM playbackmusicfile LIMIT 1")
    suspend fun getState(): PlayBackMusicFile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveState(state: PlayBackMusicFile)

    @Query("DELETE FROM playbackmusicfile")
    suspend fun deleteAll()
}

@Dao
interface SearchHistoryDao {
    @Upsert
    suspend fun upsertSong(entry: SearchHistoryEntry)

    @Upsert
    suspend fun upsertAll(entries: List<SearchHistoryEntry>)

    @Query("SELECT * FROM search_history ORDER BY timeStamp DESC")
    fun getHistory(): Flow<List<SearchHistoryEntry>>

    @Query("DELETE FROM search_history WHERE id = :id")
    suspend fun deleteFromHistory(id: Long)

    @Query("DELETE FROM search_history")
    suspend fun clearHistory()
}



