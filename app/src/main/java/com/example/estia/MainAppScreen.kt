package com.example.estia

import android.Manifest
import coil.compose.AsyncImage
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.launch
import android.content.Context
import android.content.Intent
import android.hardware.camera2.params.BlackLevelPattern
import android.net.Uri
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.Alignment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.PermissionStatus
import android.provider.Settings
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.luminance
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(navController: NavController, fileExplorerViewModel: FileExplorerViewModel) {
    val mainAppScreenViewModel = viewModel<MainAppScreenViewModel>()

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        containerColor = Color.Black,
        topBar = {
            TransparentTopAppBar(mainAppScreenViewModel){
                    screen ->
                mainAppScreenViewModel.changeScreen(screen)
            }
        },
        bottomBar = {
            TransparentBottomBar(mainAppScreenViewModel){
                    screen ->
                mainAppScreenViewModel.changeScreen(screen)
            }
        },
        content = { innerPadding -> innerPadding
            Box(){
                when (mainAppScreenViewModel.currentScreen) {
                    "ExploreScreen" -> RenderExploreScreen(mainAppScreenViewModel)
                    "SearchScreen" -> RenderSearchScreen(mainAppScreenViewModel)
                    "FileExplorerScreen" ->
                        RenderFileExplorerScreen(
                            mainAppScreenViewModel,
                            fileExplorerViewModel)
                    "AccountScreen" -> RenderAccountScreen(mainAppScreenViewModel)
                    "SettingsScreen" -> RenderSettingsScreen(mainAppScreenViewModel)
                    "PlayListScreen" -> RenderPlayListScreen(mainAppScreenViewModel)
                }
                ExpandableSwipeableDrawer(mainAppScreenViewModel, fileExplorerViewModel)
            }
        }
    )
}

@Composable
fun ExpandableSwipeableDrawer(
    mainAppScreenViewModel: MainAppScreenViewModel,
    fileExplorerViewModel: FileExplorerViewModel
) {
    val nowPlaying by fileExplorerViewModel.nowPlaying.collectAsState()
    val dominantColor by fileExplorerViewModel.dominantColor.collectAsState()

    val collapsedHeight = 180.dp
    val expandedHeight = 770.dp

    val density = LocalDensity.current
    val collapsedHeightPx = with(density) { collapsedHeight.toPx() }
    val expandedHeightPx = with(density) { expandedHeight.toPx() }

    var heightPx by remember { mutableStateOf(collapsedHeightPx) }

    val scope = rememberCoroutineScope()
    val animatedHeightDp by animateDpAsState(
        targetValue = with(density) { heightPx.toDp() },
        label = "drawerHeight"
    )

    val thresholdDistance = (expandedHeightPx - collapsedHeightPx) * 0.05f
    var wasExpanded by remember { mutableStateOf(false) }

    val draggableState = rememberDraggableState { delta ->
        heightPx = (heightPx - delta).coerceIn(collapsedHeightPx, expandedHeightPx)
        wasExpanded = heightPx > (collapsedHeightPx + expandedHeightPx) / 2  // Approximate last state
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(animatedHeightDp)
                .padding(bottom = 110.dp, start = 12.dp, end = 12.dp)
                .draggable(orientation = Orientation.Vertical,
                    state = draggableState,
                    onDragStopped = {
                        scope.launch {
                            heightPx = if (wasExpanded) {
                                // From expanded: collapse if dragged down enough
                                if (expandedHeightPx - heightPx > thresholdDistance)
                                    collapsedHeightPx
                                else
                                    expandedHeightPx
                            } else {
                                // From collapsed: expand if dragged up enough
                                if (heightPx - collapsedHeightPx > thresholdDistance)
                                    expandedHeightPx
                                else
                                    collapsedHeightPx
                            }
                        }
                    }
                ),
            colors = CardDefaults.cardColors(containerColor = Color.Black)
        ) {
            var list : List<Color> = listOf(
                dominantColor,
                dominantColor
            )
            if (mainAppScreenViewModel.isExpandedNowPlaying) {
                list = listOf(
                    dominantColor.copy(alpha = 1.2f),
                    dominantColor.copy(alpha = 1.1f),
                    dominantColor,
                    dominantColor.copy(alpha = 0.9f),
                    dominantColor.copy(alpha = 0.8f),
                    dominantColor.copy(alpha = 0.7f),
                    dominantColor.copy(alpha = 0.6f),
                    dominantColor.copy(alpha = 0.5f),
                    dominantColor.copy(alpha = 0.4f),
                    dominantColor.copy(alpha = 0.3f),
                    dominantColor.copy(alpha = 0.2f),
                    dominantColor.copy(alpha = 0.1f),
                    dominantColor.copy(alpha = 0.05f),
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = list
                        )
                    ),
            ){
                if (nowPlaying == null) {
                    Row() {
                        Text(
                            modifier = Modifier.padding(
                                horizontal = 16.dp, vertical = 16.dp
                            ),
                            fontFamily = SpotifyBold,
                            text = "Nothing Selected To Play",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                } else {
                    if (mainAppScreenViewModel.isExpandedNowPlaying) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = list
                                    )
                                ),
                            contentAlignment = Alignment.TopCenter
                        ){
                            Image(
                                painter = painterResource(id = R.drawable.thick_line_icon),
                                contentDescription = "Click and drag from this point",
                                modifier = Modifier
                                    .size(25.dp),
                                colorFilter = ColorFilter.tint(Color.Gray)
                            )
                        }
                        LargeMusicPlayer(mainAppScreenViewModel, nowPlaying, dominantColor)
                    } else {
                        SmallMusicPlayer(nowPlaying, dominantColor)
                    }
                }
            }
        }
    }
    LaunchedEffect(heightPx) {
        mainAppScreenViewModel.isExpandedNowPlaying = heightPx >= (expandedHeightPx * 0.9f)
    }
}


