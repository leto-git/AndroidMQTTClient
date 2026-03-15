package com.example.androidmqttclient.data

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
    val timestamp: Long = System.currentTimeMillis()
)
