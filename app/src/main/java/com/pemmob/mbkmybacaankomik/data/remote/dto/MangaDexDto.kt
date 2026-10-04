package com.pemmob.mbkmybacaankomik.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO untuk response daftar manga dari endpoint GET /manga
 */
data class MangaListResponse(
    @SerializedName("result") val result: String? = null,
    @SerializedName("response") val response: String? = null,
    @SerializedName("data") val data: List<MangaDataDto> = emptyList(),
    @SerializedName("limit") val limit: Int = 0,
    @SerializedName("offset") val offset: Int = 0,
    @SerializedName("total") val total: Int = 0
)

/**
 * DTO untuk response detail single manga dari endpoint GET /manga/{id}
 */
data class MangaDetailResponse(
    @SerializedName("result") val result: String? = null,
    @SerializedName("response") val response: String? = null,
    @SerializedName("data") val data: MangaDataDto? = null
)

/**
 * Data entitas Manga dari MangaDex
 */
data class MangaDataDto(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String? = null,
    @SerializedName("attributes") val attributes: MangaAttributesDto? = null,
    @SerializedName("relationships") val relationships: List<RelationshipDto>? = null
)

data class MangaAttributesDto(
    @SerializedName("title") val title: Map<String, String>? = null,
    @SerializedName("altTitles") val altTitles: List<Map<String, String>>? = null,
    @SerializedName("description") val description: Map<String, String>? = null,
    @SerializedName("originalLanguage") val originalLanguage: String? = null,
    @SerializedName("publicationDemographic") val publicationDemographic: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("year") val year: Int? = null,
    @SerializedName("contentRating") val contentRating: String? = null,
    @SerializedName("tags") val tags: List<TagDto>? = null,
    @SerializedName("latestUploadedChapter") val latestUploadedChapter: String? = null,
    @SerializedName("availableTranslatedLanguages") val availableTranslatedLanguages: List<String>? = null
)

data class TagDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("attributes") val attributes: TagAttributesDto? = null
)

data class TagAttributesDto(
    @SerializedName("name") val name: Map<String, String>? = null,
    @SerializedName("group") val group: String? = null
)

data class RelationshipDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("attributes") val attributes: RelationshipAttributesDto? = null
)

data class RelationshipAttributesDto(
    @SerializedName("fileName") val fileName: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("volume") val volume: String? = null
)

/**
 * DTO untuk response daftar chapter dari endpoint GET /manga/{id}/feed
 */
data class ChapterListResponse(
    @SerializedName("result") val result: String? = null,
    @SerializedName("response") val response: String? = null,
    @SerializedName("data") val data: List<ChapterDataDto> = emptyList(),
    @SerializedName("limit") val limit: Int = 0,
    @SerializedName("offset") val offset: Int = 0,
    @SerializedName("total") val total: Int = 0
)

data class ChapterDataDto(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String? = null,
    @SerializedName("attributes") val attributes: ChapterAttributesDto? = null,
    @SerializedName("relationships") val relationships: List<RelationshipDto>? = null
)

data class ChapterAttributesDto(
    @SerializedName("volume") val volume: String? = null,
    @SerializedName("chapter") val chapter: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("translatedLanguage") val translatedLanguage: String? = null,
    @SerializedName("externalUrl") val externalUrl: String? = null,
    @SerializedName("publishAt") val publishAt: String? = null,
    @SerializedName("readableAt") val readableAt: String? = null,
    @SerializedName("pages") val pages: Int? = null
)

/**
 * DTO untuk response halaman baca dari endpoint GET /at-home/server/{chapterId}
 */
data class AtHomeServerResponse(
    @SerializedName("result") val result: String? = null,
    @SerializedName("baseUrl") val baseUrl: String? = null,
    @SerializedName("chapter") val chapter: AtHomeChapterDto? = null
)

data class AtHomeChapterDto(
    @SerializedName("hash") val hash: String? = null,
    @SerializedName("data") val data: List<String> = emptyList(),
    @SerializedName("dataSaver") val dataSaver: List<String> = emptyList()
)
