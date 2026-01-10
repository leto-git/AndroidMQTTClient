package com.example.androidmqttclient.data

/**
 * [Subscription] is the data class that is used to represent a subscription.
 */
data class Subscription (
    val qos: Int,
    val topic: String,
    val color: Long
)