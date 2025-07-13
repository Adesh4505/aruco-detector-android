package com.example.aruco_scanner.data

import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.PointF
import android.graphics.Rect
import androidx.camera.core.ImageProxy
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.ArucoDetector

data class MarkerDetectionResult(
    val ids: List<Int>,
    val corners: List<List<PointF>>
)

class ArUcoDecoder {

    private val detector: ArucoDetector by lazy {
        ArucoDetector()
    }

    fun detectMarkers(imageProxy: ImageProxy): MarkerDetectionResult {
        val mat = imageProxyToMat(imageProxy)

        val cornersMatList = mutableListOf<Mat>()
        val ids = Mat()

        detector.detectMarkers(mat, cornersMatList, ids)
        imageProxy.close()

        val corners = cornersMatList.mapNotNull { cornerMat ->
            if (cornerMat.empty() || cornerMat.type() != CvType.CV_32FC2) {
                null
            } else {
                val pts = FloatArray(8)
                cornerMat.get(0, 0, pts)
                listOf(
                    PointF(pts[0], pts[1]),
                    PointF(pts[2], pts[3]),
                    PointF(pts[4], pts[5]),
                    PointF(pts[6], pts[7])
                )
            }
        }

        return MarkerDetectionResult(ids.toIntList(), corners)
    }


    private fun imageProxyToMat(imageProxy: ImageProxy): Mat {
        val yBuffer = imageProxy.planes[0].buffer
        val vuBuffer = imageProxy.planes[2].buffer

        val ySize = yBuffer.remaining()
        val vuSize = vuBuffer.remaining()

        val nv21 = ByteArray(ySize + vuSize)
        yBuffer.get(nv21, 0, ySize)
        vuBuffer.get(nv21, ySize, vuSize)

        val yuvImage = android.graphics.YuvImage(nv21, ImageFormat.NV21, imageProxy.width, imageProxy.height, null)
        val out = java.io.ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, imageProxy.width, imageProxy.height), 100, out)
        val jpegBytes = out.toByteArray()

        val bitmap = BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)
        val mat = Mat(bitmap.height, bitmap.width, CvType.CV_8UC4)
        Utils.bitmapToMat(bitmap, mat)
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGBA2RGB)

        return mat
    }

    private fun Mat.toIntList(): List<Int> {
        val buffer = IntArray(this.rows())
        this.get(0, 0, buffer)
        return buffer.toList()
    }
}