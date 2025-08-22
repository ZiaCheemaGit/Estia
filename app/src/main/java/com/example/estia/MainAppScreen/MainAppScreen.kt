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
import com.example.estia.AccountScreen.MainScreen.AccountScreenViewModel
import com.example.estia.AccountScreen.LocalFilesScreen.FileExplorerViewModel
import com.example.estia.EstiaDownloadFile
import com.example.estia.HomeScreen.HomeScreenViewModel
import com.example.estia.PlayListScreen.PlayListScreenViewModel
import com.example.estia.PlayerDrawer.PlayerDrawerViewModel
import com.example.estia.SearchScreen.MainScreen.RenderSearchScreen
import com.example.estia.SearchScreen.MainScreen.SearchScreenViewModel
import com.example.estia.HomeScreen.RenderExploreScreen
import com.example.estia.LikedSongFile
import com.example.estia.ScreenRouter
import com.example.estia.SearchScreen.DeepSearch.DeepSearchScreenViewModel
import com.example.estia.WindowInfo

@Composable
fun MainAppScreen(
    mainAppScreenViewModel: MainAppScreenViewModel,
    navController: NavController,
    fileExplorerViewModel: FileExplorerViewModel,
    expandableDrawerViewModel: PlayerDrawerViewModel,
    playListScreenViewModel : PlayListScreenViewModel,
    accountScreenViewModel: AccountScreenViewModel,
    windowInfo: WindowInfo,
    deepSearchScreenViewModel: DeepSearchScreenViewModel,
) {
    RequestMediaPlaybackPermission()

    val searchScreenViewModel : SearchScreenViewModel = viewModel()
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
            TransparentBottomBar(mainAppScreenViewModel, navController, windowInfo){
                    screen ->
                mainAppScreenViewModel.changeScreen(screen)
            }
        },
        content = { innerPadding ->
            Box(){

                when (mainAppScreenViewModel.currentScreen) {

//                    "ExploreScreen" -> RenderExploreScreen(
//                        homeScreenViewModel,
//                        mainAppScreenViewModel,
//                        innerPadding
//                    )

                    "SearchScreen" -> RenderSearchScreen(
                        innerPadding,
                        searchScreenViewModel,
                        mainAppScreenViewModel = mainAppScreenViewModel,
                        playListScreenViewModel,
                        deepSearcScreenViewModel = deepSearchScreenViewModel,
                        expandableDrawerViewModel = expandableDrawerViewModel,
                        windowInfo = windowInfo
                    )

                    "AccountScreen" -> RenderAccountScreen(
                        expandableDrawerViewModel,
                        playListScreenViewModel,
                        innerPadding,
                        mainAppScreenViewModel,
                        fileExplorerViewModel,
                        accountScreenViewModel,
                        navController,
                    )

                }

                LaunchedEffect(fileExplorerViewModel.permanentAllSongsList) {
                    playListScreenViewModel.setLocalStorageQueue(fileExplorerViewModel.permanentAllSongsList.value)
                }

                LaunchedEffect(nowPlaying) {
                    playListScreenViewModel.setNowPlaying(nowPlaying)
                }

                LaunchedEffect(mainAppScreenViewModel.currentScreen) {
                    fileExplorerViewModel.showSearchBar.value = false
                }

                LaunchedEffect(Unit) {
                    accountScreenViewModel.observe()
                }
            }
        }
    )
}

@Composable
fun TransparentBottomBar(
    mainAppScreenViewModel: MainAppScreenViewModel,
    navController: NavController,
    windowInfo: WindowInfo,
    screenToShow : (String) -> Unit
) {
    val bottomAppBarHeight = windowInfo.screenHeight * 0.15f
    val bottomBarIconSize = windowInfo.screenHeight * 0.032f

    BottomAppBar(
        modifier = Modifier
            .height(bottomAppBarHeight)
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
                            if(navController.currentDestination?.route != ScreenRouter.mainAppScreen){
                                navController.popBackStack(navController.graph.startDestinationId, true)
                                navController.navigate(ScreenRouter.mainAppScreen)
                            }
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
                            modifier = Modifier.size(bottomBarIconSize),
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







