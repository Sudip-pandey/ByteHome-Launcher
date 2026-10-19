package com.pandey.bytehome.data

import android.content.Context

/** Dock slot assignments (package names), persisted in SharedPreferences. */
object DockStore {

    private const val PREFS = "bytehome_prefs"
    const val SLOTS = 4

    private fun key(slot: Int) = "dock_$slot"

    fun get(context: Context, slot: Int): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(key(slot), null)

    fun set(context: Context, slot: Int, packageName: String?) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(key(slot), packageName)
            .apply()
    }

    fun clear(context: Context) {
        val edit = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
        for (i in 0 until SLOTS) edit.remove(key(i))
        edit.apply()
    }
}
