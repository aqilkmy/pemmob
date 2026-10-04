package com.pemmob.mbkmybacaankomik.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pemmob.mbkmybacaankomik.data.model.Chapter
import com.pemmob.mbkmybacaankomik.data.model.ChapterDetail
import com.pemmob.mbkmybacaankomik.data.repository.MangaDexRepository
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

class ReadViewModel(
    private val repository: MangaDexRepository = MangaDexRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReadUiState>(ReadUiState.Loading)
    val uiState: StateFlow<ReadUiState> = _uiState.asStateFlow()

    private var currentKomikSlug: String = ""
    private var currentChapterSlug: String = ""

    /**
     * Memuat halaman gambar chapter komik dari MangaDex At-Home server.
     */
    fun loadChapter(komikSlug: String, chapterSlug: String) {
        currentKomikSlug = komikSlug
        currentChapterSlug = chapterSlug

        viewModelScope.launch {
            _uiState.value = ReadUiState.Loading
            try {
                val detailResult = repository.getChapterDetail(komikSlug, chapterSlug)
                val mangaDetailResult = repository.getMangaDetail(komikSlug)

                val allChapters = mangaDetailResult.getOrNull()?.second ?: emptyList()
                val detail = detailResult.getOrThrow()

                val currentIndex = allChapters.indexOfFirst { it.slug == chapterSlug || it.id == chapterSlug }
                    .let { if (it == -1) 0 else it }

                _uiState.value = ReadUiState.Success(
                    chapterDetail = detail,
                    allChapters = allChapters,
                    currentChapterIndex = currentIndex,
                    areControlsVisible = true
                )
            } catch (e: Exception) {
                _uiState.value = ReadUiState.Error(
                    message = e.localizedMessage ?: "Gagal memuat chapter komik dari server MangaDex."
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
