package com.example.nearbyconnectionpractise

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.nearbyconnectionpractise.ui.AudioScreen
import com.example.nearbyconnectionpractise.ui.HomeScreen
import com.example.nearbyconnectionpractise.ui.MessageScreen
import com.example.nearbyconnectionpractise.viewmodel.NearbyViewModel

enum class WalkieTalkieScreens() {
    HOMESCREEN,
    MESSAGE_SCREEN,
    AUDIO_SCREEN
}


@Composable
fun WalkieTalkieApp(
    nearbyViewModel: NearbyViewModel = viewModel(),
    navController: NavHostController = rememberNavController()
) {
    val homeUiState by nearbyViewModel.homeUiState.collectAsState()
    val messageUiState by nearbyViewModel.messageUiState.collectAsState()
    val audioUiState by nearbyViewModel.audioUiState.collectAsState()
    val connectionConfirmation by nearbyViewModel.connectionConfirmation.collectAsState()

//    val backStackEntry by navController.currentBackStackEntryAsState()

    NavHost(
        navController = navController,
        startDestination = WalkieTalkieScreens.HOMESCREEN.name,
        modifier = Modifier
    ){
        composable(route = WalkieTalkieScreens.HOMESCREEN.name) {
            HomeScreen(
                homeUiState = homeUiState,
                connectionConfirmation = connectionConfirmation,
                onAcceptConnection = {it -> nearbyViewModel.acceptConnection(it)},
                onRejectConnection = {it -> nearbyViewModel.rejectConnection(it)},
                onStartAdvertising = { nearbyViewModel.startAdvertising() },
                onStartDiscovering = { nearbyViewModel.startDiscovery() },
                navigateToMessageScreen = {navController.navigate(WalkieTalkieScreens.MESSAGE_SCREEN.name)},
                navigateToAudioScreen = {navController.navigate(WalkieTalkieScreens.AUDIO_SCREEN.name)}
            )
        }

        composable(route = WalkieTalkieScreens.MESSAGE_SCREEN.name) {
            MessageScreen(
                sentMessage = messageUiState.lastSentMessage,
                receivedMessage = messageUiState.lastReceivedMessage,
                currentMessage = messageUiState.currentMessage,
                sendMessage = {nearbyViewModel.sendMessage()},
                onCurrentMessageChange = {nearbyViewModel.onCurrMessageChange(it)}
            )
        }

        composable(route = WalkieTalkieScreens.AUDIO_SCREEN.name) {
            AudioScreen(
                startSendAudioStream = { nearbyViewModel.startSendingAudioStream() },
                stopSendAudioStream = { nearbyViewModel.stopSendingAudioStream() },
            )
        }
    }


}