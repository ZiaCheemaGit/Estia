package com.example.estia

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.estia.AccountScreen.MainScreen.AccountScreenViewModel
import com.example.estia.AccountScreen.LocalFilesScreen.FileExplorerViewModel
import com.example.estia.PlayerDrawer.LibraryManagerDialog.LibraryManagerDialog
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

    val showDialog = expandableDrawerViewModel.libraryManagerDialogShown.value

    AnimatedVisibility(
        visible = showDialog && nowPlaying != null,
        enter = slideInVertically(
            initialOffsetY = { it }
        ),
        exit = slideOutVertically(
            targetOffsetY = { it }
        )
    ) {
        LibraryManagerDialog(
            expandableDrawerViewModel,
            accountScreenViewModel,
            mainAppScreenViewModel
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





