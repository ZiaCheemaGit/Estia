package com.example.estia.HomeScreen.MainScreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.estia.AccountScreen.LocalFilesScreen.FileExplorerViewModel
import com.example.estia.DeezerAlbum
import com.example.estia.DeezerArtist
import com.example.estia.DeezerPlaylist
import com.example.estia.DeezerService
import com.example.estia.DeezerTrack
import com.example.estia.HomeScreen.HomeScreenRouter
import com.example.estia.MainAppScreen.MainAppScreenViewModel
import com.example.estia.MusicFile
import com.example.estia.PlayListScreen.PlayListScreenViewModel
import com.example.estia.PlayerDrawer.PlayerDrawerViewModel
import com.example.estia.R
import com.example.estia.SearchScreen.ArtistDisplayScreen.ArtistScreenDisplay
import com.example.estia.SearchScreen.MainScreen.SearchScreenViewModel
import com.example.estia.SpotifyBold
import com.example.estia.WindowInfo
import kotlinx.coroutines.launch

@Composable
fun RenderExploreScreen(
    homeScreenViewModel: HomeScreenViewModel,
    mainAppScreenViewModel : MainAppScreenViewModel,
    innerPadding: PaddingValues,
    expandableDrawerViewModel: PlayerDrawerViewModel,
    playListScreenViewModel: PlayListScreenViewModel,
    searchScreenViewModel: SearchScreenViewModel,
    fileExplorerViewModel: FileExplorerViewModel,
    windowInfo: WindowInfo
){
    val homeScreenNavControl = rememberNavController()

    NavHost(navController = homeScreenNavControl, startDestination = HomeScreenRouter.mainScreen){

        composable(HomeScreenRouter.mainScreen) {
            MainScreen(
                homeScreenViewModel,
                mainAppScreenViewModel,
                innerPadding,
                homeScreenNavControl
            )
        }

        composable(HomeScreenRouter.artistDisplayScreen) {
            ArtistScreenDisplay(
                mainAppScreenViewModel,
                homeScreenNavControl,
                searchScreenViewModel,
                playListScreenViewModel,
                expandableDrawerViewModel,
                windowInfo,
            )
        }

    }
}

