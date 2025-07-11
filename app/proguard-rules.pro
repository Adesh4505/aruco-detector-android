# --- OpenCV: Keep all classes and avoid warnings ---
-keep class org.opencv.** { *; }
-dontwarn org.opencv.**

# --- CameraX: Keep all camera classes ---
-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**

# --- Keep Kotlin metadata (for Jetpack Compose) ---
-keep class kotlin.Metadata { *; }
-keepclassmembers class ** {
    @kotlin.Metadata *;
}

# --- Optional: Keep your app's own model/data classes ---
-keep class com.example.aruco_scanner.data.** { *; }
-keep class com.example.aruco_scanner.presentation.** { *; }
