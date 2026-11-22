package com.ucp.biomass.ar.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.ucp.biomass.ar.presentation.screens.BioMassScreen
import com.ucp.biomass.ar.presentation.theme.BioMassTheme
import com.ucp.biomass.ar.presentation.theme.BioMassVoidBlack
import com.ucp.biomass.ar.presentation.viewmodel.BioMassViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val biomassViewModel: BioMassViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BioMassTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BioMassVoidBlack
                ) {
                    val cameraPermissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission()
                    ) { isGranted ->
                        if (isGranted) {
                            biomassViewModel.onCameraPermissionGranted()
                        }
                    }

                    LaunchedEffect(Unit) {
                        val isGranted = ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED

                        if (isGranted) {
                            biomassViewModel.onCameraPermissionGranted()
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }

                    BioMassScreen(
                        viewModel = biomassViewModel,
                        onRequestCameraPermission = {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    )
                }
            }
        }
    }
}
