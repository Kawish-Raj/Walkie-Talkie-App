package com.example.nearbyconnectionpractise.ui

import WavyShape
import android.bluetooth.BluetoothClass.Device
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.nfc.Tag
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
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
    notInitiateConnection: () -> Unit,
    onStartAdvertising: () -> Unit,
    onStartDiscovering: () -> Unit,
    onStartConnecting: () -> Unit,
    onStopConnecting:() -> Unit,
    onAcceptConnection: (endpointId: String) -> Unit,
    onRejectConnection: (endpointId: String) -> Unit,
    navigateToMessageScreen: () -> Unit,
    navigateToAudioScreen: () -> Unit,
    sensorManager: SensorManager?,
    linearAccelerationSensor: Sensor?,
    modifier: Modifier = Modifier){

    val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event?.sensor?.type == Sensor.TYPE_LINEAR_ACCELERATION) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val magnitude = sqrt(x * x + y * y + z * z)
                if (magnitude > 12.0 && homeUiState.deviceConnectionStatus == DeviceConnectionStatus.NOT_INITIATED) {
                    onStartConnecting()
                    sensorManager?.unregisterListener(this)
                }
                Log.d("SensorDemo", "Acceleration Magnitude: $magnitude m/s²")
            }
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }
    LaunchedEffect(homeUiState) {
        if(homeUiState.deviceConnectionStatus == DeviceConnectionStatus.NOT_INITIATED){
            sensorManager?.registerListener(listener, linearAccelerationSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
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
                        rotationZ = if(homeUiState.deviceConnectionStatus == DeviceConnectionStatus.NOT_INITIATED) 0f
                        else animateRotation
                        scaleX = animateScale
                        scaleY = animateScale
                        clip = (homeUiState.deviceConnectionStatus == DeviceConnectionStatus.DISCOVERING
                                || homeUiState.deviceConnectionStatus == DeviceConnectionStatus.ADVERTISING
                                || homeUiState.deviceConnectionStatus == DeviceConnectionStatus.CONNECTING
                                || homeUiState.deviceConnectionStatus == DeviceConnectionStatus.CONNECTED);
                        shape = WaveyCircleShape()
                    }
                    .alpha(if(homeUiState.deviceConnectionStatus == DeviceConnectionStatus.NOT_INITIATED) 0f else 1f)
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
                modifier = Modifier.fillMaxHeight().width(320.dp)
            ) {
                when(homeUiState.deviceConnectionStatus){
                    DeviceConnectionStatus.NOT_INITIATED -> OpeningCard(
                        {
                            sensorManager?.unregisterListener(listener)
                            onStartConnecting()
                        })
                    DeviceConnectionStatus.DISCONNECTED -> {
                        AlertDialog(
                            onDismissRequest = { notInitiateConnection() },
                            title = {Text("Connection Lost")},
                            text = {Text("Connection Lost due to unknown reasons")},
                            confirmButton = {
                                TextButton(onClick = {
                                    notInitiateConnection()
                                }) {
                                    Text("Ok")
                                }
                            }

                        )
                    }
                    DeviceConnectionStatus.CONNECTING -> ConnectingCard(
                        message = "Connecting to nearby devices...",
                        isConnecting = true,
                        stopConnecting = onStopConnecting
                        )
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
fun ConnectingCard(modifier: Modifier = Modifier,
                   isConnecting: Boolean = false,
                   stopConnecting: () -> Unit = {},
                   message: String) {
    Text(
        text = message,
        fontSize = 24.sp,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .padding(4.dp)
    )
    if(isConnecting){
        Button(
            onClick = {stopConnecting()},
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9e2a2b))
        ) {
            Text("Abandon Search")
        }
    }
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

    /********** For Separate Advertising and Discovering Buttons **************/
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

@Composable
fun OpeningCard(
    startConnecting: () -> Unit,
    modifier: Modifier = Modifier
){

    Card (

    ){
        var visible by remember { mutableStateOf(false) }
        val density = LocalDensity.current

        LaunchedEffect(Unit) {
            visible = true
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxHeight(0.8f).padding(32.dp)
        ){
            val yourPhoneFontSize = 46f

            Column() {
                AnimatedVisibility(
                    visible = visible,
                    enter = slideInVertically(
                        initialOffsetY ={ with(density){40.dp.roundToPx()}}
                    ) + fadeIn(initialAlpha = 0.3f)
                ) {
                    Text("Either",
                        fontSize = 32.sp)
                }

                Text("SHAKE",
                    fontSize = (1.70 * yourPhoneFontSize).sp)
                Text("Your Phone",
                    fontSize = yourPhoneFontSize.sp)
            }


            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Or",
                    fontSize = 32.sp)
                Text("Press",
                    fontSize = 32.sp)
                Text("To",
                    fontSize = 32.sp)
            }

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
    }

}

@Preview(showBackground = true,
    showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    val fakeState = HomeUiState(
        deviceConnectionStatus = DeviceConnectionStatus.NOT_INITIATED
    )
    val connectionConfirmation: ConnectionConfirmation? = null
    HomeScreen(
        modifier = Modifier,
        connectionConfirmation = connectionConfirmation,
        homeUiState = fakeState,
        onAcceptConnection = {},
        onRejectConnection = {},
        onStartAdvertising = {},
        onStartDiscovering = {},
        onStartConnecting = {},
        onStopConnecting = {},
        notInitiateConnection = {},
        navigateToMessageScreen = {},
        navigateToAudioScreen = {},
        linearAccelerationSensor = null,
        sensorManager = null
    )
}