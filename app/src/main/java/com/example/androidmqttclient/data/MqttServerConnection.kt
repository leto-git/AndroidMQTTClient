package com.example.androidmqttclient.data

/**
 * [MQTTVersion] is used to represent the MQTT version.
 */
enum class MQTTVersion {
    V3_1_1,
    V5
}

/**
 * [MqttServerConnection] is the data class that is used to represent a server connection.
 */
data class MqttServerConnection(
    // Is client connected to this server?
    val isConnected: Boolean = false,
    // MQTT version
    val mqttVersion: MQTTVersion,

    // Connection parameters
    val serverName: String,
    val serverAddress: String,
    val serverPort: Int,
    val clientID: String,
    val username: String,
    val password: String,
    val keepAlive: Int,
    val cleanSession: Boolean,
    val willQos: Int,
    val willRetain: Boolean,
    val willTopic: String,
    val willMessage: String
)
