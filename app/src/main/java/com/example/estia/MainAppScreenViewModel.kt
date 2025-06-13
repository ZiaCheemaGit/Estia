package com.example.estia

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class MainAppScreenViewModel : ViewModel(){
    var isExpandedNowPlaying by mutableStateOf(false)

    var currentScreen by mutableStateOf("ExploreScreen")
    var selectedIcon by mutableStateOf("exploreIcon")

    // Login options and their mapping with their icons in res/drawable/
    val unselectedBottomBarIcons = mapOf(
        "exploreIcon" to R.drawable.explore_icon_unselected,
        "searchIcon" to R.drawable.search_icon_unselected,
        "fileExplorerIcon" to R.drawable.file_explorer_icon_unselected,
        "accountIcon" to R.drawable.account_icon_unselected,
    )

    val unselectedTopBarIcons = mapOf(
        "playListIcon" to R.drawable.playlist_icon_unselected,
        "settingsIcon" to R.drawable.settings_icon_unselected
    )

    val selectedBottomBarIcons = mapOf(
        "exploreIcon" to R.drawable.explore_icon_selected,
        "searchIcon" to R.drawable.search_icon_selected,
        "fileExplorerIcon" to R.drawable.file_explorer_icon_selected,
        "accountIcon" to R.drawable.account_icon_selected,
    )

    val selectedTopBarIcons = mapOf(
        "playListIcon" to R.drawable.playlist_icon_selected,
        "settingsIcon" to R.drawable.settings_icon_selected,
    )

    val ScreenMapping = mapOf(
        "playListIcon" to "PlayListScreen",
        "exploreIcon" to "ExploreScreen",
        "searchIcon" to "SearchScreen",
        "fileExplorerIcon" to "FileExplorerScreen",
        "accountIcon" to "AccountScreen",
        "settingsIcon" to "SettingsScreen"
    )

    fun changeScreen(icon : String){
        currentScreen = ScreenMapping[icon].toString()
    }
}

