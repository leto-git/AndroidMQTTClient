package com.example.androidmqttclient.data

/**
 * Data class for representing MQTT messages.
 */
data class MqttMessage(
    val topic: String,
    val message: String,
    val qos: Int,
    val retain: Boolean
)
