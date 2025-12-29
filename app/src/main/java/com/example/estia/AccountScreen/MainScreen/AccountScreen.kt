
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.util.TableInfo
import coil.compose.AsyncImage
import com.example.estia.AccountScreen.AccountScreenRouter
import com.example.estia.AccountScreen.CreatePlayListScreen.CreatePlayListScreen
import com.example.estia.AccountScreen.DownloadedSongsScreen.DownloadedSongsScreen
import com.example.estia.AccountScreen.ImportScreen.ImportScreen
import com.example.estia.AccountScreen.LikedSongsScreen.LikedSongsScreen
import com.example.estia.AccountScreen.MainScreen.AccountScreenViewModel
import com.example.estia.AccountScreen.LocalFilesScreen.FileExplorerViewModel
import com.example.estia.AccountScreen.LocalFilesScreen.RenderFileExplorerScreen
import com.example.estia.AccountScreen.YTImportedScreenDisplay.YTImportedPlayListsScreen
import com.example.estia.AccountScreen.YTVideosDisplayScreen.YTVideosDisplayScreen
import com.example.estia.MainAppScreen.MainAppScreenViewModel
import com.example.estia.PlayListScreen.PlayListScreenViewModel
import com.example.estia.PlayerDrawer.LibraryManagerDialog.LibraryManagerDialog
import com.example.estia.PlayerDrawer.PlayerDrawerViewModel
import com.example.estia.R
import com.example.estia.SpotifyBold
import kotlin.math.exp

@Composable
fun RenderAccountScreen(
    expandableDrawerViewModel: PlayerDrawerViewModel,
    playListScreenViewModel: PlayListScreenViewModel,
    innerPadding: PaddingValues,
    mainAppScreenViewModel : MainAppScreenViewModel,
    fileExplorerViewModel: FileExplorerViewModel,
    viewModel: AccountScreenViewModel,
    navController: NavController,
) {
    val accountScreenNavController = rememberNavController()

    NavHost(
        navController = accountScreenNavController,
        startDestination = AccountScreenRouter.mainScreen,
        builder = {

            composable(AccountScreenRouter.mainScreen) {
                AccountScreenDisplay(
                    expandableDrawerViewModel,
                    playListScreenViewModel,
                    innerPadding,
                    mainAppScreenViewModel,
                    fileExplorerViewModel,
                    viewModel,
                    accountScreenNavController
                )
            }

            composable(AccountScreenRouter.localFilesScreen) {
                RenderFileExplorerScreen(
                    playListScreenViewModel,
                    mainAppScreenViewModel,
                    fileExplorerViewModel,
                    expandableDrawerViewModel,
                    accountScreenNavController,
                )
            }

            composable(AccountScreenRouter.likedSongsScreen) {
                LikedSongsScreen(
                    playListScreenViewModel = playListScreenViewModel,
                    fileExplorerViewModel = fileExplorerViewModel,
                    list = viewModel.likedSongs.collectAsState().value,
                    mainAppScreenViewModel = mainAppScreenViewModel,
                    expandableDrawerViewModel = expandableDrawerViewModel,
                    navController = accountScreenNavController
                )
            }

            composable(AccountScreenRouter.estiaDownloads) {
                DownloadedSongsScreen(
                    playListScreenViewModel = playListScreenViewModel,
                    fileExplorerViewModel = fileExplorerViewModel,
                    list = viewModel.estiaDownloads.collectAsState().value,
                    mainAppScreenViewModel = mainAppScreenViewModel,
                    expandableDrawerViewModel = expandableDrawerViewModel,
                    navController = accountScreenNavController,
                    accountScreenViewModel = viewModel
                )
            }

            composable(AccountScreenRouter.ImportScreen) {
                ImportScreen(
                    accountScreenViewModel = viewModel,
                    mainAppScreenViewModel = mainAppScreenViewModel,
                    expandableDrawerViewModel = expandableDrawerViewModel,
                    navController = accountScreenNavController,
                )
            }

            composable(AccountScreenRouter.YTVideosDisplayScreenToImport) {
                YTVideosDisplayScreen(
                    viewModel,
                    accountScreenNavController
                )
            }

            composable(AccountScreenRouter.createPlayListScreen) {
                CreatePlayListScreen(
                    expandableDrawerViewModel = expandableDrawerViewModel,
                    accountScreenViewModel = viewModel,
                    mainAppScreenViewModel = mainAppScreenViewModel,
                    navController = accountScreenNavController
                )
            }

            composable(AccountScreenRouter.YTImportedPlayListScreen) {
                val list = viewModel.selectedYTPlayListToDisplay.value
                if(list != null){
                    YTImportedPlayListsScreen(
                        playListScreenViewModel = playListScreenViewModel,
                        expandableDrawerViewModel = expandableDrawerViewModel,
                        fileExplorerViewModel = fileExplorerViewModel,
                        mainAppScreenViewModel = mainAppScreenViewModel,
                        playlist = list,
                        navController = accountScreenNavController
                    )
                }
            }

        }
    )
}

