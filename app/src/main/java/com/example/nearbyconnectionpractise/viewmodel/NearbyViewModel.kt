package com.example.nearbyconnectionpractise.viewmodel

import android.app.Application
import android.content.ContentValues.TAG
import android.os.Build
import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.example.nearbyconnectionpractise.ui.ConnectionConfirmation
import com.example.nearbyconnectionpractise.ui.HomeUiState
import com.example.nearbyconnectionpractise.ui.MessageUiState
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID


enum class DeviceConnectionStatus {
    NOT_INITIATED,
    ADVERTISING,
    DISCOVERING,
    CONNECTED
}

class NearbyViewModel(application: Application): AndroidViewModel(application) {

    private val _homeUiState = MutableStateFlow(HomeUiState())
    private val _connectionConfirmation = MutableStateFlow<ConnectionConfirmation?>(null)
    private val _messageUiState = MutableStateFlow(MessageUiState())

    val homeUiState: StateFlow<HomeUiState> = _homeUiState.asStateFlow()
    val connectionConfirmation: StateFlow<ConnectionConfirmation?> = _connectionConfirmation.asStateFlow()
    val messageUiState = _messageUiState.asStateFlow()

    private  val connectionsClient = Nearby.getConnectionsClient(application.applicationContext)
    private val SERVICE_ID: String = application.packageName
    private val USERNAME: String = "Kawish's " + "${Build.MODEL} " + UUID.randomUUID().toString().take(4)

    private var connectedEndpointId: String? = null

    fun startAdvertising() {
        val advertisingOption = AdvertisingOptions.Builder()
            .setStrategy(Strategy.P2P_POINT_TO_POINT)
            .build()

        connectionsClient
            .startAdvertising(
                USERNAME,
                SERVICE_ID,
                connectionLifecycleCallback,
                advertisingOption
            )
            .addOnSuccessListener {
                // Advertising started!
                _homeUiState.update { currentState ->
                    currentState.copy(
                        deviceConnectionStatus = DeviceConnectionStatus.ADVERTISING
                    )
                }
                Log.d(TAG, "Advertising Started")
            }
            .addOnFailureListener { e ->
                // Advertising failed.
                Log.d(TAG, "Advertising Failed")
                e.printStackTrace()
            }
    }

    fun startDiscovery() {
        val discoveryOptions =
            DiscoveryOptions.Builder().setStrategy(Strategy.P2P_POINT_TO_POINT).build()
        connectionsClient
            .startDiscovery(SERVICE_ID, endpointDiscoveryCallback, discoveryOptions)
            .addOnSuccessListener{
                // Discovery started!
                _homeUiState.update { currentState ->
                    currentState.copy(
                        deviceConnectionStatus = DeviceConnectionStatus.DISCOVERING
                    )
                }
                Log.d(TAG, "Discovery Started")
            }
            .addOnFailureListener { e: Exception ->
                // Discovery failed.
                Log.d(TAG, "Discovery failed")
                e.printStackTrace()
            }
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            Log.d(TAG, "onConnectionInitiated")
            _connectionConfirmation.value = ConnectionConfirmation(
                endpointId = endpointId,
                endpointName = info.endpointName,
                authenticationDigits = info.authenticationDigits
            )
        }

        override fun onConnectionResult(endpointId: String, resolution: ConnectionResolution) {
            Log.d(TAG, "onConnectionResult")

            when (resolution.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    connectedEndpointId = endpointId
                    _homeUiState.update { currentState ->
                        currentState.copy(
                            deviceConnectionStatus = DeviceConnectionStatus.CONNECTED
                        )
                    }
                    Log.d(TAG, "ConnectionsStatusCodes.STATUS_OK")
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    Log.d(TAG, "ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED")
                }
                ConnectionsStatusCodes.STATUS_ERROR -> {
                    Log.d(TAG, "ConnectionsStatusCodes.STATUS_ERROR")
                }
                else -> {
                    Log.d(TAG, "Unknown status code ${resolution.status.statusCode}")
                }
            }
        }

        override fun onDisconnected(endpointId: String) {
            Log.d(TAG, "onDisconnected")
        }
    }


    fun acceptConnection(endpointId: String){
        connectionsClient.acceptConnection(endpointId,payloadCallback)
        _connectionConfirmation.value = null
    }

    fun rejectConnection(endpointId: String){
        connectionsClient.rejectConnection(endpointId)
        _connectionConfirmation.value = null
    }


    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            connectionsClient
                .requestConnection(USERNAME, endpointId, connectionLifecycleCallback)
                .addOnSuccessListener {
                    // We successfully requested a connection. Now both sides
                    // must accept before the connection is established.
                    Log.d(TAG, "We successfully requested a connection. " +
                            "Now both sides must accept before the connection is established.")
                }
                .addOnFailureListener { e ->
                    // Nearby Connections failed to request the connection.
                    Log.d(TAG, "Nearby connections failed to request the connection.")
                    e.printStackTrace()
                }
        }

        override fun onEndpointLost(endpointId: String) {
            Log.d(TAG, "onEndpointLost")
        }
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            // Placeholder: just log what kind of payload we got
            Log.d(TAG, "onPayloadReceived from $endpointId: ${payload.type}")
            if(payload.type == Payload.Type.BYTES){
                val receivedBytes = payload.asBytes()!!
                val receivedString = String(receivedBytes)
                _messageUiState.update { currState ->
                    currState.copy(
                        lastReceivedMessage = "Your Friend: $receivedString"
                    )
                }
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            // Placeholder: log transfer status
            Log.d(TAG, "onPayloadTransferUpdate from $endpointId: status=${update.status}")
        }
    }

    fun sendMessage(){
        val bytesPayload = Payload.fromBytes(_messageUiState.value.currentMessage.toByteArray())
        if(connectedEndpointId != null){
            connectionsClient.sendPayload(connectedEndpointId!!,bytesPayload)
            _messageUiState.update { currState ->
                currState.copy(
                    lastSentMessage = "You: ${currState.currentMessage}",
                    currentMessage = ""
                )
            }
        }
        else{
            _messageUiState.update {currState ->
                currState.copy(
                    lastSentMessage = "Error sending message, most likely endpointId is null")
            }
        }
    }

    fun onCurrMessageChange(updatedMessage: String){
        _messageUiState.update { currState ->
            currState.copy(
                currentMessage = updatedMessage
            )
        }
    }

}