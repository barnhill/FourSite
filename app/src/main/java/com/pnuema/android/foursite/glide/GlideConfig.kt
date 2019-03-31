package com.pnuema.android.foursite.glide

import android.content.Context
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.engine.cache.ExternalPreferredCacheDiskCacheFactory
import com.bumptech.glide.module.AppGlideModule

/**
 * Glide configuration module
 */
@GlideModule
class GlideConfig : AppGlideModule() {
    companion object {
        private const val GLIDE_CACHE_SIZE_BYTES = 100 * 1024 * 1024 //100 meg cache
        private const val GLIDE_CACHE_FOLDER = "glide_cache"
    }

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        builder.setDiskCache(ExternalPreferredCacheDiskCacheFactory(context, GLIDE_CACHE_FOLDER, GLIDE_CACHE_SIZE_BYTES.toLong()))
    }

    // Disable manifest parsing to avoid adding similar modules twice.
    override fun isManifestParsingEnabled(): Boolean {
        return false
    }
}