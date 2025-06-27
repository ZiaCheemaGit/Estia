package com.example.estia.MainAppScreen

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.IconButton
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.example.estia.FileExplorerScreen.FileExplorerViewModel
import com.example.estia.FileExplorerScreen.RenderFileExplorerScreen
import com.example.estia.PlayListScreen.PlayListScreenViewModel
import com.example.estia.R
import com.example.estia.SpotifyBold
import com.example.estia.PlayerDrawer.playerDrawer
import com.example.estia.PlayListScreen.RenderPlayListScreen
import com.example.estia.PlayerDrawer.PlayerDrawerViewModel
import com.example.estia.SearchScreen.RenderSearchScreen
import com.example.estia.SearchScreen.SearchScreenViewModel
import kotlin.math.exp

@Composable
fun MainAppScreen(
    navController: NavController,
    fileExplorerViewModel: FileExplorerViewModel) {

    val expandableDrawerViewModel = viewModel<PlayerDrawerViewModel>()
        
    RequestMediaPlaybackPermission()

    val mainAppScreenViewModel = viewModel<MainAppScreenViewModel>()
    val playListScreenViewModel : PlayListScreenViewModel = viewModel()
    val searchScreenViewModel : SearchScreenViewModel = viewModel()

    mainAppScreenViewModel.setContextandDB(LocalContext.current)
    mainAppScreenViewModel.initService(LocalContext.current)
    mainAppScreenViewModel.loadPlayBackState()

    val nowPlaying by mainAppScreenViewModel.nowPlaying.collectAsState()

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
        content = { innerPadding ->
            Box(){
                when (mainAppScreenViewModel.currentScreen) {
                    "ExploreScreen" -> RenderExploreScreen(mainAppScreenViewModel)

                    "SearchScreen" -> RenderSearchScreen(
                        innerPadding,
                        searchScreenViewModel,
                        mainAppScreenViewModel = mainAppScreenViewModel
                    )

                    "FileExplorerScreen" ->
                        RenderFileExplorerScreen(
                            playListScreenViewModel,
                            mainAppScreenViewModel,
                            fileExplorerViewModel,
                            innerPadding,
                            expandableDrawerViewModel = expandableDrawerViewModel
                        )

                    "AccountScreen" -> RenderAccountScreen(mainAppScreenViewModel)

                    "SettingsScreen" -> RenderSettingsScreen(mainAppScreenViewModel)

                    "PlayListScreen" -> RenderPlayListScreen(
                        fileExplorerViewModel,
                        expandableDrawerViewModel,
                        innerPadding,
                        playListScreenViewModel,
                        mainAppScreenViewModel)

                }
                if(nowPlaying != null){
                    playerDrawer(
                        playListScreenViewModel,
                        mainAppScreenViewModel,
                        innerPadding = innerPadding,
                        
                    )
                }

                val list by fileExplorerViewModel.permanentAllSongsList.collectAsState()

                LaunchedEffect(list) {
                    playListScreenViewModel.setLocalStorageQueue(fileExplorerViewModel.permanentAllSongsList.value)
                }

                LaunchedEffect(nowPlaying) {
                    playListScreenViewModel.setNowPlaying(nowPlaying)
                }
            }
        }
    )
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


@Composable
fun bt(mainAppScreenViewModel: MainAppScreenViewModel){
    Card(
        modifier = Modifier
            .height(60.dp)
            .fillMaxWidth()
            .padding(top = 300.dp),
        colors = CardDefaults.cardColors(Color.Blue)
    ){
        val nowPlaying by mainAppScreenViewModel.nowPlaying.collectAsState()
        if(nowPlaying?.coverArtUri == null){
            Image(
                painter = painterResource(id = R.drawable.music_logo),
                contentDescription = "Simple Music Icon",
                modifier = Modifier.size(55.dp),
            )
        }
        else{
            AsyncImage(
                model = nowPlaying?.coverArtUri,
                contentDescription = "Simple Music Icon",
                modifier = Modifier.size(55.dp),
            )
        }

    }
}


@Composable
fun RequestMediaPlaybackPermission() {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
        }
        else {
            Toast.makeText(context, "Playback permission denied. Music may not work in background.", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            launcher.launch(Manifest.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK)
        }
    }
}







