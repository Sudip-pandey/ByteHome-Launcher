package com.pandey.bytehome

import android.app.role.RoleManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.pandey.bytehome.data.DockStore
import com.pandey.bytehome.data.FavoritesStore
import com.pandey.bytehome.databinding.ActivitySettingsBinding

/**
 * Launcher settings. The important action lives here: choosing which
 * launcher Android uses, so the user can never get stuck inside ByteHome.
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    private val roleRequest = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // Result is informational only; the system applies the user's choice.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.chooseLauncherButton.setOnClickListener { openHomeSettings() }
        binding.resetButton.setOnClickListener {
            DockStore.clear(this)
            FavoritesStore.set(this, emptyList())
            Toast.makeText(this, getString(R.string.dock_reset_done), Toast.LENGTH_SHORT).show()
        }
    }

    private fun openHomeSettings() {
        // Modern path: ask the system for the HOME role.
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val rm = getSystemService(RoleManager::class.java)
                if (rm != null && rm.isRoleAvailable(RoleManager.ROLE_HOME)) {
                    roleRequest.launch(rm.createRequestRoleIntent(RoleManager.ROLE_HOME))
                    return
                }
            }
        } catch (e: Exception) {
            // Fall through to the settings screen.
        }
        // Fallback: the system's Home-app settings page.
        try {
            startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
        } catch (e: Exception) {
            Toast.makeText(
                this,
                getString(R.string.open_system_settings_hint),
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
