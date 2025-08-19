package com.example.estia.SearchScreen.AlbumDisplayScreen

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
import com.example.estia.SearchScreen.MainScreen.SearchScreenViewModel
import com.example.estia.SearchScreen.MainScreen.SongItemComposable
import com.example.estia.SpotifyBold

@Composable
fun AlbumScreenDisplay(
    mainAppScreenViewModel: MainAppScreenViewModel,
    navController: NavController,
    searchScreenViewModel: SearchScreenViewModel,
    playListScreenViewModel: PlayListScreenViewModel,
    expandableDrawerViewModel: PlayerDrawerViewModel,
    windowInfo: com.example.estia.WindowInfo
){

    val album = mainAppScreenViewModel.selectedAlbum.value

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
                            text = album?.title.toString(),
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
                        model = album?.cover_xl,
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

                Row(modifier = Modifier.fillMaxWidth()){
                    val releaseDate = mainAppScreenViewModel.selectedAlbumDetails.value?.release_date.toString()
                    Text(
                        modifier = Modifier
                            .padding(top = 10.dp, bottom = 10.dp,start = 20.dp),
                        text = "Album  -  Released ${releaseDate}",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontFamily = SpotifyBold
                    )
                }
            }
        }

        if(mainAppScreenViewModel.isLoadingSelectedAlbumDetails.value){
            item{ CircularProgressIndicator() }
        }
        else{
            val count = mainAppScreenViewModel.selectedAlbumDetails.value?.tracks?.data?.size ?: 0
            items(count){ it->
                val musicFile = mainAppScreenViewModel.selectedAlbumDetails.value?.tracks?.data[it]
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
        }

        // Bottom Space
        item{
            Spacer(Modifier.height(
                expandableDrawerViewModel.collapsedHeightDp + windowInfo.screenHeight * 0.15f))
        }
    }
}