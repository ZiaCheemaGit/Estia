
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.estia.AccountScreen.AccountScreenViewModel
import com.example.estia.MainAppScreen.MainAppScreenViewModel
import com.example.estia.R
import com.example.estia.SpotifyBold

@Composable
fun RenderAccountScreen(
    innerPadding: PaddingValues,
    mainAppScreenViewModel : MainAppScreenViewModel,
    viewModel: AccountScreenViewModel
) {
    Box(){
        Column(
            modifier = Modifier
                .height(170.dp)
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF4B1D00),
                            Color(0xFF4B1D00),
                            Color(0xFF4B1D00),
                            Color.Black
                        )
                    )
                )
        ){
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 30.dp).fillMaxSize()
            ){
                Image(
                    painter = painterResource(id = R.drawable.library_icon_unselected),
                    contentDescription = "Play Queue",
                    modifier = Modifier.size(30.dp),
                    colorFilter = ColorFilter.tint(Color.White)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Your Library",
                    fontSize = 25.sp,
                    fontFamily = SpotifyBold,
                    color = Color.White
                )
                Spacer(Modifier.width(110.dp))
                Image(
                    painter = painterResource(id = R.drawable.settings_icon_unselected),
                    contentDescription = "Play Queue",
                    modifier = Modifier
                        .size(25.dp),
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }
        }

        LazyColumn(
            modifier = Modifier.padding(top = 170.dp)
        ) {

        }
    }

}


