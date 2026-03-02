package com.example.androidmqttclient.data

/**
 * [MQTTUiState] is the data class that is used to represent the current state of the MQTT client.
 */
data class MQTTUiState(
    // Connection related properties
    val isConnected: Boolean = false,
    val connectedServer: String = "",
    val serverConnections: List<MqttServerConnection> = listOf(),

    // Subscription related properties
    val subscriptions: List<MqttSubscription> = listOf(),
    val messages: List<String> = listOf(),

    // Status related properties
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
