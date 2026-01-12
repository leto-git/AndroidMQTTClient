package com.example.androidmqttclient.data

/**
 * [MqttMessage] is a data class for representing MQTT messages.
 */
data class MqttMessage(
    val topic: String,
    val qos: Int,
    val retain: Boolean,
    val message: String
)
