package com.pemmob.mbkmybacaankomik.data.remote

/**
 * Interface service untuk API Komiku (https://api.komiku.id).
 *
 * Untuk mengaktifkan Retrofit, uncomment bagian di bawah dan tambahkan
 * dependency Retrofit + Gson di build.gradle.kts:
 *   implementation("com.squareup.retrofit2:retrofit:2.9.0")
 *   implementation("com.squareup.retrofit2:converter-gson:2.9.0")
 *
 * Contoh endpoint Komiku:
 *   GET /api/                      -> daftar komik terbaru
 *   GET /api/?orderby=popular      -> komik populer
 *   GET /api/?genre={slug}         -> komik per genre
 *   GET /api/cari/?post={keyword}  -> pencarian komik
 *   GET /{slug}/api/               -> detail komik + chapter list
 *
 * Untuk saat ini menggunakan DummyDataSource agar UI dapat di-preview.
 */

/*
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface KomikuApiService {

    @GET("api/")
    suspend fun getKomikTerbaru(
        @Query("orderby") orderBy: String = "update",
        @Query("paged") page: Int = 1
    ): KomikuListResponse

    @GET("api/")
    suspend fun getKomikPopuler(
        @Query("orderby") orderBy: String = "popular",
        @Query("paged") page: Int = 1
    ): KomikuListResponse

    @GET("api/")
    suspend fun getKomikByGenre(
        @Query("genre") genre: String,
        @Query("paged") page: Int = 1
    ): KomikuListResponse

    @GET("api/cari/")
    suspend fun searchKomik(
        @Query("post") keyword: String
    ): KomikuListResponse

    @GET("{slug}/api/")
    suspend fun getDetailKomik(
        @Path("slug") slug: String
    ): KomikuDetailResponse
}

object RetrofitInstance {
    private const val BASE_URL = "https://api.komiku.id/"

    val api: KomikuApiService by lazy {
        retrofit2.Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
            .create(KomikuApiService::class.java)
    }
}
*/
