package com.example.estia.HomeScreen

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.estia.MainAppScreen.MainAppScreenViewModel

@Composable
fun RenderExploreScreen(mainAppScreenViewModel : MainAppScreenViewModel){
    LazyColumn(
    ) {
        items(100) { index ->
            Text(
                text = "Explore Screen     Explore Screen   Explore Screen   Explore Screen" +
                        "Explore Screen   Explore Screen    Explore Screen",
                modifier = Modifier.padding(16.dp),
                color = Color.White
            )
        }
    }
}