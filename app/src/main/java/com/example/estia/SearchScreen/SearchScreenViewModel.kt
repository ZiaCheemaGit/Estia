package com.example.estia.SearchScreen

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class SearchScreenViewModel : ViewModel() {

    val filterOptionsList = listOf("Songs", "Albums", "Artists")
    val selectedFilter = mutableStateOf(filterOptionsList[0])
    val searchQuery = mutableStateOf("")

    private var searchJob: Job? = null

    var searchResults = mutableStateOf<List<DeezerTrack>>(emptyList())
        private set

    var isLoading = mutableStateOf(false)
        private set

    fun fillSearchResultsBySongs(query: String) {
        if (query.isBlank()) {
            searchResults.value = emptyList()
            return
        }
        // Cancel any previous search in progress
        searchJob?.cancel()

        // Launch new search
        searchJob = viewModelScope.launch {
            searchResults.value = emptyList()
            try {
                isLoading.value = true
                val response = DeezerService.api.searchTracks(query)
                searchResults.value = response.data ?: emptyList()
            } catch (e: Exception) {
                // Only handle exception if not from cancellation
                if (e !is CancellationException) {
                    searchResults.value = emptyList()
                }
            } finally {
                isLoading.value = false
            }
        }
    }


}
