package com.example.androidmqttclient.data.repository

import android.content.Context

/**
 * [MQTTRepository] is the repository for the MQTT client.
 * It is responsible for managing the MQTT client and its connection.
 */
class MQTTRepository(private val context: Context) {
    // TODO: Paho client implementation
    // private var mqttClient: MqttClient = null

    fun subscribe(topic: String, qos: Int) {
        TODO("Not yet implemented")
    }

    fun unsubscribe(topic: String) {
        TODO("Not yet implemented")
    }
}