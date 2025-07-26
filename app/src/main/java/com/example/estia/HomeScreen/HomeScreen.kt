package com.example.estia.HomeScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.estia.MainAppScreen.MainAppScreenViewModel
import com.example.estia.R
import com.example.estia.SpotifyBold

@Composable
fun RenderExploreScreen(
    homeScreenViewModel: HomeScreenViewModel,
    mainAppScreenViewModel : MainAppScreenViewModel,
    innerPadding: PaddingValues
){

    LazyColumn(
    ) {
        item{
            Spacer(Modifier.height(innerPadding.calculateTopPadding() + 10.dp))
        }

        // Charts
        item{
            Column(modifier = Modifier.padding(10.dp)){
                Text(
                    text = "Charts",
                    fontFamily = SpotifyBold,
                    fontSize = 30.sp,
                    color = Color.White,
                    )
            }
            LazyRow(Modifier.padding(start = 10.dp, end = 10.dp)) {

                // Global Song Charts
                item{
                    Box(Modifier.size(200.dp)){
                        Image(
                            painter = painterResource(id = R.drawable.global_charts_image_background),
                            contentDescription = "Global Charts",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(10.dp))
                        )
                        Text(
                            textAlign = TextAlign.Center,
                            text = "Global Song",
                            fontFamily = SpotifyBold,
                            fontSize = 24.sp,
                            color = Color(0xFFFCECC5),
                            modifier = Modifier
                                .padding(top = 70.dp).fillMaxSize()
                        )
                        Text(
                            textAlign = TextAlign.Center,
                            text = "Charts",
                            fontFamily = SpotifyBold,
                            fontSize = 34.sp,
                            color = Color(0xFFFCECC5),
                            modifier = Modifier
                                .padding(top = 95.dp).fillMaxSize()
                        )
                    }
                }

                // Space in between
                item{
                    Spacer(Modifier.width(20.dp))
                }

                // Users Country Song Charts
                item{
                    val userCountryName = homeScreenViewModel.userCountryName.value
                    Box(Modifier.size(200.dp)){
                        Image(
                            painter = painterResource(id = R.drawable.country_image_background),
                            contentDescription = "User Country Charts",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(10.dp))
                        )
                        Text(
                            textAlign = TextAlign.Center,
                            text = userCountryName + " Song",
                            fontFamily = SpotifyBold,
                            fontSize = 24.sp,
                            color = Color.Gray,
                            modifier = Modifier
                                .padding(top = 70.dp).fillMaxSize()
                        )
                        Text(
                            textAlign = TextAlign.Center,
                            text = "Charts",
                            fontFamily = SpotifyBold,
                            fontSize = 34.sp,
                            color = Color.Gray,
                            modifier = Modifier
                                .padding(top = 95.dp).fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}