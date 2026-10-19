package com.pandey.bytehome.data

import android.content.Context

/** Home-screen favorite apps, persisted in SharedPreferences. */
object FavoritesStore {

    private const val PREFS = "bytehome_prefs"
    private const val KEY = "favorites"
    const val MAX = 8

    fun get(context: Context): MutableList<String> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, "") ?: ""
        return raw.split(",").filter { it.isNotBlank() }.toMutableList()
    }

    fun set(context: Context, list: List<String>) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY, list.take(MAX).joinToString(","))
            .apply()
    }

    /** @return false when already pinned or the list is full. */
    fun add(context: Context, packageName: String): Boolean {
        val cur = get(context)
        if (cur.contains(packageName) || cur.size >= MAX) return false
        cur.add(packageName)
        set(context, cur)
        return true
    }

    fun remove(context: Context, packageName: String) {
        val cur = get(context)
        if (cur.remove(packageName)) set(context, cur)
    }
}
