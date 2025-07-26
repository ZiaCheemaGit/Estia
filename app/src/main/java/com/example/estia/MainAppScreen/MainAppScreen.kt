package com.example.estia.MainAppScreen

import RenderAccountScreen
import android.Manifest
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.IconButton
import androidx.compose.ui.graphics.ColorFilter
import com.example.estia.AccountScreen.AccountScreenViewModel
import com.example.estia.FileExplorerScreen.FileExplorerViewModel
import com.example.estia.FileExplorerScreen.RenderFileExplorerScreen
import com.example.estia.HomeScreen.HomeScreenViewModel
import com.example.estia.PlayListScreen.PlayListScreenViewModel
import com.example.estia.PlayerDrawer.playerDrawer
import com.example.estia.PlayListScreen.RenderPlayListScreen
import com.example.estia.PlayerDrawer.PlayerDrawerViewModel
import com.example.estia.SearchScreen.RenderSearchScreen
import com.example.estia.SearchScreen.SearchScreenViewModel
import com.example.estia.HomeScreen.RenderExploreScreen

@Composable
fun MainAppScreen(
    mainAppScreenViewModel: MainAppScreenViewModel,
    navController: NavController,
    fileExplorerViewModel: FileExplorerViewModel,
) {
    val expandableDrawerViewModel = viewModel<PlayerDrawerViewModel>()
        
    RequestMediaPlaybackPermission()

    val accountScreenViewModel: AccountScreenViewModel = viewModel()
    val playListScreenViewModel : PlayListScreenViewModel = viewModel()
    val searchScreenViewModel : SearchScreenViewModel = viewModel()
    val playerDrawerViewModel : PlayerDrawerViewModel = viewModel()
    val homeScreenViewModel: HomeScreenViewModel = viewModel()

    mainAppScreenViewModel.setContextandDB(LocalContext.current)
    mainAppScreenViewModel.initService(LocalContext.current)
    mainAppScreenViewModel.loadPlayBackState()

    searchScreenViewModel.initializeDataBAse(LocalContext.current)

    val nowPlaying by mainAppScreenViewModel.nowPlaying.collectAsState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        containerColor = Color.Black,
        bottomBar = {
            TransparentBottomBar(mainAppScreenViewModel){
                    screen ->
                mainAppScreenViewModel.changeScreen(screen)
            }
        },
        content = { innerPadding ->
            Box(){
                when (mainAppScreenViewModel.currentScreen) {

                    "ExploreScreen" -> RenderExploreScreen(
                        homeScreenViewModel,
                        mainAppScreenViewModel,
                        innerPadding
                    )

                    "SearchScreen" -> RenderSearchScreen(
                        innerPadding,
                        searchScreenViewModel,
                        mainAppScreenViewModel = mainAppScreenViewModel,
                        playListScreenViewModel
                    )

                    "FileExplorerScreen" ->
                        RenderFileExplorerScreen(
                            playListScreenViewModel,
                            mainAppScreenViewModel,
                            fileExplorerViewModel,
                            innerPadding,
                            expandableDrawerViewModel = expandableDrawerViewModel
                        )

                    "AccountScreen" -> RenderAccountScreen(
                        innerPadding,
                        mainAppScreenViewModel,
                        accountScreenViewModel
                    )

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
                        expandableDrawerViewModel = playerDrawerViewModel,
                        navController = navController,
                        fileExplorerViewModel = fileExplorerViewModel
                    )
                }

                val list by fileExplorerViewModel.permanentAllSongsList.collectAsState()

                LaunchedEffect(list) {
                    playListScreenViewModel.setLocalStorageQueue(fileExplorerViewModel.permanentAllSongsList.value)
                }

                LaunchedEffect(nowPlaying) {
                    playListScreenViewModel.setNowPlaying(nowPlaying)
                }

                LaunchedEffect(mainAppScreenViewModel.currentScreen) {
                    fileExplorerViewModel.showSearchBar.value = false
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







