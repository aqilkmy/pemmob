package com.pemmob.mbkmybacaankomik.data.repository

import com.pemmob.mbkmybacaankomik.data.model.Chapter
import com.pemmob.mbkmybacaankomik.data.model.ChapterDetail
import com.pemmob.mbkmybacaankomik.data.model.Komik
import com.pemmob.mbkmybacaankomik.data.remote.DummyDataSource
import com.pemmob.mbkmybacaankomik.data.remote.RetrofitInstance
import com.pemmob.mbkmybacaankomik.data.remote.dto.ChapterDataDto
import com.pemmob.mbkmybacaankomik.data.remote.dto.MangaDataDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs

/**
 * Repository untuk mengelola data MangaDex dan memetakan DTO ke model aplikasi (Komik, Chapter, ChapterDetail).
 */
class MangaDexRepository {

    private val api = RetrofitInstance.api

    // Mapping kategori label/genre ke MangaDex Tag UUID
    private val genreTagMap = mapOf(
        "fantasy" to "cdc58593-87dd-415e-bbc0-2ec27bf404cc",
        "action" to "391b0423-d847-456f-aff0-8b0cfc03066b",
        "romance" to "423e2eae-a7a2-4a8b-ac03-a8351462d71d",
        "comedy" to "4d32cc48-9f00-4cca-9b5a-a839f0764984",
        "horror" to "cdad7e68-1419-41dd-bdce-27753074a640",
        "sci-fi" to "256c8bd9-4904-4360-bf4f-508a76d67183",
        "mystery" to "ee968100-4191-4968-93d3-f82d72be7e46"
    )

