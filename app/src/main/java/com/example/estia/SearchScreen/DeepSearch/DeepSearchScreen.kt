package com.example.estia.SearchScreen.DeepSearch

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.estia.MainAppScreen.MainAppScreenViewModel
import com.example.estia.MusicFile
import com.example.estia.PlayListScreen.PlayListScreenViewModel
import com.example.estia.R
import com.example.estia.SearchScreen.MainScreen.SearchScreenViewModel
import com.example.estia.SearchScreen.MusicBrainzTrack
import com.example.estia.SpotifyBold
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun DeepSearchScreenDisplay(
    deepSearchScreenViewModel: DeepSearchScreenViewModel,
    mainAppScreenViewModel : MainAppScreenViewModel,
    playListScreenViewModel: PlayListScreenViewModel,
){

}

@Composable
fun DeepSearchSongItemComposable(
    mainAppScreenViewModel: MainAppScreenViewModel,
    searchScreenViewModel: SearchScreenViewModel,
    musicFile: MusicBrainzTrack,
    playListScreenViewModel: PlayListScreenViewModel
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    var name = musicFile.title
    var artist : String = musicFile.artistCredit.joinToString(",") { it.name }
    val coverArt = null//musicFile.album?.cover_medium
    if (name.length > 45) name = name.take(45)
    if (artist.length > 45) artist = artist.take(45) + "..."

    val swipeOffset = remember { Animatable(0f) }
    val maxOffset = 250f // Max swipe distance to reveal hidden UI
    val dragThreshold = 100f

    val gestureModifier = Modifier.pointerInput(Unit) {
        detectHorizontalDragGestures(
            onHorizontalDrag = { _, dragAmount ->
                val newOffset =
                    (swipeOffset.value + dragAmount).coerceIn(0f, maxOffset)
                scope.launch { swipeOffset.snapTo(newOffset) }
            },
            onDragEnd = {
                scope.launch {
                    if (swipeOffset.value >= dragThreshold) {
                        swipeOffset.animateTo(maxOffset)

                        // add song to playList
                        val localMusic = MusicFile(
                            name = musicFile.title,
                            artist = musicFile.artistCredit.joinToString(", "){it.name},//searchScreenViewModel.getAllArtists(musicFile.id.toString()),
                            album = null,//musicFile.album?.title,
                            duration = musicFile.length?.toLong(),
                            filePath = null,
                            coverArtUri = null,//musicFile.album?.cover_xl,
                            source = "....",
                            id = musicFile.id
                        )

                        searchScreenViewModel.addToHistory(localMusic)
                        playListScreenViewModel.enqueueInPlayQueue(localMusic)

                        swipeOffset.animateTo(0f) // snap back
                    } else {
                        swipeOffset.animateTo(0f) // also snap back if not enough
                    }
                }
            },
            onDragCancel = {
                scope.launch {
                    swipeOffset.animateTo(0f) // also snap back on cancel
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .background(Color.Black)
            .fillMaxWidth()
    ) {
        // Hidden Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(65.dp)
                .background(Color.Green),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Add To Queue",
                color = Color.White,
                modifier = Modifier.padding(5.dp),
                fontFamily = SpotifyBold,
                fontSize = 15.sp
            )
        }

        // Visible Row
        Row(
            modifier = Modifier
                .offset { IntOffset(swipeOffset.value.roundToInt(), 0) }
                .fillMaxWidth()
                .height(65.dp)
                .background(Color.Black)
                .then(gestureModifier),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
                    .height(65.dp)
                    .clickable(onClick = {
                        scope.launch {

                            val localMusic = MusicFile(
                                name = musicFile.title,
                                artist = musicFile.artistCredit.joinToString(", ") { it.name },//searchScreenViewModel.getAllArtists(musicFile.id.toString()),
                                album = null,//musicFile.album?.title,
                                duration = musicFile.length?.toLong(),
                                filePath = null,
                                coverArtUri = null,//musicFile.album?.cover_xl,
                                source = "....",
                                id = musicFile.id
                            )

                            keyboardController?.hide()
                            searchScreenViewModel.addToHistory(localMusic)
                            mainAppScreenViewModel.setNowPlaying(localMusic)

                        }
                    })
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(
                        modifier = Modifier
                            .height(50.dp)
                            .width(50.dp)
                    ) {
                        val placeholderPainter = painterResource(id = R.drawable.music_icon_compressed)

                        AsyncImage(
                            model = coverArt,
                            contentDescription = "Cover Art",
                            modifier = Modifier.fillMaxSize(),
                            placeholder = placeholderPainter,
                            error = placeholderPainter,
                            fallback = placeholderPainter,
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(Modifier.width(10.dp))

                    Column(
                        modifier = Modifier.width(250.dp)
                    ) {
                        Text(
                            maxLines = 1,
                            fontSize = 14.sp,
                            fontFamily = SpotifyBold,
                            text = name,
                            color = Color.White,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            maxLines = 1,
                            fontSize = 12.sp,
                            fontFamily = SpotifyBold,
                            text = artist,
                            color = Color.Gray
                        )
                    }
                    Spacer(Modifier.width(20.dp))
                }
            }
        }
    }
}