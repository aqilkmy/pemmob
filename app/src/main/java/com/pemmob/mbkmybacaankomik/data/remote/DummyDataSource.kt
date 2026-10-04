package com.pemmob.mbkmybacaankomik.data.remote

import com.pemmob.mbkmybacaankomik.data.model.Chapter
import com.pemmob.mbkmybacaankomik.data.model.ChapterDetail
import com.pemmob.mbkmybacaankomik.data.model.Komik

/**
 * Sumber data dummy untuk keperluan preview dan pengembangan UI.
 * Saat integrasi API Komiku siap, cukup ganti pemanggilan DummyDataSource
 * dengan call ke KomikuApiService (Retrofit) di repository.
 */
object DummyDataSource {

    val dummyKomikList: List<Komik> = listOf(
        Komik(
            id = "1",
            title = "Legenda Garuda Putih",
            slug = "legenda-garuda-putih",
            thumbnail = "https://komiku.id/wp-content/uploads/2024/01/garuda.jpg",
            type = "Manhwa",
            status = "Ongoing",
            genre = listOf("Fantasy", "Action"),
            author = "Studio Angin Timur",
            rating = 4.9f,
            totalReaders = "2,1 Juta",
            synopsis = "Terlahir di era kejayaan nusantara di mana pendekar bisa muncul dari segala penjuru, Arga mampu meyanturkan keris...",
            latestChapter = "Spt. 94 Baru",
            totalChapters = 94,
            isHot = true,
            isNew = false
        ),
        Komik(
            id = "2",
            title = "Pendekar Petir",
            slug = "pendekar-petir",
            thumbnail = "https://komiku.id/wp-content/uploads/2024/02/petir.jpg",
            type = "Manga",
            status = "Ongoing",
            genre = listOf("Action", "Shonen"),
            author = "Aki • Sangguan",
            rating = 4.6f,
            totalReaders = "980K",
            synopsis = "Seorang pemuda dengan kekuatan petir yang tak terbatas berjuang untuk melindungi desanya.",
            latestChapter = "Bab 65",
            totalChapters = 65,
            isHot = true,
            isNew = false
        ),
        Komik(
            id = "3",
            title = "Dewa Pedang",
            slug = "dewa-pedang",
            thumbnail = "https://komiku.id/wp-content/uploads/2024/02/pedang.jpg",
            type = "Manhua",
            status = "Ongoing",
            genre = listOf("Fantasy", "Xianxia"),
            author = "Fantasy • Malam",
            rating = 4.7f,
            totalReaders = "1,2 Juta",
            synopsis = "Di alam kultivasi, hanya yang terkuat yang bisa mencapai puncak. Seorang pemuda lemah menemukan pedang legenda.",
            latestChapter = "Bab 112",
            totalChapters = 112,
            isHot = false,
            isNew = true
        ),
        Komik(
            id = "4",
            title = "Surya Mentari",
            slug = "surya-mentari",
            thumbnail = "https://komiku.id/wp-content/uploads/2024/03/surya.jpg",
            type = "Manhwa",
            status = "Ongoing",
            genre = listOf("Romance", "Slice of Life"),
            author = "Komiku • Tamat",
            rating = 4.5f,
            totalReaders = "756K",
            synopsis = "Kisah cinta yang menghangatkan hati antara dua insan yang dipertemukan oleh takdir di kota metropolitan.",
            latestChapter = "Bab 88",
            totalChapters = 88,
            isHot = false,
            isNew = false
        ),
        Komik(
            id = "5",
            title = "Gadis Bayangan",
            slug = "gadis-bayangan",
            thumbnail = "https://komiku.id/wp-content/uploads/2024/03/bayangan.jpg",
            type = "Manhwa",
            status = "Ongoing",
            genre = listOf("Fantasy", "Mystery"),
            author = "Niken • Mloggan",
            rating = 4.8f,
            totalReaders = "1,5 Juta",
            synopsis = "Seorang gadis dengan kemampuan mengendalikan bayangan terseret dalam konspirasi kerajaan yang berbahaya.",
            latestChapter = "Bab 77",
            totalChapters = 77,
            isHot = true,
            isNew = false
        ),
        Komik(
            id = "6",
            title = "Raja Iblis Reborn",
            slug = "raja-iblis-reborn",
            thumbnail = "https://komiku.id/wp-content/uploads/2024/04/iblis.jpg",
            type = "Manhwa",
            status = "Ongoing",
            genre = listOf("Action", "Fantasy"),
            author = "Dark • Studio",
            rating = 4.7f,
            totalReaders = "2,0 Juta",
            synopsis = "Raja iblis terkuat reinkarnasi sebagai manusia biasa dan mulai perjalanan balas dendamnya.",
            latestChapter = "Bab 145",
            totalChapters = 145,
            isHot = true,
            isNew = false
        )
    )

