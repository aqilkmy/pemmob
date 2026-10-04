package com.pemmob.mbkmybacaankomik

import android.app.Application
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.pemmob.mbkmybacaankomik.data.remote.RetrofitInstance

class MbkApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Mengonfigurasi Coil agar menggunakan OkHttpClient dengan MangaDexDns
        // sehingga seluruh gambar cover & halaman komik MangaDex dimuat tanpa terhalang DNS ISP
        SingletonImageLoader.setSafe { context ->
            ImageLoader.Builder(context)
                .components {
                    add(
                        OkHttpNetworkFetcherFactory(
                            callFactory = { RetrofitInstance.okHttpClient }
                        )
                    )
                }
                .build()
        }
    }
}
