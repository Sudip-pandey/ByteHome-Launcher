package com.pandey.bytehome.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent

/**
 * Single cached copy of the launchable-app list.
 *
 * The list is loaded ONCE per process and only rebuilt when
 * [invalidate] is called (package install / remove / change).
 * Never query PackageManager during scrolling.
 */
object AppRepository {

    private const val SELF_PACKAGE = "com.pandey.bytehome"

    @Volatile
    private var cached: List<AppInfo>? = null

    fun getApps(context: Context): List<AppInfo> {
        cached?.let { return it }
        return synchronized(this) {
            cached?.let { return it }
            val fresh = load(context.applicationContext)
            cached = fresh
            fresh
        }
    }

    fun invalidate() {
        synchronized(this) { cached = null }
    }

    private fun load(context: Context): List<AppInfo> {
        val pm = context.packageManager
        val probe = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveInfos = try {
            pm.queryIntentActivities(probe, 0)
        } catch (e: Exception) {
            return emptyList()
        }
        val apps = ArrayList<AppInfo>(resolveInfos.size)
        for (ri in resolveInfos) {
            try {
                val ai = ri.activityInfo ?: continue
                if (ai.packageName == SELF_PACKAGE) continue // never list ourselves
                if (!ai.enabled) continue
                val label = ri.loadLabel(pm)?.toString()?.trim()
                    .takeIf { !it.isNullOrEmpty() } ?: ai.packageName
                apps.add(AppInfo(label, ai.packageName, ComponentName(ai.packageName, ai.name)))
            } catch (e: Exception) {
                // Skip broken entries instead of crashing.
            }
        }
        apps.sortBy { it.sortKey }
        return apps
    }
}
