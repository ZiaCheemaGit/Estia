package com.example.estia.PlayListScreen

import android.annotation.SuppressLint
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.estia.AccountScreen.LocalFilesScreen.FileExplorerViewModel
import com.example.estia.MainAppScreen.MainAppScreenViewModel
import com.example.estia.MusicFile
import com.example.estia.PlayerDrawer.PlayerDrawerViewModel
import com.example.estia.R
import com.example.estia.SpotifyBold

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun RenderPlayListScreen(
    fileExplorerViewModel: FileExplorerViewModel,
    expandableDrawerViewModel : PlayerDrawerViewModel,
    innerPadding: PaddingValues,
    playListScreenViewModel: PlayListScreenViewModel,
    mainAppScreenViewModel : MainAppScreenViewModel){

    MusicListView(
        expandableDrawerViewModel,
        playListScreenViewModel,
        queue = playListScreenViewModel.visiblePlayQueue.value,
        fileExplorerViewModel,
        listState = remember { LazyListState() },
        mainAppScreenViewModel,
        innerPadding = innerPadding
    )

}

@Composable
fun MusicListView(
    expandableDrawerViewModel : PlayerDrawerViewModel,
    playListScreenViewModel: PlayListScreenViewModel,
    queue: List<MusicFile>,
    fileExplorerViewModel: FileExplorerViewModel,
    listState: LazyListState,
    mainAppScreenViewModel: MainAppScreenViewModel,
    innerPadding : PaddingValues
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // LazyColumn with song list
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 15.dp, end = 15.dp)
        ) {
            // top padding
            item{
                Spacer(Modifier.height(innerPadding.calculateTopPadding()))
            }

            // Now Playing Song
            item{
                val nowPlaying by mainAppScreenViewModel.nowPlaying.collectAsState()
                if(nowPlaying != null){

                    val musicNameColor = Color.White
                    val artistColor = Color.Gray

                    val coverArt = nowPlaying?.coverArtUri

                    var name = nowPlaying?.name ?: ""
                    var artist = nowPlaying?.artist ?: ""

                    if (name.length > 45) name = name.take(45) + "..."
                    if (artist.length > 45) artist = artist.take(45) + "..."
                    if (artist == "<unknown>") artist = "Unknown Artist"


                    Box(modifier = Modifier
                        .background(Color.Black)
                        .fillMaxWidth()
                    ) {
                        Column(
                            Modifier.height(100.dp)
                        ){
                            Text(
                                "Now Playing",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontFamily = SpotifyBold,
                            )
                            Spacer(Modifier.height(20.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(65.dp)
                                    .background(Color.Black),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(65.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Column(
                                            modifier = Modifier
                                                .height(50.dp)
                                                .width(50.dp)
                                        ) {
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
                                        NowPlayingAnimationBar()
                                    }
                                }
                            }
                        }
                    }

                }
            }

            // Play Queue
            items(queue.size) { index ->

                val musicNameColor = Color.White
                val artistColor = Color.Gray

                var name = queue[index].name ?: ""
                var artist = queue[index].artist ?: ""

                if (name.length > 45) name = name.take(45) + "..."
                if (artist.length > 45) artist = artist.take(45) + "..."
                if (artist == "<unknown>") artist = "Unknown Artist"

                val coverArt = queue[index].coverArtUri

                Box(
                    modifier = Modifier
                        .background(Color.Black)
                        .fillMaxWidth()
                ) {
                    Column(
                    ) {
                        if (index == 0) {
                            Spacer(Modifier.height(30.dp))
                            Row(
                                Modifier.height(30.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ){
                                Text(
                                    modifier = Modifier.weight(1f),
                                    text = "Next In : Queue",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontFamily = SpotifyBold,
                                )
                                IconButton(onClick = {
                                    playListScreenViewModel.clearPlayQueue()
                                }) {
                                    Image(
                                        modifier = Modifier.size(20.dp),
                                        painter = painterResource(id = R.drawable.trash_icon),
                                        contentDescription = "Clear Queue",
                                        colorFilter = ColorFilter.tint(Color.White)
                                    )
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(65.dp)
                                .background(Color.Black),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                                    .height(65.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(
                                        modifier = Modifier
                                            .height(50.dp)
                                            .width(50.dp)
                                    ) {
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

                                    Column(
                                        modifier = Modifier.width(250.dp)
                                    ) {
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
                                }
                            }
                        }
                    }
                }
            }

            // now playing based next Up Songs : Local Storage Queue
            if(mainAppScreenViewModel.nowPlaying.value?.source == "Local Storage"){
                val localStorageQueue = playListScreenViewModel.visibleLocalStorageQueue.value
                items(localStorageQueue.size) { index ->

                    val musicNameColor = Color.White
                    val artistColor = Color.Gray

                    var name = localStorageQueue[index].name ?: ""
                    var artist = localStorageQueue[index].artist ?: ""

                    if (name.length > 45) name = name.take(45) + "..."
                    if (artist.length > 45) artist = artist.take(45) + "..."
                    if (artist == "<unknown>") artist = "Unknown Artist"

                    val coverArt = localStorageQueue[index].coverArtUri

                    Box(
                        modifier = Modifier
                            .background(Color.Black)
                            .fillMaxWidth()
                    ) {
                        Column(
                        ) {
                            if (index == 0) {
                                Spacer(Modifier.height(30.dp))
                                Row(
                                    Modifier.height(30.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        modifier = Modifier.weight(1f),
                                        text = "Next In : Local Storage",
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontFamily = SpotifyBold,
                                    )
                                    IconButton(onClick = {
                                        playListScreenViewModel.clearLocalStorageQueue()
                                    }) {
                                        Image(
                                            modifier = Modifier.size(20.dp),
                                            painter = painterResource(id = R.drawable.trash_icon),
                                            contentDescription = "Clear Queue",
                                            colorFilter = ColorFilter.tint(Color.White)
                                        )
                                    }
                                }
                                Spacer(Modifier.height(10.dp))
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(65.dp)
                                    .background(Color.Black),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp)
                                        .height(65.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Column(
                                            modifier = Modifier
                                                .height(50.dp)
                                                .width(50.dp)
                                        ) {
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

                                        Column(
                                            modifier = Modifier.width(250.dp)
                                        ) {
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
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Space
            item{
                Spacer(Modifier.height(innerPadding.calculateBottomPadding() + expandableDrawerViewModel.collapsedHeightDp))
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


