package com.example.estia

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
import com.example.estia.downloader.DownloaderObject
import kotlinx.coroutines.launch
import org.schabi.newpipe.extractor.NewPipe

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

    SetSystemBarsColor(Color.Transparent) // Set system bars color

    val nowPlaying by mainAppScreenViewModel.nowPlaying.collectAsState()

    val navController = rememberNavController() // screen rendering controller

    // start Screen navigation
    NavHost(navController = navController, startDestination = ScreenRouter.mainAppScreen, builder = {
        // Screens to navigate for navigationController
        composable(ScreenRouter.loginScreen, content = { LoginScreen(navController) })

        composable(
            ScreenRouter.mainAppScreen,
            content = { MainAppScreen(
                mainAppScreenViewModel,
                navController,
                fileExplorerViewModel,
                expandableDrawerViewModel,
                playListScreenViewModel,
                accountScreenViewModel,
                windowInfo
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

    if(expandableDrawerViewModel.libraryManagerDialogShown.value){
        LibraryManagerDialog(
            expandableDrawerViewModel,
            accountScreenViewModel,
            mainAppScreenViewModel
        )
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
fun LibraryManagerDialog(
    expandableDrawerViewModel: PlayerDrawerViewModel,
    accountScreenViewModel: AccountScreenViewModel,
    mainAppScreenViewModel: MainAppScreenViewModel
){
    Box(
        Modifier
            .fillMaxSize()
            .padding(top = 200.dp, start = 40.dp, end = 40.dp, bottom = 200.dp),
    ){
        Card(
            Modifier.background(Color.Transparent)
                .clip(RoundedCornerShape(30.dp))
                .fillMaxSize(),
            colors = CardDefaults.cardColors(mainAppScreenViewModel.dominantColor.value)
        ) {
        }
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





