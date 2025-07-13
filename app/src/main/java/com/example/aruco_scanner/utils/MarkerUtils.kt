package com.example.aruco_scanner.utils

object MarkerUtils {

    private val markerMap = mapOf(
        23 to "Welcome to the demo!",
        42 to "You found the answer 😉",
        100 to "Access granted",
        7 to "Lucky number detected",
        5 to "Scan successful!"
    )

    fun decodeMarkerId(id: Int): String {
        return markerMap[id] ?: "Unknown marker (ID: $id)"
    }
}