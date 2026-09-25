package com.example

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.service.ElixirOverlayService
import com.example.ui.elixir.ElixirMainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.ElixirViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ElixirViewModel by viewModels()

    private val screenCaptureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            ElixirOverlayService.startService(this, result.resultCode, result.data!!)
            viewModel.setScreenCaptureActive(true)
        } else {
            viewModel.setScreenCaptureActive(false)
            viewModel.engine.updateVisionStatus("Screen Capture permission was declined")
        }
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        // Proceed with screen capture
        launchScreenCaptureIntent()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                ElixirMainScreen(
                    viewModel = viewModel,
                    onStartScreenCapture = { requestAndStartCapture() },
                    onStopScreenCapture = { stopCapture() },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    private fun requestAndStartCapture() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }
        launchScreenCaptureIntent()
    }

    private fun launchScreenCaptureIntent() {
        val mpManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
        if (mpManager != null) {
            screenCaptureLauncher.launch(mpManager.createScreenCaptureIntent())
        }
    }

    private fun stopCapture() {
        ElixirOverlayService.stopService(this)
        viewModel.setScreenCaptureActive(false)
    }
}
