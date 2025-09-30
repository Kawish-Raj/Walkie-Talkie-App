package com.example.nearbyconnectionpractise.ui

import android.nfc.Tag
import android.util.Log
import android.view.RoundedCorner
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun AudioScreen(
    startSendAudioStream: () -> Unit,
    stopSendAudioStream: () -> Unit,
    isSending: Boolean,
    modifier: Modifier = Modifier
){

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
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
                startSendAudioStream,
                stopSendAudioStream
            )

        }
    }

}

@Composable
fun PushToTalkButton(
    startSendAudioStream: () -> Unit,
    stopSendAudioStream: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    Surface (
        tonalElevation = 3.dp,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.primary ,
        modifier = Modifier
            .padding(16.dp)
            .size(150.dp,50.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        startSendAudioStream()
                        Log.d("Content Value", "START audio stream")

                        try { awaitRelease() } catch (_: Exception) {}

                        isPressed = false
                        stopSendAudioStream()
                        Log.d("Content Value", "STOP audio stream")
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
        stopSendAudioStream = {},
        isSending = false
    )
}