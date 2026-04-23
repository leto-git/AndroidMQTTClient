package com.example.androidmqttclient.data

import androidx.compose.ui.graphics.Color

/**
 * Data class for representing MQTT messages.
 */
data class AMCMessage(
    // MQTT message properties
    val topic: String,
    val payload: String,
    val qos: Int,
    val retain: Boolean,
    // Timestamp for arrival or delivery
    val timestamp: Long = System.currentTimeMillis(),
    // Subscription color for message
    var subscriptionColor: Color ?= null
)
