package com.example.aruco_scanner.presentation

import android.graphics.PointF
import android.util.Log
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.aruco_scanner.data.ArUcoDecoder
import java.util.concurrent.Executors

@Composable
fun CameraPreview(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
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

                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                        Log.d("CameraX", " Surface provider set")
                    }

                    val analyzer = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(cameraExecutor) { image ->
                                try {
                                    val result = decoder.detectMarkers(image)

                                    if (result.ids.isNotEmpty() && result.corners.isNotEmpty()) {
                                        Log.d("Aruco", " Detected ID: ${result.ids.first()}")

                                        //  Step 1: Get dimensions
                                        val imageWidth = image.width      // usually rotated
                                        val imageHeight = image.height
                                        val viewWidth = previewView.width
                                        val viewHeight = previewView.height

                                        //  Step 2: Scale + flip coordinates
                                        val scaleX = viewWidth.toFloat() / imageHeight
                                        val scaleY = viewHeight.toFloat() / imageWidth

                                        val scaledCorners = result.corners.first().map { pt ->
                                            PointF(pt.y * scaleX, pt.x * scaleY) // swap X/Y
                                        }

                                        //  Send scaled corners to UI
                                        viewModel.onMarkersDetected(
                                            markerId = result.ids.first(),
                                            corners = scaledCorners
                                        )
                                    } else {
                                        viewModel.clearOverlay() // optional: hide overlay
                                        Log.d("Aruco", "No marker detected")
                                    }

                                    image.close()
                                } catch (e: Exception) {
                                    Log.e("Analyzer", "Exception in analyzer", e)
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

                    Log.d("CameraX", " Camera bound to lifecycle")

                } catch (e: Exception) {
                    Toast.makeText(ctx, "Camera init failed: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e("CameraX", "Camera init error", e)
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = modifier
    )
}