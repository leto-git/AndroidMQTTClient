package com.example.androidmqttclient.data.repository

import android.util.Log
import com.example.androidmqttclient.data.AMCMessage
import org.eclipse.paho.client.mqttv3.MqttClient
import com.example.androidmqttclient.data.AMCServerConnection
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import kotlin.text.toByteArray

/**
 * Wrapper around the Paho MQTT client for V3.1.1.
 */
class MqttV3ClientWrapper(
    private val connection: AMCServerConnection,
    private val serverUri: String,
    private val onConnectComplete: (AMCServerConnection, Boolean, String?) -> Unit,
    private val onConnectionLost: (Throwable?) -> Unit,
    private val onMessageArrived: (String, ByteArray, Int, Boolean) -> Unit,
): MqttClientWrapper {

    private var client: MqttClient? = null

    private val tag = "MQTTV3ClientWrapper"

    override fun connect(options: Any?) {
        // Cast options to MqttConnectOptions for V3 client
        val optionsV3 = options as? MqttConnectOptions
            ?: throw( IllegalArgumentException("Expected MqttConnectOptions for V3 client") )

        // Create a new client if not already created
        if (client == null) {
            // Ensure clientId is not null; MQTT V3 requires a non-empty string
            val effectiveClientId = connection.clientID.ifBlank { MqttClient.generateClientId() }
            client = MqttClient(
                serverUri,
                effectiveClientId,
                MemoryPersistence()
            )
        }

        // Set callback for events
        client?.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                onConnectComplete(connection, reconnect, serverURI)
            }
            override fun connectionLost(cause: Throwable?) {
                onConnectionLost(cause)
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                if( message != null ) {
                    onMessageArrived(
                        topic ?: "",
                        message.payload ?: byteArrayOf(),
                        message.qos,
                        message.isRetained
                    )
                }
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                // Intentionally empty
            }
        })

        // Connect to the server
        val activeClient = client ?: throw IllegalStateException("V5 Client initialization failed")
        activeClient.connect(optionsV3)
    }

    override fun disconnect(quiesceTime: Long?) {
        // Disconnect from the server
        if (quiesceTime == null) {
            client?.disconnect()
        } else {
            client?.disconnect(quiesceTime)
        }
    }

    override fun subscribe(topic: String, qos: Int) {
        val activeClient = client ?: throw( IllegalStateException("Client not initialized") )
        activeClient.subscribe(topic, qos)
    }

    override fun unsubscribe(topic: String) {
        val activeClient = client ?: throw( IllegalStateException("Client not initialized") )
        activeClient.unsubscribe(topic)
    }

    override fun publish(message: AMCMessage) {
        val activeClient = client ?: throw( IllegalStateException("Client not initialized") )
        val mqttMessage = MqttMessage(
            message.payload.toByteArray(Charsets.UTF_8)
        ).apply {
                qos = message.qos
                isRetained = message.retain
        }
        activeClient.publish(message.topic, mqttMessage)
    }

    override fun isConnected(): Boolean {
        return client?.isConnected ?: false
    }

    override fun close() {
        // NOTE: Do NOT call `setCallback(null)` after `close()`, otherwise the app crashes!
        client?.setCallback(null)

        // Disconnect forcibly if still connected. This will kill all existing
        // background threads (like a reconnect attempt) and prevent "zombie clients".
        if (client?.isConnected == true) {
            try {
                client?.disconnectForcibly(500, 500)
            } catch (e: Exception) {
                Log.e(
                    tag,
                    "Forcible disconnect failed (likely already disconnected): ${e.message}"
                )
            }
        }

        // Close the client and release all resources
        client?.close()
        client = null
    }

    override fun translateConnectOptions(
        connection: AMCServerConnection,
        automaticReconnect: Boolean,
        connectionTimeoutSec: Int
    ): MqttConnectOptions {

        return MqttConnectOptions().apply {
            isAutomaticReconnect = automaticReconnect
            connectionTimeout = connectionTimeoutSec
            mqttVersion = MqttConnectOptions.MQTT_VERSION_DEFAULT
            isCleanSession = connection.cleanSession
            keepAliveInterval = connection.keepAlive

            // Set username and password if provided
            if(connection.username.isNotBlank()) {
                userName = connection.username
            }
            if(connection.password.isNotBlank()) {
                password = connection.password.toCharArray()
            }
            // Set will message if provided
            if(connection.willTopic.isNotBlank()) {
                setWill(
                    connection.willTopic,
                    connection.willMessage.toByteArray(Charsets.UTF_8),
                    connection.willQos,
                    connection.willRetain)
            }
        }
    }
}