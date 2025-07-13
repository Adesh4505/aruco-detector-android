package com.example.aruco_scanner.presentation

import android.graphics.PointF
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    cameraView: @Composable () -> Unit
) {
    val uiState = viewModel.uiState
    val context = LocalContext.current
    val corners: List<PointF> = viewModel.overlayCorners

    // Trigger vibration on scan success
    LaunchedEffect(uiState.showBanner) {
        if (uiState.showBanner) {
            val vibrator = context.getSystemService<Vibrator>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera feed
        cameraView()

        // Green outline on marker
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (corners.size == 4) {
                val pts = corners.map { point -> Offset(point.x, point.y) }
                drawLine(color = Color.Green, start = pts[0], end = pts[1], strokeWidth = 4f)
                drawLine(color = Color.Green, start = pts[1], end = pts[2], strokeWidth = 4f)
                drawLine(color = Color.Green, start = pts[2], end = pts[3], strokeWidth = 4f)
                drawLine(color = Color.Green, start = pts[3], end = pts[0], strokeWidth = 4f)
            }
        }

        // Success banner
        if (uiState.showBanner && uiState.markerId != null && uiState.message != null) {
            Box(modifier = Modifier.align(Alignment.TopCenter)) {
                Banner(markerId = uiState.markerId, message = uiState.message)
            }
        }
    }
}

@Composable
fun Banner(markerId: Int, message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(Color(0xFF4CAF50))
            .padding(16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column {
            Text(
                text = "Scan complete",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )
            Text(text = "ID: $markerId", color = Color.White)
            Text(text = message, color = Color.White)
        }
    }
}