@Composable
fun AccountScreenDisplay(
    expandableDrawerViewModel: PlayerDrawerViewModel,
    playListScreenViewModel: PlayListScreenViewModel,
    innerPadding: PaddingValues,
    mainAppScreenViewModel : MainAppScreenViewModel,
    fileExplorerViewModel: FileExplorerViewModel,
    viewModel: AccountScreenViewModel,
    navController: NavController,
){
    val localFilesPlayList = fileExplorerViewModel.permanentAllSongsList.collectAsState().value

    var dC = mainAppScreenViewModel.dominantColor.value
    if(mainAppScreenViewModel.isColorCloseToWhite(dC)){
        dC = dC.copy(alpha = 0.25f)
    }
    Box(){
        Column(
            modifier = Modifier
                .height(170.dp)
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            dC,
                            Color.Black
                        )
                    )
                )
        ){
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(start = 30.dp)
                    .fillMaxSize()
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

                Spacer(Modifier.width(60.dp))

                Image(
                    painter = painterResource(id = R.drawable.plus_icon),
                    contentDescription = "Import Icon",
                    modifier = Modifier
                        .clickable() {
                            viewModel.showAddToLibraryDialog.value = true
                        }
                        .size(25.dp),
                    colorFilter = ColorFilter.tint(Color.White)
                )

                Spacer(Modifier.width(25.dp))

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
            modifier = Modifier.padding(
                start = 10.dp, end = 10.dp,
                top = 170.dp,
                bottom = innerPadding.calculateBottomPadding())
        ) {

            // Liked Songs
            item{
                Row(
                    modifier = Modifier
                        .height(65.dp)
                        .fillMaxWidth()
                        .clickable() {
                            navController.navigate(AccountScreenRouter.likedSongsScreen)
                        }
                ){
                    AsyncImage(
                        model = R.drawable.liked_song_image,
                        contentDescription = "Liked Songs playlist",
                        modifier = Modifier
                            .width(50.dp)
                            .height(50.dp),
                    )
                    Column(
                        modifier = Modifier
                            .padding(10.dp)
                            .width(200.dp)
                    ){
                        Text(
                            text = "Liked Songs",
                            modifier = Modifier,
                            fontFamily = SpotifyBold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        val likedSongs = viewModel.likedSongs.collectAsState()
                        Text(
                            text = "${likedSongs.value.size} Tracks",
                            modifier = Modifier,
                            fontFamily = SpotifyBold,
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Local Files
            item{
                Row(
                    modifier = Modifier
                        .height(65.dp)
                        .fillMaxWidth()
                        .clickable(
                            onClick = {
                                navController.navigate(AccountScreenRouter.localFilesScreen)
                            }
                        )
                ){
                    AsyncImage(
                        model = R.drawable.local_files_image,
                        contentDescription = "Liked Songs playlist",
                        modifier = Modifier
                            .width(50.dp)
                            .height(50.dp),
                    )
                    Column(
                        modifier = Modifier
                            .padding(10.dp)
                            .width(200.dp)
                    ){
                        Text(
                            text = "Local Files",
                            modifier = Modifier,
                            fontFamily = SpotifyBold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Text(
                            text = "${localFilesPlayList.size} Tracks",
                            modifier = Modifier,
                            fontFamily = SpotifyBold,
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Estia Downloads
            item{
                Row(
                    modifier = Modifier
                        .height(65.dp)
                        .fillMaxWidth()
                        .clickable(
                            onClick = {
                                navController.navigate(AccountScreenRouter.estiaDownloads)
                            }
                        )
                ){
                    AsyncImage(
                        model = R.drawable.donwload_image_bg,
                        contentDescription = "Estia Downloads Folder Image",
                        modifier = Modifier
                            .width(50.dp)
                            .height(50.dp),
                    )
                    Column(
                        modifier = Modifier
                            .padding(10.dp)
                            .width(200.dp)
                    ){
                        Text(
                            text = "Estia Downloads",
                            modifier = Modifier,
                            fontFamily = SpotifyBold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        val estiaDownloads = viewModel.estiaDownloads.collectAsState()
                        Text(
                            text = "${estiaDownloads.value.size} Tracks",
                            modifier = Modifier,
                            fontFamily = SpotifyBold,
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // YT Imported PlayLists
            val list = viewModel.importedYTPlayLists.value
            items(list.size){ it ->
                val playList = list[it]
                Row(
                    modifier = Modifier
                        .height(65.dp)
                        .fillMaxWidth()
                        .clickable(
                            onClick = {
                                viewModel.selectedYTPlayListToDisplay.value = playList
                                if (viewModel.selectedYTPlayListToDisplay.value != null) {
                                    navController.navigate(AccountScreenRouter.YTImportedPlayListScreen)
                                }
                            }
                        )
                ){
                    AsyncImage(
                        model = playList.songs.firstOrNull()?.thumbnailUrl,
                        contentDescription = "Playlist cover Image",
                        modifier = Modifier.size(50.dp),
                        placeholder = painterResource(R.drawable.music_icon_compressed),
                        error = painterResource(R.drawable.music_icon_compressed),
                        fallback = painterResource(R.drawable.music_icon_compressed)
                    )
                    Column(
                        modifier = Modifier
                            .padding(10.dp)
                            .width(200.dp)
                    ){
                        Text(
                            maxLines = 1,
                            text = playList.playlist.title,
                            modifier = Modifier,
                            fontFamily = SpotifyBold,
                            fontSize = 16.sp,
                            color = Color.White
                        )

                        Text(
                            text = "${playList.songs.size} Tracks",
                            modifier = Modifier,
                            fontFamily = SpotifyBold,
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = viewModel.showAddToLibraryDialog.value,
            enter = slideInVertically(
                initialOffsetY = { it }
            ),
            exit = slideOutVertically(
                targetOffsetY = { it }
            )
        ) {
            AddToLibraryDialog(viewModel, navController)
        }
    }
}

@Composable
fun AddToLibraryDialog(
    accountScreenViewModel: AccountScreenViewModel,
    navController: NavController
){
    Column(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
    )
    {
        LazyColumn(
            Modifier
                .background(
                    Color.Black
                )
                .padding(top = 30.dp)
                .fillMaxSize(),
        ) {

            item {
                // Main Top Text
                Column(
                    Modifier
                        .padding(top = 25.dp)
                        .height(40.dp)
                        .fillMaxWidth()
                ) {
                    Box(Modifier.fillMaxSize()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                modifier = Modifier,
                                fontSize = 20.sp,
                                text = "Add To Library",
                                fontFamily = SpotifyBold,
                                color = Color.White
                            )
                        }
                        Column(
                            Modifier.padding(start = 20.dp)
                        ) {
                            Image(
                                painter = painterResource(R.drawable.drop_down_icon),
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable() {
                                        accountScreenViewModel.showAddToLibraryDialog.value = false
                                    },
                                contentDescription = "Drop Down Button",
                                colorFilter = ColorFilter.tint(Color.White)
                            )
                        }
                    }
                }

                // Import
                Column(Modifier.padding(top = 15.dp, bottom = 15.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .height(55.dp)
                            .clickable() {
                                navController.navigate(AccountScreenRouter.ImportScreen)
                                accountScreenViewModel.showAddToLibraryDialog.value = false
                            }
                    )
                    {
                        Image(
                            painter = painterResource(R.drawable.import_icon),
                            contentDescription = "Liked Songs Image",
                            modifier = Modifier.fillMaxHeight(),
                            colorFilter = ColorFilter.tint(Color.White)
                        )
                        Column(Modifier
                            .padding(vertical = 5.dp)
                            .fillMaxHeight()
                            .fillMaxWidth()){
                            Column(Modifier
                                .fillMaxWidth()
                                .height(20.dp)) {
                                Text(
                                    modifier = Modifier.padding(start = 10.dp),
                                    fontSize = 15.sp,
                                    text = "Import",
                                    fontFamily = SpotifyBold,
                                    color = Color.White
                                )
                            }
                            Column(Modifier
                                .fillMaxWidth()
                                .height(25.dp)) {
                                Text(
                                    modifier = Modifier.padding(start = 10.dp),
                                    fontSize = 10.sp,
                                    text = "Get Your Favourite music playLists From External " +
                                            "Services Like Youtube, Spotify ",
                                    fontFamily = SpotifyBold,
                                    color = Color.White
                                )
                            }
                        }

                    }
                }

                // Create PlayList
                Column(Modifier.padding(top = 15.dp, bottom = 15.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .height(55.dp)
                            .clickable() {
                                navController.navigate(AccountScreenRouter.createPlayListScreen)
                                accountScreenViewModel.showAddToLibraryDialog.value = false
                            }
                    )
                    {
                        Image(
                            painter = painterResource(R.drawable.create_playlist_icon),
                            contentDescription = "create playlist icon",
                            modifier = Modifier.fillMaxHeight(),
                            colorFilter = ColorFilter.tint(Color.White)
                        )
                        Column(Modifier
                            .padding(vertical = 5.dp)
                            .fillMaxHeight()
                            .fillMaxWidth()){
                            Column(Modifier
                                .fillMaxWidth()
                                .height(20.dp)) {
                                Text(
                                    modifier = Modifier.padding(start = 10.dp),
                                    fontSize = 15.sp,
                                    text = "Create PlayList",
                                    fontFamily = SpotifyBold,
                                    color = Color.White
                                )
                            }
                            Column(Modifier
                                .fillMaxWidth()
                                .height(25.dp)) {
                                Text(
                                    modifier = Modifier.padding(start = 10.dp),
                                    fontSize = 10.sp,
                                    text = "Manage your Music by Creating PlayLists and set" +
                                            " your desired musical atmosphere",
                                    fontFamily = SpotifyBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }


            }
        }
    }
}