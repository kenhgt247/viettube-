# VietTube Cast — WebLink experiment

This **independent Android phone app** is designed to be displayed via **WebLink Host > Cast**, if the Mitsubishi Destinator Ultimate's Cast function works with the connected Samsung phone. It is **not** an authorized WebLink SDK app, does not show its own tile on WebLink Home, and is **not** an Android Auto video app.

## Features available in the source
- VietTube-branded, landscape-friendly native toolbar and large buttons
- Navigate to the official mobile YouTube website, search or paste a YouTube link
- Save up to 12 favorite YouTube links locally
- Fullscreen support via Android WebChromeClient
- A help dialog explaining how to manually start WebLink Host Cast
- Pause playback when the activity loses the foreground
- No root, no bypass of Android Auto or WebLink restrictions

## Important limitations
- **WebLink Host Cast must be tested separately.** A working WebLink YouTube tile does not prove that Cast can mirror this app.
- The phone and WebLink Host must stay connected; screen must normally remain unlocked.
- Video playback on Android WebView may be blocked by YouTube or WebView integrity policy; no guaranteed login. The built-in WebLink YouTube tile is a known working fallback.
- This app cannot independently tell whether the vehicle is parked. Never use video while driving. The car/WebLink must enforce its own movement restrictions.
- Independent, directly listed WebLink apps require Abalta's SDK/partner process **and** vehicle-maker/head-unit enablement.

## Install test build
The test APK package is `vn.viettube.app.weblink`, distinct from prior VietTube builds.
GitHub workflow: `.github/workflows/weblink-cast-build.yml`.
If the workflow succeeds, the release asset is named `viettube-cast.apk`, tag `viettube-cast-beta`.

## Manual in-car test (parked vehicle only)
1. Install the test APK on the Samsung Android 17 phone; open VietTube Cast and test YouTube website playback.
2. Connect WebLink Host to the Mitsubishi; choose the **green Cast tile** (not the YouTube tile).
3. Grant official WebLink screen-sharing consent; return to VietTube Cast on the phone.
4. Check video image, sound, latency, touchscreen control, and fullscreen.
5. If Cast cannot mirror this app, **stop**. Do not relabel VietTube as navigation or claim direct WebLink support.

## Direct WebLink integration research
Abalta advertises SDK access for Android/iOS/HTML5 application developers and a certification process, but this project does not include the proprietary SDK. Contact Abalta about SDK access, approval for video use while parked and whether Mitsubishi Destinator's WebLink version can load custom app tiles:
- https://abaltatech.com/solutions-weblink/
- https://abaltatech.com/contact-us/
- https://helloweblink.com/how-do-i-get-more-apps/

Ask specifically about availability for **Mitsubishi Destinator Ultimate, Vietnam**, WebLink Host Android 17, and distribution in the OEM-specific catalog.

## Development caution
The repository's legacy signing key was committed publicly. This branch builds only debug APKs with standard debug signing; use newly generated protected signing credentials for any production release. Existing main branch is unchanged.
