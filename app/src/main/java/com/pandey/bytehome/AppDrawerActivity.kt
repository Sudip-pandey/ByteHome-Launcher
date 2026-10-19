package com.pandey.bytehome

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.GridLayoutManager
import com.pandey.bytehome.data.AppInfo
import com.pandey.bytehome.data.AppRepository
import com.pandey.bytehome.data.FavoritesStore
import com.pandey.bytehome.databinding.ActivityDrawerBinding
import com.pandey.bytehome.ui.AppGridAdapter
import com.pandey.bytehome.util.AppLauncher

/**
 * Fast app drawer: cached app list, alphabetical grid, A–Z index,
 * instant local search. Doubles as an app picker for the home screen
 * favorites and dock slots (see [EXTRA_PICK_MODE]).
 */
class AppDrawerActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PICK_MODE = "pick_mode"
        const val EXTRA_FOCUS_SEARCH = "focus_search"
        const val EXTRA_RESULT_PACKAGE = "result_package"
        /** Input AND output: which dock slot is being picked (-1 = favorite). */
        const val EXTRA_RESULT_SLOT = "result_slot"
    }

    private lateinit var binding: ActivityDrawerBinding
    private lateinit var adapter: AppGridAdapter
    private var allApps: List<AppInfo> = emptyList()
    private var pickMode = false
    private var slot = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDrawerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pickMode = intent.getBooleanExtra(EXTRA_PICK_MODE, false)
        slot = intent.getIntExtra(EXTRA_RESULT_SLOT, -1)

        adapter = AppGridAdapter(
            onClick = { app -> onAppClicked(app) },
            onLongClick = { app -> onAppLongClicked(app) }
        )
        binding.appGrid.layoutManager = GridLayoutManager(this, 4)
        // Drawer is static content: no item animations, no change flicker.
        binding.appGrid.itemAnimator = null
        binding.appGrid.adapter = adapter

        binding.searchBox.addTextChangedListener { text ->
            applyFilter(text?.toString().orEmpty())
        }
        binding.alphaIndex.onLetterSelected = { letter -> scrollToLetter(letter) }

        if (intent.getBooleanExtra(EXTRA_FOCUS_SEARCH, false)) {
            binding.searchBox.requestFocus()
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        }
    }

    override fun onResume() {
        super.onResume()
        // Cheap: returns the cached list unless a package broadcast invalidated it.
        allApps = AppRepository.getApps(this)
        binding.alphaIndex.setLetters(
            allApps.mapNotNull { it.label.firstOrNull()?.uppercaseChar() }.toSet()
        )
        applyFilter(binding.searchBox.text?.toString().orEmpty())
    }

    private fun onAppClicked(app: AppInfo) {
        if (pickMode) {
            setResult(
                Activity.RESULT_OK,
                Intent()
                    .putExtra(EXTRA_RESULT_PACKAGE, app.packageName)
                    .putExtra(EXTRA_RESULT_SLOT, slot)
            )
            finish()
            return
        }
        if (!AppLauncher.launch(this, app)) {
            Toast.makeText(this, getString(R.string.could_not_open), Toast.LENGTH_SHORT).show()
        }
    }

    private fun onAppLongClicked(app: AppInfo): Boolean {
        if (pickMode) return true
        if (FavoritesStore.add(this, app.packageName)) {
            Toast.makeText(this, getString(R.string.added_to_favorites), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, getString(R.string.favorites_full), Toast.LENGTH_SHORT).show()
        }
        return true
    }

    private fun applyFilter(query: String) {
        val q = query.trim().lowercase()
        val list = if (q.isEmpty()) allApps
        else allApps.filter { it.label.lowercase().contains(q) }
        adapter.submit(list)
        binding.emptyView.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun scrollToLetter(letter: Char) {
        val list = adapter.currentList
        val idx = list.indexOfFirst { it.label.firstOrNull()?.uppercaseChar() == letter }
        if (idx >= 0) {
            (binding.appGrid.layoutManager as? GridLayoutManager)
                ?.scrollToPositionWithOffset(idx, 0)
        }
    }
}
