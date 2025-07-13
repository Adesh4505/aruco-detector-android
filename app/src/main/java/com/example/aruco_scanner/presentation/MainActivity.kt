package com.example.aruco_scanner.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import org.opencv.android.OpenCVLoader

class MainActivity : ComponentActivity() {

    private val CAMERA_PERMISSION_CODE = 100

    private var permissionRequested = false // Prevent repeated requests

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!OpenCVLoader.initDebug()) {
            Toast.makeText(this, "OpenCV failed to load", Toast.LENGTH_SHORT).show()
            return
        }

        setContent {
            var hasPermission by remember {
                mutableStateOf(
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                )
            }

            LaunchedEffect(Unit) {
                if (!hasPermission && !permissionRequested) {
                    permissionRequested = true
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(Manifest.permission.CAMERA),
                        CAMERA_PERMISSION_CODE
                    )
                }
            }

            if (hasPermission) {
                val viewModel: MainViewModel = viewModel()
                MainScreen(viewModel = viewModel) {
                    CameraPreview(viewModel, Modifier.fillMaxSize())
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Please grant camera permission to continue.")
                }
            }

            DisposableEffect(Unit) {
                val listener = object : ActivityCompat.OnRequestPermissionsResultCallback {
                    override fun onRequestPermissionsResult(
                        requestCode: Int,
                        permissions: Array<out String>,
                        grantResults: IntArray
                    ) {
                        if (requestCode == CAMERA_PERMISSION_CODE &&
                            grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED
                        ) {
                            hasPermission = true
                        }
                    }
                }

                // required to keep DisposableEffect happy
                onDispose { }
            }
        }
    }
}
