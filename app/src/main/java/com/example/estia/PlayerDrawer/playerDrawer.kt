package com.example.estia.PlayerDrawer

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.IconButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.estia.MainAppScreen.MainAppScreenViewModel
import com.example.estia.MusicFile
import com.example.estia.R
import com.example.estia.SpotifyBold
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.navigation.NavController
import com.example.estia.AccountScreen.MainScreen.AccountScreenViewModel
import com.example.estia.AccountScreen.LocalFilesScreen.FileExplorerViewModel
import com.example.estia.PlayListScreen.PlayListScreenViewModel
import com.example.estia.PlaybackController
import com.example.estia.WindowInfo
import kotlin.Long
import kotlin.toString

@Composable
fun playerDrawer(
    playListViewModel: PlayListScreenViewModel,
    mainAppScreenViewModel: MainAppScreenViewModel,
    expandableDrawerViewModel: PlayerDrawerViewModel,
    navController: NavController,
    fileExplorerViewModel: FileExplorerViewModel,
    accountScreenViewModel: AccountScreenViewModel,
    windowInfo: WindowInfo
) {

    val nowPlaying by mainAppScreenViewModel.nowPlaying.collectAsState()
    val dominantColor = mainAppScreenViewModel.dominantColor.value

    expandableDrawerViewModel.setContentResolverAndInitDB(LocalContext.current)

    val density = LocalDensity.current

    val totalHeight = LocalWindowInfo.current.containerSize.height.dp
    val expandedHeight = totalHeight
    val collapsedHeight = expandedHeight * 0.03f
    expandableDrawerViewModel.setExpandedAndCollapsedHeight(
        expandedHeight,
        collapsedHeight
    )

    LaunchedEffect(Unit) {
        expandableDrawerViewModel.initializeHeights(density)
    }

    val heightPx = expandableDrawerViewModel.heightPx
    val animatedHeightDp by animateDpAsState(
        targetValue = with(density) { heightPx.toDp() },
        label = "drawerHeight"
    )

    val scope = rememberCoroutineScope()
    val draggableState = rememberDraggableState { delta ->
        expandableDrawerViewModel.handleDrag(delta)
    }

    var startPadding = 10.dp
    var endPadding = 10.dp
    var bottomPadding = windowInfo.screenHeight * 0.15f
    if(expandableDrawerViewModel.isExpanded){
        startPadding = 0.dp
        endPadding = 0.dp
        bottomPadding = 0.dp
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                bottom = bottomPadding
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(animatedHeightDp)
                .padding(start = startPadding, end = endPadding)
                .draggable(
                    orientation = Orientation.Vertical,
                    state = draggableState,
                    onDragStopped = {
                        scope.launch {
                            scope.launch { expandableDrawerViewModel.handleDragStopped() }
                        }
                    }
                )
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    scope.launch { expandableDrawerViewModel.expandOnClick() }
                },
            colors = CardDefaults.cardColors(containerColor = Color.Black)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ){
                if (nowPlaying == null) {

                } else {
                    PlaybackController.onNext = {
                        val nextSong = playListViewModel.getNextSongFromMainQueue()
                        if (nextSong != null) {
                            mainAppScreenViewModel.setNowPlaying(nextSong)
                            mainAppScreenViewModel.play()
                        }
                    }

                    PlaybackController.onPrevious = {
                        val previousSong = playListViewModel.revertToPreviousSong()
                        if (previousSong != null) {
                            mainAppScreenViewModel.setNowPlaying(previousSong)
                            mainAppScreenViewModel.play()
                        } else {
                            mainAppScreenViewModel.setProgress(0)
                        }
                    }
                    if (mainAppScreenViewModel.isExpandedNowPlaying){
                        LargeMusicPlayer(
                            expandableDrawerViewModel = expandableDrawerViewModel,
                            mainAppScreenViewModel = mainAppScreenViewModel,
                            nowPlaying = nowPlaying,
                            dominantColor = dominantColor,
                            playListViewModel = playListViewModel,
                            playNext = PlaybackController.onNext!!,
                            playPrevious = PlaybackController.onPrevious!!,
                            navController = navController,
                            fileExplorerViewModel = fileExplorerViewModel,
                            accountScreenViewModel = accountScreenViewModel,
                            windowInfo = windowInfo
                        )
                    }
                    else{
                        SmallMusicPlayer(
                            playNext = PlaybackController.onNext!!,
                            playPrevious = PlaybackController.onPrevious!!,
                            mainAppScreenViewModel = mainAppScreenViewModel,
                            nowPlaying = nowPlaying,
                            dominantColor = dominantColor,
                            windowInfo = windowInfo
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(heightPx) {
        mainAppScreenViewModel.isExpandedNowPlaying = expandableDrawerViewModel.isExpanded
    }

}

@Composable
fun SmallMusicPlayer(
    playNext: () -> Unit,
    playPrevious: () -> Unit,
    mainAppScreenViewModel: MainAppScreenViewModel,
    nowPlaying : MusicFile?,
    dominantColor: Color,
    windowInfo: WindowInfo,
){

    var name = nowPlaying?.name!!
    var artist = nowPlaying.artist
    val nameColor = if (dominantColor.luminance() > 0.5f) Color.Black else Color.White
    if (name.length > 20) { name = name.take(20) + "..." }
    if(artist.toString().length > 25) { artist = artist.toString().take(25) + "..." }
    if(artist == "<unknown>") { artist = "Unknown Artist" }

    var currentPosition = mainAppScreenViewModel.currentPosition.value
    var duration = mainAppScreenViewModel.nowPlaying.value?.duration

    var progress = 0f
    if(duration != null){
        progress = (currentPosition.toFloat() / duration.toFloat()) * 200
        if(currentPosition >= duration.toLong()){
            playNext()
        }
    }

    val threshold = 100f
    var offsetX by remember { mutableStateOf(0f) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(mainAppScreenViewModel.dominantColor.value)
            .offset { IntOffset(offsetX.toInt(), 0) }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, dragAmount ->
                        offsetX += dragAmount
                    },
                    onDragEnd = {
                        if (offsetX > threshold) playPrevious()
                        else if (offsetX < -threshold) playNext()
                        offsetX = 0f // reset
                    }
                )
            }
    ){
        Column(
            modifier = Modifier
                .height(70.dp)
                .width(70.dp)
        ){
            val coverArt = nowPlaying.coverArtUri

            if (coverArt == null) {
                Image(
                    painter = painterResource(id = R.drawable.music_icon_compressed),
                    contentDescription = "Cover Art",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 5.dp, vertical = 5.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            } else {
                AsyncImage(
                    model = coverArt,
                    contentDescription = "Default Cover Art",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 5.dp, vertical = 5.dp)
                            .clip(RoundedCornerShape(12.dp))
                )
//                val bitmap = remember(coverArt) {
//                    BitmapFactory.decodeFile(File(coverArt).absolutePath)
//                }
//                bitmap?.let{
//                    Image(
//                        bitmap = bitmap.asImageBitmap(),
//                        contentDescription = "Default Cover Art",
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .padding(horizontal = 5.dp, vertical = 5.dp)
//                            .clip(RoundedCornerShape(12.dp))
//                    )
//                }
            }
        }
        Spacer(Modifier.width(10.dp))
        Column(
            modifier = Modifier
                .width(200.dp)
                .padding(vertical = 10.dp)
        ) {
            Text(
                text = name,
                fontSize = 14.sp,
                fontFamily = SpotifyBold,
                color = nameColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp) // control line height
            )
            Text(
                text = artist.toString(),
                fontSize = 10.sp,
                fontFamily = SpotifyBold,
                color = Color.Gray,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Column(
            ){
                Box(
                    modifier = Modifier
                        .height(3.dp)
                        .fillMaxWidth()
                        .background(Color.Gray, shape = RoundedCornerShape(1.dp))
                ) {
                    if(!mainAppScreenViewModel.isLoadingSongURL.value){
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(progress.dp)
                                .background(Color.White, shape = RoundedCornerShape(1.dp))
                        )
                    }
                    else{
                        SmallSeekBarPlaceholder()
                    }
                }
            }
        }
        Spacer(Modifier.width(50.dp))
        Column(
            modifier = Modifier.fillMaxHeight(), // or fillMaxSize()
            verticalArrangement = Arrangement.Center,
        ) {
            val isPaused by mainAppScreenViewModel.isPaused.collectAsState()


            var iconId = R.drawable.play_icon
            if (!isPaused){
                iconId = R.drawable.pause_icon
            }
            else{
                iconId = R.drawable.play_icon
            }
            IconButton(
                onClick = {
                    // your action
                    if (isPaused) {
                        mainAppScreenViewModel.resume()
                    } else {
                        mainAppScreenViewModel.pause()
                    }
                }
            ) {
                Image(
                    painter = painterResource(id = iconId),
                    contentDescription = "Pause Button",
                    modifier = Modifier.size(30.dp),
                    colorFilter = ColorFilter.tint(nameColor)
                )
            }
        }
    }
}

