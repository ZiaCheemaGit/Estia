package com.example.estia.LoginScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.estia.R
import com.example.estia.ScreenRouter

@Composable
fun LoginScreen(navController: NavController) {
    val loginScreenViewModel = viewModel<LoginScreenViewModel>()

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        containerColor = Color.Black,
        topBar = {
        },
        bottomBar = {
        },
        content = {
            innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                ,
            )
            {
                Spacer(modifier = Modifier.height(200.dp))
                Card(
                    modifier = Modifier
                        .width(380.dp)
                        .height((loginScreenViewModel.socialIcons.size * 190).dp)
                        .align(Alignment.CenterHorizontally)
                        .border(
                            width = 1.dp,
                            color = Color.Gray,
                            shape = RoundedCornerShape(16.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = Color.Black),
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.main_logo),
                        contentDescription = "Same icon as on App ",
                        modifier  = Modifier
                            .fillMaxWidth()
                            .size(100.dp)
                    )

                    Spacer(modifier = Modifier.height(40.dp))
                    loginScreenViewModel.socialIcons.forEach {
                            option ->
                        RenderLoginOption(option.key, option.value, onOptionClicked = {
                                selected ->
                            when(selected){
                                "Google" -> {
                                    // Clicked Google

                                }
                                "Spotify" -> {
                                    // Clicked Spotify

                                }
                            }
                        })
                        Spacer(modifier = Modifier.height(15.dp))
                    }
                    RenderOfflineOption(navController)
                }
            }
        }
    )

}


@Composable
fun RenderLoginOption(option: String, icon: Int, onOptionClicked: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Card(
            modifier = Modifier
                .width(340.dp)
                .height(44.dp)
                .align(Alignment.CenterHorizontally)
                .border(
                    width = 1.dp,
                    color = Color.Gray,
                    shape = RoundedCornerShape(16.dp)
                )
                .clip(RoundedCornerShape(16.dp))
                .clickable(
                    onClick = {
                        onOptionClicked(option)
                    },
                    indication = ripple(
                        bounded = true,
                        color = Color.White,
                    ),
                    interactionSource = remember { MutableInteractionSource() },
                ),
            colors = CardDefaults.cardColors(containerColor = Color.Black),
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Image(
                    painter = painterResource(id = icon),
                    contentDescription = "$option Logo",
                    modifier  = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.width(15.dp))
                Text(
                    color = Color.Gray,
                    text = "Login via $option",
                )
            }
        }
    }
}

@Composable
fun RenderOfflineOption(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Card(
            modifier = Modifier
                .width(340.dp)
                .height(44.dp)
                .align(Alignment.CenterHorizontally)
                .border(
                    width = 1.dp,
                    color = Color.Gray,
                    shape = RoundedCornerShape(16.dp)
                )
                .clip(RoundedCornerShape(16.dp))
                .clickable(
                    onClick = {
                        // Offline Mode
                        // now navigate to MainAppScreen
                        navController.navigate(ScreenRouter.mainAppScreen)
                    },
                    indication = ripple(
                        bounded = true,
                        color = Color.White,
                    ),
                    interactionSource = remember { MutableInteractionSource() },
                ),
            colors = CardDefaults.cardColors(containerColor = Color.Black),
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.music_logo),
                    contentDescription = "Simple Music Icon",
                    modifier  = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.width(15.dp))
                Text(
                    color = Color.Gray,
                    text = "Continue as Guest",
                )
            }
        }
    }
}

