package com.example.estia.AccountScreen.CreatePlayListScreen

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.estia.AccountScreen.AccountScreenRouter
import com.example.estia.AccountScreen.ImportScreen.ytPlayListLinkInput
import com.example.estia.AccountScreen.MainScreen.AccountScreenViewModel
import com.example.estia.MainAppScreen.MainAppScreenViewModel
import com.example.estia.PlayerDrawer.PlayerDrawerViewModel
import com.example.estia.R
import com.example.estia.SpotifyBold
import kotlinx.coroutines.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePlayListScreen(
    expandableDrawerViewModel: PlayerDrawerViewModel,
    accountScreenViewModel: AccountScreenViewModel,
    mainAppScreenViewModel: MainAppScreenViewModel,
    navController: NavController
) {
    val textColor = Color.White
    val spotifyGreen = Color(0xFF1DB954)

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(20.dp)
    ) {
        Spacer(Modifier.height(50.dp))
        // Header with back button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(R.drawable.back_icon),
                contentDescription = "Go Back",
                modifier = Modifier
                    .size(24.dp)
                    .clickable { navController.popBackStack() },
                colorFilter = ColorFilter.tint(textColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Create Playlist",
                fontFamily = SpotifyBold,
                fontSize = 22.sp,
                color = textColor
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Playlist Name Input
            item {

                OutlinedTextField(
                    value = accountScreenViewModel.toCreatePLayListName.value,
                    onValueChange = { accountScreenViewModel.toCreatePLayListName.value = it },
                    placeholder = { Text("Playlist Name", color = Color.Gray) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(color = textColor),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = spotifyGreen,
                        unfocusedBorderColor = Color.Gray,
                        cursorColor = spotifyGreen
                    )
                )
            }

            // Playlist Description Input
            item {
                OutlinedTextField(
                    value = accountScreenViewModel.toCreatePLayListDescription.value,
                    onValueChange = { accountScreenViewModel.toCreatePLayListDescription.value = it },
                    placeholder = { Text("Description (optional)", color = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    textStyle = LocalTextStyle.current.copy(color = textColor),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = spotifyGreen,
                        unfocusedBorderColor = Color.Gray,
                        cursorColor = spotifyGreen
                    )
                )
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }

            item {
                // Create Playlist Button
                Button(
                    enabled = accountScreenViewModel.toCreatePLayListName.value.isNotEmpty(),
                    onClick = {
                        accountScreenViewModel.addPlaylistToDB()
                        navController.popBackStack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = spotifyGreen,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Create Playlist",
                        fontSize = 18.sp,
                        fontFamily = SpotifyBold
                    )
                }
            }
        }
    }
}
