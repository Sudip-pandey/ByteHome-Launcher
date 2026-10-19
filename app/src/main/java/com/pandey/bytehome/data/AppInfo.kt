package com.pandey.bytehome.data

import android.content.ComponentName

/**
 * One launchable application. Icons are NOT held here — they are loaded
 * on demand through [com.pandey.bytehome.util.IconCache] so the app list
 * itself stays tiny in RAM.
 */
data class AppInfo(
    val label: String,
    val packageName: String,
    val componentName: ComponentName
) {
    val sortKey: String get() = label.lowercase()
}