@Composable
fun RenderPlayListScreen(mainAppScreenViewModel : MainAppScreenViewModel){
    LazyColumn(
    ) {
        items(100) { index ->
            Text(
                text = "PlayListScreenSettings ScreenSettings ScreeSettingsScreen" +
                        "SettingsScreenSettingsScreenettingsScreen",
                modifier = Modifier.padding(10.dp),
                color = Color.White
            )
        }
    }
}

@Composable
fun RenderSettingsScreen(mainAppScreenViewModel : MainAppScreenViewModel){
    LazyColumn(
    ) {
        items(100) { index ->
            Text(
                text = "Settings Screen     Settings Screen   Settings Screen   Settings Screen" +
                        "Settings Screen   Settings Screen    Settings Screen",
                modifier = Modifier.padding(16.dp),
                color = Color.White
            )
        }
    }
}

@Composable
fun RenderAccountScreen(mainAppScreenViewModel : MainAppScreenViewModel) {
    LazyColumn(
    ) {
        items(100) { index ->
            Text(
                text = "Account Screen Account Screen Account Screen  Account Screen" +
                        "Account Screen Account Screen Account Screen",
                modifier = Modifier.padding(16.dp),
                color = Color.White
            )
        }
    }
}

@Composable
fun RenderSearchScreen(mainAppScreenViewModel : MainAppScreenViewModel) {
    LazyColumn(
    ) {
        items(100) { index ->
            Text(
                text = "Search Screen  Search Screen  Search Screen   Search Screen Search Screen" +
                        "Search Screen Search Screen   Search Screen",
                modifier = Modifier.padding(16.dp),
                color = Color.White
            )
        }
    }
}

