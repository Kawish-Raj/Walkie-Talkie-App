package com.example.nearbyconnectionpractise.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentValues.TAG
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Build
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.util.Log
import androidx.collection.SimpleArrayMap
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.AndroidViewModel
import com.example.nearbyconnectionpractise.ui.AudioUiState
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
import java.io.IOException
import java.util.UUID
import kotlin.collections.containsKey
import kotlin.collections.get


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
    private val _audioUiState = MutableStateFlow(AudioUiState())

    val homeUiState: StateFlow<HomeUiState> = _homeUiState.asStateFlow()
    val connectionConfirmation: StateFlow<ConnectionConfirmation?> = _connectionConfirmation.asStateFlow()
    val messageUiState = _messageUiState.asStateFlow()
    val audioUiState: StateFlow<AudioUiState> = _audioUiState.asStateFlow()

    private  val connectionsClient = Nearby.getConnectionsClient(application.applicationContext)
    private val SERVICE_ID: String = application.packageName
    private val USERNAME: String = "Kawish's " + "${Build.MODEL} " + UUID.randomUUID().toString().take(4)

    private var connectedEndpointId: String? = null


    /***************************************************************************************
     *------------------------CONNECTION ESTABLISHING LOGIC----------------------------------
     ***************************************************************************************/

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
    /*****************************************
     * AUDIO TRACK LOGIC
     */

    private val audioTrack: AudioTrack by lazy {
        AudioTrack(
            AudioAttributes.Builder()
                // SPEAKER LOGIC
//                .setUsage(AudioAttributes.USAGE_MEDIA)
//                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                // LIBRARY LOGIC
                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build(),
            AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build(),
            bufferSize,
            AudioTrack.MODE_STREAM,
            AudioManager.AUDIO_SESSION_ID_GENERATE
        )
    }



    /*************************************************************************************
    ------------------------------- DATA-EXCHANGE LOGIC---------------------------------
     *************************************************************************************/

    private val payloadCallback = object : PayloadCallback() {

        private val backgroundThreads = SimpleArrayMap<Long, Thread>()
        private val READ_STREAM_IN_BG_TIMEOUT = 5000L


        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            // Placeholder: log transfer status
            Log.d(TAG, "onPayloadTransferUpdate from $endpointId: status=${update.status}")
            if (backgroundThreads.containsKey(update.payloadId)
                && update.status != PayloadTransferUpdate.Status.IN_PROGRESS
            ) {
                backgroundThreads[update.payloadId]?.interrupt()
            }
        }

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
            else if (payload.type == Payload.Type.STREAM) {
                val backgroundThread = object : Thread() {
                    override fun run() {
                        val inputStream = payload.asStream()!!.asInputStream()
                        var lastRead = SystemClock.elapsedRealtime()

                        audioTrack.play()

                        while (!isInterrupted) {
                            if ((SystemClock.elapsedRealtime() - lastRead) >= READ_STREAM_IN_BG_TIMEOUT) {
                                Log.e("MyApp", "Read data from stream but timed out.")
                                break
                            }

                            try {
                                val availableBytes = inputStream.available()
                                if (availableBytes > 0) {
                                    val bytes = ByteArray(availableBytes)
                                    if (inputStream.read(bytes) == availableBytes) {
                                        val read = inputStream.read(bytes)
                                        lastRead = SystemClock.elapsedRealtime()
                                        // Do something with bytes here...
                                        Log.d(TAG, "RECEIVED AUDIO BYTES")
                                        audioTrack.write(bytes,0,read)
                                    }
                                } else {
                                    // Sleep or just continue.
                                }
                            } catch (e: IOException) {
                                Log.e("MyApp", "Failed to read bytes from InputStream.", e)
                                break
                            }
                        }
                        stopSpeakerPlayback()
                    }
                }
                backgroundThread.start()
                backgroundThreads.put(payload.id, backgroundThread)
            }
        }

    }

    /**************************************************************************************
    ------------------------------- AUDIO CALL/CHAT LOGIC ---------------------------------
     **************************************************************************************/

    fun stopSpeakerPlayback() {
        try {
            audioTrack.stop()
            audioTrack.release()
        } catch (e: Exception) {
            Log.e("MyApp", "Error stopping audioTrack", e)
        }
    }


    private val sampleRate = 16000 // 16kHz for speech
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)



    @SuppressLint("MissingPermission")
    private val audioRecord = AudioRecord(
        MediaRecorder.AudioSource.MIC,
        sampleRate,
        channelConfig,
        audioFormat,
        bufferSize
    )

    private var sendingThread: Thread? = null

    // Create a stream payload (to send to peer)
    val pfd = ParcelFileDescriptor.createPipe()
    val outputStream = ParcelFileDescriptor.AutoCloseOutputStream(pfd[1])
    fun startSendingAudioStream(){
        val payload = Payload.fromStream(ParcelFileDescriptor.AutoCloseInputStream(pfd[0]))

        connectedEndpointId?.let { connectionsClient.sendPayload(it, payload) }
        // Start capturing and sending audio
        audioRecord.startRecording()
        _audioUiState.update { currState ->
            currState.copy(
                isSending = true
            )
        }

        sendingThread = Thread {
            val buffer = ByteArray(bufferSize)
            while (true) {
                val read = audioRecord.read(buffer, 0, buffer.size)
                if (read > 0) {
                    outputStream.write(buffer, 0, read)
                }
            }
        }
        sendingThread?.start()
    }

    fun stopSendingAudioStream() {
        _audioUiState.update { currState ->
            currState.copy(
                isSending = false
            )
        }
        sendingThread?.interrupt()
        sendingThread = null

        try {
            audioRecord.stop()
            audioRecord.release()
        } catch (e: Exception) {
            Log.e("MyApp", "Error stopping audioRecord", e)
        }

        try {
            outputStream.close()
        } catch (e: Exception) {
            Log.e("MyApp", "Error closing outputStream", e)
        }
    }




    /**************************************************************************************
     --------------------------------- MESSAGING LOGIC -----------------------------------
     **************************************************************************************/

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
