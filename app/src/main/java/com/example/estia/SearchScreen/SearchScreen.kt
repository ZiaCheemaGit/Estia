package com.example.estia.SearchScreen

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.estia.FileExplorerScreen.FileExplorerViewModel
import com.example.estia.MainAppScreen.MainAppScreenViewModel
import com.example.estia.MusicFile
import com.example.estia.PlayListScreen.PlayListScreenViewModel
import com.example.estia.R
import com.example.estia.SpotifyBold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform

@Composable
fun RenderSearchScreen(
    innerPadding: PaddingValues,
    viewModel: SearchScreenViewModel,
    mainAppScreenViewModel : MainAppScreenViewModel
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(context))
        }
    }

    val selectedfilter = viewModel.selectedFilter
    var searchQuery = viewModel.searchQuery.value

    LaunchedEffect(viewModel.searchQuery.value, viewModel.selectedFilter) {
        viewModel.applyFilter()
    }

    Box(
    ){
        Box{
            LazyColumn (Modifier.padding(start = 10.dp, end = 10.dp)
            ){
                // Top Space
                item{
                    Spacer(Modifier.height(innerPadding.calculateTopPadding() + 60.dp))
                }

                // Filter Options
                if(!viewModel.isLoading.value) {
                    item {
                        LazyRow(
                            modifier = Modifier
                                .padding(top = 10.dp)
                                .fillMaxWidth()
                        ) {
                            items(viewModel.filterOptionsList.size) { index ->

                                var text = viewModel.filterOptionsList[index]
                                var cardColor = Color.Gray

                                if (selectedfilter.value == text) {
                                    cardColor = Color(0xFFD8BFD8)
                                }
                                Card(
                                    modifier = Modifier
                                        .clickable(onClick = {
                                            viewModel.selectedFilter.value = text
                                            viewModel.applyFilter()
                                        })
                                        .padding(bottom = 10.dp)
                                        .height(35.dp),
                                    colors = CardDefaults.cardColors(cardColor)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = text,
                                            fontFamily = SpotifyBold,
                                            color = Color.White
                                        )
                                    }
                                }
                                Spacer(Modifier.width(10.dp))
                            }
                        }
                    }
                }
                else if(searchQuery == ""){}
                else{
                    item{
                        CircularProgressIndicator()
                    }
                }

                // Song Results
                if(viewModel.selectedFilter.value == viewModel.filterOptionsList[0]){
                    items(viewModel.songSearchResults.value.size) { index ->
                        SongItemComposable(
                            mainAppScreenViewModel,
                            viewModel,
                            musicFile = viewModel.songSearchResults.value[index]
                        )
                    }
                }

                // Album Results
                else if(viewModel.selectedFilter.value == viewModel.filterOptionsList[1]){
                    items(viewModel.albumSearchResults.value.size) { index ->
                        AlbumItemComposable(
                            mainAppScreenViewModel,
                            viewModel,
                            album = viewModel.albumSearchResults.value[index]
                        )
                    }
                }

                // Artist Results
                else if(viewModel.selectedFilter.value == viewModel.filterOptionsList[2]){
                    items(viewModel.artistSearchResults.value.size) { index ->
                        ArtistItemComposable(
                            mainAppScreenViewModel,
                            viewModel,
                            artist = viewModel.artistSearchResults.value[index]
                        )
                    }
                }

                // Bottom Space
                item{
                    Spacer(Modifier.height(innerPadding.calculateTopPadding() + 75.dp))
                }

            }
        }
        Box(
        ){
            Card(
                modifier = Modifier
                    .padding(top = innerPadding.calculateTopPadding())
                    .fillMaxWidth()
                    .height(60.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Spacer(Modifier.width(10.dp))
                    Box(){
                        Image(
                            painter = painterResource(id = R.drawable.search_icon_unselected),
                            contentDescription = "Cancel Search Button",
                            modifier = Modifier
                                .padding(start = 10.dp)
                                .size(20.dp)
                        )
                    }
                    Box(){
                        TextField(
                            textStyle = TextStyle(
                                fontSize = 16.sp,                         // Main input text size
                                fontFamily = SpotifyBold,        // Main input font
                                fontWeight = FontWeight.Thin,
                                color = Color.Black                       // Just in case
                            ),
                            value = searchQuery,
                            onValueChange = { viewModel.searchQuery.value = it },
                            placeholder = { Text("Search Estia") },
                            modifier = Modifier
                                .width(320.dp),
                            singleLine = true,
                            colors = androidx.compose.material3.TextFieldDefaults.colors(
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
                    Box(){
                        IconButton(
                            modifier = Modifier,
                            onClick = {
                                viewModel.searchQuery.value = ""
                            }){
                            Image(
                                painter = painterResource(id = R.drawable.clear_icon),
                                contentDescription = "Cancel Search Button",
                                modifier = Modifier
                                    .size(30.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SongItemComposable(
    mainAppScreenViewModel: MainAppScreenViewModel,
    searchScreenViewModel: SearchScreenViewModel,
    musicFile: DeezerTrack,
) {
    val scope = rememberCoroutineScope()
    var name = musicFile.title
    var artist : String = musicFile.artist.name
    val coverArt = musicFile.album.cover_medium
    if (name.length > 45) name = name.take(45)
    if (artist.length > 45) artist = artist.take(45) + "..."

    Box(
        modifier = Modifier
            .background(Color.Black)
            .fillMaxWidth()
    ) {
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
                    .clickable(onClick = {
                        scope.launch {

                            val localMusic = MusicFile(
                                name = musicFile.title,
                                artist = searchScreenViewModel.getOtherArtists(musicFile.id),
                                album = musicFile.album.title,
                                duration = musicFile.duration.toLong() * 1000,
                                filePath = null,
                                coverArtUri = musicFile.album.cover_xl,
                                source = "....",
                                id = musicFile.id
                            )

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

@Composable
fun AlbumItemComposable(
    mainAppScreenViewModel: MainAppScreenViewModel,
    searchScreenViewModel: SearchScreenViewModel,
    album: DeezerAlbum,
) {
    val scope = rememberCoroutineScope()
    var name = album.title
    var artist : String = album.artist.name
    val coverArt = album.cover_medium
    if (name.length > 45) name = name.take(45)
    if (artist.length > 45) artist = artist.take(45) + "..."

    Box(
        modifier = Modifier
            .background(Color.Black)
            .fillMaxWidth()
    ) {
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
                    .clickable(onClick = {

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

@Composable
fun ArtistItemComposable(
    mainAppScreenViewModel: MainAppScreenViewModel,
    searchScreenViewModel: SearchScreenViewModel,
    artist: DeezerArtist,
) {
    var name = artist.name
    val coverArt = artist.picture
    if (name.length > 45) name = name.take(45)

    Box(
        modifier = Modifier
            .background(Color.Black)
            .fillMaxWidth()
    ) {
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
                    .clickable(onClick = {

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
                    }
                    Spacer(Modifier.width(20.dp))
                }
            }
        }
    }
}