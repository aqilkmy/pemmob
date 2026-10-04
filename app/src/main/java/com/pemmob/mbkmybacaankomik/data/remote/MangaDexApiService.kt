package com.pemmob.mbkmybacaankomik.data.remote

import com.pemmob.mbkmybacaankomik.data.remote.dto.AtHomeServerResponse
import com.pemmob.mbkmybacaankomik.data.remote.dto.ChapterListResponse
import com.pemmob.mbkmybacaankomik.data.remote.dto.MangaDetailResponse
import com.pemmob.mbkmybacaankomik.data.remote.dto.MangaListResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Interface Retrofit untuk MangaDex REST API v5.
 * Dokumentasi resmi: https://api.mangadex.org/docs/
 */
interface MangaDexApiService {

    /**
     * Mengambil daftar manga dengan filter pencarian, genre/tag, atau sorting.
     */
    @GET("manga")
    suspend fun getMangaList(
        @Query("title") title: String? = null,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("includes[]") includes: List<String> = listOf("cover_art", "author", "artist"),
        @Query("contentRating[]") contentRating: List<String> = listOf("safe", "suggestive"),
        @Query("includedTags[]") includedTags: List<String>? = null,
        @Query("order[followedCount]") orderFollowedCount: String? = null,
        @Query("order[latestUploadedChapter]") orderLatestChapter: String? = null,
        @Query("order[relevance]") orderRelevance: String? = null
    ): MangaListResponse

    /**
     * Mengambil detail satu manga berdasarkan ID/UUID.
     */
    @GET("manga/{id}")
    suspend fun getMangaDetail(
        @Path("id") id: String,
        @Query("includes[]") includes: List<String> = listOf("cover_art", "author", "artist")
    ): MangaDetailResponse

    /**
     * Mengambil daftar chapter (feed) untuk sebuah manga.
     */
    @GET("manga/{id}/feed")
    suspend fun getMangaFeed(
        @Path("id") id: String,
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0,
        @Query("translatedLanguage[]") translatedLanguages: List<String>? = null,
        @Query("order[chapter]") orderChapter: String = "asc",
        @Query("includeExternalUrl") includeExternalUrl: Int = 0,
        @Query("contentRating[]") contentRating: List<String> = listOf("safe", "suggestive")
    ): ChapterListResponse

    /**
     * Mengambil URL server pembaca At-Home beserta daftar hash & nama file halaman komik.
     */
    @GET("at-home/server/{chapterId}")
    suspend fun getChapterPages(
        @Path("chapterId") chapterId: String
    ): AtHomeServerResponse
}
