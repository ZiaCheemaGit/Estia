package com.example.estia.AccountScreen.YTVideosDisplayScreen

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.room.util.TableInfo
import coil.compose.AsyncImage
import com.example.estia.AccountScreen.MainScreen.AccountScreenViewModel
import com.example.estia.MainAppScreen.MainAppScreenViewModel
import com.example.estia.MusicFile
import com.example.estia.R
import com.example.estia.SearchScreen.DeepSearch.DeepSearchScreenViewModel
import com.example.estia.SearchScreen.MainScreen.SearchScreenViewModel
import com.example.estia.SpotifyBold
import com.example.estia.YTMusicSong
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.acos
import kotlin.math.roundToInt

@Composable
fun YTVideosDisplayScreen(
    accountScreenViewModel: AccountScreenViewModel,
    navController: NavController
){
    LaunchedEffect(Unit) {

    }
    val coroutineScope = rememberCoroutineScope()

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
    )
    {
        val textColor = Color.White
        LazyColumn(
            Modifier
                .background(
                    Color.Black
                )
                .padding(top = 30.dp)
                .fillMaxSize(),
        ) {

            item {
                // Main Top Text
                Column(
                    Modifier
                        .padding(top = 25.dp)
                        .height(40.dp)
                        .fillMaxWidth()
                ) {
                    Box(Modifier.fillMaxSize()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                modifier = Modifier,
                                fontSize = 20.sp,
                                text = "Import Your Music",
                                fontFamily = SpotifyBold,
                                color = textColor
                            )
                        }
                        Column(
                            Modifier.padding(start = 20.dp)
                        ) {
                            Image(
                                painter = painterResource(R.drawable.back_icon),
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable() {
                                        navController.popBackStack()
                                    },
                                contentDescription = "Go Back Button",
                                colorFilter = ColorFilter.tint(textColor)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 2.dp)
                            .clickable() {
                                accountScreenViewModel.showYTInput.value = true
                            }
                    ) {
                        Column(
                            Modifier
                                .padding(horizontal = 15.dp)
                                .background(Color.White)
                                .fillMaxWidth()
                                .height(0.9.dp)

                        ) {}
                    }
                }
            }

            if(accountScreenViewModel.ytParsingError.value){
                item{ Text(
                    "Error Parsing",
                    color = Color.White
                ) }
            }
            else if(accountScreenViewModel.isParsingLoading.value){
                item{ CircularProgressIndicator() }
            }
            else{
                val list = accountScreenViewModel.ytParsedVideoList.value
                item{
                    Row(Modifier.padding(horizontal = 10.dp, vertical = 20.dp)){
                        Column(
                            Modifier
                                .fillMaxWidth(0.8f)
                                .padding(start = 20.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                maxLines = 1,
                                text = accountScreenViewModel.playListTitle.value,
                                color = Color.White,
                                fontFamily = SpotifyBold,
                                fontSize = 18.sp
                            )
                        }
                        if (accountScreenViewModel.importBufferFull()) {
                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(end = 10.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.tick_icon),
                                    contentDescription = "Liked Songs Image",
                                    modifier = Modifier
                                        .clickable() {
                                            accountScreenViewModel.ytImportBuffer.value =
                                                emptyList()
                                        }
                                        .size(20.dp)
                                )
                            }
                        }
                        else {
                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(end = 10.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Image(
                                    colorFilter = ColorFilter.tint(Color.White),
                                    painter = painterResource(R.drawable.empty_circle_icon),
                                    contentDescription = "Empty Circle",
                                    modifier = Modifier
                                        .clickable() {
                                            accountScreenViewModel.ytImportBuffer.value =
                                                accountScreenViewModel.ytParsedVideoList.value
                                        }
                                        .size(20.dp)
                                )
                            }
                        }
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                    ){
                        Column(Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF1DB954))
                            .fillMaxHeight()
                            .width(150.dp)
                            .clickable() {
                                coroutineScope.launch {
                                    accountScreenViewModel.addYTPlaylistToDB()
                                    navController.navigate(navController.graph.startDestinationRoute!!) {
                                        popUpTo(0) { inclusive = true }  // removes everything
                                    }
                                }
                            },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ){
                            Text(
                                text = "Done",
                                color = Color.White,
                                fontFamily = SpotifyBold,
                                fontSize = 15.sp
                            )
                        }
                    }
                    AnimatedVisibility(
                        visible = accountScreenViewModel.isAlreadyImported.value,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically()
                    ){
                        Column(Modifier.padding(20.dp)) {
                            Text(
                                "This PlayList has already been Imported.That is why some songs " +
                                        "might be selected already." +
                                        "Previous Import is cleared from " +
                                        "memory.",
                                fontFamily = SpotifyBold,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }
                    }
                }
                items(list.size) { it ->
                    val song = list[it]
                    ytSongItemComposableSimple(ytMF = song, accountScreenViewModel)
                }
            }
        }
    }
}

@Composable
fun ytSongItemComposableSimple(
    ytMF: YTMusicSong,
    accountScreenViewModel: AccountScreenViewModel
) {
    var name = ytMF.title
    var artist : String = ytMF.artists.joinToString(",")
    val coverArt = ytMF.thumbnailUrl
    if (name.length > 45) name = name.take(45)
    if (artist.length > 45) artist = artist.take(45) + "..."

    Box(
        modifier = Modifier
            .padding(start = 10.dp, end = 10.dp)
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

                    if (accountScreenViewModel.inBuffer(ytMF)) {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(end = 10.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Image(
                                painter = painterResource(R.drawable.tick_icon),
                                contentDescription = "Liked Songs Image",
                                modifier = Modifier
                                    .clickable() {
                                        accountScreenViewModel.removeSongFromBuffer(ytMF)
                                    }
                                    .size(20.dp)
                            )
                        }
                    }
                    else {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(end = 10.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            val context = LocalContext.current
                            Image(
                                colorFilter = ColorFilter.tint(Color.White),
                                painter = painterResource(R.drawable.empty_circle_icon),
                                contentDescription = "Empty Circle",
                                modifier = Modifier
                                    .clickable() {
                                        accountScreenViewModel.ytImportBuffer.value += ytMF
                                    }
                                    .size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}






