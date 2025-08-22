package com.example.estia.AccountScreen.LocalFilesScreen

import android.Manifest
import android.os.Build
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.estia.MainAppScreen.MainAppScreenViewModel
import com.example.estia.MusicFile
import com.example.estia.R
import com.example.estia.SpotifyBold
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavController
import com.example.estia.PlayListScreen.PlayListScreenViewModel
import com.example.estia.PlayerDrawer.PlayerDrawerViewModel
import com.example.estia.RememberWindowInfo
import kotlinx.coroutines.delay
import kotlin.math.roundToInt


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RenderFileExplorerScreen(
    playListScreenViewModel: PlayListScreenViewModel,
    mainAppScreenViewModel: MainAppScreenViewModel,
    fileExplorerViewModel: FileExplorerViewModel,
    expandableDrawerViewModel: PlayerDrawerViewModel,
    navController: NavController
) {
    // Choose permission based on Android version
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val mediaPermissionState = rememberPermissionState(permission = permission)

    val listState = rememberLazyListState()
    val list = fileExplorerViewModel.musicList.collectAsStateWithLifecycle(initialValue = listOf()).value
    val scope = rememberCoroutineScope()

    val permissionStatus = mediaPermissionState.status

    val hasRequestedPermission = rememberSaveable { mutableStateOf(false) }

    when (permissionStatus) {
        is PermissionStatus.Granted -> {
            LaunchedEffect(Unit) {
                fileExplorerViewModel.grantPermission()
                scope.launch {
                    fileExplorerViewModel.loadMusicFiles()
                }
            }

            Box(Modifier.fillMaxSize()) {
                if (fileExplorerViewModel.isLoading.value) {
                    LoadingScreen()
                } else {
                    if(list.size != 0) {
                        MusicListView(
                            playListScreenViewModel,
                            fileExplorerViewModel,
                            list,
                            listState,
                            mainAppScreenViewModel,
                            expandableDrawerViewModel,
                            navController)
                    }
                }
            }
        }

        is PermissionStatus.Denied -> {
            if (!hasRequestedPermission.value) {
                LaunchedEffect(Unit) {
                    mediaPermissionState.launchPermissionRequest()
                    hasRequestedPermission.value = true
                }
            }
            else if (permissionStatus.shouldShowRationale) {
                NotPermanetnlyDeniedPermissionRationaleDialog(
                    onConfirmClick = { mediaPermissionState.launchPermissionRequest() },
                    onDismissClick = {
                        mainAppScreenViewModel.selectedIcon = "exploreIcon"
                        mainAppScreenViewModel.currentScreen = "ExploreScreen"
                    }
                )
            } else {
                val context = LocalContext.current
                PermanentnlyDeniedPermissionRationaleDialog(
                    onConfirmClick = {
                        fileExplorerViewModel.openAppSettings(context)
                    },
                    onDismissClick = {
                        mainAppScreenViewModel.selectedIcon = "exploreIcon"
                        mainAppScreenViewModel.currentScreen = "ExploreScreen"
                    },
                )
            }
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                fontFamily = SpotifyBold,
                text = "...LOADING...",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PermanentnlyDeniedPermissionRationaleDialog(
    onDismissClick: () -> Unit,
    onConfirmClick: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissClick,
        title = { Text("Permission Required") },
        text = {
            Text(
                "Audio Files Access is Required to Play Songs on you Local Storage" +
                        "You permanently denied this permission. Please enable it in app settings.\n\n" +
                        "Go to Settings → Apps → Estia → Permissions → Allow Audio Access"
            )
        },
        confirmButton = {
            Button(onClick = onConfirmClick) {
                Text("Open App Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissClick) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun NotPermanetnlyDeniedPermissionRationaleDialog(
    onDismissClick: () -> Unit,
    onConfirmClick: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissClick,
        title = { Text("Permission Required") },
        text = {
            Text(
                "Audio Files Access is Required to Play Songs on you Local Storage"
            )
        },
        confirmButton = {
            Button(onClick = onConfirmClick) {
                Text("Allow")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissClick) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun MusicListView(
    playListScreenViewModel: PlayListScreenViewModel,
    fileExplorerViewModel : FileExplorerViewModel,
    list: List<MusicFile>,
    listState: LazyListState,
    mainAppScreenViewModel: MainAppScreenViewModel,
    expandableDrawerViewModel: PlayerDrawerViewModel,
    navController: NavController
) {
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
                            text = "Local Files",
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
                SongItemComposable(
                    list[index],
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


@Composable
fun SongItemComposable(
    musicFile: MusicFile,
    playListScreenViewModel: PlayListScreenViewModel,
    mainAppScreenViewModel: MainAppScreenViewModel,
    fileExplorerViewModel: FileExplorerViewModel,
) {
    val showSearchBar = fileExplorerViewModel.showSearchBar
    var searchQuery = fileExplorerViewModel.searchQuery

    val swipeOffset = remember { Animatable(0f) }
    val maxOffset = 250f // Max swipe distance to reveal hidden UI
    val dragThreshold = 100f
    val scope = rememberCoroutineScope()

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

    var name = musicFile.name ?: ""
    var artist = musicFile.artist ?: ""

    if (name.length > 45) name = name.take(45) + "..."
    if (artist.length > 45) artist = artist.take(45) + "..."

    Box(
        modifier = Modifier
            .background(Color.Black)
            .fillMaxWidth()
            .padding(start = 15.dp, end = 15.dp)
    ) {
        // Hidden row
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
                        searchQuery.value = ""
                        showSearchBar.value = false
                        mainAppScreenViewModel.setNowPlaying(musicFile)
                    }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(
                        modifier = Modifier
                            .height(50.dp)
                            .width(50.dp)
                    ) {
                        val coverArt = musicFile.coverArtUri
                        val placeholderPainter = painterResource(id = R.drawable.music_icon_compressed)

                        AsyncImage(
                            model = coverArt, // can be null — Coil handles it
                            contentDescription = "Cover Art",
                            modifier = Modifier.fillMaxSize(),
                            placeholder = placeholderPainter,
                            error = placeholderPainter,
                            fallback = placeholderPainter, // shown when model is null
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
fun NoMusicFoundInLocalStorageScreen(){
    Box(modifier = Modifier.height(100.dp)){
        Text("No Music Files Found in your Local Storage", color = Color.White)
        Text("No Music Files Found in your Local Storage", color = Color.White)
        Text("No Music Files Found in your Local Storage", color = Color.White)
        Text("No Music Files Found in your Local Storage", color = Color.White)
        Text("No Music Files Found in your Local Storage", color = Color.White)
    }
}



