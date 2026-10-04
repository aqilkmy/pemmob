package com.pemmob.mbkmybacaankomik.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pemmob.mbkmybacaankomik.data.model.Chapter
import com.pemmob.mbkmybacaankomik.data.model.ChapterDetail
import com.pemmob.mbkmybacaankomik.data.remote.DummyDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * State untuk halaman membaca komik (ReadScreen).
 */
sealed class ReadUiState {
    object Loading : ReadUiState()
    data class Success(
        val chapterDetail: ChapterDetail,
        val allChapters: List<Chapter>,
        val currentChapterIndex: Int,
        val areControlsVisible: Boolean = true
    ) : ReadUiState()
    data class Error(val message: String) : ReadUiState()
}

class ReadViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<ReadUiState>(ReadUiState.Loading)
    val uiState: StateFlow<ReadUiState> = _uiState.asStateFlow()

    private var currentKomikSlug: String = "legenda-garuda-putih"
    private var currentChapterSlug: String = "bab-1"

    /**
     * Memuat halaman gambar chapter komik.
     * Siap dihubungkan ke Retrofit API di masa mendatang.
     */
    fun loadChapter(komikSlug: String, chapterSlug: String) {
        currentKomikSlug = komikSlug.ifBlank { "legenda-garuda-putih" }
        currentChapterSlug = chapterSlug.ifBlank { "bab-1" }

        viewModelScope.launch {
            _uiState.value = ReadUiState.Loading
            try {
                // Di masa depan: call repository / RetrofitInstance.api.getChapterPages(chapterSlug)
                val allChapters = DummyDataSource.dummyChapterList
                val detail = DummyDataSource.getChapterDetail(currentKomikSlug, currentChapterSlug)
                val currentIndex = allChapters.indexOfFirst { it.slug == currentChapterSlug }.let {
                    if (it == -1) 0 else it
                }

                _uiState.value = ReadUiState.Success(
                    chapterDetail = detail,
                    allChapters = allChapters,
                    currentChapterIndex = currentIndex,
                    areControlsVisible = true
                )
            } catch (e: Exception) {
                _uiState.value = ReadUiState.Error(
                    message = e.localizedMessage ?: "Gagal memuat chapter komik."
                )
            }
        }
    }

    /**
     * Menyembunyikan / menampilkan header dan toolbar navigasi (immersive reader).
     */
    fun toggleControls() {
        val current = _uiState.value
        if (current is ReadUiState.Success) {
            _uiState.value = current.copy(areControlsVisible = !current.areControlsVisible)
        }
    }

    fun setControlsVisible(visible: Boolean) {
        val current = _uiState.value
        if (current is ReadUiState.Success) {
            _uiState.value = current.copy(areControlsVisible = visible)
        }
    }
}
