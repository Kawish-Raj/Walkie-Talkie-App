package com.example.nearbyconnectionpractise

import android.hardware.Sensor
import android.hardware.SensorManager
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
import com.example.nearbyconnectionpractise.ui.TestScreen
import com.example.nearbyconnectionpractise.viewmodel.NearbyViewModel

enum class WalkieTalkieScreens() {
    HOMESCREEN,
    MESSAGE_SCREEN,
    AUDIO_SCREEN,
    TEST_SCREEN
}


@Composable
fun WalkieTalkieApp(
    nearbyViewModel: NearbyViewModel = viewModel(),
    navController: NavHostController = rememberNavController(),
    sensorManager: SensorManager,
    linearAccelerationSensor: Sensor?
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
        composable(route = WalkieTalkieScreens.TEST_SCREEN.name){
            TestScreen()
        }

        composable(route = WalkieTalkieScreens.HOMESCREEN.name) {
            HomeScreen(
                homeUiState = homeUiState,
                connectionConfirmation = connectionConfirmation,
                notInitiateConnection = { nearbyViewModel.nonInitiateConnection()},

                /********************** For Separate Advertising and Discovering Buttons ****************/
//                onAcceptConnection = {it -> nearbyViewModel.acceptConnection(it)},
//                onRejectConnection = {it -> nearbyViewModel.rejectConnection(it)},
//                onStartAdvertising = { nearbyViewModel.startAdvertising() },
//                onStartDiscovering = { nearbyViewModel.startDiscovery() },
                onAcceptConnection = {},
                onRejectConnection = {},
                onStartAdvertising = {},
                onStartDiscovering = {},

                /******************* For CONNECTING ONLY BUTTON ********************/
                onStartConnecting = { nearbyViewModel.startConnecting() },
                onStopConnecting = { nearbyViewModel.stopConnecting()},
                navigateToMessageScreen = {navController.navigate(WalkieTalkieScreens.MESSAGE_SCREEN.name)},
                navigateToAudioScreen = {navController.navigate(WalkieTalkieScreens.AUDIO_SCREEN.name){
                    popUpTo(WalkieTalkieScreens.HOMESCREEN.name) {
                        inclusive = true  // this removes the HomeScreen as well
                    }
                    launchSingleTop = true  // prevents multiple copies of the same screen
                } },
                linearAccelerationSensor = linearAccelerationSensor,
                sensorManager = sensorManager
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
                homeUiState = homeUiState,
                startSendAudioStream = { nearbyViewModel.startSendingAudioStream() },
                stopSendAudioStream = { nearbyViewModel.stopSendingAudioStream() },
                disconnect = { nearbyViewModel.disconnect()},
                navigateToHomeScreen = {navController.navigate(WalkieTalkieScreens.HOMESCREEN.name){
                    popUpTo(WalkieTalkieScreens.AUDIO_SCREEN.name) {
                        inclusive = true  // this removes the HomeScreen as well
                    }
                    launchSingleTop = true  // prevents multiple copies of the same screen
                }
                }
            )
        }
    }


}