package com.pandey.bytehome

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import com.pandey.bytehome.data.AppRepository
import com.pandey.bytehome.data.DockStore
import com.pandey.bytehome.data.FavoritesStore
import com.pandey.bytehome.databinding.ActivityMainBinding
import com.pandey.bytehome.ui.AppGridAdapter
import com.pandey.bytehome.util.AppLauncher
import com.pandey.bytehome.util.IconCache
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ByteHome home screen. Lifecycle-aware and quiet by design:
 * the clock ticks once per minute and the battery receiver only lives
 * while this screen is visible. No background services, no polling.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val handler = Handler(Looper.getMainLooper())

    private val clockTick = object : Runnable {
        override fun run() {
            updateClock()
            val now = System.currentTimeMillis()
            handler.postDelayed(this, 60_000 - (now % 60_000) + 200)
        }
    }

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
            if (level >= 0 && scale > 0) {
                binding.batteryText.text = "BAT ${level * 100 / scale}%"
            }
        }
    }

    /** Result from the app drawer acting as a picker (favorites / dock slot). */
    private val drawerPickLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult
        val pkg = result.data?.getStringExtra(AppDrawerActivity.EXTRA_RESULT_PACKAGE)
            ?: return@registerForActivityResult
        val slot = result.data?.getIntExtra(AppDrawerActivity.EXTRA_RESULT_SLOT, -1) ?: -1
        if (slot in 0 until DockStore.SLOTS) {
            DockStore.set(this, slot, pkg)
            refreshDock()
        } else {
            if (FavoritesStore.add(this, pkg)) {
                refreshFavorites()
                Toast.makeText(this, getString(R.string.added_to_favorites), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, getString(R.string.favorites_full), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.deviceInfo.text = deviceLine()
        binding.favoritesList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        binding.drawerButton.setOnClickListener { openDrawer() }
        binding.dockDrawerButton.setOnClickListener { openDrawer() }
        binding.searchButton.setOnClickListener {
            startActivity(
                Intent(this, AppDrawerActivity::class.java)
                    .putExtra(AppDrawerActivity.EXTRA_FOCUS_SEARCH, true)
            )
        }
        binding.settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        binding.addFavoriteButton.setOnClickListener {
            drawerPickLauncher.launch(
                Intent(this, AppDrawerActivity::class.java)
                    .putExtra(AppDrawerActivity.EXTRA_PICK_MODE, true)
            )
        }
    }

    override fun onResume() {
        super.onResume()
        updateClock()
        updateDate()
        updateWifi()
        refreshFavorites()
        refreshDock()
        handler.post(clockTick)
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(batteryReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(batteryReceiver, filter)
        }
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(clockTick)
        try {
            unregisterReceiver(batteryReceiver)
        } catch (e: IllegalArgumentException) {
            // Already unregistered; nothing to do.
        }
    }

    private fun openDrawer() {
        try {
            startActivity(Intent(this, AppDrawerActivity::class.java))
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.could_not_open), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateClock() {
        binding.clockText.text =
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }

    private fun updateDate() {
        binding.dateText.text =
            SimpleDateFormat("EEE • dd MMM", Locale.getDefault()).format(Date())
                .uppercase(Locale.getDefault())
    }

    private fun updateWifi() {
        val onWifi = try {
            val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val caps = cm.activeNetwork?.let { cm.getNetworkCapabilities(it) }
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        } catch (e: Exception) {
            false
        }
        binding.wifiText.text = if (onWifi) "WiFi Connected" else "WiFi Off"
    }

    private fun deviceLine(): String {
        val model = try {
            Build.MODEL ?: "Android"
        } catch (e: Exception) {
            "Android"
        }
        return "$model • v1.0"
    }

    private fun refreshFavorites() {
        val byPkg = AppRepository.getApps(this).associateBy { it.packageName }
        val favs = FavoritesStore.get(this).mapNotNull { byPkg[it] }
        binding.favoritesList.adapter = AppGridAdapter(
            onClick = { app ->
                if (!AppLauncher.launch(this, app)) {
                    Toast.makeText(this, getString(R.string.could_not_open), Toast.LENGTH_SHORT).show()
                }
            },
            onLongClick = { app ->
                FavoritesStore.remove(this, app.packageName)
                refreshFavorites()
                true
            }
        ).apply { submit(favs) }
    }

    private fun refreshDock() {
        val slots = listOf(
            binding.dockSlot0,
            binding.dockSlot1,
            binding.dockSlot2,
            binding.dockSlot3
        )
        slots.forEachIndexed { index, button ->
            val pkg = DockStore.get(this, index) ?: defaultDockPackage(index).also {
                // Remember the resolved default so it survives re-installs of the target.
                if (it != null) DockStore.set(this, index, it)
            }
            setDockButton(button, pkg)
            button.setOnClickListener {
                val target = DockStore.get(this, index) ?: defaultDockPackage(index)
                if (target == null || !AppLauncher.launchPackage(this, target)) {
                    Toast.makeText(this, getString(R.string.could_not_open), Toast.LENGTH_SHORT).show()
                }
            }
            button.setOnLongClickListener {
                drawerPickLauncher.launch(
                    Intent(this, AppDrawerActivity::class.java)
                        .putExtra(AppDrawerActivity.EXTRA_PICK_MODE, true)
                        .putExtra(AppDrawerActivity.EXTRA_RESULT_SLOT, index)
                )
                true
            }
        }
    }

    private fun setDockButton(button: ImageButton, packageName: String?) {
        val icon = if (packageName != null) {
            try {
                IconCache.get(this, packageName)
            } catch (e: Exception) {
                null
            }
        } else null
        button.setImageDrawable(
            icon ?: try {
                getDrawable(android.R.drawable.sym_def_app_icon)
            } catch (e: Exception) {
                null
            }
        )
    }

    /** Best-effort defaults: dialer, SMS, browser, camera. Never hardcoded packages. */
    private fun defaultDockPackage(slot: Int): String? {
        val intent = when (slot) {
            0 -> Intent(Intent.ACTION_DIAL)
            1 -> Intent(Intent.ACTION_SENDTO, "smsto:".toUri())
            2 -> Intent(Intent.ACTION_VIEW, "https://www.google.com".toUri())
            3 -> Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            else -> return null
        }
        return try {
            packageManager.resolveActivity(intent, 0)?.activityInfo?.packageName
        } catch (e: Exception) {
            null
        }
    }
}
