package com.example.nearbyconnectionpractise.ui

import com.example.nearbyconnectionpractise.viewmodel.DeviceConnectionStatus

data class HomeUiState(
    val deviceConnectionStatus: DeviceConnectionStatus = DeviceConnectionStatus.DISCONNECTED
)

data class ConnectionConfirmation(
    val endpointId: String,
    val endpointName: String,
    val authenticationDigits: String
)
