package com.example.estia.AccountScreen.MainScreen

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.estia.LikedSongFile
import com.example.estia.MusicFile
import com.example.estia.EstiaDownloadFile
import com.example.estia.MusicDataBase
import com.example.estia.downloader.DownloaderObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import java.io.File
import androidx.compose.runtime.mutableStateMapOf
import com.example.estia.YTImportedPlayLists
import com.example.estia.YTImportedSong
import com.example.estia.YTMusicSong
import com.example.estia.importedYTPlaylistWithSongs
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.playlist.PlaylistInfo
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import org.schabi.newpipe.extractor.stream.StreamType
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class AccountScreenViewModel() : ViewModel() {

    val likedSongs = MutableStateFlow(listOf<LikedSongFile>())
    val estiaDownloads = MutableStateFlow(listOf<EstiaDownloadFile>())
    val importedYTPlayLists = MutableStateFlow(listOf<importedYTPlaylistWithSongs>())

    val selectedYTPlayListToDisplay = mutableStateOf<importedYTPlaylistWithSongs?>(null)

    val isLikedSong = mutableStateOf(false)
    val isDownloadedSong = mutableStateOf(false)
    val showAddToLibraryDialog = mutableStateOf(false)

    private var nowPlaying: MusicFile? = null

    val downloadIdToMusicFileIDMap = mutableStateMapOf<Long, String>()
    val musicFileIDMapToDownloadProgress = mutableStateMapOf<String, Int>()

    lateinit var context: Context

    fun setNowPlaying(musicFile: MusicFile){
        this.nowPlaying = musicFile
        isLikedSong.value = likedSongs.value.any{ it.id == mfToLSF().id }
        isDownloadedSong.value = estiaDownloads.value.any { it.id == mfToEDF().id }
    }

    private lateinit var db: MusicDataBase
    fun initializeContextAndDB(c: Context){
        this.context = c
        db = MusicDataBase.Companion.getInstance(c)
        likedSongsLoadFromDB()
        estiaDownloadsLoadFromDB()
        loadImportedYTPlaylistsFromDB()
    }

    fun addSongToLikedSongs(){
        viewModelScope.launch{
            val file = mfToLSF()
            likedSongs.value += file
            isLikedSong.value = true
            likedSongSaveToDB(file)
        }
    }

    fun removeSongFromLikedSongs(){
        viewModelScope.launch {
            val file = mfToLSF()
            db.likedSongsDao().deleteMusicFile(file)
            likedSongs.value = likedSongs.value.drop(likedSongs.value.indexOf(file))
            isLikedSong.value = false
        }
    }

    fun isSongDownloadCompleted(musicFileID: String): Boolean{
        val key = downloadIdToMusicFileIDMap.entries
            .find { it.value == musicFileID }
            ?.key
        if (key != null){
            return DownloaderObject.isDownloadCompleted(context, key)
        }
        else{
            return true
        }
    }

    fun addSongToEstiaDownloads(){
        viewModelScope.launch{
            if(nowPlaying != null){

                val path = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                    "Estia/${nowPlaying!!.id}.estia"
                ).absolutePath

                val downloadID = DownloaderObject.downloadFile(context, nowPlaying!!)
                isDownloadedSong.value = true
                val file = mfToEDF().copy(
                    duration = getMetadataDuration(nowPlaying?.filePath.toString()).toLong(),
                    filePath = path
                )
                estiaDownloads.value += file
                downloadIdToMusicFileIDMap.put(downloadID, nowPlaying!!.id)
                musicFileIDMapToDownloadProgress.put(nowPlaying!!.id, 0)
            }
        }
    }

    fun removeSongFromEstiaDownloads(){
        viewModelScope.launch {
            val path = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                "Estia/${nowPlaying!!.id}.estia"
            ).absolutePath
            val deleted = deleteFileAtPath(path)

            if(deleted){
                val file = mfToEDF()
                db.estiaDownloadsDao().deleteMusicFile(file)
                estiaDownloads.value = estiaDownloads.value.drop(estiaDownloads.value.indexOf(file))
                isDownloadedSong.value = false
            }
        }
    }

    fun deleteFileAtPath(path: String): Boolean {
        val file = File(path)
        return if (file.exists()) {
            file.delete()
        } else {
            false
        }
    }

    fun mfToLSF(): LikedSongFile{
        return LikedSongFile(
            name = nowPlaying?.name ?: "",
            id = nowPlaying?.id ?: "",
            artist = nowPlaying?.artist ?: "Unknown Artist",
            album = nowPlaying?.album ?: "Unknown Album",
            duration = nowPlaying?.duration ?: 0L,
            coverArtUri = nowPlaying?.coverArtUri,
        )
    }

    fun mfToEDF(): EstiaDownloadFile{
        val filePath = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
            "Estia/${nowPlaying?.id}.estia"
        ).absolutePath
        return EstiaDownloadFile(
            name = nowPlaying?.name ?: "",
            id = nowPlaying?.id ?: "",
            artist = nowPlaying?.artist ?: "Unknown Artist",
            album = nowPlaying?.album ?: "Unknown Album",
            duration = nowPlaying?.duration ?: 0L,
            coverArtUri = nowPlaying?.coverArtUri,
            filePath = filePath,
        )
    }

    fun EDFToMF(f: EstiaDownloadFile): MusicFile{
        return MusicFile(
            name = f.name,
            id = f.id,
            artist = f.artist,
            album = f.album,
            duration = f.duration,
            filePath = f.filePath,
            coverArtUri = f.coverArtUri,
            source = f.source,
        )
    }

    suspend private fun likedSongSaveToDB(file: LikedSongFile){
        db.likedSongsDao().upsertMusicFile(file)
    }

    suspend private fun estiaDownloadSaveToDB(file: EstiaDownloadFile){
        db.estiaDownloadsDao().upsertMusicFile(file)
    }

    private fun loadImportedYTPlaylistsFromDB(){
        viewModelScope.launch(Dispatchers.IO) {
            importedYTPlayLists.value = emptyList()
            importedYTPlayLists.value = db.ytPlaylistDao().getAllPlaylistsWithSongs()
        }
    }

    private fun likedSongsLoadFromDB(){
        viewModelScope.launch(Dispatchers.IO) {
            likedSongs.value = db.likedSongsDao().getAllMusic()
        }
    }

    private fun estiaDownloadsLoadFromDB(){
        viewModelScope.launch(Dispatchers.IO){
            estiaDownloads.value = db.estiaDownloadsDao().getAllMusic()
        }
    }

    suspend fun observe(){
        val done = true
        while(done){
            if(DownloaderObject.downloaderIsActive.value){
                downloadIdToMusicFileIDMap.forEach { it ->
                    if(isSongDownloadCompleted(it.value)){
                        downloadIdToMusicFileIDMap.remove(it.key)
                        musicFileIDMapToDownloadProgress.remove(it.value)

                        val songID = it.value
                        val fileToDB = estiaDownloads.value.find{
                            it.id == songID
                        }
                        if(fileToDB != null){
                            estiaDownloadSaveToDB(fileToDB)
                        }
                    }
                    else{
                        musicFileIDMapToDownloadProgress[it.value] =
                            DownloaderObject.getDownloadProgress(context, it.key)
                    }
                }
            }
            delay(2000)

        }
    }

    suspend fun getMetadataDuration(url: String): Long = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(url, HashMap())
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            durationStr?.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            Log.e("Metadata", "Error retrieving metadata", e)
            0L
        } finally {
            retriever.release()
        }
    }

    // Create PlayList Logic

    val toCreatePLayListName = mutableStateOf("")
    val toCreatePLayListDescription = mutableStateOf("")

    fun addPlaylistToDB(){
        viewModelScope.launch {
            var playList = YTImportedPlayLists(
                id = 0,
                title = toCreatePLayListName.value,
                ytPlayListID = null,
                description = toCreatePLayListDescription.value
            )
            val playListID = db.ytPlaylistDao().insertPlaylist(playList)
            playList = playList.copy(id = playListID)

            importedYTPlayLists.value += importedYTPlaylistWithSongs(
                playlist = playList,
                songs = emptyList(),
            )
            clearPlayListCreationVariables()
        }
    }

    fun clearPlayListCreationVariables(){
        toCreatePLayListName.value = ""
        toCreatePLayListDescription.value = ""
    }

    // YT Importing Logic

    val playListUrl = mutableStateOf("")
    val playListTitle = mutableStateOf("")
    val ytPlayListID = mutableStateOf("")
    val ytPlayListDescp = mutableStateOf("")
    val showYTInput = mutableStateOf(false)
    val ytParsingError = mutableStateOf(false)
    val ytParsedVideoList = mutableStateOf(emptyList<YTMusicSong>())
    val ytImportBuffer = mutableStateOf(emptyList<YTMusicSong>())
    val isParsingLoading = mutableStateOf(false)
    val isAlreadyImported = mutableStateOf(false)

    fun clearParsingVariables(){
        playListUrl.value = ""
        playListTitle.value = ""
        showYTInput.value = false
        ytParsingError.value = false
        ytParsedVideoList.value = emptyList<YTMusicSong>()
        ytImportBuffer.value = emptyList<YTMusicSong>()
        isParsingLoading.value = false
        isAlreadyImported.value = false
    }

    fun removeSongFromBuffer(song: YTMusicSong){
        ytImportBuffer.value = ytImportBuffer.value.filterNot {
            it.videoId == song.videoId
        }
    }

    enum class YTLinkType { VIDEO, PLAYLIST, INVALID, UNKNOWN }

    fun importBufferFull(): Boolean{
        return ytImportBuffer.value.size == ytParsedVideoList.value.size &&
                ytParsedVideoList.value.isNotEmpty()
    }

    fun classifyYouTubeUrl(raw: String): YTLinkType{
        val url = raw.trim()
        val uri = runCatching { Uri.parse(url) }.getOrNull() ?: return YTLinkType.INVALID
        val host = uri.host?.lowercase() ?: return YTLinkType.INVALID

        // Accept youtu.be and any *.youtube.com (www, m, music, etc.)
        val isYouTube = host == "youtu.be" || host.endsWith("youtube.com")
        if (!isYouTube) return YTLinkType.INVALID

        val path = uri.path.orEmpty()
        val listParam = uri.getQueryParameter("list")
        val vParam = uri.getQueryParameter("v")

        return when {
            !listParam.isNullOrBlank() || path.startsWith("/playlist") -> YTLinkType.PLAYLIST
            !vParam.isNullOrBlank() -> YTLinkType.VIDEO
            host == "youtu.be" && path.length > 1 -> YTLinkType.VIDEO        // https://youtu.be/VIDEO_ID
            path.startsWith("/shorts/") -> YTLinkType.VIDEO                  // shorts count as videos
            else -> YTLinkType.UNKNOWN
        }
    }

    fun inBuffer(song: YTMusicSong): Boolean{
        return ytImportBuffer.value.any(){
            it.videoId == song.videoId
        }
    }

    suspend fun getSongsFromYoutubePlaylist(url: String): List<YTMusicSong> {
        return withContext(Dispatchers.IO) {
            try {
                val songs = mutableListOf<YTMusicSong>()
                val playlistInfo = PlaylistInfo.getInfo(ServiceList.YouTube, url)

                ytPlayListDescp.value = playlistInfo.description.content
                ytPlayListID.value = playlistInfo.id
                playListTitle.value = playlistInfo.name

                // helper to process page items
                fun processItems(items: List<InfoItem>) {
                    items.filterIsInstance<StreamInfoItem>()
                        .filter { it.streamType == StreamType.AUDIO_STREAM || it.streamType == StreamType.VIDEO_STREAM }
                        .forEach { item ->
                            val artist = listOfNotNull(item.uploaderName)
                            val thumbnailUrl = item.thumbnails
                                .maxByOrNull { (it.width ?: 0) * (it.height ?: 0) }
                                ?.url

                            val path = downloadImageToTempCache(context, thumbnailUrl.toString())

                            songs.add(
                                YTMusicSong(
                                    videoId = item.url.substringAfter("v="),
                                    title = item.name ?: "",
                                    artists = artist,
                                    duration = item.duration.toString(),
                                    thumbnailUrl = path,
                                    album = null
                                )
                            )
                        }
                }

                // process first page
                processItems(playlistInfo.relatedItems)

                // process subsequent pages
                var nextPage: Page? = playlistInfo.nextPage
                while (nextPage != null) {
                    val collector = PlaylistInfo.getMoreItems(
                        playlistInfo.service,
                        playlistInfo.url,
                        nextPage
                    )

                    processItems(collector.items)
                    nextPage = collector.nextPage
                }

                songs

            } catch (e: Exception) {
                e.printStackTrace()
                emptyList<YTMusicSong>()
            }
        }
    }

    fun checkIfAlreadyImported(){
        viewModelScope.launch(Dispatchers.IO){
            isAlreadyImported.value = false
            val foundPL = importedYTPlayLists.value
                .find { it.playlist.ytPlayListID == ytPlayListID.value }

            foundPL?.songs?.forEach { it ->
                ytImportBuffer.value += YTMusicSong(
                    title = it.title,
                    videoId = it.videoId,
                    artists = it.artist?.split(",") ?: emptyList(),
                    album = null,
                    duration = it.duration.toString(),
                    thumbnailUrl = it.thumbnailUrl
                )
                if(it.thumbnailUrl != null){
                    deleteFileAtPath(it.thumbnailUrl)
                }
            }
            if (foundPL != null) {
                isAlreadyImported.value = true
                db.ytPlaylistDao().removePlaylistWithSongs(foundPL.playlist.id)
                importedYTPlayLists.value = importedYTPlayLists.value.filterNot {
                    it.playlist.id == foundPL.playlist.id
                }
            }
        }
    }

    fun startParsingVideos(url: String = playListUrl.value){
        isParsingLoading.value = true
        Log.d("", "Starting Parsing")
        ytParsingError.value = false

        if(classifyYouTubeUrl(url) == YTLinkType.INVALID){
            ytParsingError.value = true
        }
        if(classifyYouTubeUrl(url) == YTLinkType.PLAYLIST){
            viewModelScope.launch{
                ytParsedVideoList.value = getSongsFromYoutubePlaylist(url)
                checkIfAlreadyImported()
                isParsingLoading.value = false
            }
        }
    }

    fun addYTPlaylistToDB(){
        if(ytImportBuffer.value.isNotEmpty()){
            viewModelScope.launch {
                val playList = YTImportedPlayLists(
                    0, playListTitle.value, ytPlayListID.value, ytPlayListDescp.value)
                val playListID = db.ytPlaylistDao().insertPlaylist(playList)

                val list = emptyList<YTImportedSong>().toMutableList()
                ytImportBuffer.value.forEach { song ->

                    list.add(
                        YTImportedSong(
                            songId = 0,
                            playlistId = playListID,
                            videoId = song.videoId,
                            title = song.title,
                            artist = song.artists.joinToString(","),
                            thumbnailUrl = copyImageToPermanentFolder(
                                context,
                                song.thumbnailUrl.toString()
                            ),
                            filePath = null,
                            duration = timeStringToMillis(song.duration.toString())
                        )
                    )
                }

                importedYTPlayLists.value += importedYTPlaylistWithSongs(
                    playlist = playList.copy(id = playListID),
                    songs = list
                )

                clearTemporaryImageCache(context)

                db.ytPlaylistDao().insertSongs(list)
                clearParsingVariables()
            }
        }
    }

    fun timeStringToMillis(time: String): Long {
        // Split into parts
        val parts = time.split(":").map { it.toLongOrNull() ?: 0L }

        // Handle formats: HH:mm:ss, mm:ss, or just ss
        return when (parts.size) {
            3 -> { // HH:mm:ss
                val (h, m, s) = parts
                (h * 3600 + m * 60 + s) * 1000
            }
            2 -> { // mm:ss
                val (m, s) = parts
                (m * 60 + s) * 1000
            }
            1 -> { // ss
                parts[0] * 1000
            }
            else -> 0L
        }
    }

    fun downloadImageToTempCache(
        context: Context,
        imageUrl: String,
        fileName: String = "downloaded_image_${System.currentTimeMillis()}.jpg"
    ): String? {
        return try {
            val url = URL(imageUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                return null
            }

            // Ensure temp folder exists
            val tempDir = File(context.filesDir, "temporaryImportedImageCache")
            if (!tempDir.exists()) {
                tempDir.mkdirs()
            }

            val file = File(tempDir, fileName)
            val inputStream = connection.inputStream
            val outputStream = FileOutputStream(file)

            val buffer = ByteArray(4096)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()
            connection.disconnect()

            file.absolutePath // return full path
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun copyImageToPermanentFolder(
        context: Context,
        sourcePath: String,
        folderName: String = "ImportedImages"): String? {
        return try {
            val sourceFile = File(sourcePath)
            if (!sourceFile.exists()) return null

            val destDir = File(context.filesDir, folderName)
            if (!destDir.exists()) destDir.mkdirs()

            val destFile = File(destDir, sourceFile.name)

            FileInputStream(sourceFile).use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun clearTemporaryImageCache(context: Context) {
        val tempDir = File(context.filesDir, "temporaryImportedImageCache")
        if (tempDir.exists() && tempDir.isDirectory) {
            tempDir.listFiles()?.forEach { it.delete() }
        }
    }

}
