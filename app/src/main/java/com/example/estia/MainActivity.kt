package com.example.estia

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.util.TableInfo
import coil.compose.AsyncImage
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.estia.AccountScreen.MainScreen.AccountScreenViewModel
import com.example.estia.AccountScreen.LocalFilesScreen.FileExplorerViewModel
import com.example.estia.LoginScreen.LoginScreen
import com.example.estia.MainAppScreen.MainAppScreen
import com.example.estia.MainAppScreen.MainAppScreenViewModel
import com.example.estia.PlayListScreen.PlayListScreenViewModel
import com.example.estia.PlayerDrawer.PlayerDrawerViewModel
import com.example.estia.PlayerDrawer.playerDrawer
import com.example.estia.SearchScreen.DeepSearch.DeepSearchScreenViewModel
import com.example.estia.downloader.DownloaderObject
import kotlinx.coroutines.launch
import org.schabi.newpipe.extractor.NewPipe
import kotlin.math.exp

@Composable
fun LibraryManagerDialog(
    expandableDrawerViewModel: PlayerDrawerViewModel,
    accountScreenViewModel: AccountScreenViewModel,
    mainAppScreenViewModel: MainAppScreenViewModel,
    nowPlaying: MusicFile
){
    accountScreenViewModel.setNowPlaying(nowPlaying)

    Box(
        Modifier
            .fillMaxSize()
            .padding(top = 200.dp, start = 40.dp, end = 40.dp, bottom = 200.dp),
    ){
        LazyColumn(
            Modifier
                .clip(RoundedCornerShape(30.dp))
                .background(Color.Gray)
                .background(Color.White.copy(alpha = 0.6f))
                .fillMaxSize(),
        ) {

            item{
                // Main Top Text
                Column(
                    Modifier
                        .padding(top = 15.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        modifier = Modifier,
                        fontSize = 20.sp,
                        text = "Manage Your Library",
                        fontFamily = SpotifyBold,
                        color = Color.Black
                    )
                }

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
                            color = Color.Black
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
                        }
                        else {
                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(end = 10.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Image(
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
                            color = Color.Black
                        )

                        if (false) {
                            Column(
                                Modifier
                                    .clickable() {

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
                                    painter = painterResource(R.drawable.empty_circle_icon),
                                    contentDescription = "Empty Circle",
                                    modifier = Modifier
                                        .clickable() {

                                        }
                                        .size(20.dp)
                                )
                            }
                        }
                    }
                }

                // Done Button
                Column(
                    Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable() {
                                expandableDrawerViewModel.libraryManagerDialogShown.value = false
                            }
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Green.copy(alpha = 0.85f))
                            .width(100.dp)
                            .height(30.dp)
                    ) {
                        Text(
                            fontSize = 20.sp,
                            text = "Done",
                            fontFamily = SpotifyBold,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SetSystemBarsColor(color: Color = Color.Black) {
    val view = LocalView.current
    val window = (view.context as Activity).window

    DisposableEffect(Unit) {
        window.statusBarColor = color.toArgb()
        window.navigationBarColor = color.toArgb()

        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }

        onDispose { }
    }
}

@Composable
fun Main(
    fileExplorerViewModel: FileExplorerViewModel,
)
{
    val windowInfo = RememberWindowInfo()

    val expandableDrawerViewModel = viewModel<PlayerDrawerViewModel>()
    val mainAppScreenViewModel : MainAppScreenViewModel = viewModel()
    val playListScreenViewModel : PlayListScreenViewModel = viewModel()
    val accountScreenViewModel: AccountScreenViewModel = viewModel()
    val deepSearchScreenViewModel: DeepSearchScreenViewModel = viewModel()

    accountScreenViewModel.initializeContextAndDB(LocalContext.current)

    SetSystemBarsColor(Color.Transparent) // Set system bars color

    val nowPlaying by mainAppScreenViewModel.nowPlaying.collectAsState()

    val navController = rememberNavController() // screen rendering controller

    // start Screen navigation
    NavHost(navController = navController, startDestination = ScreenRouter.mainAppScreen, builder = {
        // Screens to navigate for navigationController
        composable(ScreenRouter.loginScreen, content = { LoginScreen(navController) })

        composable(
            ScreenRouter.mainAppScreen,
            content = {
                    MainAppScreen(
                        mainAppScreenViewModel,
                        navController,
                        fileExplorerViewModel,
                        expandableDrawerViewModel,
                        playListScreenViewModel,
                        accountScreenViewModel,
                        windowInfo,
                        deepSearchScreenViewModel = deepSearchScreenViewModel,
                    )
            }
        )
    })

    if(nowPlaying != null){
        playerDrawer(
            playListScreenViewModel,
            mainAppScreenViewModel,
            expandableDrawerViewModel = expandableDrawerViewModel,
            navController = navController,
            fileExplorerViewModel = fileExplorerViewModel,
            accountScreenViewModel = accountScreenViewModel,
            windowInfo
        )
    }

    if(expandableDrawerViewModel.libraryManagerDialogShown.value && nowPlaying != null){
        LibraryManagerDialog(
            expandableDrawerViewModel,
            accountScreenViewModel,
            mainAppScreenViewModel,
            nowPlaying!!
        )
    }
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_MyApp)
        super.onCreate(savedInstanceState)

        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        val ctx = this
        lifecycleScope.launch{ DownloaderObject.initialize(ctx) }

        DownloaderImpl.init(null)
        NewPipe.init(DownloaderImpl.getInstance())

        enableEdgeToEdge()
        setContent {
            val fileExplorerViewModel = viewModel<FileExplorerViewModel>()
            fileExplorerViewModel.setContentResolverAndInitDB(contentResolver, context = this)
            fileExplorerViewModel.loadMusicFiles()
            Main(fileExplorerViewModel)
        }
    }

}





