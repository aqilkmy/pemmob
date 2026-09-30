package com.pemmob.mbkmybacaankomik.viewmodel

import androidx.lifecycle.ViewModel
import com.pemmob.mbkmybacaankomik.data.model.Komik
import com.pemmob.mbkmybacaankomik.data.remote.DummyDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * UiState untuk HomeScreen mengikuti pola Loading / Success / Error (MVVM).
 */
sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(
        val featuredKomik: Komik,
        val komikList: List<Komik>,
        val selectedGenre: String = "Semua"
    ) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _selectedGenre = MutableStateFlow("Semua")
    val selectedGenre: StateFlow<String> = _selectedGenre.asStateFlow()

    init {
        loadHomePage()
    }

    fun loadHomePage() {
        // TODO: Ganti dengan call ke KomikuApiService ketika Retrofit sudah dikonfigurasi.
        // Contoh:
        // viewModelScope.launch {
        //     _uiState.value = HomeUiState.Loading
        //     try {
        //         val result = RetrofitInstance.api.getKomikTerbaru()
        //         _uiState.value = HomeUiState.Success(...)
        //     } catch (e: Exception) {
        //         _uiState.value = HomeUiState.Error(e.message ?: "Gagal memuat data")
        //     }
        // }

        _uiState.value = HomeUiState.Success(
            featuredKomik = DummyDataSource.dummyFeaturedKomik,
            komikList = DummyDataSource.dummyKomikList,
            selectedGenre = _selectedGenre.value
        )
    }

    fun onGenreSelected(genre: String) {
        _selectedGenre.value = genre
        val currentState = _uiState.value
        if (currentState is HomeUiState.Success) {
            _uiState.value = currentState.copy(selectedGenre = genre)
        }
        // TODO: Panggil API dengan filter genre saat integrasi API:
        // viewModelScope.launch { ... RetrofitInstance.api.getKomikByGenre(genre) ... }
    }
}
