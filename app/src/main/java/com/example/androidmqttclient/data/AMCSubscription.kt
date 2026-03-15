package com.example.androidmqttclient.data

/**
 * Data class for representing MQTT subscriptions.
 */
data class AMCSubscription (
    val qos: Int,
    val topic: String,
    val color: Long
)