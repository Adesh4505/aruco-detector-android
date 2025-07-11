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

    private val detector by lazy { ArucoDetector() }

    fun detectMarkers(imageProxy: ImageProxy): MarkerDetectionResult {
        val mat = imageProxyToMat(imageProxy)
        val cornersMatList = mutableListOf<Mat>()
        val ids = Mat()

        detector.detectMarkers(mat, cornersMatList, ids)
        imageProxy.close()

        val corners = cornersMatList.map { mat ->
            val pts = FloatArray(8)
            mat.get(0, 0, pts)
            listOf(
                PointF(pts[0], pts[1]),
                PointF(pts[2], pts[3]),
                PointF(pts[4], pts[5]),
                PointF(pts[6], pts[7])
            )
        }

        return MarkerDetectionResult(ids.toIntList(), corners)
    }

    private fun imageProxyToMat(imageProxy: ImageProxy): Mat {
        val y = imageProxy.planes[0].buffer
        val vu = imageProxy.planes[2].buffer
        val data = ByteArray(y.remaining() + vu.remaining())
        y.get(data, 0, y.remaining())
        vu.get(data, y.remaining(), vu.remaining())

        val yuvImage = android.graphics.YuvImage(data, ImageFormat.NV21, imageProxy.width, imageProxy.height, null)
        val out = java.io.ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, imageProxy.width, imageProxy.height), 100, out)
        val bitmap = BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.size())

        return Mat(bitmap.height, bitmap.width, CvType.CV_8UC4).apply {
            Utils.bitmapToMat(bitmap, this)
            Imgproc.cvtColor(this, this, Imgproc.COLOR_RGBA2RGB)
        }
    }

    private fun Mat.toIntList(): List<Int> {
        val buffer = IntArray(rows())
        get(0, 0, buffer)
        return buffer.toList()
    }
}