    val dummyFeaturedKomik: Komik = dummyKomikList[0]

    val dummyChapterList: List<Chapter> = listOf(
        Chapter(
            id = "c1",
            title = "Bangkitnya Benang Jiwa",
            chapterNumber = "1",
            releaseDate = "1 Jan 2025",
            totalPages = 64,
            isNew = false,
            slug = "bab-1"
        ),
        Chapter(
            id = "c2",
            title = "Kota Pelindung",
            chapterNumber = "2",
            releaseDate = "5 Jan 2025",
            totalPages = 58,
            isNew = true,
            slug = "bab-2"
        ),
        Chapter(
            id = "c3",
            title = "Benang Merah Takdir",
            chapterNumber = "3",
            releaseDate = "10 Jan 2025",
            totalPages = 72,
            isNew = false,
            slug = "bab-3"
        ),
        Chapter(
            id = "c4",
            title = "Rahasia Sang Tetua",
            chapterNumber = "4",
            releaseDate = "3 Feb 2025",
            totalPages = 65,
            isNew = false,
            slug = "bab-4"
        ),
        Chapter(
            id = "c5",
            title = "Pertarungan Pertama",
            chapterNumber = "5",
            releaseDate = "9 Feb 2025",
            totalPages = 72,
            isNew = false,
            slug = "bab-5"
        )
    )

    /**
     * Mendapatkan data halaman baca untuk chapter tertentu.
     * Mengembalikan ChapterDetail lengkap dengan tombol prev/next slug.
     * Ketika REST API siap, fungsi ini akan digantikan oleh pemanggilan repository/API.
     */
    fun getChapterDetail(komikSlug: String, chapterSlug: String): ChapterDetail {
        val komik = dummyKomikList.find { it.slug == komikSlug } ?: dummyKomikList[0]
        val chapters = dummyChapterList
        val currentIndex = chapters.indexOfFirst { it.slug == chapterSlug }.let { if (it == -1) 0 else it }
        val currentChapter = chapters[currentIndex]

        val prevSlug = if (currentIndex > 0) chapters[currentIndex - 1].slug else null
        val nextSlug = if (currentIndex < chapters.size - 1) chapters[currentIndex + 1].slug else null

        // Sample manga/manhwa page images untuk visualisasi pembaca
        val samplePages = listOf(
            "https://picsum.photos/seed/${komik.slug}-${currentChapter.slug}-p1/800/1200",
            "https://picsum.photos/seed/${komik.slug}-${currentChapter.slug}-p2/800/1200",
            "https://picsum.photos/seed/${komik.slug}-${currentChapter.slug}-p3/800/1200",
            "https://picsum.photos/seed/${komik.slug}-${currentChapter.slug}-p4/800/1200",
            "https://picsum.photos/seed/${komik.slug}-${currentChapter.slug}-p5/800/1200",
            "https://picsum.photos/seed/${komik.slug}-${currentChapter.slug}-p6/800/1200"
        )

        return ChapterDetail(
            komikSlug = komik.slug,
            komikTitle = komik.title,
            chapterSlug = currentChapter.slug,
            chapterTitle = currentChapter.title,
            chapterNumber = currentChapter.chapterNumber,
            pages = samplePages,
            prevChapterSlug = prevSlug,
            nextChapterSlug = nextSlug
        )
    }
}
