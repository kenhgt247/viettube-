# VietTube — parked-video prototype (experimental)

**This is a test branch. `main` and its latest release are unchanged.**

## What works in this prototype
- The phone launcher opens an adaptive WebView at `https://m.youtube.com/`.
- Back, home, refresh and open-in-YouTube-app controls.
- Full-screen video support through WebChromeClient.
- Video pauses when the Activity loses focus.
- The manifest describes the app as video and declares a parked-app `CAR_LAUNCHER` Activity, **not** a navigation app.

## Important Android Auto limitation (verified Nov 2026)
**Android Auto still does not generally accept video parked apps.** Declaring
`CAR_LAUNCHER` does **not** make this app appear in the Android Auto app list.
Android 17 on the phone alone does not make the feature available: Google's
vehicle compatibility/rollout and app approval remain decisive. The current
Google documentation states games are the only supported Android Auto parked
app category. Do not disguise this app as a navigation, weather or game app.

References:
- https://developer.android.com/training/cars/parked/auto
- https://developer.android.com/training/cars
- https://android-developers.googleblog.com/2026/05/android-for-cars-unifying-platforms-premium-experiences.html

## Test
1. Check out `feature/viettube-parked-video`.
2. Build with Android Studio or `gradle :app:assembleDebug` with Android SDK installed.
3. Install the debug APK (package `vn.viettube.app.beta`, installed separately from the original VietTube) on an Android 17 phone and test YouTube playback
   in the phone UI *while parked*. Embedded YouTube/Google sign-in may be
   restricted by Google; the "Mở YT" button opens the official app.
4. Open Android Auto > Customize launcher. **It is expected not to show
   VietTube on hosts without Google video parked-app support**.
5. If Google later enables support, re-check eligibility, vehicle parked
   state enforcement, Play distribution, app quality, and actual hardware.

## Safety
- This prototype doesn't unlock Android Auto video during driving.
- It contains no bypass, spoofed navigation UI, or speed-permission hack.
- Android Auto parked-app support, when available, must rely on the platform's
  authoritative vehicle state and its own blocking of video while moving.

## Note about signing
The legacy project contains a publicly committed app keystore and credentials.
Do **not** rely on that key for a production release. Rotate signing material
and move secrets out of Git before production distribution.
