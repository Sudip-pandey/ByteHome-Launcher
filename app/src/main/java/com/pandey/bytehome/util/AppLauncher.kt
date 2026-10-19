package com.pandey.bytehome.util

import android.content.Context
import android.content.Intent
import com.pandey.bytehome.data.AppInfo

/**
 * Defensive app launching. A failed launch must NEVER crash ByteHome —
 * it returns false and the caller shows a toast instead.
 */
object AppLauncher {

    fun launch(context: Context, app: AppInfo): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                component = app.componentName
                addCategory(Intent.CATEGORY_LAUNCHER)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun launchPackage(context: Context, packageName: String): Boolean {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
                ?: return false
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }
}