@Composable
fun MainScreen(
    homeScreenViewModel: HomeScreenViewModel,
    mainAppScreenViewModel : MainAppScreenViewModel,
    innerPadding: PaddingValues,
    navController: NavController
){
    LazyColumn(
    ) {
        item{
            Spacer(Modifier.height(innerPadding.calculateTopPadding() + 10.dp))
        }

        // Songs Charts
        item{
            Column(modifier = Modifier.padding(10.dp)){
                Text(
                    text = "Trending Songs",
                    fontFamily = SpotifyBold,
                    fontSize = 30.sp,
                    color = Color.White,
                )
            }
            LazyRow(Modifier.padding(start = 10.dp, end = 10.dp)) {

                val list = homeScreenViewModel.globalChartsSongs.value
                if(homeScreenViewModel.isLoadingGlobalCharts.value) {
                    item{ CircularProgressIndicator() }
                }
                else if(list.size >= 10){
                    items(10) { it ->
                        val song = list[it]
                        HorizontalSongItem(song, mainAppScreenViewModel)
                        Spacer(Modifier.width(25.dp))
                    }
                }
            }
        }

        item{
            Spacer(Modifier.height(25.dp))
        }

        // Artists Charts
        item{
            Column(modifier = Modifier.padding(10.dp)){
                Text(
                    text = "Trending Artists",
                    fontFamily = SpotifyBold,
                    fontSize = 30.sp,
                    color = Color.White,
                )
            }
            LazyRow(Modifier.padding(start = 10.dp, end = 10.dp)) {

                val list = homeScreenViewModel.globalChartsArtists.value
                if(homeScreenViewModel.isLoadingGlobalCharts.value) {
                    item{ CircularProgressIndicator() }
                }
                else if(list.size >= 10){
                    items(10) { it ->
                        val artist = list[it]
                        HorizontalArtistItem(artist, homeScreenViewModel, navController, mainAppScreenViewModel)
                        Spacer(Modifier.width(25.dp))
                    }
                }
            }
        }

        item{
            Spacer(Modifier.height(25.dp))
        }

        // PlayLists Charts
        item{
            Column(modifier = Modifier.padding(10.dp)){
                Text(
                    text = "Trending PlayLists",
                    fontFamily = SpotifyBold,
                    fontSize = 30.sp,
                    color = Color.White,
                )
            }
            LazyRow(Modifier.padding(start = 10.dp, end = 10.dp)) {
                val list = homeScreenViewModel.globalChartsPlaylists.value
                if(homeScreenViewModel.isLoadingGlobalCharts.value) {
                    item{ CircularProgressIndicator() }
                }
                else if(list.size >= 10){
                    items(10){ it->
                        val playList = list[it]
                        HorizontalPlayListItem(playList)
                        Spacer(Modifier.width(25.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun HorizontalPlayListItem(playList: DeezerPlaylist){
    Column(Modifier.width(120.dp)){
        AsyncImage(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .height(120.dp),
            model = playList.pictureMedium,
            contentDescription = "Album CoverArt",
            placeholder = painterResource(R.drawable.music_logo),
            fallback = painterResource(R.drawable.music_logo)
        )
        Spacer(Modifier.height(10.dp))
        Column() {
            Text(
                playList.title,
                maxLines = 1,
                fontFamily = SpotifyBold,
                fontSize = 13.sp,
                color = Color.White
            )
            Text(
                maxLines = 1,
                text = playList.trackCount.toString() + " Tracks",
                fontFamily = SpotifyBold,
                fontSize = 10.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun HorizontalArtistItem(
    artist: DeezerArtist,
    homeScreenViewModel: HomeScreenViewModel,
    navController: NavController,
    mainAppScreenViewModel: MainAppScreenViewModel
){
    Column(Modifier
        .width(120.dp)
        .clickable() {
            mainAppScreenViewModel.selectedArtist.value = artist
            mainAppScreenViewModel.loadArtistData()
            navController.navigate(HomeScreenRouter.artistDisplayScreen)
        }){
        AsyncImage(
            modifier = Modifier
                .clip(RoundedCornerShape(60.dp))
                .height(120.dp),
            model = artist.picture_medium,
            contentDescription = "Album CoverArt",
            placeholder = painterResource(R.drawable.music_logo),
            fallback = painterResource(R.drawable.music_logo)
        )
        Spacer(Modifier.height(10.dp))
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                artist.name,
                maxLines = 1,
                fontFamily = SpotifyBold,
                fontSize = 13.sp,
                color = Color.White
            )
        }
    }
}

@Composable
fun HorizontalSongItem(mf: DeezerTrack, mainAppScreenViewModel: MainAppScreenViewModel){
    var artists: String = mf.artist.name
    LaunchedEffect(Unit){
        artists = DeezerService.api.getTrackDetails(mf.id).contributors.joinToString(", ")
    }

    Column(Modifier
        .width(120.dp)
        .clickable() {
            mainAppScreenViewModel.setNowPlaying(
                MusicFile(
                    name = mf.title,
                    id = mf.title,
                    artist = artists,
                    album = mf.album.title,
                    duration = mf.duration.toLong(),
                    filePath = null,
                    coverArtUri = mf.album.cover_xl,
                    source = "....",
                    streamableURL = null
                )
            )
        }){
        AsyncImage(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .height(120.dp),
            model = mf.album.cover_medium,
            contentDescription = "Album CoverArt",
            placeholder = painterResource(R.drawable.music_logo),
            fallback = painterResource(R.drawable.music_logo)
        )
        Spacer(Modifier.height(10.dp))
        Column() {
            Text(
                mf.title,
                maxLines = 1,
                fontFamily = SpotifyBold,
                fontSize = 13.sp,
                color = Color.White
            )
            Text(
                maxLines = 1,
                text = mf.artist.name,
                fontFamily = SpotifyBold,
                fontSize = 10.sp,
                color = Color.Gray
            )
        }
    }
}


