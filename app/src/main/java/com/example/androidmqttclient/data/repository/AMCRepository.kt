package com.example.androidmqttclient.data.repository

import android.util.Log
import com.example.androidmqttclient.data.AMCMessage
import com.example.androidmqttclient.data.AMCServerConnection
import com.example.androidmqttclient.data.AMCSubscription
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

/**
 * Repository for the MQTT client.
 *
 * It is responsible for managing the MQTT client and its connection.
 */
class AMCRepository() {
    // MQTT client instance
    private var mqttClient: MqttClient? = null
    // Tag for logging
    private val tag: String = "MQTTRepository"
    // Shared flow for incoming messages
    private val _incomingMessages = MutableSharedFlow<AMCMessage>(
        replay = 0,
        extraBufferCapacity = 32,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    // Expose shared flow as read-only property
    val incomingMessages = _incomingMessages.asSharedFlow()
    // Coroutine scope for repository operations
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * Connect to a server.
     *
     * @param connection The connection object containing the server information.
     *
     * @return A [Result] object indicating the success or failure of the connection.
     */
    suspend fun connect(connection: AMCServerConnection): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Build server address
            val rawAddress = connection.serverAddress
            val prefix = if (rawAddress.contains("://")) "" else "tcp://"
            val fullBaseAddress = "$prefix$rawAddress"

            // Only add port if the address doesn't already have one
            val address = if (fullBaseAddress.substringAfter("://").contains(":")) {
                fullBaseAddress
            } else {
                "$fullBaseAddress:${connection.serverPort}"
            }

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
            // Generate client ID if none is provided
            val clientId = connection.clientID.ifBlank { MqttClient.generateClientId() }

            // Create MQTT client
            mqttClient = MqttClient(
                address,
                clientId,
                MemoryPersistence()
            )

            // Set callback listener for asynchronous events
            mqttClient?.setCallback(object: MqttCallbackExtended {
                override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                    Log.d(tag, "Connected to $serverURI")
                }

                override fun connectionLost(cause: Throwable?) {
                    Log.d(tag, "Connection lost", cause)
                    // TODO: Handle connection loss, show error message
                }

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    Log.d(tag, "Message arrived: $message")
                    // Translate to internal message format
                    val mqttMessage = AMCMessage(
                        topic = topic ?: "",
                        payload = message?.toString() ?: "",
                        qos = message?.qos ?: 0,
                        retain = message?.isRetained ?: false,
                        timestamp = System.currentTimeMillis()
                    )
                    // Emit message to shared flow, which will be collected by the ViewModel
                    repositoryScope.launch {
                        _incomingMessages.emit(mqttMessage)
                    }
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                }
            })

            // Connect to server
            Log.d(tag, "Connecting to $address")
            mqttClient?.connect(options)
            Log.d(tag, "Connected to $address")

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Error connecting to server ${connection.serverName}", e)
            Result.failure(e)
        }
    }

    /**
     * Disconnect from the server.
     *
     * @return A [Result] object indicating the success or failure of the disconnection.
     */
    suspend fun disconnect(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            mqttClient?.disconnect()

            Log.d(tag, "Disconnected")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Error disconnecting", e)
            Result.failure(e)
        }
    }

    /**
     * Subscribe to a topic.
     *
     * @param subscription The subscription object containing the topic and qos.
     *
     * @return A [Result] object indicating the success or failure of the subscription.
     */
    suspend fun subscribe(subscription: AMCSubscription): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            mqttClient?.subscribe(subscription.topic, subscription.qos)

            Log.d(tag, "Subscribed to $subscription.topic")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Error subscribing to $subscription.topic", e)
            Result.failure(e)
        }
    }

    /**
     * Unsubscribe from a topic.
     *
     * @param topic The topic to unsubscribe from.
     *
     * @return A [Result] object indicating the success or failure of the unsubscribe.
     */
    suspend fun unsubscribe(topic: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            mqttClient?.unsubscribe(topic)
            Log.d(tag, "Unsubscribed from $topic")

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Error unsubscribing from $topic", e)
            Result.failure(e)
        }
    }

    /**
     * Publish a message.
     *
     * @param topic The topic to publish to.
     * @param message The message to publish.
     * @param qos The quality of service to use when publishing.
     * @param retain Whether the message should be retained.
     *
     * @return A [Result] object indicating the success or failure of the publish.
     */
    suspend fun publish(
        topic: String,
        message: String,
        qos: Int,
        retain: Boolean
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(tag, "Trying to publish to $topic: $message")
            val mqttMessage = MqttMessage(message.toByteArray()).apply {
                this.qos = qos
                this.isRetained = retain
            }
            mqttClient?.publish(topic, mqttMessage)

            Log.d(tag, "Successfully published.")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Error publishing to $topic", e)
            Result.failure(e)
        }
    }
}