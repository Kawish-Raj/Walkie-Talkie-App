package com.example.nearbyconnectionpractise.ui

import WavyShape
import android.bluetooth.BluetoothClass.Device
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.nfc.Tag
import android.util.Log
import androidx.compose.animation.core.Ease
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseInOutElastic
import androidx.compose.animation.core.EaseOutBounce
import androidx.compose.animation.core.EaseOutElastic
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nearbyconnectionpractise.ui.theme.NearByConnectionPractiseTheme
import com.example.nearbyconnectionpractise.viewmodel.DeviceConnectionStatus
import com.example.nearbyconnectionpractise.viewmodel.NearbyViewModel
import kotlin.math.sqrt

@Composable
fun HomeScreen(
    homeUiState: HomeUiState,
    connectionConfirmation: ConnectionConfirmation?,
    onStartAdvertising: () -> Unit,
    onStartDiscovering: () -> Unit,
    onStartConnecting: () -> Unit,
    onAcceptConnection: (endpointId: String) -> Unit,
    onRejectConnection: (endpointId: String) -> Unit,
    navigateToMessageScreen: () -> Unit,
    navigateToAudioScreen: () -> Unit,
    sensorManager: SensorManager,
    linearAccelerationSensor: Sensor?,
    modifier: Modifier = Modifier){

    val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event?.sensor?.type == Sensor.TYPE_LINEAR_ACCELERATION) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val magnitude = sqrt(x * x + y * y + z * z)
                if (magnitude > 12.0) {
                    sensorManager.unregisterListener(this)
                    onStartConnecting()
                }
                Log.d("SensorDemo", "Acceleration Magnitude: $magnitude m/s²")
            }
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }
    LaunchedEffect(Unit) {
        sensorManager.registerListener(listener, linearAccelerationSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    val animateRotation: Float by animateFloatAsState(
        targetValue = if(homeUiState.deviceConnectionStatus != DeviceConnectionStatus.DISCOVERING
            && homeUiState.deviceConnectionStatus != DeviceConnectionStatus.CONNECTING){
            0f
        }else{
            720f
        },
        animationSpec = repeatable(
            iterations = Int.MAX_VALUE,
            animation = tween(
                durationMillis = 20000,
                easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        )
    )

//    Log.i("Information","screenWidth: ${LocalConfiguration.current.screenWidthDp.dp}")

    val scaleRatio = (LocalConfiguration.current.screenWidthDp.dp/320.dp)

    val animateScale: Float by animateFloatAsState(
        targetValue = if(homeUiState.deviceConnectionStatus != DeviceConnectionStatus.ADVERTISING){
            1f
        }else{
            scaleRatio
        },
        animationSpec = repeatable(
            iterations = Int.MAX_VALUE,
            animation = tween(
                durationMillis = ((1000/374f)* LocalConfiguration.current.screenWidthDp).toInt(),
                easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        )
    )



    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
        ){
            Card(
                modifier = Modifier
                    .size(320.dp)
                    .padding(8.dp)
                    .graphicsLayer {
                        rotationZ = animateRotation
                        scaleX = animateScale
                        scaleY = animateScale
                        clip = (homeUiState.deviceConnectionStatus == DeviceConnectionStatus.DISCOVERING
                                || homeUiState.deviceConnectionStatus == DeviceConnectionStatus.ADVERTISING
                                || homeUiState.deviceConnectionStatus == DeviceConnectionStatus.CONNECTING
                                || homeUiState.deviceConnectionStatus == DeviceConnectionStatus.CONNECTED);
                        shape = WaveyCircleShape()
                    }
            ) {
                connectionConfirmation?.let { request ->
                    AlertDialog(
                        onDismissRequest = { },
                        title = { Text("Accept connection to ${request.endpointName}") },
                        text = { Text("Confirm code: ${request.authenticationDigits}") },
                        confirmButton = {
                            TextButton(onClick = { onAcceptConnection(request.endpointId) }) {
                                Text("Accept")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { onRejectConnection(request.endpointId) }) {
                                Text("Reject")
                            }
                        }
                    )
                }
            }
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .size(320.dp)
            ) {
                when(homeUiState.deviceConnectionStatus){
                    DeviceConnectionStatus.NOT_INITIATED -> OpeningOptionsCard(
                        {onStartAdvertising()},
                        {onStartDiscovering()},
                        {onStartConnecting()})
                    DeviceConnectionStatus.CONNECTING -> ConnectingCard(message = "Connecting to nearby devices...")
                    DeviceConnectionStatus.DISCOVERING -> ConnectingCard(message = "Discovering nearby devices...")
                    DeviceConnectionStatus.ADVERTISING -> ConnectingCard(message = "Advertising to nearby devices...")
                    DeviceConnectionStatus.CONNECTED -> {
                        navigateToAudioScreen()
//                        ConnectedCard()
//                        Button(
//                            onClick = { navigateToMessageScreen()}
//                        ) {
//                            Text("Start Chating")
//                        }
//                        Button(
//                            onClick = { navigateToAudioScreen()}
//                        ) {
//                            Text("Start Call")
//                        }
                    }
                }
            }

        }
    }
}

@Composable
fun ConnectingCard(modifier: Modifier = Modifier, message: String) {
    Text(
        text = message,
        fontSize = 24.sp,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .padding(4.dp)
    )
}

@Composable
fun ConnectedCard(modifier: Modifier = Modifier) {
    Text(
        text = "Connected",
        fontSize = 32.sp,
        modifier = modifier
            .padding(4.dp)
    )
}

@Composable
fun OpeningOptionsCard(startAdvertising: () -> Unit,
                       startDiscovering: () -> Unit,
                       startConnecting: () -> Unit,
                       modifier: Modifier = Modifier){

    /********** For Seperate Advertising and Discovering Buttons **************/
//    Button(
//        onClick = startAdvertising,
//        modifier = Modifier
//    ) {
//        Text(
//            text = "Advertise",
//            fontSize = 32.sp,
//            modifier = Modifier.padding(4.dp)
//        )
//    }
//
//    Spacer(
//        modifier = Modifier.size(32.dp)
//    )
//
//    Button(
//        onClick = startDiscovering,
//        modifier = Modifier
//    ) {
//        Text(
//            text = "Discover",
//            fontSize = 32.sp,
//            modifier = Modifier.padding(4.dp)
//        )
//    }
//    Spacer(
//        modifier = Modifier.size(32.dp)
//    )
    Button(
        onClick = startConnecting,
        modifier = Modifier
    ) {
        Text(
            text = "Connect",
            fontSize = 32.sp,
            modifier = Modifier.padding(4.dp)
        )
    }

}

//@Preview(showBackground = true,
//    showSystemUi = true)
//@Composable
//fun HomeScreenPreview() {
//    val fakeState = HomeUiState(
//        deviceConnectionStatus = DeviceConnectionStatus.CONNECTING
//    )
//    val connectionConfirmation: ConnectionConfirmation? = null
//    HomeScreen(
//        modifier = Modifier,
//        connectionConfirmation = connectionConfirmation,
//        homeUiState = fakeState,
//        onAcceptConnection = {},
//        onRejectConnection = {},
//        onStartAdvertising = {},
//        onStartDiscovering = {},
//        onStartConnecting = {},
//        navigateToMessageScreen = {},
//        navigateToAudioScreen = {},
//        linearAccelerationSensor = null,
//        sensorManager = null
//    )
//}