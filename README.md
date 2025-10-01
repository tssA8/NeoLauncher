# NeoLauncher

Android home screen launcher with a dynamic, customizable layout.
Focused on smooth performance, simple extensibility, and production-ready defaults.

## Features

* Custom bottom tool list — implemented with a canvas-based view for flexible rendering and hit-testing.

* All Apps → Hotseat drag & drop — long-press any app in All Apps and place it into the bottom hotseat.

* Default shortcuts & widgets seeding — optional programmatic seeding on first run (e.g., Search widget, common apps).

* Compose-ready surfaces — optional Jetpack Compose widgets (e.g., Note display) embedded via ComposeView.

* Lightweight, performant — minimal overdraw, careful main-thread work, and lazy data loading.

## Pre-requisites

* Android Studio (AGP 7.2+)

* Android minSdk 26, targetSdk 35, compileSdk 34

* Android Build Tools 30.0.3+

* Kotlin & Jetpack libraries (optional Compose integration)

## How to use
# Build & install (debug)
./gradlew :app:installDebug

# (ADB) Set NeoLauncher as the default Home (Android 10+)
* adb shell cmd package set-home-activity com.pt.ifp.neolauncher/.activity.HomeActivity

# Clear current Home preference to re-prompt chooser
* adb shell cmd package clear-preferred-activities com.pt.ifp.neolauncher

* Optional: programmatically seed defaults on first run

* Seed a Search widget and common apps to the first page/hotseat.

* Example entry point: call your applyDefaultShortcuts(...) during profile bootstrap after permissions are granted.

* Optional: open the Note editor (Activity Result)

* From the home surface, launch NoteEditActivity and update the on-screen Note when the user taps Save.

## License

Copyright 2025 GuoHsaun Tseng

