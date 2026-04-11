package com.example.androidmqttclient.data

/**
 * Data class for representing the UI state of the MQTT client.
 */
data class AMCUiState(
    // Connection related properties
    val isConnected: Boolean = false,
    val connectedServer: AMCServerConnection? = null,
    val serverConnections: List<AMCServerConnection> = listOf(),

    // Subscription related properties
    val subscriptions: List<AMCSubscription> = listOf(),
    val receivedMessages: List<AMCMessage> = listOf(),

    // Publish related properties
    val publishedMessages: List<AMCMessage> = listOf(),

    // Status related properties
    val isConnecting: Boolean = false,
    val isSubscribing: Boolean = false,
    val isUnsubscribing: Boolean = false,
    val isPublishing: Boolean = false,

    // Error and info messages displayed to the user as snackBar
    val errorMessage: String? = null,
    val infoMessage: String? = null,

    val logMessages: List<AMCLogEntry> = listOf()
)
