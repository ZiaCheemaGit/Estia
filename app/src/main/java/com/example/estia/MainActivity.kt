package com.example.estia

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.estia.FileExplorerScreen.FileExplorerViewModel
import com.example.estia.LoginScreen.LoginScreen
import com.example.estia.MainAppScreen.MainAppScreen
import com.example.estia.MainAppScreen.MainAppScreenViewModel
import com.example.estia.ArtistInfoScreen.ArtistInfoScreen
import com.example.estia.downloader.AndroidDownloader
import com.example.estia.downloader.Downloader
import com.example.estia.downloader.DownloaderObject
import kotlinx.coroutines.launch
import org.schabi.newpipe.extractor.NewPipe

@Composable
fun Main(
    fileExplorerViewModel: FileExplorerViewModel,
)
{
    SetSystemBarsColor(Color.Transparent) // Set system bars color
    val mainAppScreenViewModel : MainAppScreenViewModel = viewModel()
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
                fileExplorerViewModel
            ) })
        composable(
            ScreenRouter.artistInfoScreen,
            content = { ArtistInfoScreen(mainAppScreenViewModel) })
    })
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





