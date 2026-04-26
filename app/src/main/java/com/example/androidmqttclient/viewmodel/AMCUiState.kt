package com.example.androidmqttclient.viewmodel

import com.example.androidmqttclient.data.model.MQTTConnectionState
import com.example.androidmqttclient.data.model.AMCLogEntry
import com.example.androidmqttclient.data.model.AMCMessage
import com.example.androidmqttclient.data.model.AMCServerConnection
import com.example.androidmqttclient.data.model.AMCSubscription

/**
 * Data class for representing the UI state of the MQTT client.
 */
data class AMCUiState(
    // Connection related properties
    val connectedServer: AMCServerConnection? = null,
    val connectionState: MQTTConnectionState = MQTTConnectionState.DISCONNECTED,

    val serverConnections: List<AMCServerConnection> = listOf(),

    // Subscription related properties
    val activeSubscriptions: List<AMCSubscription> = listOf(),

    val receivedMessagesLimit: Int = 200,
    val numReceivedMessages: Int = 0,
    val receivedMessages: List<AMCMessage> = listOf(),

    // Publish related properties
    val publishTopic: String = "",
    val publishQos: Int = 0,
    val publishRetain: Boolean = false,
    val publishMessage: String = "",

    val publishedMessagesLimit: Int = 200,
    val numPublishedMessages: Int = 0,
    val publishedMessages: List<AMCMessage> = listOf(),

    // Status related properties
    val isSubscribing: Boolean = false,
    val isUnsubscribing: Boolean = false,
    val isPublishing: Boolean = false,

    // Status messages and logging
    val errorMessage: String? = null,
    val infoMessage: String? = null,

    val logMessagesLimit: Int = 200,
    val logMessages: List<AMCLogEntry> = listOf()
)