@Composable
fun LargeMusicPlayer(
    playNext: () -> Unit,
    playPrevious: () -> Unit,
    playListViewModel: PlayListScreenViewModel,
    expandableDrawerViewModel: PlayerDrawerViewModel,
    mainAppScreenViewModel: MainAppScreenViewModel,
    fileExplorerViewModel: FileExplorerViewModel,
    nowPlaying: MusicFile?,
    dominantColor: Color,
    navController: NavController,
    accountScreenViewModel: AccountScreenViewModel,
    windowInfo: WindowInfo
) {

    val colorList = listOf(
        dominantColor,
        dominantColor.copy(alpha = 0.8f),
        dominantColor.copy(alpha = 0.6f),
        dominantColor.copy(alpha = 0.4f),
        dominantColor.copy(alpha = 0.2f),
    )

    var name = nowPlaying?.name ?: ""
    var artist = nowPlaying?.artist ?: ""

    if (name.length > 15) { name = name.take(15) + "..." }
    if(artist.toString().length > 20) { artist = artist.toString().take(20) + "..." }

    if (artist == "<unknown>") artist = "Unknown Artist"

    val nameColor = if (dominantColor.luminance() > 0.5f) Color.Black else Color.White
    val artistColor = if (dominantColor.luminance() > 0.5f) Color(0xFFA9A9A9) else Color.Gray

    var currentPosition = mainAppScreenViewModel.currentPosition.longValue
    var duration : Long = mainAppScreenViewModel.nowPlaying.value?.duration ?: 0L

    val listState = rememberLazyListState()

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                val isAtTop = listState.firstVisibleItemIndex == 0 &&
                        listState.firstVisibleItemScrollOffset == 0

                if (available.y > 0 && isAtTop && expandableDrawerViewModel.isExpanded) {
                    // Stick to finger during drag
                    expandableDrawerViewModel.handleDrag(available.y)
                    return Offset.Zero
                }

                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                // If drag ended slowly (no fling), snap
                expandableDrawerViewModel.handleDragStopped()
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                // If user flings, snap to closest state
                expandableDrawerViewModel.handleDragStopped()
                return Velocity.Zero
            }
        }
    }

    Box(
        modifier = Modifier
            .nestedScroll(nestedScrollConnection)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item{
                Column(
                    modifier = Modifier
                        .background(
                            brush = Brush.verticalGradient(
                                colors = colorList
                            )
                        )
                        .padding(start = 25.dp)
                ){
                    Spacer(Modifier.height(50.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        // Close icon aligned to start
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    expandableDrawerViewModel.collapse()
                                }
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.drop_down_icon),
                                contentDescription = "Close Now Playing Drawer",
                                modifier = Modifier.fillMaxSize(),
                                colorFilter = ColorFilter.tint(Color.White)
                            )
                        }

                        // Centered text
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Now Playing",
                                fontSize = 16.sp,
                                fontFamily = SpotifyBold,
                                color = nameColor,
                            )
                            nowPlaying?.source?.let { source ->
                                Text(
                                    text = "From $source",
                                    fontSize = 14.sp,
                                    fontFamily = SpotifyBold,
                                    color = nameColor.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))

                    Column(
                        modifier = Modifier
                            .height(360.dp)
                            .width(360.dp)
                            .clip(RoundedCornerShape(10.dp)),
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
                    Spacer(Modifier.height(10.dp))

                    Column(
                        modifier = Modifier
                            .width(360.dp),
                    ) {
                        Row() {
                            Column(
                                modifier = Modifier
                                    .width(200.dp)
                                    .clipToBounds()
                            ) {
                                Text(
                                    maxLines = 1,
                                    text = name,
                                    fontSize = 20.sp,
                                    fontFamily = SpotifyBold,
                                    color = nameColor,
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    maxLines = 1,
                                    text = artist,
                                    fontSize = 16.sp,
                                    fontFamily = SpotifyBold,
                                    color = artistColor,
                                )
                            }

                            // More Icons
                        }
                    }
                    Spacer(Modifier.height(10.dp))

                    if (mainAppScreenViewModel.isLoadingSongURL.value) {
                        LargeSeekBarPlaceholder()
                    } else {
                        SeekBar(
                            currentPosition = currentPosition,
                            duration = duration,
                            onSeek = { newPosition ->
                                mainAppScreenViewModel.setProgress(newPosition)
                            },
                            playNext = playNext
                        )
                    }

                    Row(
                        modifier = Modifier
                            .width(360.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = mainAppScreenViewModel.formatDuration(currentPosition),
                            fontSize = 12.sp,
                            fontFamily = SpotifyBold,
                            color = nameColor
                        )
                        Text(
                            text = nowPlaying?.duration?.let {
                                mainAppScreenViewModel.formatDuration(it)
                            } ?: "0:00",
                            fontSize = 12.sp,
                            fontFamily = SpotifyBold,
                            color = nameColor
                        )
                    }

                    Column(
                        modifier = Modifier
                            .width(360.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val isPaused by mainAppScreenViewModel.isPaused.collectAsState()

                        val iconId = if (isPaused) {
                            R.drawable.play_icon_large
                        } else {
                            R.drawable.pause_icon_large
                        }
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                enabled = !mainAppScreenViewModel.isLoadingSongURL.value,
                                onClick = {
                                    expandableDrawerViewModel.libraryManagerDialogShown.value = true
//                                mainAppScreenViewModel.downloadToDB()
//                                fileExplorerViewModel.loadMusicFiles()
                                }
                            ) {
                                if (!mainAppScreenViewModel.isLoadingSongURL.value) {
                                    Image(
                                        painter = painterResource(id = R.drawable.add_to_list_icon),
                                        contentDescription = "Download Song Button",
                                        modifier = Modifier.size(50.dp),
                                        colorFilter = ColorFilter.tint(nameColor)
                                    )
                                } else {
                                    Spacer(Modifier.size(30.dp))
                                }
                            }

                            IconButton(
                                modifier = Modifier.size(100.dp),
                                onClick = {
                                    val currentPositionMs =
                                        mainAppScreenViewModel.currentPosition.longValue
                                    if (currentPositionMs > 5000) {
                                        // If more than 5 seconds in, just restart the current track
                                        mainAppScreenViewModel.setProgress(0)
                                    } else {
                                        // Otherwise, go to the previous song from history
                                        playPrevious()
                                    }
                                }

                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.play_previous_icon),
                                    contentDescription = "Pause Button",
                                    modifier = Modifier.size(45.dp),
                                    colorFilter = ColorFilter.tint(nameColor)
                                )
                            }
                            IconButton(
                                onClick = {
                                    if (isPaused) {
                                        mainAppScreenViewModel.resume()
                                    } else {
                                        mainAppScreenViewModel.pause()
                                    }
                                }
                            ) {
                                Image(
                                    painter = painterResource(id = iconId),
                                    contentDescription = "Pause Button",
                                    modifier = Modifier.size(65.dp),
                                    colorFilter = ColorFilter.tint(nameColor)
                                )
                            }
                            IconButton(
                                modifier = Modifier.size(100.dp),
                                onClick = {
                                    playNext()
                                }
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.play_next_icon),
                                    contentDescription = "Pause Button",
                                    modifier = Modifier.size(45.dp),
                                    colorFilter = ColorFilter.tint(nameColor)
                                )
                            }

                            IconButton(
                                onClick = {
                                    mainAppScreenViewModel.changeScreen("playListIcon")
                                    expandableDrawerViewModel.collapse()
                                }
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.play_queue_icon),
                                    contentDescription = "Play Queue",
                                    modifier = Modifier.size(35.dp),
                                    colorFilter = ColorFilter.tint(nameColor)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(50.dp))
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .background(dominantColor.copy(alpha = 0.2f))
                        .padding(start = 25.dp)
                        .fillMaxWidth()
                ){
                    LyricsScreen(
                        nameColor = nameColor,
                        viewModel = expandableDrawerViewModel,
                        nowPlaying = nowPlaying,
                        mainAppScreenViewModel
                    )
                    Spacer(Modifier.height(50.dp))
                }
            }
        }
    }
}

