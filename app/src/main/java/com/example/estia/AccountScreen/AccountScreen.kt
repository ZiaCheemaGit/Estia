
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.estia.AccountScreen.AccountScreenViewModel
import com.example.estia.MainAppScreen.MainAppScreenViewModel
@Composable
fun RenderAccountScreen(
    innerPadding: PaddingValues,
    mainAppScreenViewModel : MainAppScreenViewModel,
    viewModel: AccountScreenViewModel
) {
    LazyColumn(
    ) {
        // top Space
        item{
            Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding()))
        }

        item() {

        }

        // Bottom Space
        item{
            Spacer(modifier = Modifier.height(innerPadding.calculateBottomPadding()))
        }
    }
}


