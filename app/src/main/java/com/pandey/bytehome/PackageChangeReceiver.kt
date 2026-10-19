package com.pandey.bytehome

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.pandey.bytehome.data.AppRepository
import com.pandey.bytehome.util.IconCache

/**
 * Keeps the cached app list honest: any install / remove / update
 * invalidates the cache so the next onResume rebuilds it exactly once.
 * Declared in the manifest; these broadcasts are exempt from the
 * implicit-broadcast restrictions.
 */
class PackageChangeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_PACKAGE_ADDED,
            Intent.ACTION_PACKAGE_REMOVED,
            Intent.ACTION_PACKAGE_CHANGED -> {
                intent.data?.schemeSpecificPart?.let { IconCache.remove(it) }
                AppRepository.invalidate()
            }
        }
    }
}
