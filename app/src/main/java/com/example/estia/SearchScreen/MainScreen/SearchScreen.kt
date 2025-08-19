package com.example.estia.SearchScreen.MainScreen

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.platform.WindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.estia.MainAppScreen.MainAppScreenViewModel
import com.example.estia.MusicFile
import com.example.estia.PlayListScreen.PlayListScreenViewModel
import com.example.estia.PlayerDrawer.PlayerDrawerViewModel
import com.example.estia.R
import com.example.estia.ScreenRouter
import com.example.estia.SearchScreen.AlbumDisplayScreen.AlbumScreenDisplay
import com.example.estia.SearchScreen.ArtistDisplayScreen.ArtistScreenDisplay
import com.example.estia.SearchScreen.DeepSearch.DeepSearchScreenDisplay
import com.example.estia.SearchScreen.DeepSearch.DeepSearchScreenViewModel
import com.example.estia.SearchScreen.DeepSearch.DeepSearchSongItemComposable
import com.example.estia.SearchScreen.DeezerAlbum
import com.example.estia.SearchScreen.DeezerArtist
import com.example.estia.SearchScreen.DeezerService
import com.example.estia.SearchScreen.DeezerTrack
import com.example.estia.SearchScreen.SearchScreenRouter
import com.example.estia.SearchScreen.MainScreen.SearchScreenViewModel
import com.example.estia.SpotifyBold
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun RenderSearchScreen(
    innerPadding: PaddingValues,
    viewModel: SearchScreenViewModel,
    mainAppScreenViewModel : MainAppScreenViewModel,
    playListScreenViewModel: PlayListScreenViewModel,
    deepSearcScreenViewModel: DeepSearchScreenViewModel,
    expandableDrawerViewModel: PlayerDrawerViewModel,
    windowInfo: com.example.estia.WindowInfo
) {
    val searchScreenNavController = rememberNavController()

    NavHost(
        navController = searchScreenNavController,
        startDestination = SearchScreenRouter.mainScreen
    ) {
        composable(SearchScreenRouter.mainScreen){
            SearchScreenDisplay(
                innerPadding,
                viewModel,
                mainAppScreenViewModel,
                playListScreenViewModel,
                searchScreenNavController
            )
        }

        composable(SearchScreenRouter.deepSearchScreen) {
            DeepSearchScreenDisplay(
                viewModel,
                deepSearcScreenViewModel,
                mainAppScreenViewModel,
                playListScreenViewModel,
                searchScreenNavController
            )
        }

        composable(SearchScreenRouter.albumScreen) {
            AlbumScreenDisplay(
                mainAppScreenViewModel,
                searchScreenNavController,
                searchScreenViewModel = viewModel,
                playListScreenViewModel = playListScreenViewModel,
                expandableDrawerViewModel = expandableDrawerViewModel,
                windowInfo = windowInfo
            )
        }

        composable(SearchScreenRouter.artistScreen) {
            ArtistScreenDisplay(
                mainAppScreenViewModel = mainAppScreenViewModel,
                navController = searchScreenNavController,
                searchScreenViewModel = viewModel,
                playListScreenViewModel = playListScreenViewModel,
                expandableDrawerViewModel = expandableDrawerViewModel,
                windowInfo = windowInfo
            )

        }
    }
}

