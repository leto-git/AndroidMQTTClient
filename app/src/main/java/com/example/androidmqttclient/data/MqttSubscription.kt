package com.example.androidmqttclient.data

/**
 * [MqttSubscription] is the data class that is used to represent a subscription.
 */
data class MqttSubscription (
    val qos: Int,
    val topic: String,
    val color: Long
)