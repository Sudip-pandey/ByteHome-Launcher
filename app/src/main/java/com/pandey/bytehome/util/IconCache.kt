package com.pandey.bytehome.util

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.LruCache

/**
 * Small in-memory cache of app icons. Icons are expensive to decode, so each
 * package's icon is decoded at most once per process. Corrupt icon resources
 * are caught here and never crash the launcher.
 */
object IconCache {

    private val cache = object : LruCache<String, Drawable>(96) {
        override fun sizeOf(key: String, value: Drawable): Int = 1
    }

    @Synchronized
    fun get(context: Context, packageName: String): Drawable? {
        cache.get(packageName)?.let { return it }
        val icon = try {
            context.packageManager.getApplicationIcon(packageName)
        } catch (e: Exception) {
            null
        } catch (e: OutOfMemoryError) {
            null
        }
        if (icon != null) cache.put(packageName, icon)
        return icon
    }

    @Synchronized
    fun remove(packageName: String) {
        cache.remove(packageName)
    }

    @Synchronized
    fun clear() = cache.evictAll()
}
