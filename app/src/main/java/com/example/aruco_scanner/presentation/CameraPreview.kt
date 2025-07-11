package com.example.aruco_scanner.presentation

import android.graphics.PointF
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.aruco_scanner.data.ArUcoDecoder
import java.util.concurrent.Executors

@Composable
fun CameraPreview(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val decoder = ArUcoDecoder()

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }

            val cameraExecutor = Executors.newSingleThreadExecutor()
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().apply {
                        setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val analyzer = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .apply {
                            setAnalyzer(cameraExecutor) { image ->
                                try {
                                    val result = decoder.detectMarkers(image)

                                    if (result.ids.isNotEmpty() && result.corners.isNotEmpty()) {
                                        val (w, h) = previewView.width to previewView.height
                                        val (iw, ih) = image.width to image.height
                                        val scaleX = w.toFloat() / ih
                                        val scaleY = h.toFloat() / iw

                                        val scaledCorners = result.corners.first().map {
                                            PointF(it.y * scaleX, it.x * scaleY)
                                        }

                                        viewModel.onMarkersDetected(result.ids.first(), scaledCorners)
                                    } else {
                                        viewModel.clearOverlay()
                                    }

                                    image.close()
                                } catch (e: Exception) {
                                    image.close()
                                }
                            }
                        }

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        analyzer
                    )

                } catch (e: Exception) {
                    Toast.makeText(ctx, "Camera init failed", Toast.LENGTH_LONG).show()
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = modifier
    )
}
