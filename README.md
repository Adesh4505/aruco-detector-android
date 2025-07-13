# ArUco Auto Scanner (Android – Jetpack Compose)

This app continuously scans ArUco markers using CameraX and OpenCV. Once a marker is detected consistently in 3 consecutive frames (under 300ms), the app auto-decodes it, vibrates, highlights it with a green box, and displays a success banner with the decoded message.

## 🔧 Tech Stack
- Kotlin + Jetpack Compose
- OpenCV Android SDK
- CameraX (Preview + ImageAnalysis)
- Clean MVVM Architecture

## 💡 Features
- Auto marker detection (no tap needed)
- 300ms debounce window
- Green highlight and success toast
- Vibration on scan complete
- Marker ID decoding via map

## 🔑 Marker Decoding
- ID → Message map via `MarkerUtils.kt`
- Example:
    - ID 42 → "You found the answer"
    - ID 100 → "Access granted"

## 📱 Requirements
- Android 10+ (minSdk 24)
- Camera permission

##  Build
```bash
./gradlew assembleRelease
