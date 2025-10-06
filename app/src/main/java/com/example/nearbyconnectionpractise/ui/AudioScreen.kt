package com.example.nearbyconnectionpractise.ui

import android.graphics.BlurMaskFilter
import android.graphics.LinearGradient
import android.graphics.Shader
import android.util.Log
import android.view.KeyEvent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun AudioScreen(
    startSendAudioStream: () -> Unit,
    stopSendAudioStream: () -> Unit,
    modifier: Modifier = Modifier
){
    var isPressed by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    val verticalLineAnimationProgress = remember { Animatable(0f) }
    val bottomLineAnimationProgress = remember { Animatable(0f) }
    val topLineAnimationProgress = remember { Animatable(0f) }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.toFloat()
    val screenHeight = configuration.screenHeightDp.toFloat()

    // Compute ratio (width / height)
    val ratio = screenWidth / screenHeight

    LaunchedEffect(isPressed) {
        val vertAnimationDuration = 150
        if(isPressed){
            bottomLineAnimationProgress.animateTo(1f, tween((vertAnimationDuration * ratio).toInt()))
            verticalLineAnimationProgress.animateTo(1f, tween(vertAnimationDuration))
            topLineAnimationProgress.animateTo(1f, tween((vertAnimationDuration * ratio).toInt()))
        }
        else{
            topLineAnimationProgress.animateTo(0f,tween((vertAnimationDuration * ratio).toInt()))
            verticalLineAnimationProgress.animateTo(0f,tween(vertAnimationDuration))
            bottomLineAnimationProgress.animateTo(0f,tween((vertAnimationDuration * ratio).toInt()))
        }
    }

    SiriCanvas(
        bottomLineAnimationProgress,
        verticalLineAnimationProgress,
        topLineAnimationProgress
    )

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable(true)
            .onKeyEvent { event ->
                when (event.nativeKeyEvent.keyCode) {
                    KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN -> {
                        when (event.nativeKeyEvent.action) {
                            KeyEvent.ACTION_DOWN -> {
                                if(!isPressed){
                                    isPressed = true
                                    startSendAudioStream()
                                }
                            }
                            KeyEvent.ACTION_UP -> {
                                if(isPressed){
                                    isPressed = false
                                    stopSendAudioStream()
                                }
                            }
                        }
                        true
                    }
                    else -> false
                }
            }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
        ) {
            Card(
                modifier = Modifier
                    .size(320.dp)
                    .padding(8.dp)
                    .graphicsLayer {
                        clip = true;
                        shape = WaveyCircleShape()
                    }
            ) {

            }
//            if(!isSending){
//                Button(
//                    onClick = { startSendAudioStream()}
//                ) {
//                    Text("Press To Talk")
//                }
//            } else {
//                Button(
//                    onClick = { stopSendAudioStream()}
//                ) {
//                    Text("Press To Stop")
//                }
//            }
            PushToTalkButton(
                isPressed = isPressed,
                onPress = {
                    isPressed = true
                    startSendAudioStream()
                    Log.d("Content Value", "START audio stream")
                },
                onRelease = {
                    isPressed = false
                    stopSendAudioStream()
                    Log.d("Content Value", "STOP audio stream")
                }
            )

        }
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }

}

@Composable
fun PushToTalkButton(
    isPressed: Boolean,
    onPress: () -> Unit,
    onRelease: () -> Unit
) {
    Surface(
        tonalElevation = 3.dp,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(16.dp)
            .size(150.dp, 50.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onPress()
                        try { awaitRelease() } catch (_: Exception) {}
                        onRelease()
                    }
                )
            }
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(if (isPressed) "Talking..." else "Hold to Talk")
        }
    }
}

/******************************************************************************************
 ------------------------------  SIRI ANIMATION LOGIC -------------------------------------
 ******************************************************************************************
 */

@Composable
fun SiriCanvas(
    bottomLineAnimationProgress: Animatable<Float, AnimationVector1D>,
    verticalLineAnimationProgress: Animatable<Float, AnimationVector1D>,
    topLineAnimationProgress: Animatable<Float, AnimationVector1D>){
    Canvas (modifier = Modifier.fillMaxSize())  {

        val strokeWidth = 8.dp.toPx()
        val radius = size.minDimension / 2 - strokeWidth

        // Create a rotating sweep gradient
        val brush = Brush.verticalGradient(
            colors = listOf(Color.Cyan, Color.Magenta, Color.Blue, Color.Cyan),
        )


        // Add a blurred outer glow
        drawIntoCanvas {
            val paint = Paint().asFrameworkPaint()
            paint.shader = LinearGradient(
                0f,0f,
                0f,size.height,
                intArrayOf(
                    android.graphics.Color.CYAN,
                    android.graphics.Color.MAGENTA,
                    android.graphics.Color.BLUE,
//                        android.graphics.Color.CYAN
                ),
                null,
                Shader.TileMode.CLAMP
            )
            paint.style = android.graphics.Paint.Style.STROKE
            paint.strokeWidth = strokeWidth + 24.dp.toPx()
            paint.maskFilter = BlurMaskFilter(65f, BlurMaskFilter.Blur.NORMAL)

            // BOTTOM LINE
            it.nativeCanvas.drawLine(
                (size.width/2)-((size.width/2)*bottomLineAnimationProgress.value),
                size.height,
                (size.width/2)+((size.width/2)*bottomLineAnimationProgress.value),
                size.height,
                paint
            )

            // LEFT LINE
            it.nativeCanvas.drawLine(
                0F,
                size.height,
                0F,
                (size.height-(size.height*verticalLineAnimationProgress.value)),
                paint
            )

            //RIGHT LINE
            it.nativeCanvas.drawLine(
                size.width,
                size.height,
                size.width,
                (size.height-(size.height*verticalLineAnimationProgress.value)),
                paint
            )

            //TOP LINE 1
            it.nativeCanvas.drawLine(
                0f,
                0f,
                ((size.width/2)*topLineAnimationProgress.value),
                0f,
                paint
            )

            //TOP LINE 2
            it.nativeCanvas.drawLine(
                size.width,
                0f,
                size.width-((size.width/2)*topLineAnimationProgress.value),
                0f,
                paint
            )
        }
    }
}


@Preview(
    showSystemUi = true,
    showBackground = true
)
@Composable
fun AudioScreenPreview(){
    AudioScreen(
        startSendAudioStream = {},
        stopSendAudioStream = {}
    )
}