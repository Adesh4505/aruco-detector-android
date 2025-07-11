package com.example.aruco_scanner.utils

object MarkerUtils {

    private val markerMap = mapOf(
        5 to "Scan successful!",
        7 to "Lucky number detected",
        23 to "Welcome to the demo!",
        42 to "You found the answer 😉",
        100 to "Access granted"
    )

    fun decodeMarkerId(id: Int): String =
        markerMap[id] ?: "Unknown marker (ID: $id)"
}
