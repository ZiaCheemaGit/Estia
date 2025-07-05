package com.example.estia


import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.RemoteViews
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.estia.FileExplorerScreen.FileExplorerViewModel
import com.example.estia.LoginScreen.LoginScreen
import com.example.estia.MainAppScreen.MainAppScreen

@Composable
fun Main(fileExplorerViewModel: FileExplorerViewModel)
{
    SetSystemBarsColor() // Set system bars color

    val navController = rememberNavController() // screen rendering controller
    var startPoint = ""

    if(false){ // if user logged in then
        startPoint = ScreenRouter.mainAppScreen
    }
    else{ // user not logged in
        startPoint = ScreenRouter.loginScreen
    }

    // start Screen navigation
    NavHost(navController = navController, startDestination = startPoint, builder = {
        // Screens to navigate for navigationController
        composable(ScreenRouter.loginScreen, content = { LoginScreen(navController) })
        composable(
            ScreenRouter.mainAppScreen,
            content = { MainAppScreen(navController, fileExplorerViewModel) })
    })
}

@Composable
fun SetSystemBarsColor() {
    val view = LocalView.current
    val window = (view.context as Activity).window

    DisposableEffect(Unit) {
        window.statusBarColor = Color.Black.toArgb()
        window.navigationBarColor = Color.Black.toArgb()

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
        enableEdgeToEdge()
        setContent {
            val fileExplorerViewModel = viewModel<FileExplorerViewModel>()
            fileExplorerViewModel.setContentResolverAndInitDB(contentResolver, context = this)
            fileExplorerViewModel.loadMusicFiles()
            Main(fileExplorerViewModel)
        }
    }
}






