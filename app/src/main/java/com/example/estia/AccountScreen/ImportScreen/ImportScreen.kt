package com.example.estia.AccountScreen.ImportScreen

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.estia.AccountScreen.AccountScreenRouter
import com.example.estia.AccountScreen.MainScreen.AccountScreenViewModel
import com.example.estia.MainAppScreen.MainAppScreenViewModel
import com.example.estia.PlayerDrawer.PlayerDrawerViewModel
import com.example.estia.R
import com.example.estia.SpotifyBold
import kotlinx.coroutines.launch

@Composable
fun ImportScreen(
    expandableDrawerViewModel: PlayerDrawerViewModel,
    accountScreenViewModel: AccountScreenViewModel,
    mainAppScreenViewModel: MainAppScreenViewModel,
    navController: NavController
) {
    val textColor = Color.White
    val spotifyGreen = Color(0xFF1DB954)
    val coroutineScope = rememberCoroutineScope()

    if (accountScreenViewModel.showYTInput.value) {
        ytPlayListLinkInput(
            accountScreenViewModel,
            onConfirm = {
                coroutineScope.launch {
                    navController.navigate(AccountScreenRouter.YTVideosDisplayScreenToImport)
                    accountScreenViewModel.showYTInput.value = false
                    accountScreenViewModel.startParsingVideos()
                }
            },
            onDismiss = {
                accountScreenViewModel.showYTInput.value = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Spacer(Modifier.height(50.dp))

        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(R.drawable.back_icon),
                contentDescription = "Go Back",
                modifier = Modifier
                    .size(26.dp)
                    .clickable {
                        navController.popBackStack()
                        accountScreenViewModel.clearParsingVariables()
                    },
                colorFilter = ColorFilter.tint(textColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Import Playlist",
                fontFamily = SpotifyBold,
                fontSize = 22.sp,
                color = textColor
            )
        }

        Spacer(Modifier.height(20.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // YouTube Import Option as a Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clickable { accountScreenViewModel.showYTInput.value = true },
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF121212) // Spotify dark gray
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Icon
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(Color.Black, shape = RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = R.drawable.youtube_icon,
                                contentDescription = "YouTube Icon",
                                modifier = Modifier.size(32.dp),
                                contentScale = ContentScale.Fit
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Texts
                        Column(
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Import from YouTube",
                                fontFamily = SpotifyBold,
                                fontSize = 16.sp,
                                color = textColor
                            )
                            Text(
                                text = "Add your playlists from YouTube",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ytPlayListLinkInput(
    accountScreenViewModel: AccountScreenViewModel,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                Text(
                    text = "Enter YouTube Playlist Link",
                    fontFamily = SpotifyBold,
                    fontSize = 18.sp,
                    color = Color.Black
                )

                // Input field
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = accountScreenViewModel.playListUrl.value,
                    onValueChange = { accountScreenViewModel.playListUrl.value = it },
                    placeholder = { Text("Paste YouTube Link here") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        fontFamily = SpotifyBold,
                        color = Color.Black
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF5F5F5),
                        unfocusedContainerColor = Color(0xFFF5F5F5),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedPlaceholderColor = Color.Gray,
                        unfocusedPlaceholderColor = Color.Gray
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { onDismiss() }) {
                        Text("Cancel", color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = { onConfirm(); onDismiss() },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("OK", fontFamily = SpotifyBold, color = Color.White)
                    }
                }
            }
        }
    }
}


