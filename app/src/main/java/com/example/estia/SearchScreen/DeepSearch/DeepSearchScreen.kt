package com.example.estia.SearchScreen.DeepSearch

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.estia.MainAppScreen.MainAppScreenViewModel
import com.example.estia.MusicFile
import com.example.estia.PlayListScreen.PlayListScreenViewModel
import com.example.estia.R
import com.example.estia.SearchScreen.MainScreen.AlbumItemComposable
import com.example.estia.SearchScreen.MainScreen.ArtistItemComposable
import com.example.estia.SearchScreen.MainScreen.SearchHistorySongItemComposable
import com.example.estia.SearchScreen.MainScreen.SearchScreenViewModel
import com.example.estia.SearchScreen.MainScreen.SongItemComposable
import com.example.estia.SearchScreen.MusicBrainzTrack
import com.example.estia.SearchScreen.MusicBrainzTrackDetails
import com.example.estia.SpotifyBold
import com.example.estia.YTMusicSong
import kotlinx.coroutines.launch
import java.nio.file.WatchEvent
import kotlin.math.roundToInt

@Composable
fun DeepSearchScreenDisplay(
    searchScreenViewModel: SearchScreenViewModel,
    deepSearchScreenViewModel: DeepSearchScreenViewModel,
    mainAppScreenViewModel : MainAppScreenViewModel,
    playListScreenViewModel: PlayListScreenViewModel,
    navController: NavController
){
    var dC = mainAppScreenViewModel.dominantColor.value
    if(mainAppScreenViewModel.isColorCloseToWhite(dC)){
        dC = dC.copy(alpha = 0.25f)
    }

    LazyColumn(Modifier.fillMaxSize()){
        item {
            Column(
                Modifier
                    .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            dC,dC,dC,dC,dC,dC,dC,
                            Color.Black
                        )
                    )
                )
            ){
                Row(
                    Modifier
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(top = 60.dp),
                    ) {
                        IconButton(
                            onClick = {
                                navController.popBackStack()
                            }
                        ) {
                            Image(
                                modifier = Modifier.size(20.dp),
                                painter = painterResource(id = R.drawable.back_icon),
                                contentDescription = "go back button",
                                colorFilter = ColorFilter.tint(Color.White)
                            )
                        }
                    }
                    Column {
                        Text(
                            modifier = Modifier
                                .padding(top = 70.dp, start = 20.dp),
                            text = "Deep Search",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontFamily = SpotifyBold
                        )
                    }
                }

                // Song name Field
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier
                            .width(90.dp)
                            .padding(start = 20.dp),
                        text = "Title",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontFamily = SpotifyBold
                    )
                    Spacer(Modifier.width(10.dp))
                    TextField(
                        modifier = Modifier.clip(RoundedCornerShape(16.dp)),
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            fontFamily = SpotifyBold,
                            fontWeight = FontWeight.Thin,
                            color = Color.Black
                        ),
                        value = deepSearchScreenViewModel.songName.value,
                        onValueChange = { deepSearchScreenViewModel.songName.value = it },
                        placeholder = { Text("Song Name") },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedPlaceholderColor = Color.Gray,
                            unfocusedPlaceholderColor = Color.Gray
                        )
                    )
                }

                Spacer(Modifier.height(15.dp))

                // Artist name Field
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier
                            .width(90.dp)
                            .padding(start = 20.dp),
                        text = "Artist",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontFamily = SpotifyBold
                    )
                    Spacer(Modifier.width(10.dp))
                    TextField(
                        modifier = Modifier.clip(RoundedCornerShape(16.dp)),
                        textStyle = TextStyle(
                            fontSize = 16.sp,                         // Main input text size
                            fontFamily = SpotifyBold,        // Main input font
                            fontWeight = FontWeight.Thin,
                            color = Color.Black                       // Just in case
                        ),
                        value = deepSearchScreenViewModel.artistName.value,//searchQuery,
                        onValueChange = { deepSearchScreenViewModel.artistName.value = it },
                        placeholder = { Text("Artist Name") },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedPlaceholderColor = Color.Gray,
                            unfocusedPlaceholderColor = Color.Gray
                        )
                    )
                }

                Spacer(Modifier.height(15.dp))

                // Search Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Card(
                        modifier = Modifier.clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = {
                                deepSearchScreenViewModel.search()
                            }
                        ),
                        colors = CardDefaults.cardColors(mainAppScreenViewModel.dominantColor.value)
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(10.dp),
                            text = "Search",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontFamily = SpotifyBold
                        )
                    }
                }

                Spacer(Modifier.height(5.dp))
            }
        }

        items(deepSearchScreenViewModel.songSearchResults.value.size){ it ->
            DeepSearchSongItemComposable(
                mainAppScreenViewModel,
                searchScreenViewModel,
                deepSearchScreenViewModel.songSearchResults.value[it],
                playListScreenViewModel,
                deepSearchScreenViewModel
            )
        }
        if(deepSearchScreenViewModel.isLoadingSongSearchResults.value){
            item{ CircularProgressIndicator() }
        }
    }
}

@Composable
fun DeepSearchSongItemComposable(
    mainAppScreenViewModel: MainAppScreenViewModel,
    searchScreenViewModel: SearchScreenViewModel,
    ytMF: YTMusicSong,
    playListScreenViewModel: PlayListScreenViewModel,
    deepSearchScreenViewModel: DeepSearchScreenViewModel
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    var name = ytMF.title
    var artist : String = ytMF.artists.joinToString(",")
    val coverArt = ytMF.thumbnailUrl
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
//                scope.launch {
//                    if (swipeOffset.value >= dragThreshold) {
//                        swipeOffset.animateTo(maxOffset)
//
//                        // add song to playList
//                        val localMusic = MusicFile(
//                            name = ytMF.title,
//                            artist = musicFile.artistCredit.joinToString(", "){it.name},//searchScreenViewModel.getAllArtists(musicFile.id.toString()),
//                            album = null,//musicFile.album?.title,
//                            duration = musicFile.length?.toLong(),
//                            filePath = null,
//                            coverArtUri = coverArt,
//                            source = "....",
//                            id = ytMF.videoId
//                        )
//
//                        searchScreenViewModel.addToHistory(localMusic)
//                        playListScreenViewModel.enqueueInPlayQueue(localMusic)
//
//                        swipeOffset.animateTo(0f) // snap back
//                    } else {
//                        swipeOffset.animateTo(0f) // also snap back if not enough
//                    }
//                }
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
            .padding(start = 10.dp, end = 10.dp)
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
                            val d = deepSearchScreenViewModel.durationToMillis(ytMF.duration)

                            val localMusic = MusicFile(
                                name = ytMF.title,
                                artist = ytMF.artists.joinToString(", "),
                                album = ytMF.album,
                                duration = d,
                                filePath = null,
                                coverArtUri = coverArt,
                                source = "YTMusicSearch",
                                id = ytMF.videoId
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

