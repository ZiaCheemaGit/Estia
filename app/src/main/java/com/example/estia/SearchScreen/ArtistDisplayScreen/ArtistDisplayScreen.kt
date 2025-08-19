package com.example.estia.SearchScreen.ArtistDisplayScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.estia.MainAppScreen.MainAppScreenViewModel
import com.example.estia.PlayListScreen.PlayListScreenViewModel
import com.example.estia.PlayerDrawer.PlayerDrawerViewModel
import com.example.estia.R
import com.example.estia.SearchScreen.DeezerAlbum
import com.example.estia.SearchScreen.MainScreen.AlbumItemComposable
import com.example.estia.SearchScreen.MainScreen.SearchScreenViewModel
import com.example.estia.SearchScreen.MainScreen.SongItemComposable
import com.example.estia.SpotifyBold
import com.example.estia.WindowInfo

@Composable
fun ArtistScreenDisplay(
    mainAppScreenViewModel: MainAppScreenViewModel,
    navController: NavController,
    searchScreenViewModel: SearchScreenViewModel,
    playListScreenViewModel: PlayListScreenViewModel,
    expandableDrawerViewModel: PlayerDrawerViewModel,
    windowInfo: WindowInfo
){

    val artist = mainAppScreenViewModel.selectedArtist.value

    var dC = mainAppScreenViewModel.dominantColor.value
    if(mainAppScreenViewModel.isColorCloseToWhite(dC)){
        dC = dC.copy(alpha = 0.25f)
    }

    LazyColumn(Modifier.fillMaxSize()){
        item {
            Column(
                Modifier
//                    .background(
//                        brush = Brush.verticalGradient(
//                            colors = listOf(
//                                dC,dC,dC,dC,dC,dC,dC,
//                                Color.Black
//                            )
//                        )
//                    )
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
                    Column() {
                        Text(
                            modifier = Modifier
                                .padding(top = 70.dp, start = 20.dp, bottom = 10.dp),
                            text = artist?.name.toString(),
                            color = Color.White,
                            fontSize = 20.sp,
                            fontFamily = SpotifyBold
                        )
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val placeholderPainter = painterResource(id = R.drawable.music_icon_compressed)

                    AsyncImage(
                        model = artist?.picture_xl,
                        contentDescription = "Cover Art",
                        modifier = Modifier
                            .size(310.dp)
                            .clip(
                                RoundedCornerShape(16.dp)
                            ),
                        placeholder = placeholderPainter,
                        error = placeholderPainter,
                        fallback = placeholderPainter,
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        if(mainAppScreenViewModel.isLoadingSelectedArtistDetails.value){
            item{ CircularProgressIndicator() }
        }
        else{
            val tracksCount = mainAppScreenViewModel.selectedArtistDetails.value?.topTracks?.size ?: 0
            if(tracksCount > 0){
                item {
                    Column() {
                        Text(
                            modifier = Modifier
                                .padding(top = 10.dp, start = 25.dp, bottom = 10.dp),
                            text = "Artist - Most Played Tracks",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontFamily = SpotifyBold
                        )
                    }
                }
            }

            items(tracksCount){ it->
                val musicFile = mainAppScreenViewModel.selectedArtistDetails.value?.topTracks[it]
                if(musicFile != null){
                    Column(Modifier.padding(horizontal = 10.dp)){
                        SongItemComposable(
                            mainAppScreenViewModel,
                            searchScreenViewModel = searchScreenViewModel,
                            musicFile = musicFile,
                            playListScreenViewModel = playListScreenViewModel,
                        )
                    }
                }
            }

            val albumsCount = mainAppScreenViewModel.selectedArtistDetails.value?.albums?.size ?: 0
            if(albumsCount > 0){
                item {
                    Column() {
                        Text(
                            modifier = Modifier
                                .padding(top = 10.dp, start = 25.dp, bottom = 10.dp),
                            text = "Albums",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontFamily = SpotifyBold
                        )
                    }
                }
            }

            items(albumsCount){ it->
                val album = mainAppScreenViewModel.selectedArtistDetails.value?.albums[it]
                Column(Modifier.padding(horizontal = 10.dp)){
                    val albumArtist = album?.artist
                    if(albumArtist != null){
                        AlbumItemComposable(
                            mainAppScreenViewModel,
                            searchScreenViewModel = searchScreenViewModel,
                            album = DeezerAlbum(
                                id = album?.id ?: 0L,
                                title = album?.title.toString(),
                                cover_small = album?.cover_small.toString(),
                                cover_medium = album?.cover_medium.toString(),
                                cover_big = album?.cover_big.toString(),
                                cover_xl = album?.cover_xl.toString(),
                                artist = albumArtist
                            ),
                            navController = navController,
                        )
                    }
                }
            }
        }

        // Bottom Space
        item{
            Spacer(Modifier.height(
                expandableDrawerViewModel.collapsedHeightDp + windowInfo.screenHeight * 0.15f))
        }
    }
}