@Composable
fun RenderExploreScreen(mainAppScreenViewModel : MainAppScreenViewModel){
    LazyColumn(
    ) {
        items(100) { index ->
            Text(
                text = "Explore Screen     Explore Screen   Explore Screen   Explore Screen" +
                        "Explore Screen   Explore Screen    Explore Screen",
                modifier = Modifier.padding(16.dp),
                color = Color.White
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransparentTopAppBar(mainAppScreenViewModel: MainAppScreenViewModel, screenToShow : (String) -> Unit) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Black.copy(alpha = 0.7f), // Semi-transparent
            titleContentColor = Color.White,
            actionIconContentColor = Color.White
        ),
        title = {
            Row(horizontalArrangement = Arrangement.Center) {
                Image(
                    painter = painterResource(id = R.drawable.main_logo),
                    contentDescription = "Simple Music Icon",
                    modifier = Modifier.size(45.dp),
                    colorFilter = ColorFilter.tint(Color.White)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    modifier = Modifier.padding(9.dp),
                    text = "ESTIA",
                    fontFamily = SpotifyBold,
                    color = Color.White,
                    fontSize = MaterialTheme.typography.titleLarge.fontSize,
                )
                Spacer(modifier = Modifier.width(130.dp))
                mainAppScreenViewModel.unselectedTopBarIcons.forEach {
                    icon ->
                    IconButton(
                        onClick = {
                            mainAppScreenViewModel.selectedIcon = icon.key
                            screenToShow(icon.key) }
                    ) {
                        val iconId = if (mainAppScreenViewModel.selectedIcon == icon.key) {
                            mainAppScreenViewModel.selectedTopBarIcons[icon.key] ?: icon.value
                        } else {
                            icon.value
                        }
                        Image(


                            painter = painterResource(id = iconId),
                            contentDescription = "Home",
                            modifier = Modifier.size(24.dp),
                            colorFilter = ColorFilter.tint(Color.White)
                        )
                    }
                    Spacer(modifier = Modifier.width(15.dp))
                }
            }
        },
        modifier = Modifier.background(
            Brush.verticalGradient(
                colors = listOf(
                    Color.Black.copy(alpha = 0.7f),
                    Color.Transparent
                )
            )
        )
    )
}

@Composable
fun TransparentBottomBar(mainAppScreenViewModel: MainAppScreenViewModel, screenToShow : (String) -> Unit) {
    BottomAppBar(
        modifier = Modifier
            .height(110.dp)
            .background(Color.Black.copy(alpha = 0.6f)),
        containerColor = Color.Transparent,
        content = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                mainAppScreenViewModel.unselectedBottomBarIcons.forEach {
                    icon ->
                    IconButton(
                        onClick = {
                            mainAppScreenViewModel.selectedIcon = icon.key
                            screenToShow(icon.key) }
                    ) {
                        val iconId = if (mainAppScreenViewModel.selectedIcon == icon.key) {
                            mainAppScreenViewModel.selectedBottomBarIcons[icon.key] ?: icon.value
                        } else {
                            icon.value
                        }
                        Image(
                            painter = painterResource(id = iconId),
                            contentDescription = "Home",
                            modifier = Modifier.size(24.dp),
                            colorFilter = ColorFilter.tint(Color.White)
                        )
                    }
                }

            }
        }
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RenderFileExplorerScreen(
    mainAppScreenViewModel: MainAppScreenViewModel,
    fileExplorerViewModel: FileExplorerViewModel
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
                    MusicListView(list, listState, fileExplorerViewModel)
                }
            }
        }

        is PermissionStatus.Denied -> {
            if (permissionStatus.shouldShowRationale) {
                NotPermanetnlyDeniedPermissionRationaleDialog(
                    onConfirmClick = { mediaPermissionState.launchPermissionRequest() },
                    onDismissClick = {
                        mainAppScreenViewModel.selectedIcon = "exploreIcon"
                        mainAppScreenViewModel.currentScreen = "ExploreScreen"
                    }
                )
            } else {
                val context = LocalContext.current
                PermanetnlyDeniedPermissionRationaleDialog(
                    onConfirmClick = {
                        openAppSettings(context)
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
fun MusicListView(
    list : List<MusicFile>,
    listState : LazyListState,
    fileExplorerViewModel : FileExplorerViewModel
        ){
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 15.dp, end = 15.dp)
    ) {
        items(list.size) { index ->
            var name = list[index].name!!
            var artist = list[index].artist!!

            if (name.length > 45) { name = name.take(45) + "..." }
            if(artist.length > 45) { artist = artist.take(45) + "..." }
            if(artist == "<unknown>") { artist = "Unknown Artist" }

            if (index == 0) Spacer(modifier = Modifier.height(120.dp))

            Column(
                modifier =  Modifier
                    .padding(top = 2.dp, bottom = 2.dp)
                    .height(65.dp)
                    .clickable(
                        onClick = {
                            fileExplorerViewModel.setNowPlaying(list[index])
                        }
                    ),
            ){
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Column(
                        modifier = Modifier
                            .height(50.dp)
                            .width(50.dp)
                    ){
                        val coverArt = list[index].coverArtUri

                        if(coverArt == null)
                        {
                            Image(
                                painter = painterResource(id = R.drawable.music_icon_compressed),
                                contentDescription = "Simple Music Icon",
                                modifier  = Modifier.fillMaxSize()
                            )
                        }
                        else{
                            AsyncImage(
                                model = coverArt,
                                contentDescription = "Cover Art",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(
                    ) {
                        Text(
                            fontSize = 14.sp,
                            fontFamily = SpotifyBold,
                            text = name,
                            color = Color.White,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            fontSize = 12.sp,
                            fontFamily = SpotifyBold,
                            text = artist,
                            color = Color.Gray
                        )
                    }
                }
            }

            if (index == list.size - 1) Spacer(modifier = Modifier.height(175.dp))

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
fun PermanetnlyDeniedPermissionRationaleDialog(
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

fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

@Composable
fun SmallMusicPlayer(nowPlaying : MusicFile?, dominantColor: Color){
    var name = nowPlaying?.name!!
    var artist = nowPlaying?.artist!!

    if (name.length > 30) { name = name.take(30) + "..." }
    if(artist.length > 50) { artist = artist.take(50) + "..." }
    if(artist == "<unknown>") { artist = "Unknown Artist" }
    Row(
    ){
        Column(
        ){
            val coverArt = nowPlaying?.coverArtUri
            if (coverArt == null){
                Image(
                    painter = painterResource(id = R.drawable.music_icon_compressed),
                    contentDescription = "Simple Music Icon",
                    modifier = Modifier
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            }
            else{
                AsyncImage(
                    model = coverArt,
                    contentDescription = "Cover Art",
                    modifier = Modifier
                        .padding(horizontal = 5.dp, vertical = 5.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }
        }
        Spacer(Modifier.width(5.dp))
        Column(
            modifier = Modifier
                .padding(vertical = 10.dp),
        ){
            var nameColor = Color.White
            if (dominantColor.luminance() > 0.5f) {
                nameColor = Color.Black
            }
            Text(
                fontSize = 14.sp,
                fontFamily = SpotifyBold,
                text = name,

                color = nameColor,
                )
            Text(
                fontSize = 10.sp,
                fontFamily = SpotifyBold,
                text = artist,
                color = Color.Gray,
            )
        }
    }
}

@Composable
fun LargeMusicPlayer(
    mainAppScreenViewModel: MainAppScreenViewModel,
    nowPlaying: MusicFile?,
    dominantColor: Color,
    ) {
    var name = nowPlaying?.name ?: ""
    var artist = nowPlaying?.artist ?: ""

    if (name.length > 30) name = name.take(30) + "..."
    if (artist.length > 50) artist = artist.take(50) + "..."
    if (artist == "<unknown>") artist = "Unknown Artist"

    val nameColor = if (dominantColor.luminance() > 0.5f) Color.Black else Color.White

    Box(
        modifier = Modifier
            .padding(top = 30.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp) // height for the icon row
                ) {
                    Text(
                        text = "Now Playing",
                        fontSize = 16.sp,
                        fontFamily = SpotifyBold,
                        color = nameColor,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                Spacer(modifier = Modifier.height(15.dp))
            }
            item {
                Column(
                    modifier = Modifier
                        .height(320.dp)
                        .width(320.dp)
                        .clip(RoundedCornerShape(20.dp)),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val coverArt = nowPlaying?.coverArtUri
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
            }

            item {
                Column(
                    modifier = Modifier.padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = name,
                        fontSize = 16.sp,
                        fontFamily = SpotifyBold,
                        color = nameColor
                    )
                    Text(
                        text = artist,
                        fontSize = 13.sp,
                        fontFamily = SpotifyBold,
                        color = Color.Gray
                    )
                }
            }

        }
    }
}



