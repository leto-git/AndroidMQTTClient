package com.example.androidmqttclient.data.repository

import android.content.Context
import android.util.Log
import com.example.androidmqttclient.data.MqttServerConnection
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

/**
 * [MQTTRepository] is the repository for the MQTT client.
 * It is responsible for managing the MQTT client and its connection.
 */
class MQTTRepository(private val context: Context) {

    private var mqttClient: MqttClient? = null
    private val tag: String = "MQTTRepository"

    /**
     * Connect to a server.
     * @param connection The connection object containing the server information.
     */
    fun connect(connection: MqttServerConnection): Result<Unit> {
        try {
            // Build server address
            val address = if (connection.serverAddress.contains("://"))
                "${connection.serverAddress}:${connection.serverPort}"
            else
                // Prepend protocol if not present
                "tcp://${connection.serverAddress}:${connection.serverPort}"

            // Translate connection to MqttConnectOptions
            val options = MqttConnectOptions().apply {
                mqttVersion = MqttConnectOptions.MQTT_VERSION_3_1_1
                isAutomaticReconnect = true
                isCleanSession = connection.cleanSession
                keepAliveInterval = connection.keepAlive

                if(connection.username.isNotBlank()) {
                    userName = connection.username
                }
                if(connection.password.isNotBlank()) {
                    password = connection.password.toCharArray()
                }

                if(connection.willTopic.isNotBlank()) {
                    setWill(
                        connection.willTopic,
                        connection.willMessage.toByteArray(),
                        connection.willQos,
                        connection.willRetain)
                }
            }

            // Connect to server
            Log.d(tag, "Connecting to $address")
            mqttClient = MqttClient(
                address,
                connection.clientID,
                MemoryPersistence()
            )
            mqttClient?.connect(options)
            Log.d(tag, "Connected to $address")

            return Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Error connecting to server ${connection.serverName}", e)

            return Result.failure(e)
        }
    }

    /**
     * Disconnect from the server.
     */
    fun disconnect() {
        try {
            mqttClient?.disconnect()
            Log.d(tag, "Disconnected")
        } catch (e: Exception) {
            Log.e(tag, "Error disconnecting", e)
        }
    }

    /**
     * Subscribe to a topic.
     * @param topic The topic to subscribe to.
     * @param qos The quality of service to use when subscribing.
     */
    fun subscribe(topic: String, qos: Int) {
        try {
            mqttClient?.subscribe(topic, qos)
        } catch (e: Exception) {
            Log.e(tag, "Error subscribing to $topic", e)
        }
    }

    /**
     * Unsubscribe from a topic.
     * @param topic The topic to unsubscribe from.
     */
    fun unsubscribe(topic: String) {
        try {
            mqttClient?.unsubscribe(topic)
        } catch (e: Exception) {
            Log.e(tag, "Error unsubscribing from $topic", e)
        }
    }

    /**
     * [publish] publishes a message.
     * @param topic The topic to publish to.
     * @param message The message to publish.
     * @param qos The quality of service to use when publishing.
     * @param retain Whether the message should be retained.
     */
    fun publish(topic: String, message: String, qos: Int, retain: Boolean) {
        TODO("Not yet implemented")
    }
}