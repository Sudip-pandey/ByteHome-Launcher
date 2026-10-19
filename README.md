# ByteHome

**ByteHome Version 1.0** — a personal, lightweight Android launcher optimized for low/mid-range hardware.

ByteHome is a real HOME launcher (not a mock UI): set it as the default launcher and the Android Home button returns to ByteHome every time.

## Overview

ByteHome replaces the stock launcher with a fast, dark "personal cyber HUD" home screen: a large digital clock, date, battery and Wi-Fi status, favorite apps, a configurable dock, and an app drawer with A–Z navigation and instant search. It is built for stability first — every app launch and icon load is defensive, so one broken app can never crash the launcher.

## Features

- **Real launcher role** — `CATEGORY_HOME` intent filter, `singleTask` launch mode; Home button returns to ByteHome without crashing.
- **Home screen** — large clock (updates once per minute), date, battery %, Wi-Fi indicator, device line, favorites row, dock, drawer/search/settings actions, subtle BYTEHOME branding.
- **Fast app drawer** — alphabetically sorted grid, A–Z side index for instant jumps, instant case-insensitive local search. The app list is loaded once and cached; `PackageManager` is never queried while scrolling.
- **ByteHome Icon System** — every app icon sits in an identical lightweight frame (consistent size, spacing, border); original icons are never distorted. Icons are decoded once and held in an `LruCache`.
- **Configurable dock** — 4 slots + app-drawer button. Defaults are resolved from system intents (dialer, SMS, browser, camera) — never hardcoded packages. Long-press any slot to assign any installed app; assignments persist.
- **Favorites** — pin up to 8 apps to the home screen (long-press in the drawer, or the + button). Long-press a favorite to remove it.
- **Settings** — "Choose Default Launcher" (system Home-role request with a settings fallback, so you can always switch back), plus dock/favorites reset.
- **Zero permissions** — ByteHome requests no permissions at all.

## Performance goals

Target device: **Oppo A57 — 4 GB RAM, MediaTek Helio G35**. Design rules for V1:

- Kotlin + XML layouts + `RecyclerView`. No Jetpack Compose, no animation frameworks.
- Minimal dependencies: `core-ktx`, `appcompat`, `recyclerview`, `constraintlayout`, `lifecycle-runtime-ktx`.
- No background services, no sensors, no network calls, no blur/shadows/3D, no per-frame work.
- Clock ticks once per minute; battery receiver is registered only while the home screen is visible.
- Static tiled-grid background drawn once by the GPU; item animations disabled in the drawer.
- Defensive coding throughout: null-safe package info, `try/catch` around every icon load and app launch, graceful handling of uninstalled/disabled apps and corrupt icon resources.

## Supported Android version

- `minSdk 26` (Android 8.0), `targetSdk 34`, `compileSdk 34`.

## Target device

Oppo A57 (4 GB RAM, Helio G35). The launcher must stay usable under memory pressure; lists and icon caches are bounded.

## Build instructions

Requirements: JDK 17, Android SDK (API 34), Gradle 8.7+.

```bash
# Clone, then build the debug APK:
gradle assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

> Note: this repository does not ship the Gradle wrapper JAR (binary). If you
> prefer the wrapper, install Gradle 8.7+ once and run `gradle wrapper` locally.
> CI builds use the `gradle/actions/setup-gradle` action and do not need it.

## GitHub Actions

`.github/workflows/android.yml` runs on every push to `main` and on manual dispatch:

1. Checkout → 2. Temurin JDK 17 → 3. Android SDK → 4. Gradle 8.7 via `setup-gradle`
5. `gradle assembleDebug` → 6. Uploads the APK as the **`ByteHome-V1-debug-apk`** artifact.

## Installation

1. Open the latest successful workflow run → **Artifacts** → download `ByteHome-V1-debug-apk`.
2. Transfer the APK to the phone and install it (allow "install unknown apps" for your file manager/browser when prompted).
3. Open ByteHome once, then press the Home button and choose ByteHome as the default launcher.

## How to set ByteHome as default launcher

- Press the **Home** button → select **ByteHome** → "Always".
- Or: ByteHome → **Settings** → **Choose Default Launcher**.

## How to switch back to the system launcher

- ByteHome → **Settings** → **Choose Default Launcher** → pick your system launcher (e.g. Oppo's System Launcher).
- Or: Android **Settings → Apps → Default apps → Home app** → choose the system launcher.
- ByteHome never locks you in: the system Home settings are always one tap away.

## Project structure

```
app/src/main/
├── AndroidManifest.xml                 # HOME intent filter, package-change receiver
├── java/com/pandey/bytehome/
│   ├── MainActivity.kt                 # Home screen: clock, status, favorites, dock
│   ├── AppDrawerActivity.kt            # Drawer + A–Z index + search (+ picker mode)
│   ├── SettingsActivity.kt             # Default-launcher choice, reset
│   ├── PackageChangeReceiver.kt        # Invalidates the app cache on install/remove
│   ├── data/
│   │   ├── AppInfo.kt                  # label / package / component (no drawables)
│   │   ├── AppRepository.kt            # single cached app list, loaded once
│   │   ├── FavoritesStore.kt           # home favorites (SharedPreferences)
│   │   └── DockStore.kt                # dock assignments (SharedPreferences)
│   ├── ui/
│   │   ├── AppGridAdapter.kt           # shared grid adapter (drawer + favorites)
│   │   └── AlphaIndexView.kt           # A–Z fast-scroll strip (custom View)
│   └── util/
│       ├── IconCache.kt                # LruCache of decoded app icons
│       └── AppLauncher.kt              # defensive app launching (never crashes)
└── res/
    ├── layout/                         # activity_main/drawer/settings, item_app
    ├── drawable/                       # grid tile, icon frame, dock bg, icon art
    ├── mipmap-anydpi-v26/              # adaptive launcher icon (vector, no PNGs)
    └── values/                         # cyber-HUD colors, strings, theme
```

## Known limitations

- No widgets, no live wallpapers, no icon-pack support in V1 (by design — scope is speed + stability).
- The app drawer uses `notifyDataSetChanged()`; lists are small enough that this is not a bottleneck.
- Physical testing was done via build validation; on-device testing on the Oppo A57 (Home-button behavior, drawer smoothness under memory pressure) is still recommended.
- Not tested on tablets, foldables, or landscape — the layout is portrait-phone first.

## Future roadmap

- Optional widget host (opt-in, off by default)
- Per-app hidden apps list
- Backup/restore of dock + favorites
- Optional icon-pack support
- Landscape/tablet layout pass

---

Personal project by Sudip Pandey. Built for one phone, kept deliberately small.
