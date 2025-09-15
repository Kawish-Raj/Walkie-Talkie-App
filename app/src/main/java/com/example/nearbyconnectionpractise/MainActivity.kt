package com.example.nearbyconnectionpractise

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import com.example.nearbyconnectionpractise.ui.HomeScreen
import com.example.nearbyconnectionpractise.ui.theme.NearByConnectionPractiseTheme
import com.example.nearbyconnectionpractise.viewmodel.NearbyViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var showPermissionDialog by rememberSaveable { mutableStateOf(false) }

            NearByConnectionPractiseTheme {

                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    WalkieTalkieApp()

                    if (showPermissionDialog) {
                        PermissionDeniedDialog(
                            onGoToSettings = {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", packageName, null)
                                }
                                startActivity(intent)
                            },
                            onExitApp = { finish() }
                        )
                    }
                }
            }

            // launch permission check when UI is ready
            requestPermissions(
                onDenied = { showPermissionDialog = true }
            )
        }
    }

    private val requestMultiplePermissionsLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.entries.all { it.value }

            if (allGranted) {
                Log.i("Permissions", "All granted")
            } else {
                Log.i("Permissions", "Some denied")
                permissions.entries.forEach{
                    entry ->
                    if(!entry.value){
                        Log.w("Permissions","Permission denied: ${entry.key}")
                    }
                }
                // Instead of finish(), trigger Compose state
                deniedCallback?.invoke()
            }
        }

    private var deniedCallback: (() -> Unit)? = null

    private fun requestPermissions(onDenied: () -> Unit) {
        deniedCallback = onDenied

        val permissionsToRequest = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.NEARBY_WIFI_DEVICES
        )

        val notGranted = permissionsToRequest.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (notGranted.isNotEmpty()) {
            requestMultiplePermissionsLauncher.launch(notGranted.toTypedArray())
        } else {
            Log.i("Permissions", "All already granted")
        }
    }
}

@Composable
fun PermissionDeniedDialog(
    onGoToSettings: () -> Unit,
    onExitApp: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {}, // prevent dismiss on outside tap
        title = { Text(stringResource(R.string.permissions_required)) },
        text = { Text(stringResource(R.string.permission_requirement_explanation)) },
        confirmButton = {
            TextButton(onClick = onGoToSettings) {
                Text(stringResource(R.string.go_to_settings))
            }
        },
        dismissButton = {
            TextButton(onClick = onExitApp) {
                Text(stringResource(R.string.exit_app))
            }
        }
    )
}
