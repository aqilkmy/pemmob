package com.pemmob.mbkmybacaankomik.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pemmob.mbkmybacaankomik.data.model.Komik
import com.pemmob.mbkmybacaankomik.data.repository.MangaDexRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UiState untuk HomeScreen mengikuti pola Loading / Success / Error (MVVM).
 */
sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(
        val featuredKomik: Komik,
        val komikList: List<Komik>,
        val selectedGenre: String = "Semua",
        val searchQuery: String = ""
    ) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

class HomeViewModel(
    private val repository: MangaDexRepository = MangaDexRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _selectedGenre = MutableStateFlow("Semua")
    val selectedGenre: StateFlow<String> = _selectedGenre.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private var cachedFeatured: Komik? = null
    private var searchJob: Job? = null

    init {
        loadHomePage()
    }

    fun loadHomePage() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                val featured = cachedFeatured ?: repository.getFeaturedManga().getOrNull()
                cachedFeatured = featured

                val genreParam = if (_selectedGenre.value.equals("semua", ignoreCase = true)) null else _selectedGenre.value
                val listResult = repository.getMangaList(genre = genreParam, query = _searchQuery.value.ifBlank { null })
                val list = listResult.getOrDefault(emptyList())

                val displayFeatured = featured ?: list.firstOrNull() ?: Komik(title = "MangaDex")

                _uiState.value = HomeUiState.Success(
                    featuredKomik = displayFeatured,
                    komikList = list,
                    selectedGenre = _selectedGenre.value,
                    searchQuery = _searchQuery.value
                )
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.localizedMessage ?: "Gagal memuat katalog MangaDex.")
            }
        }
    }

    fun onGenreSelected(genre: String) {
        _selectedGenre.value = genre
        viewModelScope.launch {
            val current = _uiState.value
            val featured = if (current is HomeUiState.Success) current.featuredKomik else (cachedFeatured ?: Komik())

            val genreParam = if (genre.equals("semua", ignoreCase = true)) null else genre
            val list = repository.getMangaList(genre = genreParam, query = _searchQuery.value.ifBlank { null })
                .getOrDefault(emptyList())

            _uiState.value = HomeUiState.Success(
                featuredKomik = featured,
                komikList = list,
                selectedGenre = genre,
                searchQuery = _searchQuery.value
            )
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400) // Debounce pencarian
            val genreParam = if (_selectedGenre.value.equals("semua", ignoreCase = true)) null else _selectedGenre.value
            val list = repository.getMangaList(genre = genreParam, query = newQuery.ifBlank { null })
                .getOrDefault(emptyList())

            val current = _uiState.value
            val featured = if (current is HomeUiState.Success) current.featuredKomik else (cachedFeatured ?: Komik())

            _uiState.value = HomeUiState.Success(
                featuredKomik = featured,
                komikList = list,
                selectedGenre = _selectedGenre.value,
                searchQuery = newQuery
            )
        }
    }
}
