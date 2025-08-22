package com.example.estia.PlayerDrawer.LibraryManagerDialog

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.estia.AccountScreen.DownloadedSongsScreen.PercentageCircularProgress
import com.example.estia.AccountScreen.MainScreen.AccountScreenViewModel
import com.example.estia.MainAppScreen.MainAppScreenViewModel
import com.example.estia.MusicFile
import com.example.estia.PlayerDrawer.PlayerDrawerViewModel
import com.example.estia.R
import com.example.estia.SpotifyBold

@Composable
fun LibraryManagerDialog(
    expandableDrawerViewModel: PlayerDrawerViewModel,
    accountScreenViewModel: AccountScreenViewModel,
    mainAppScreenViewModel: MainAppScreenViewModel
){
    val nowPlaying = mainAppScreenViewModel.nowPlaying.collectAsState().value
    val context = LocalContext.current

    if(nowPlaying != null){
        LaunchedEffect(nowPlaying) {
            if (nowPlaying.coverArtUri != null) {
                val path = mainAppScreenViewModel.copyImageToInternalStorage(
                    context,
                    nowPlaying.coverArtUri!!,
                    nowPlaying.id
                )
                accountScreenViewModel.setNowPlaying(nowPlaying.copy(coverArtUri = path))
            }
            else{
                accountScreenViewModel.setNowPlaying(nowPlaying)
            }
        }

        val textColor = Color.White

        Column(
            Modifier
                .fillMaxSize()
                .background(Color.Black)
        )
        {
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
                                    text = "Manage Your Library",
                                    fontFamily = SpotifyBold,
                                    color = textColor
                                )
                            }
                            Column(
                                Modifier.padding(start = 20.dp)
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.drop_down_icon),
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clickable() {
                                            expandableDrawerViewModel.libraryManagerDialogShown.value =
                                                false
                                        },
                                    contentDescription = "Drop Down Button",
                                    colorFilter = ColorFilter.tint(textColor)
                                )
                            }
                        }
                    }

                    if (nowPlaying.source != "Local Storage") {
                        // Liked Songs
                        Column(Modifier.padding(top = 15.dp, bottom = 15.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .padding(horizontal = 10.dp)
                                    .height(40.dp)
                            )
                            {
                                Image(
                                    painter = painterResource(R.drawable.liked_song_image),
                                    contentDescription = "Liked Songs Image",
                                    modifier = Modifier.size(35.dp)
                                )
                                Text(
                                    modifier = Modifier.padding(start = 10.dp),
                                    fontSize = 15.sp,
                                    text = "Liked Songs",
                                    fontFamily = SpotifyBold,
                                    color = textColor
                                )


                                if (accountScreenViewModel.isLikedSong.value) {
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
                                                    accountScreenViewModel.removeSongFromLikedSongs()
                                                }
                                                .size(20.dp)
                                        )
                                    }
                                } else {
                                    Column(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(end = 10.dp),
                                        horizontalAlignment = Alignment.End
                                    ) {
                                        Image(
                                            colorFilter = ColorFilter.tint(textColor),
                                            painter = painterResource(R.drawable.empty_circle_icon),
                                            contentDescription = "Empty Circle",
                                            modifier = Modifier
                                                .clickable() {
                                                    accountScreenViewModel.addSongToLikedSongs()
                                                }
                                                .size(20.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Estia Downloads
                        Column(Modifier.padding(top = 15.dp, bottom = 15.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .padding(horizontal = 10.dp)
                                    .height(40.dp)
                            )
                            {
                                Image(
                                    painter = painterResource(R.drawable.donwload_image_bg),
                                    contentDescription = "Download image back ground",
                                    modifier = Modifier.size(35.dp)
                                )
                                Text(
                                    modifier = Modifier.padding(start = 10.dp),
                                    fontSize = 15.sp,
                                    text = "Estia Downloads",
                                    fontFamily = SpotifyBold,
                                    color = textColor
                                )


//                                val isCD = accountScreenViewModel.downloadIdToMusicFileIDMap
//                                    .any { it.value == nowPlaying.id }
//                                val progress = accountScreenViewModel
//                                    .musicFileIDMapToDownloadProgress[nowPlaying.id] ?: 0
//                                if(isCD){
//                                    Column(
//                                        Modifier
//                                            .size(20.dp)
//                                            .fillMaxWidth()
//                                            .padding(end = 10.dp),
//                                        horizontalAlignment = Alignment.End
//                                    ) {
//                                        PercentageCircularProgress(progress)
//                                    }
//                                }
                                 if (accountScreenViewModel.isDownloadedSong.value) {
                                    Column(
                                        Modifier
                                            .clickable() {
                                                accountScreenViewModel.removeSongFromEstiaDownloads()
                                            }
                                            .fillMaxWidth()
                                            .padding(end = 10.dp),
                                        horizontalAlignment = Alignment.End
                                    ) {
                                        Image(
                                            painter = painterResource(R.drawable.tick_icon),
                                            contentDescription = "Liked Songs Image",
                                            modifier = Modifier
                                                .size(20.dp)
                                        )
                                    }
                                } else {
                                    Column(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(end = 10.dp),
                                        horizontalAlignment = Alignment.End
                                    ) {
                                        Image(
                                            colorFilter = ColorFilter.tint(textColor),
                                            painter = painterResource(R.drawable.empty_circle_icon),
                                            contentDescription = "Empty Circle",
                                            modifier = Modifier
                                                .clickable() {
                                                    accountScreenViewModel.addSongToEstiaDownloads()
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
        }
    }
}