@Composable
fun SeekBar(
    playNext : () -> Unit,
    currentPosition: Long,
    duration: Long,
    onSeek: (Long) -> Unit
) {
    val barWidthDp = 360.dp
    val dotSize = 10.dp
    val density = LocalDensity.current
    val barWidthPx = with(density) { barWidthDp.toPx() }

    var dragOffsetPx by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    val progressFraction = when {
        duration > 0 && isDragging -> dragOffsetPx / barWidthPx
        duration > 0 -> currentPosition.toFloat() / duration
        else -> 0f
    }

    if(currentPosition >= duration){
        playNext()
    }

    // Sync dragOffset with current position when not dragging
    LaunchedEffect(currentPosition, duration, isDragging) {
        if (!isDragging && duration > 0) {
            dragOffsetPx = (currentPosition.toFloat() / duration) * barWidthPx
        }
    }

    Box(
        modifier = Modifier
            .width(barWidthDp)
            .padding(vertical = 8.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val tappedFraction = (offset.x / barWidthPx).coerceIn(0f, 1f)
                    val position = (tappedFraction * duration).toLong()
                    dragOffsetPx = offset.x
                    onSeek(position)
                }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        // Background track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(Color.Gray, shape = RoundedCornerShape(50))
        )

        // Progress fill
        Box(
            modifier = Modifier
                .width(with(density) { (barWidthPx * progressFraction).toDp() })
                .height(3.dp)
                .background(Color.White, shape = RoundedCornerShape(50))
        )

        // Thumb (dot)
        Box(
            modifier = Modifier
                .offset {
                    val offset = if (isDragging) dragOffsetPx else (barWidthPx * progressFraction)
                    IntOffset((offset - dotSize.toPx() / 2).toInt(), 0)
                }
                .size(dotSize)
                .background(Color.White, shape = CircleShape)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            isDragging = true
                        },
                        onDragEnd = {
                            val fraction = dragOffsetPx / barWidthPx
                            val position = (fraction * duration).toLong()
                            dragOffsetPx = (position.toFloat() / duration) * barWidthPx
                            isDragging = false
                            onSeek(position)
                        },
                        onDragCancel = {
                            isDragging = false
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            dragOffsetPx = (dragOffsetPx + dragAmount.x).coerceIn(0f, barWidthPx)
                        }
                    )
                }
        )
    }
}

