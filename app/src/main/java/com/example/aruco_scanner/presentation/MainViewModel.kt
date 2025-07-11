package com.example.aruco_scanner.presentation

import android.graphics.PointF
import android.os.SystemClock
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aruco_scanner.utils.MarkerUtils
import kotlinx.coroutines.*

data class ScanUIState(
    val markerId: Int? = null,
    val message: String? = null,
    val showBanner: Boolean = false,
    val isScanning: Boolean = true
)

class MainViewModel : ViewModel() {

    var uiState by mutableStateOf(ScanUIState())
        private set

    var overlayCorners by mutableStateOf<List<PointF>>(emptyList())
        private set

    private var lastDetections = mutableListOf<Pair<Int, Long>>()
    private var scanPauseJob: Job? = null

    fun onMarkersDetected(markerId: Int, corners: List<PointF>?) {
        if (!uiState.isScanning) return

        val now = SystemClock.elapsedRealtime()
        lastDetections.add(markerId to now)
        if (lastDetections.size > 3) lastDetections.removeAt(0)

        corners?.let { overlayCorners = it }

        val ids = lastDetections.map { it.first }
        val times = lastDetections.map { it.second }

        if (ids.distinct().size == 1 && (times.last() - times.first()) < 300) {
            onScanSuccess(markerId, MarkerUtils.decodeMarkerId(markerId))
        }
    }

    private fun onScanSuccess(markerId: Int, decoded: String) {
        uiState = uiState.copy(
            markerId = markerId,
            message = decoded,
            showBanner = true,
            isScanning = false
        )

        scanPauseJob?.cancel()
        scanPauseJob = viewModelScope.launch {
            delay(2000)
            uiState = uiState.copy(showBanner = false, isScanning = true)
            lastDetections.clear()
            overlayCorners = emptyList()
        }
    }

    fun clearOverlay() {
        overlayCorners = emptyList()
    }
}
