# ITKODERS Translate

Android-first automatic on-screen translator. Target language: Uzbek.

## Current MVP
- Native Kotlin Android app.
- User-controlled Accessibility Service for reading text exposed by the active app's accessibility tree.
- Floating translation panel drawn over other apps (requires explicit user approval).
- Automatic source-language detection through the translation provider.
- Translation history and manual text translation.
- Clear pause/stop controls and per-app exclusions.

## Important limitations
Android does not allow unrestricted silent reading of every app screen. This MVP only reads text exposed through Android accessibility APIs after the user explicitly enables the service. Secure/DRM windows, canvas-rendered text, many games, and apps that hide accessibility nodes cannot be translated. Translation requires an internet connection and the configured MyMemory endpoint may rate-limit requests. Do not use this app to collect credentials or private content.

## Build APK
Push to GitHub. The workflow in `.github/workflows/android.yml` builds a debug APK and uploads it as an artifact. Open Actions → Android CI → latest successful run → Artifacts.

## Local build
Open this repository in Android Studio and build the `:app:assembleDebug` task.

## Privacy
Text exposed by the enabled accessibility service is sent to the configured translation provider only when translation is enabled. Review the provider's privacy terms before use. The app includes a persistent notification while its accessibility service is active.
