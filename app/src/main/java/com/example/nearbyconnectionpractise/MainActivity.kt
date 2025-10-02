package com.example.nearbyconnectionpractise

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor // CHANGE HERE
import android.hardware.SensorEvent // CHANGE HERE
import android.hardware.SensorEventListener // CHANGE HERE
import android.hardware.SensorManager // CHANGE HERE
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import kotlin.math.sqrt // CHANGE HERE

class MainActivity : ComponentActivity() { // CHANGE HERE (implements SensorEventListener)

    // CHANGE HERE (sensor variables)
    private lateinit var sensorManager: SensorManager
    private var linearAccelerationSensor: Sensor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // CHANGE HERE (initialize sensor manager + sensor)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        linearAccelerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

        setContent {
            var showPermissionDialog by rememberSaveable { mutableStateOf(false) }

            NearByConnectionPractiseTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    WalkieTalkieApp(
                        linearAccelerationSensor = linearAccelerationSensor,
                        sensorManager = sensorManager
                    )

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

//    // CHANGE HERE (register sensor when activity is visible)
//    override fun onResume() {
//        super.onResume()
//        linearAccelerationSensor?.also { sensor ->
//            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
//        }
//    }
//
//    // CHANGE HERE (unregister to save battery)
//    override fun onPause() {
//        super.onPause()
//        sensorManager.unregisterListener(this)
//    }
//
//    // CHANGE HERE (handle sensor data)
//    override fun onSensorChanged(event: SensorEvent?) {
//        if (event?.sensor?.type == Sensor.TYPE_LINEAR_ACCELERATION) {
//            val x = event.values[0]
//            val y = event.values[1]
//            val z = event.values[2]
//
//            // Compute magnitude of acceleration
//            val magnitude = sqrt(x * x + y * y + z * z)
//
//            if(magnitude > 12.0){
//                sensorManager.unregisterListener(this)
//            }
//
//            Log.d("SensorDemo", "Acceleration Magnitude: $magnitude m/s²")
//        }
//    }

//    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
//        // Not needed for now
//    }

    private val requestMultiplePermissionsLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.entries.all { it.value }

            if (allGranted) {
                Log.i("Permissions", "All granted")
            } else {
                Log.i("Permissions", "Some denied")
                permissions.entries.forEach { entry ->
                    if (!entry.value) {
                        Log.w("Permissions", "Permission denied: ${entry.key}")
                    }
                }
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
            Manifest.permission.NEARBY_WIFI_DEVICES,
            Manifest.permission.RECORD_AUDIO
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
