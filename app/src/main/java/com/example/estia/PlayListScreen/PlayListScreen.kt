package com.example.estia.PlayListScreen

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.estia.MainAppScreen.MainAppScreenViewModel
import com.example.estia.MusicFile
import com.example.estia.R
import com.example.estia.SpotifyBold
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun RenderPlayListScreen(
    playListScreenViewModel: PlayListScreenViewModel,
    mainAppScreenViewModel : MainAppScreenViewModel){

    LaunchedEffect(mainAppScreenViewModel.nowPlaying.value) {
        mainAppScreenViewModel.nowPlaying.value?.let { currentSong ->

            if (playListScreenViewModel.playList.value.isNotEmpty()) {
                val firstSong = playListScreenViewModel.playList.value[0]

                if (currentSong.name != firstSong.name && currentSong.artist != firstSong.artist) {
                    playListScreenViewModel.replaceNowPlayingMusicFile(currentSong)
                }

            } else {
                playListScreenViewModel.addNowPlayingMusicFile(currentSong)
            }

        }
    }


    MusicListView(
        list = playListScreenViewModel.playList.value,
        listState = remember { LazyListState() },
        mainAppScreenViewModel
    )

}

@Composable
fun MusicListView(
    list: List<MusicFile>,
    listState: LazyListState,
    mainAppScreenViewModel: MainAppScreenViewModel
) {

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (
                    listState.firstVisibleItemIndex == 0 &&
                    listState.firstVisibleItemScrollOffset == 0 &&
                    available.y > 10f
                ) {
                }


                return Offset.Zero
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
    ) {

        // LazyColumn with song list
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 70.dp, start = 15.dp, end = 15.dp) // Leave space for search bar
        ) {
            item{
                if(list.size == 0){
                    Spacer(Modifier.height(120.dp))
                    NoMusicFoundInPlayQueue()
                }
            }

            items(list.size) { index ->
                val swipeOffset = remember { Animatable(0f) }
                val maxOffset = 250f // Max swipe distance to reveal hidden UI
                val dragThreshold = 100f
                val scope = rememberCoroutineScope()

                val nowPlaying by mainAppScreenViewModel.nowPlaying.collectAsState()

                val musicNameColor = if (nowPlaying?.name == list[index].name &&
                    nowPlaying?.artist == list[index].artist &&
                    nowPlaying?.source == list[index].source
                )
                    Color.Green else Color.White

                val artistColor = if (nowPlaying?.name == list[index].name &&
                    nowPlaying?.artist == list[index].artist &&
                    nowPlaying?.source == list[index].source
                )
                    Color.Green else Color.Gray

                val gestureModifier = Modifier.pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, dragAmount ->
                            val newOffset = (swipeOffset.value + dragAmount).coerceIn(0f, maxOffset)
                            scope.launch { swipeOffset.snapTo(newOffset) }
                        },
                        onDragEnd = {
                            scope.launch {
                                if (swipeOffset.value >= dragThreshold) {
                                    swipeOffset.animateTo(maxOffset)
                                    // (optional) trigger some action here

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


                var name = list[index].name ?: ""
                var artist = list[index].artist ?: ""

                if (name.length > 45) name = name.take(45) + "..."
                if (artist.length > 45) artist = artist.take(45) + "..."
                if (artist == "<unknown>") artist = "Unknown Artist"

                if(index == 0) {
                    Spacer(Modifier.height(50.dp))
                }


                Box(modifier = Modifier
                    .background(Color.Black)
                    .fillMaxWidth()
                ) {
                    // Hidden row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(65.dp)
                            .background(Color.Red),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Hidden UI", color = Color.White, modifier = Modifier.padding(16.dp))
                    }

                    // Visible song row
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
                                .clickable {
                                    mainAppScreenViewModel.setNowPlaying(list[index])
                                }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(
                                    modifier = Modifier
                                        .height(50.dp)
                                        .width(50.dp)
                                ) {
                                    val coverArt = list[index].coverArtUri
                                    if (coverArt == null) {
                                        Image(
                                            painter = painterResource(id = R.drawable.music_icon_compressed),
                                            contentDescription = "Simple Music Icon",
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        AsyncImage(
                                            model = coverArt,
                                            contentDescription = "Cover Art",
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }

                                Spacer(Modifier.width(10.dp))

                                Column (
                                    modifier = Modifier.width(250.dp)
                                ){
                                    Text(
                                        fontSize = 14.sp,
                                        fontFamily = SpotifyBold,
                                        text = name,
                                        color = musicNameColor,
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        fontSize = 12.sp,
                                        fontFamily = SpotifyBold,
                                        text = artist,
                                        color = artistColor
                                    )
                                }
                                if (nowPlaying?.name == list[index].name &&
                                    nowPlaying?.artist == list[index].artist &&
                                    nowPlaying?.source == list[index].source
                                ) {
                                    NowPlayingAnimationBar()
                                }
                            }
                        }
                    }

                }


                if (index == list.size - 1) {
                    Spacer(modifier = Modifier.height(175.dp))
                }
            }
        }
    }
}


@Composable
fun NoMusicFoundInPlayQueue(){
    Text(
        "Nothing in Play Queue",
        color = Color.White
    )
}




@Composable
fun NowPlayingAnimationBar() {
    val infiniteTransition = rememberInfiniteTransition()

    val barHeights = List(3) {
        infiniteTransition.animateFloat(
            initialValue = 4f,
            targetValue = 20f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 500 + it * 100, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    Row(
        modifier = Modifier
            .width(20.dp)
            .height(20.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        barHeights.forEach { heightAnim ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(heightAnim.value.dp)
                    .background(Color.Green, shape = RoundedCornerShape(50))
            )
        }
    }
}


