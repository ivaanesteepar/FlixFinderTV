package com.example.flixfindertv.utils

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import java.io.File

object ImageLoaderProvider {
    fun getImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .diskCache {
                DiskCache.Builder()
                    .directory(File(context.cacheDir, "image_cache"))
                    .maxSizeBytes(50L * 1024 * 1024) // 50MB
                    .build()
            }
            .build()
    }
}