@Composable
fun SearchScreenDisplay(
    innerPadding: PaddingValues,
    viewModel: SearchScreenViewModel,
    mainAppScreenViewModel : MainAppScreenViewModel,
    playListScreenViewModel: PlayListScreenViewModel,
    navController: NavController
){
    viewModel.loadSearchHistoryFromDB()
    val selectedfilter = viewModel.selectedFilter
    var searchQuery = viewModel.searchQuery.value

    var dC = mainAppScreenViewModel.dominantColor.value
    if(mainAppScreenViewModel.isColorCloseToWhite(dC)){
        dC = dC.copy(alpha = 0.25f)
    }

    LaunchedEffect(viewModel.searchQuery.value, viewModel.selectedFilter) {
        viewModel.applyFilter()
    }

    Box(modifier = Modifier.fillMaxSize()){
        Column(
            modifier = Modifier
                .height(170.dp)
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            dC,
                            dC,
                            dC,
                            Color.Black
                        )
                    )
                )
        ){
            Spacer(Modifier.height(50.dp))

            // Search Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 15.dp, end = 15.dp)
                    .height(50.dp)
                    .background(Color.White),
            ) {
                Row{
                    Spacer(Modifier.width(10.dp))
                    Column(
                        Modifier.height(50.dp),
                        verticalArrangement = Arrangement.Center
                    ){
                        Image(
                            painter = painterResource(id = R.drawable.search_icon_unselected),
                            contentDescription = "Cancel Search Button",
                            modifier = Modifier
                                .size(20.dp)
                        )
                    }

                    Column(
                        Modifier
                            .height(50.dp)
                            .width(310.dp),
                        verticalArrangement = Arrangement.Center
                    ){
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
                        )}

                    Column{
                        if (!viewModel.searchQuery.value.isEmpty()) {
                            IconButton(
                                modifier = Modifier,
                                onClick = {
                                    viewModel.searchQuery.value = ""
                                }) {
                                Image(
                                    painter = painterResource(id = R.drawable.clear_icon),
                                    contentDescription = "Cancel Search Button",
                                    modifier = Modifier
                                        .size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Filter Options
            LazyRow(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .fillMaxWidth()
            ) {
                item{
                    Spacer(Modifier.width(20.dp))
                }

                items(viewModel.filterOptionsList.size) { index ->

                    var text = viewModel.filterOptionsList[index]
                    var cardColor = Color.Gray

                    if (selectedfilter.value == text) {
                        cardColor = dC
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

        LazyColumn(
            modifier = Modifier
                .padding(
                    top = 170.dp,
                    start = 10.dp,
                    end = 10.dp)
        ) {

            // History
            if (searchQuery == "") {
                viewModel.clearSearchResults()
                val history = viewModel.history.value
                for (index in history.indices) {
                    item{
                        val musicFile = viewModel.history.value[index]
                        val tempFile = MusicFile(
                            id = musicFile.id.toString(),
                            name = musicFile.name,
                            artist = musicFile.artist,
                            album = musicFile.album,
                            duration = musicFile.duration,
                            filePath = musicFile.filePath,
                            coverArtUri = musicFile.coverArtUri,
                            source = musicFile.source
                        )
                        SearchHistorySongItemComposable(
                            mainAppScreenViewModel,
                            viewModel,
                            musicFile = tempFile,
                            playListScreenViewModel
                        )
                    }
                }
            }

            // Loading
            if (viewModel.isLoading.value) {
                item { CircularProgressIndicator() }
            }

            // Song Results
            if (viewModel.selectedFilter.value == viewModel.filterOptionsList[0]) {
                items(viewModel.songSearchResults.value.size) { index ->
                    SongItemComposable(
                        mainAppScreenViewModel,
                        viewModel,
                        musicFile = viewModel.songSearchResults.value[index],
                        playListScreenViewModel
                    )
                }

                if(viewModel.songSearchResults.value.isNotEmpty()){
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ){
                            Card(
                                modifier = Modifier
                                    .padding(top = 15.dp, bottom = 15.dp)
                                    .height(70.dp)
                                    .clickable(
                                        onClick = {
                                            navController.navigate(SearchScreenRouter.deepSearchScreen)
                                        }
                                    ),
                                colors = CardDefaults.cardColors(mainAppScreenViewModel.dominantColor.value)
                            ) {
                                Column{
                                    Text(
                                        text = "Can't Find What you are Looking for?",
                                        color = Color.White,
                                        modifier = Modifier
                                            .padding(top = 15.dp,start = 10.dp, end = 10.dp),
                                        fontFamily = SpotifyBold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "Try Deep Search",
                                        color = Color.White,
                                        modifier = Modifier.align(Alignment.CenterHorizontally),
                                        fontFamily = SpotifyBold,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Album Results
            else if (viewModel.selectedFilter.value == viewModel.filterOptionsList[1]) {
                items(viewModel.albumSearchResults.value.size) { index ->
                    AlbumItemComposable(
                        mainAppScreenViewModel,
                        viewModel,
                        album = viewModel.albumSearchResults.value[index],
                        navController
                    )
                }
            }

            // Artist Results
            else {
                items(viewModel.artistSearchResults.value.size) { index ->
                    ArtistItemComposable(
                        mainAppScreenViewModel,
                        viewModel,
                        artist = viewModel.artistSearchResults.value[index],
                        navController
                    )
                }
            }

            item{
                Spacer(Modifier.height(innerPadding.calculateTopPadding() + 180.dp))
            }
        }
    }
}

@Composable
fun SongItemComposable(
    mainAppScreenViewModel: MainAppScreenViewModel,
    searchScreenViewModel: SearchScreenViewModel,
    musicFile: DeezerTrack,
    playListScreenViewModel: PlayListScreenViewModel
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    var name = musicFile.title
    var artist : String = musicFile.artist.name
    val coverArt = musicFile.album?.cover_medium
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
                            artist = searchScreenViewModel.getAllArtists(musicFile.id.toString()),
                            album = musicFile.album.title,
                            duration = musicFile.duration.toLong() * 1000,
                            filePath = null,
                            coverArtUri = musicFile.album.cover_xl,
                            source = "....",
                            id = musicFile.id.toString()
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
                                artist = searchScreenViewModel.getAllArtists(musicFile.id.toString()),
                                album = musicFile.album.title,
                                duration = musicFile.duration.toLong() * 1000,
                                filePath = null,
                                coverArtUri = musicFile.album.cover_xl,
                                source = "....",
                                id = musicFile.id.toString()
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
                            text = artist + " - Song",
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
fun SearchHistorySongItemComposable(
    mainAppScreenViewModel: MainAppScreenViewModel,
    searchScreenViewModel: SearchScreenViewModel,
    musicFile: MusicFile,
    playListScreenViewModel: PlayListScreenViewModel
) {
    val scope = rememberCoroutineScope()
    var name = musicFile.name
    var artist : String = musicFile.artist.toString()
    val coverArt = musicFile.coverArtUri
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

                        playListScreenViewModel.enqueueInPlayQueue(musicFile)

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

                            mainAppScreenViewModel.setNowPlaying(musicFile)

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
                    IconButton(onClick = {
                        searchScreenViewModel.removeFromHistory(musicFile)
                    }) {
                        Image(
                            modifier = Modifier.size(30.dp),
                            painter = painterResource(id = R.drawable.clear_icon),
                            contentDescription = "Clear Queue",
                            colorFilter = ColorFilter.tint(Color.White)
                        )
                    }
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
    navController: NavController
) {
    //val scope = rememberCoroutineScope()
    var name = album.title
    var artist : String = album.artist?.name ?: ""
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
                .clickable(
                    onClick = {
                        mainAppScreenViewModel.selectedAlbum.value = album
                        mainAppScreenViewModel.getSelectedAlbumTracks()
                        navController.navigate(SearchScreenRouter.albumScreen)
                    }
                )
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
                            text = artist + " - Album",
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
    navController: NavController
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
                        mainAppScreenViewModel.selectedArtist.value = artist
                        mainAppScreenViewModel.loadArtistData()
                        navController.navigate(SearchScreenRouter.artistScreen)
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