package com.pemmob.mbkmybacaankomik.data.model

/**
 * Model utama untuk data Komik.
 * Field disesuaikan dengan response API Komiku.
 */
data class Komik(
    val id: String = "",
    val title: String = "",
    val slug: String = "",
    val thumbnail: String = "",
    val type: String = "",      // Manga, Manhwa, Manhua
    val status: String = "",    // Ongoing, Completed
    val genre: List<String> = emptyList(),
    val author: String = "",
    val rating: Float = 0f,
    val totalReaders: String = "",
    val synopsis: String = "",
    val latestChapter: String = "",
    val totalChapters: Int = 0,
    val isHot: Boolean = false,
    val isNew: Boolean = false,
    val updateSchedule: String = ""
)

/**
 * Model untuk data Chapter dalam sebuah Komik.
 */
data class Chapter(
    val id: String = "",
    val title: String = "",
    val chapterNumber: String = "",
    val releaseDate: String = "",
    val totalPages: Int = 0,
    val isNew: Boolean = false,
    val slug: String = ""
)

/**
 * Model untuk kategori / genre komik di menu cepat.
 */
data class KategoriItem(
    val id: String,
    val label: String,
    val iconRes: Int? = null,
    val slug: String
)
