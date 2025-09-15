package com.example.nearbyconnectionpractise.ui

import android.app.ActivityManager.RunningTaskInfo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun MessageScreen(
    sentMessage: String,
    receivedMessage: String,
    currentMessage: String,
    onCurrentMessageChange: (String) -> Unit,
    sendMessage: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(sentMessage)
        Text(receivedMessage)
        TextField(
            value = currentMessage,
            onValueChange = { newValue ->
                if(newValue.length <= 2000){
                    onCurrentMessageChange(newValue)
                }
                            },
            singleLine = true,
            shape = shapes.large,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Write your message here")},
            keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                    ),
            keyboardActions = KeyboardActions(
                onDone = { sendMessage() }
            )
        )
    }

}

@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
fun MessageScreenPreview(){
    MessageScreen(
        "Hi there sent message",
        "Hello, received message",
        "",
        {},
        {}
    )
}