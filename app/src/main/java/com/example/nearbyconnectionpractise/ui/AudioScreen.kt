package com.example.nearbyconnectionpractise.ui

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
import com.example.nearbyconnectionpractise.viewmodel.DeviceConnectionStatus

@Composable
fun AudioScreen(
    startSendAudioStream: () -> Unit,
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
            Button(
                onClick = { startSendAudioStream()}
            ) {
                Text("Press To Talk")
            }
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
        startSendAudioStream = {}
    )
}