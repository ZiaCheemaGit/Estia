package com.example.estia.AccountScreen.LikedSongsScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.estia.AccountScreen.LocalFilesScreen.FileExplorerViewModel
import com.example.estia.AccountScreen.LocalFilesScreen.NoMusicFoundInLocalStorageScreen
import com.example.estia.AccountScreen.LocalFilesScreen.SongItemComposable
import com.example.estia.AudioFetcher
import com.example.estia.LikedSongFile
import com.example.estia.MainAppScreen.MainAppScreenViewModel
import com.example.estia.MusicFile
import com.example.estia.PlayListScreen.PlayListScreenViewModel
import com.example.estia.PlayerDrawer.PlayerDrawerViewModel
import com.example.estia.R
import com.example.estia.RememberWindowInfo
import com.example.estia.SpotifyBold
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun LikedSongsScreen(
    playListScreenViewModel: PlayListScreenViewModel,
    fileExplorerViewModel : FileExplorerViewModel,
    list: List<LikedSongFile>,
    mainAppScreenViewModel: MainAppScreenViewModel,
    expandableDrawerViewModel: PlayerDrawerViewModel,
    navController: NavController
) {
    val listState = rememberLazyListState()

    val windowInfo = RememberWindowInfo()
    val isScrolledDown by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 && listState.firstVisibleItemScrollOffset > 0
        }
    }

    val coroutineScope = rememberCoroutineScope()

    val showSearchBar = fileExplorerViewModel.showSearchBar
    var searchQuery = fileExplorerViewModel.searchQuery

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (
                    listState.firstVisibleItemIndex == 0 &&
                    listState.firstVisibleItemScrollOffset == 0 &&
                    available.y > 10f
                ) {
                    showSearchBar.value = true
                }

                if (showSearchBar.value && available.y < 0 || available.y > 0) {
                    keyboardController?.hide()
                }

                if (searchQuery.value == "" && available.y < 0) {
                    showSearchBar.value = false
                    keyboardController?.hide()
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
        var dC = mainAppScreenViewModel.dominantColor.value
        if(mainAppScreenViewModel.isColorCloseToWhite(dC)){
            dC = dC.copy(alpha = 0.25f)
        }
        if (!showSearchBar.value) {
            Column(
                modifier = Modifier
                    .height(150.dp)
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                dC,
                                Color.Black
                            )
                        )
                    )
            ) {
                Row {
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
                            text = "Liked Songs",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontFamily = SpotifyBold
                        )
                    }
                    Spacer(Modifier.width(160.dp))
                    Column(
                        modifier = Modifier
                            .padding(top = 60.dp, end = 20.dp),
                    ) {
                        IconButton(
                            onClick = {
                                fileExplorerViewModel.showSearchBar.value = true
                            }
                        ) {
                            Image(
                                modifier = Modifier.size(20.dp),
                                painter = painterResource(id = R.drawable.search_icon_unselected),
                                contentDescription = "Search in local files",
                                colorFilter = ColorFilter.tint(Color.White)
                            )
                        }
                    }
                }
            }
        }
        else {
            Column(
                modifier = Modifier
                    .height(130.dp)
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                dC,
                                Color.Black
                            )
                        )
                    )
            ) {
            }
        }

        // LazyColumn with song list
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 140.dp)
        ) {
            // No Music Found
            item{
                if(list.size == 0){
                    NoMusicFoundInLocalStorageScreen()
                }
            }

            // songs list
            items(list.size) { index ->
                var url:String? = null
                val song = list[index]
                LaunchedEffect(Unit) {
                    coroutineScope.launch{
                        url = AudioFetcher().fetchAudioStreamUrl_newpipe(
                            artist = song.artist,
                            songName = song.name
                        )
                        while(url != null){
                            delay(500)
                        }
                    }
                }
                val musicFile = MusicFile(
                    name = song.name,
                    id = song.id,
                    artist = song.artist,
                    album = song.album,
                    duration = song.duration,
                    filePath = null,
                    coverArtUri = song.coverArtUri,
                    source = song.source,
                    streamableURL = null
                )
                SongItemComposable(
                    musicFile,
                    playListScreenViewModel,
                    mainAppScreenViewModel,
                    fileExplorerViewModel
                )
            }

            // Bottom Space
            item{
                Spacer(Modifier.height(
                    expandableDrawerViewModel.collapsedHeightDp + windowInfo.screenHeight * 0.15f))
            }
        }

        // Up Arrow
        AnimatedVisibility(
            visible = isScrolledDown,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            var bottomPadding = 120.dp
            val nowPlaying = mainAppScreenViewModel.nowPlaying.collectAsState()
            if(nowPlaying.value != null){
                bottomPadding = 200.dp
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 320.dp, bottom = bottomPadding)
                    .background(Color.Transparent),
                contentAlignment = Alignment.BottomCenter
            ){
                IconButton(
                    modifier = Modifier
                        .padding(),
                    onClick = {
                        coroutineScope.launch {
                            listState.scrollToItem(0)
                        }
                    }){
                    Image(
                        painter = painterResource(id = R.drawable.go_to_top_icon),
                        contentDescription = "Go to Top",
                        modifier = Modifier
                            .size(25.dp),
                        colorFilter = ColorFilter.tint(Color.White)
                    )
                }
            }
        }

        // Floating search bar
        AnimatedVisibility(
            visible = showSearchBar.value,
            enter = slideInVertically(),
            exit = slideOutVertically() + fadeOut(),
            modifier = Modifier
                .padding(horizontal = 15.dp, vertical = 15.dp)
        ) {
            Column() {
                Spacer(Modifier.height(50.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(Modifier.width(10.dp))
                        Box() {
                            Image(
                                painter = painterResource(id = R.drawable.search_icon_unselected),
                                contentDescription = "Cancel Search Button",
                                modifier = Modifier
                                    .padding(start = 10.dp)
                                    .size(20.dp)
                            )
                        }
                        Box() {
                            TextField(
                                textStyle = TextStyle(
                                    fontSize = 16.sp,                         // Main input text size
                                    fontFamily = SpotifyBold,        // Main input font
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black                       // Just in case
                                ),
                                value = searchQuery.value,
                                onValueChange = { searchQuery.value = it },
                                placeholder = { Text("Search in Local Storage") },
                                modifier = Modifier
                                    .width(290.dp)
                                    .focusRequester(focusRequester),
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
                        Box() {
                            IconButton(
                                modifier = Modifier
                                    .padding(start = 10.dp),
                                onClick = {
                                    searchQuery.value = ""
                                    showSearchBar.value = false
                                    keyboardController?.hide()
                                }) {
                                Image(
                                    painter = painterResource(id = R.drawable.swipe_up_icon),
                                    contentDescription = "Cancel Search Button",
                                    modifier = Modifier
                                        .size(20.dp)
                                )
                            }
                        }
                    }

                    LaunchedEffect(searchQuery.value) {
                        fileExplorerViewModel.search()
                    }

                    LaunchedEffect(showSearchBar.value) {
                        if (showSearchBar.value) {
                            delay(100) // Let search bar fully compose
                            focusRequester.requestFocus()
                            keyboardController?.show()
                        }
                    }


                }
            }
        }

    }

}





