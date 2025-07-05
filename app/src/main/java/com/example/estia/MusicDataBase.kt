package com.example.estia

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Database(
    entities = [MusicFile::class, PlayBackMusicFile::class, LyricsEntry::class],
    version = 1
)
abstract class MusicDataBase : RoomDatabase() {
    abstract fun musicDao(): MusicFileDao
    abstract fun lyricsDao(): LyricsDao
    abstract fun playBackMusicFileDao(): PlayBackMusicFileDao

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


@Entity
data class MusicFile(
    val name : String,
    @PrimaryKey(autoGenerate = false)
    val id : Long,
    val artist : String? = null,
    val album : String? = null,
    var duration: Long? = null,
    var filePath : String? = null,
    var coverArtUri : String? = null,
    var source : String?,
)


@Entity()
data class PlayBackMusicFile(
    @PrimaryKey
    val rowId: Int = 0,

    val id: Long,
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
interface PlayBackMusicFileDao {
    @Query("SELECT * FROM playbackmusicfile LIMIT 1")
    suspend fun getState(): PlayBackMusicFile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveState(state: PlayBackMusicFile)

    @Query("DELETE FROM playbackmusicfile")
    suspend fun deleteAll()
}



