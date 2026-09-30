package com.pemmob.mbkmybacaankomik.viewmodel

import androidx.lifecycle.ViewModel
import com.pemmob.mbkmybacaankomik.data.model.Chapter
import com.pemmob.mbkmybacaankomik.data.model.Komik
import com.pemmob.mbkmybacaankomik.data.remote.DummyDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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

class DetailViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    /**
     * Muat detail komik berdasarkan slug.
     * TODO: Ganti dengan call ke API (RetrofitInstance.api.getDetailKomik(slug)) saat siap.
     */
    fun loadDetail(komikSlug: String = "legenda-garuda-putih") {
        val komik = DummyDataSource.dummyKomikList.find { it.slug == komikSlug }
            ?: DummyDataSource.dummyKomikList[0]

        _uiState.value = DetailUiState.Success(
            komik    = komik,
            chapters = DummyDataSource.dummyChapterList
        )
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
                chapters         = sorted
            )
        }
    }
}
