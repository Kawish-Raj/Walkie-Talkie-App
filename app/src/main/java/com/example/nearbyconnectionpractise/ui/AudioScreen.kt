package com.example.nearbyconnectionpractise.ui

import android.util.Log
import android.view.KeyEvent
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
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