    /**
     * Mengambil manga unggulan (featured) terpopuler di MangaDex.
     */
    suspend fun getFeaturedManga(): Result<Komik> = withContext(Dispatchers.IO) {
        try {
            val response = api.getMangaList(
                limit = 1,
                orderFollowedCount = "desc"
            )
            val firstManga = response.data.firstOrNull()
            if (firstManga != null) {
                Result.success(mapMangaDtoToKomik(firstManga))
            } else {
                Result.success(DummyDataSource.dummyFeaturedKomik)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback ke data dummy jika ada kendala koneksi
            Result.success(DummyDataSource.dummyFeaturedKomik)
        }
    }

    /**
     * Mengambil daftar komik dengan filter genre atau kata kunci pencarian.
     */
    suspend fun getMangaList(
        genre: String? = null,
        query: String? = null,
        limit: Int = 20,
        offset: Int = 0
    ): Result<List<Komik>> = withContext(Dispatchers.IO) {
        try {
            val tagId = genre?.lowercase()?.let { genreTagMap[it] }
            val includedTags = if (tagId != null) listOf(tagId) else null

            val response = if (!query.isNullOrBlank()) {
                api.getMangaList(
                    title = query.trim(),
                    limit = limit,
                    offset = offset,
                    includedTags = includedTags,
                    orderRelevance = "desc"
                )
            } else {
                api.getMangaList(
                    limit = limit,
                    offset = offset,
                    includedTags = includedTags,
                    orderFollowedCount = "desc"
                )
            }

            val list = response.data.map { mapMangaDtoToKomik(it) }
            if (list.isNotEmpty()) {
                Result.success(list)
            } else if (!query.isNullOrBlank()) {
                Result.success(emptyList())
            } else {
                Result.success(DummyDataSource.dummyKomikList)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback ke data dummy jika offline
            Result.success(DummyDataSource.dummyKomikList)
        }
    }

    /**
     * Mengambil detail komik dan daftar chapternya.
     */
    suspend fun getMangaDetail(mangaId: String): Result<Pair<Komik, List<Chapter>>> = withContext(Dispatchers.IO) {
        try {
            // 1. Ambil detail manga
            val detailResponse = api.getMangaDetail(mangaId)
            val mangaDto = detailResponse.data

            val komik = if (mangaDto != null) {
                mapMangaDtoToKomik(mangaDto)
            } else {
                DummyDataSource.dummyKomikList.find { it.slug == mangaId || it.id == mangaId }
                    ?: DummyDataSource.dummyFeaturedKomik
            }

            // 2. Ambil daftar chapter dari feed
            // Coba ambil chapter bahasa Indonesia dan Inggris yang tidak memiliki externalUrl (bisa dibaca langsung)
            var feedResponse = try {
                api.getMangaFeed(
                    id = mangaId,
                    limit = 100,
                    translatedLanguages = listOf("id", "en"),
                    orderChapter = "asc",
                    includeExternalUrl = 0
                )
            } catch (e: Exception) {
                null
            }

            // Jika kosong, coba ambil semua bahasa yang tersedia
            if (feedResponse == null || feedResponse.data.isEmpty()) {
                feedResponse = try {
                    api.getMangaFeed(
                        id = mangaId,
                        limit = 100,
                        orderChapter = "asc",
                        includeExternalUrl = 0
                    )
                } catch (e: Exception) {
                    null
                }
            }

            val chapters = feedResponse?.data
                ?.filter { (it.attributes?.pages ?: 0) > 0 }
                ?.distinctBy { it.attributes?.chapter ?: it.id }
                ?.map { mapChapterDtoToChapter(it) }
                ?: emptyList()

            val finalChapters = if (chapters.isNotEmpty()) chapters else DummyDataSource.dummyChapterList

            Result.success(Pair(komik.copy(totalChapters = finalChapters.size), finalChapters))
        } catch (e: Exception) {
            e.printStackTrace()
            val fallbackKomik = DummyDataSource.dummyKomikList.find { it.slug == mangaId || it.id == mangaId }
                ?: DummyDataSource.dummyFeaturedKomik
            Result.success(Pair(fallbackKomik, DummyDataSource.dummyChapterList))
        }
    }

    /**
     * Mengambil detail halaman baca untuk chapter tertentu dari server MangaDex At-Home.
     */
    suspend fun getChapterDetail(
        mangaId: String,
        chapterId: String
    ): Result<ChapterDetail> = withContext(Dispatchers.IO) {
        try {
            // Ambil halaman gambar dari At-Home server
            val pagesResponse = api.getChapterPages(chapterId)
            val baseUrl = pagesResponse.baseUrl
            val hash = pagesResponse.chapter?.hash

            // Prioritaskan dataSaver (kompresi ringan untuk mobile), fallback ke data asli
            val fileNames = if (!pagesResponse.chapter?.dataSaver.isNullOrEmpty()) {
                pagesResponse.chapter?.dataSaver!!
            } else {
                pagesResponse.chapter?.data ?: emptyList()
            }

            val isDataSaver = !pagesResponse.chapter?.dataSaver.isNullOrEmpty()
            val folder = if (isDataSaver) "data-saver" else "data"

            val pageUrls = if (!baseUrl.isNullOrBlank() && !hash.isNullOrBlank()) {
                fileNames.map { fileName ->
                    "$baseUrl/$folder/$hash/$fileName"
                }
            } else {
                emptyList()
            }

            // Ambil info manga dan daftar chapter untuk navigasi prev/next
            val (_, allChapters) = getMangaDetail(mangaId).getOrDefault(
                Pair(DummyDataSource.dummyFeaturedKomik, DummyDataSource.dummyChapterList)
            )

            val currentIndex = allChapters.indexOfFirst { it.slug == chapterId || it.id == chapterId }
                .let { if (it == -1) 0 else it }
            val currentChapter = allChapters.getOrNull(currentIndex)

            val prevSlug = if (currentIndex > 0) allChapters[currentIndex - 1].slug else null
            val nextSlug = if (currentIndex < allChapters.size - 1) allChapters[currentIndex + 1].slug else null

            val chapterDetail = ChapterDetail(
                komikSlug = mangaId,
                komikTitle = currentChapter?.title ?: "Manga",
                chapterSlug = chapterId,
                chapterTitle = currentChapter?.title ?: "Bab ${currentChapter?.chapterNumber ?: "1"}",
                chapterNumber = currentChapter?.chapterNumber ?: "1",
                pages = if (pageUrls.isNotEmpty()) pageUrls else DummyDataSource.getChapterDetail(mangaId, chapterId).pages,
                prevChapterSlug = prevSlug,
                nextChapterSlug = nextSlug
            )

            Result.success(chapterDetail)
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback ke dummy chapter detail jika offline
            Result.success(DummyDataSource.getChapterDetail(mangaId, chapterId))
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // FUNGSI PEMETAAN (MAPPERS)
    // ─────────────────────────────────────────────────────────────────────────────

    private fun mapMangaDtoToKomik(dto: MangaDataDto): Komik {
        val attr = dto.attributes

        // Judul: Utamakan bahasa Inggris, lalu Indonesia, lalu alternatif, lalu nilai pertama
        val title = attr?.title?.get("en")
            ?: attr?.title?.get("id")
            ?: attr?.altTitles?.firstNotNullOfOrNull { it["en"] ?: it["id"] }
            ?: attr?.title?.values?.firstOrNull()
            ?: "Tanpa Judul"

        // Author
        val authorName = dto.relationships?.firstOrNull { it.type == "author" }?.attributes?.name
            ?: dto.relationships?.firstOrNull { it.type == "artist" }?.attributes?.name
            ?: "MangaDex Creator"

        // Cover thumbnail
        val coverFileName = dto.relationships?.firstOrNull { it.type == "cover_art" }?.attributes?.fileName
        val coverUrl = if (!coverFileName.isNullOrBlank()) {
            "https://uploads.mangadex.org/covers/${dto.id}/$coverFileName.512.jpg"
        } else {
            "https://picsum.photos/seed/${dto.id}/400/600"
        }

        // Tipe komik berdasarkan originalLanguage
        val type = when (attr?.originalLanguage?.lowercase()) {
            "ko" -> "Manhwa"
            "zh", "zh-hk" -> "Manhua"
            else -> "Manga"
        }

        // Status
        val status = when (attr?.status?.lowercase()) {
            "completed" -> "Completed"
            "hiatus" -> "Hiatus"
            else -> "Ongoing"
        }

        // Genre dari tags
        val genres = attr?.tags?.mapNotNull { it.attributes?.name?.get("en") } ?: listOf("Manga")

        // Sinopsis
        val synopsis = attr?.description?.get("id")
            ?: attr?.description?.get("en")
            ?: attr?.description?.values?.firstOrNull()
            ?: "Sinopsis belum tersedia untuk judul ini."

        // Rating perkiraan stabil (4.6 - 4.9) berdasarkan hash id
        val ratingHash = abs(dto.id.hashCode() % 40) / 100f
        val calculatedRating = 4.6f + ratingHash

        return Komik(
            id = dto.id,
            title = title,
            slug = dto.id, // Gunakan MangaDex UUID sebagai slug
            thumbnail = coverUrl,
            type = type,
            status = status,
            genre = genres,
            author = authorName,
            rating = String.format(Locale.US, "%.1f", calculatedRating).toFloatOrNull() ?: 4.8f,
            totalReaders = "${100 + abs(dto.id.hashCode() % 900)}K",
            synopsis = synopsis,
            latestChapter = if (!attr?.latestUploadedChapter.isNullOrBlank()) "Bab Baru" else "Ongoing",
            totalChapters = 0,
            isHot = true,
            isNew = false
        )
    }

    private fun mapChapterDtoToChapter(dto: ChapterDataDto): Chapter {
        val attr = dto.attributes
        val chNum = attr?.chapter ?: "1"
        val chTitle = if (!attr?.title.isNullOrBlank()) {
            attr?.title!!
        } else {
            "Bab $chNum"
        }

        // Format tanggal rilis
        val formattedDate = formatPublishDate(attr?.publishAt)

        return Chapter(
            id = dto.id,
            title = chTitle,
            chapterNumber = chNum,
            releaseDate = formattedDate,
            totalPages = attr?.pages ?: 0,
            isNew = false,
            slug = dto.id // Gunakan chapter UUID sebagai slug
        )
    }

    private fun formatPublishDate(isoDateString: String?): String {
        if (isoDateString.isNullOrBlank()) return "Terbaru"
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = inputFormat.parse(isoDateString.take(19))
            val outputFormat = SimpleDateFormat("d MMM yyyy", Locale.forLanguageTag("id-ID"))
            outputFormat.format(date ?: return "Terbaru")
        } catch (e: Exception) {
            "Terbaru"
        }
    }
}
