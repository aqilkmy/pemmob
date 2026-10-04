package com.pemmob.mbkmybacaankomik.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pemmob.mbkmybacaankomik.data.model.Komik
import com.pemmob.mbkmybacaankomik.data.repository.MangaDexRepository
import com.pemmob.mbkmybacaankomik.data.repository.MangaOrderType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Tab Navigasi Bawah (Material 3 Navigation Bar)
 */
enum class HomeTab(
    val label: String,
    val iconSelected: ImageVector,
    val iconUnselected: ImageVector
) {
    BERANDA("Beranda", Icons.Filled.Home, Icons.Outlined.Home),
    POPULER("Populer", Icons.Filled.Leaderboard, Icons.Outlined.Leaderboard),
    TERBARU("Terbaru", Icons.Filled.NewReleases, Icons.Outlined.NewReleases)
}

/**
 * UiState untuk HomeScreen mengikuti pola Material 3 dan MVVM.
 */
sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(
        val featuredKomik: Komik,
        val komikList: List<Komik>,
        val selectedGenre: String = "Semua",
        val searchQuery: String = "",
        val currentTab: HomeTab = HomeTab.BERANDA,
        val isListLoading: Boolean = false
    ) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

class HomeViewModel(
    private val repository: MangaDexRepository = MangaDexRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _currentTab = MutableStateFlow(HomeTab.BERANDA)
    val currentTab: StateFlow<HomeTab> = _currentTab.asStateFlow()

    private val _selectedGenre = MutableStateFlow("Semua")
    val selectedGenre: StateFlow<String> = _selectedGenre.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private var cachedFeatured: Komik? = null
    private var loadJob: Job? = null
    private var searchJob: Job? = null

    init {
        loadHomePage()
    }

    /**
     * Memuat halaman awal (Beranda) beserta manga featured & katalog.
     */
    fun loadHomePage() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                val featured = cachedFeatured ?: repository.getFeaturedManga().getOrNull()
                cachedFeatured = featured

                val genreParam = if (_selectedGenre.value.equals("semua", ignoreCase = true)) null else _selectedGenre.value
                val order = getOrderTypeForTab(_currentTab.value)

                val listResult = repository.getMangaList(
                    genre = genreParam,
                    query = _searchQuery.value.ifBlank { null },
                    orderType = order
                )
                val list = listResult.getOrDefault(emptyList())
                val displayFeatured = featured ?: list.firstOrNull() ?: Komik(title = "MangaDex")

                _uiState.value = HomeUiState.Success(
                    featuredKomik = displayFeatured,
                    komikList = list,
                    selectedGenre = _selectedGenre.value,
                    searchQuery = _searchQuery.value,
                    currentTab = _currentTab.value,
                    isListLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.localizedMessage ?: "Gagal memuat katalog MangaDex.")
            }
        }
    }

    /**
     * Berpindah tab antara Beranda, Populer, dan Terbaru.
     */
    fun onTabSelected(tab: HomeTab) {
        if (_currentTab.value == tab && _searchQuery.value.isBlank()) return
        _currentTab.value = tab

        val current = _uiState.value
        val featured = if (current is HomeUiState.Success) current.featuredKomik else (cachedFeatured ?: Komik())

        // Tampilkan loading halus pada daftar saat berganti tab
        if (current is HomeUiState.Success) {
            _uiState.value = current.copy(currentTab = tab, isListLoading = true)
        }

        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val genreParam = if (_selectedGenre.value.equals("semua", ignoreCase = true)) null else _selectedGenre.value
            val order = getOrderTypeForTab(tab)

            val list = repository.getMangaList(
                genre = genreParam,
                query = _searchQuery.value.ifBlank { null },
                orderType = order
            ).getOrDefault(emptyList())

            _uiState.value = HomeUiState.Success(
                featuredKomik = featured,
                komikList = list,
                selectedGenre = _selectedGenre.value,
                searchQuery = _searchQuery.value,
                currentTab = tab,
                isListLoading = false
            )
        }
    }

    /**
     * Memfilter manga berdasarkan kategori genre yang dipilih.
     */
    fun onGenreSelected(genre: String) {
        _selectedGenre.value = genre
        val current = _uiState.value
        val featured = if (current is HomeUiState.Success) current.featuredKomik else (cachedFeatured ?: Komik())

        if (current is HomeUiState.Success) {
            _uiState.value = current.copy(selectedGenre = genre, isListLoading = true)
        }

        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val genreParam = if (genre.equals("semua", ignoreCase = true)) null else genre
            val order = getOrderTypeForTab(_currentTab.value)

            val list = repository.getMangaList(
                genre = genreParam,
                query = _searchQuery.value.ifBlank { null },
                orderType = order
            ).getOrDefault(emptyList())

            _uiState.value = HomeUiState.Success(
                featuredKomik = featured,
                komikList = list,
                selectedGenre = genre,
                searchQuery = _searchQuery.value,
                currentTab = _currentTab.value,
                isListLoading = false
            )
        }
    }

    /**
     * Melakukan pencarian judul komik dengan debounce 400ms.
     */
    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
        val current = _uiState.value
        val featured = if (current is HomeUiState.Success) current.featuredKomik else (cachedFeatured ?: Komik())

        if (current is HomeUiState.Success) {
            _uiState.value = current.copy(searchQuery = newQuery, isListLoading = true)
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400) // Debounce pencarian
            val genreParam = if (_selectedGenre.value.equals("semua", ignoreCase = true)) null else _selectedGenre.value
            val order = getOrderTypeForTab(_currentTab.value)

            val list = repository.getMangaList(
                genre = genreParam,
                query = newQuery.ifBlank { null },
                orderType = if (newQuery.isNotBlank()) MangaOrderType.RELEVANCE else order
            ).getOrDefault(emptyList())

            _uiState.value = HomeUiState.Success(
                featuredKomik = featured,
                komikList = list,
                selectedGenre = _selectedGenre.value,
                searchQuery = newQuery,
                currentTab = _currentTab.value,
                isListLoading = false
            )
        }
    }

    /**
     * Menghapus input pencarian dan mengembalikan tampilan normal.
     */
    fun onClearSearch() {
        onSearchQueryChanged("")
    }

    private fun getOrderTypeForTab(tab: HomeTab): MangaOrderType {
        return when (tab) {
            HomeTab.BERANDA -> MangaOrderType.POPULAR
            HomeTab.POPULER -> MangaOrderType.POPULAR
            HomeTab.TERBARU -> MangaOrderType.LATEST
        }
    }
}
