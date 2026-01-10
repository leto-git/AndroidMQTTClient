package com.example.androidmqttclient.data

/**
 * [MQTTUiState] is the data class that is used to represent the current state of the MQTT client.
 */
data class MQTTUiState(
    val isConnected: Boolean = false,
    val subscriptions: List<Subscription> = listOf(),
    val messages: List<String> = listOf(),
)