@Composable
fun LyricsScreen(
    nameColor : Color,
    viewModel: PlayerDrawerViewModel,
    nowPlaying: MusicFile?,
    mainAppScreenViewModel: MainAppScreenViewModel
) {
    val lyrics by remember { derivedStateOf { viewModel.lyrics } }
    val isLoading by remember { derivedStateOf { viewModel.isLyricsLoading } }
    val error by remember { derivedStateOf { viewModel.lyricsError } }

    var bgColor = mainAppScreenViewModel.dominantColor.value
    if(mainAppScreenViewModel.dominantColor.value.alpha > 0.7){
        bgColor = mainAppScreenViewModel.dominantColor.value.copy(alpha = 0.7f)
    }

    Card(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .height(460.dp)
            .width(360.dp),
        colors = CardDefaults.cardColors(bgColor)
    ){
        Row{
            Text(
                text = "Lyrics",
                fontSize = 24.sp,
                fontFamily = SpotifyBold,
                color = nameColor,
                modifier = Modifier
                    .padding(16.dp)
                    .width(260.dp)
            )
            IconButton(
                onClick = {
                    viewModel.refreshLyrics(
                        nowPlaying?.artist ?: "",
                        nowPlaying?.name ?: ""
                    )
                }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.refresh_icon),
                    contentDescription = "Close Button",
                    modifier = Modifier
                        .size(30.dp)
                        .padding(top = 6.dp),
                    colorFilter = ColorFilter.tint(nameColor)
                )
            }
        }


        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            error != null -> {
                Text(
                    text = "Error: $error",
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp)
                )
            }

            lyrics.isNotEmpty() -> {
                val lines = lyrics
                    .split("\n")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }

                Box(
                    modifier = Modifier.fillMaxSize(),
                ){
                    LazyColumn(modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .nestedScroll(remember { object : NestedScrollConnection {} })
                    ) {
                        items(lines.size) { index ->
                            if (lines[index] == ":-:") {
                                Spacer(modifier = Modifier.height(18.dp)) // creates visual space
                            } else {
                                Text(
                                    text = lines[index],
                                    fontSize = 15.sp,
                                    fontFamily = SpotifyBold,
                                    color = Color.White,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            else -> {
                Text("No lyrics found.", modifier = Modifier.padding(16.dp))
            }
        }
    }

    LaunchedEffect(nowPlaying) {
        viewModel.fetchLyrics(
            nowPlaying?.artist ?: "",
            nowPlaying?.name ?: ""
        )
    }

}

@Composable
fun SmallSeekBarPlaceholder(modifier: Modifier = Modifier) {
    val shimmerWidth = 80.dp
    val shimmerColor = Color.White.copy(alpha = 0.6f)
    val trackColor = Color.Gray.copy(alpha = 0.5f)

    val density = LocalDensity.current
    var barWidthPx by remember { mutableStateOf(0f) }

    val infiniteTransition = rememberInfiniteTransition()
    val offsetX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = barWidthPx,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = modifier
            .onGloballyPositioned {
                barWidthPx = it.size.width.toFloat()
            }
            .clip(RoundedCornerShape(50))
            .background(trackColor)
            .height(3.dp)
            .fillMaxWidth()
    ) {
        val shimmerOffset1 = (offsetX % barWidthPx)
        val shimmerOffset2 = (shimmerOffset1 - barWidthPx)

        listOf(shimmerOffset1, shimmerOffset2).forEach { offset ->
            Box(
                modifier = Modifier
                    .offset { IntOffset(offset.toInt(), 0) }
                    .width(shimmerWidth)
                    .fillMaxHeight()
                    .background(shimmerColor, shape = RoundedCornerShape(50))
            )
        }
    }
}

@Composable
fun LargeSeekBarPlaceholder() {
    val shimmerWidth = 80.dp
    val shimmerColor = Color.White.copy(alpha = 0.6f)
    val trackColor = Color.Gray.copy(alpha = 0.5f)

    val density = LocalDensity.current
    var barWidthPx by remember { mutableStateOf(0f) }

    val infiniteTransition = rememberInfiniteTransition()
    val offsetX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = barWidthPx,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .onGloballyPositioned {
                barWidthPx = it.size.width.toFloat()
            }
            .clip(RoundedCornerShape(50))
            .background(trackColor)
            .height(3.dp)
            .width(360.dp)
    ) {
        val shimmerOffset1 = (offsetX % barWidthPx)
        val shimmerOffset2 = (shimmerOffset1 - barWidthPx)

        listOf(shimmerOffset1, shimmerOffset2).forEach { offset ->
            Box(
                modifier = Modifier
                    .offset { IntOffset(offset.toInt(), 0) }
                    .width(shimmerWidth)
                    .fillMaxHeight()
                    .background(shimmerColor, shape = RoundedCornerShape(50))
            )
        }
    }
}
