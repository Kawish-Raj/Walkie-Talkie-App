package com.example.nearbyconnectionpractise.ui

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOutBounce
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nearbyconnectionpractise.viewmodel.DeviceConnectionStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
        val density = LocalDensity.current
        var visible by remember { mutableStateOf(false) }


        /********** animation vars *****************/
        var animateEitherAlpha = remember { Animatable(0f) }
        var animateShakeAlpha = remember { Animatable(0f) }
        var animateYourAlpha = remember { Animatable(0f) }
        var animatePhoneAlpha = remember {Animatable(0f)}


        var animateEitherTranslation = remember {Animatable((with(density){40.dp.roundToPx()}).toFloat())}
        var animateShakeTranslationX = remember { Animatable((with(density){60.dp.roundToPx()}).toFloat()) }
        var animateYourTranslationY = remember { Animatable((with(density){-80.dp.roundToPx()}).toFloat()) }
        var animatePhoneTranslationY = remember { Animatable((with(density){-80.dp.roundToPx()}).toFloat())}


        /**************** Together when visible animation logic **************************/

        val stiffnessVal = 50f

        val slideInFromBottom by animateFloatAsState(
            targetValue = if(visible) 0f else (with(density){-40.dp.roundToPx()}).toFloat(),
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = stiffnessVal
            )
        )

        val slideInFromTop by animateFloatAsState(
            targetValue = if(visible) 0f else (with(density){40.dp.roundToPx()}).toFloat(),
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = stiffnessVal
            )
        )

        val animateSHAKE by animateFloatAsState(
            targetValue = if(visible) 0f else (with(density){60.dp.roundToPx()}).toFloat(),
            animationSpec = spring(
                dampingRatio = 0.08f,
                stiffness = 150f
            )
        )

        val slideInFromLEFT by animateFloatAsState(
            targetValue = if(visible) 0f else (with(density){-40.dp.roundToPx()}).toFloat(),
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = stiffnessVal
            )
        )
        val slideInFromRIGHT by animateFloatAsState(
            targetValue = if(visible) 0f else (with(density){40.dp.roundToPx()}).toFloat(),
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = stiffnessVal
            )
        )
        val slideInAlpha by animateFloatAsState(
            targetValue = if(visible) 1f else 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = stiffnessVal
            )
        )
        val appearWhenVisible by animateFloatAsState(
            targetValue = if(visible) 1f else 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = stiffnessVal
            )
        )

        /************* Delays ***************/
        val eitherDelay = 250
        val shakeDelay = 100L
        val yourDelay = 300
        val phoneDelay = 0

        /************** Durations ****************/
        val eitherDuration = 300
        val shakeDuration = 300
        val yourDuration = 400
        val phoneDuration = 400

        val launchWaiting = (eitherDelay + eitherDuration + shakeDelay + shakeDuration).toInt()


        LaunchedEffect(Unit) {
            delay(100)
            visible  = true
            launch {
                animateEitherAlpha.animateTo(1f,
                    animationSpec = tween(
                        delayMillis = eitherDelay,
                        durationMillis = eitherDuration,
                        easing = CubicBezierEasing(0.3f,0.8f,0.3f, 1f)
                    ))
                delay(shakeDelay)
                animateShakeAlpha.animateTo(1f,
                    )
            }
            launch {
                animateEitherTranslation.animateTo(0f,
                    animationSpec = tween(
                        delayMillis = eitherDelay,
                        durationMillis = eitherDuration,
                        easing = CubicBezierEasing(0.3f,0.8f,0.6f, 3.3f)
                    ))
                delay(shakeDelay)
                animateShakeTranslationX.animateTo(0f,
                    animationSpec = spring(
                        dampingRatio = 0.08f,
                        stiffness = 150f
                    )
                )
            }
            launch {
                delay((launchWaiting + yourDelay).toLong())
                animateYourAlpha.animateTo(1f)
                delay((yourDuration - 100).toLong())
                animatePhoneAlpha.animateTo(1f)
            }
            animateYourTranslationY.animateTo(0f,
                animationSpec = tween(
                    delayMillis = launchWaiting + yourDelay,
                    durationMillis = yourDuration,
                    easing = EaseOutBounce
                ))

            animatePhoneTranslationY.animateTo(0f,
                animationSpec = tween(
                    delayMillis = phoneDelay,
                    durationMillis = phoneDuration,
                    easing = EaseOutBounce
                ))

        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxHeight(0.8f).padding(32.dp)
        ){
            val yourPhoneFontSize = 46f

            Column(
            ) {
                Text("Either",
                    fontSize = 32.sp,
                    modifier = Modifier.graphicsLayer(
                        translationY = slideInFromBottom,
                        alpha = slideInAlpha
                    ))

                Text("SHAKE",
                    fontSize = (1.70 * yourPhoneFontSize).sp,
                    modifier = Modifier.graphicsLayer(
//                        translationX = animateSHAKE,
                        alpha = slideInAlpha
                    ))
                    Row {
                        Text("Your ",
                            fontSize = yourPhoneFontSize.sp,
                            modifier = Modifier.graphicsLayer(
                                translationY = slideInFromTop,
                                alpha = slideInAlpha
                            ))
                        Text("Phone",
                            fontSize = yourPhoneFontSize.sp,
                            modifier = Modifier.graphicsLayer(
                                translationY = slideInFromTop,
                                alpha = slideInAlpha
                            )
                        )
                    }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                    Text("Or",
                        fontSize = 32.sp,
                        modifier = Modifier
                            .graphicsLayer(
                                translationX = slideInFromLEFT,
                                alpha = slideInAlpha
                            ))
                    Text("Press",
                        fontSize = 32.sp,
                        modifier = Modifier
                            .graphicsLayer(
                                translationX = slideInFromRIGHT,
                                alpha = slideInAlpha
                            ))
                    Text("To",
                        fontSize = 32.sp,
                        modifier = Modifier
                            .graphicsLayer(
                                translationX = slideInFromLEFT,
                                alpha = slideInAlpha
                            ))
            }

            Button(
                onClick = startConnecting,
                modifier = Modifier.graphicsLayer(
                    alpha = appearWhenVisible
                )
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