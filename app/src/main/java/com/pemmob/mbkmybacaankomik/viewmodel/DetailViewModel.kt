package com.pemmob.mbkmybacaankomik.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pemmob.mbkmybacaankomik.data.model.Chapter
import com.pemmob.mbkmybacaankomik.data.model.Komik
import com.pemmob.mbkmybacaankomik.data.repository.MangaDexRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UiState untuk DetailScreen (Loading / Success / Error).
 */
sealed class DetailUiState {
    object Loading : DetailUiState()
    data class Success(
        val komik: Komik,
        val chapters: List<Chapter>,
        val isChapterSortAsc: Boolean = true
    ) : DetailUiState()
    data class Error(val message: String) : DetailUiState()
}

class DetailViewModel(
    private val repository: MangaDexRepository = MangaDexRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    /**
     * Memuat detail komik dan daftar chapter dari MangaDex REST API.
     */
    fun loadDetail(komikSlug: String = "") {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            try {
                val result = repository.getMangaDetail(komikSlug)
                result.onSuccess { (komik, chapters) ->
                    _uiState.value = DetailUiState.Success(
                        komik = komik,
                        chapters = chapters,
                        isChapterSortAsc = true
                    )
                }.onFailure { error ->
                    _uiState.value = DetailUiState.Error(
                        error.localizedMessage ?: "Gagal memuat detail komik dari MangaDex."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = DetailUiState.Error(
                    e.localizedMessage ?: "Terjadi kesalahan saat memuat data komik."
                )
            }
        }
    }

    fun toggleChapterSort() {
        val current = _uiState.value
        if (current is DetailUiState.Success) {
            val sorted = if (current.isChapterSortAsc) {
                current.chapters.sortedByDescending { it.chapterNumber.toFloatOrNull() ?: 0f }
            } else {
                current.chapters.sortedBy { it.chapterNumber.toFloatOrNull() ?: 0f }
            }
            _uiState.value = current.copy(
                isChapterSortAsc = !current.isChapterSortAsc,
                chapters = sorted
            )
        }
    }
}
