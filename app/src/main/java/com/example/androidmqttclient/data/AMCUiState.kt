package com.example.androidmqttclient.data

/**
 * Data class for representing the UI state of the MQTT client.
 */
data class AMCUiState(
    // Connection related properties
    val isConnected: Boolean = false,
    val connectedServer: String = "",
    val serverConnections: List<AMCServerConnection> = listOf(),

    // Subscription related properties
    val subscriptions: List<AMCSubscription> = listOf(),
    val receivedMessages: List<AMCMessage> = listOf(),

    // Status related properties
    val isConnecting: Boolean = false,
    val isSubscribing: Boolean = false,
    val isPublishing: Boolean = false,
    val errorMessage: String? = null
)
