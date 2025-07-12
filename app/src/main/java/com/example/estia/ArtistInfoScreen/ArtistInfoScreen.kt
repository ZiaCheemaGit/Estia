package com.example.estia.ArtistInfoScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.estia.MainAppScreen.MainAppScreenViewModel


@Composable
fun ArtistInfoScreen(mainAppScreenViewModel: MainAppScreenViewModel) {

    val artistInfoViewModel: ArtistInfoViewModel = viewModel()

    LazyColumn(
        modifier = Modifier
            .background(Color.Transparent)
            .padding(vertical = 10.dp)
            .fillMaxSize()
    ) {
        item{

        }
    }
}
