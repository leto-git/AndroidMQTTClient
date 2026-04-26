/*
 * Copyright 2026 Tobias Leikam (RheinMain University of Applied Sciences)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package com.example.androidmqttclient.data.remote

import android.util.Log
import com.example.androidmqttclient.data.model.AMCMessage
import com.example.androidmqttclient.data.model.AMCServerConnection
import org.eclipse.paho.mqttv5.client.IMqttToken
import org.eclipse.paho.mqttv5.client.MqttCallback
import org.eclipse.paho.mqttv5.client.MqttClient
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence
import org.eclipse.paho.mqttv5.common.MqttException
import org.eclipse.paho.mqttv5.common.MqttMessage
import org.eclipse.paho.mqttv5.common.packet.MqttProperties

/**
 * Wrapper around the Paho MQTT client for V5.0.
 */
class MqttV5ClientWrapper(
    private val connection: AMCServerConnection,
    private val serverUri: String,
    private val onConnectComplete: (AMCServerConnection, Boolean, String?) -> Unit,
    private val onConnectionLost: (Throwable?) -> Unit,
    private val onMessageArrived: (String, ByteArray, Int, Boolean) -> Unit,
    private val onProtocolError: (Throwable?) -> Unit,
): MqttClientWrapper {

    private var client: MqttClient? = null

    private val tag = "MQTTV5ClientWrapper"

    override fun connect(options: Any?) {
        val optionsV5 = options as? MqttConnectionOptions
            ?: throw( IllegalArgumentException("Expected MqttConnectionOptions for V5 client") )

        // Create a new client if not already created
        if (client == null) {
            // Ensure clientId is not null; empty string is valid for MQTT v5
            val effectiveClientId = connection.clientID.ifBlank { "" }
            client = MqttClient(
                serverUri,
                effectiveClientId,
                MemoryPersistence()
            )
        }

        client?.setCallback(object: MqttCallback {
            override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                onConnectComplete(connection, reconnect, serverURI)
            }

            override fun disconnected(disconnectResponse: MqttDisconnectResponse?) {
                Log.d(tag, "Disconnected: $disconnectResponse")

                val returnCode = disconnectResponse?.returnCode ?: 0
                val exception = disconnectResponse?.exception

                // If there is an exception, it's a connection loss,
                // even if the return code is 0 (which happens on EOF/Broker termination).
                if (returnCode != 0 || exception != null) {
                    onConnectionLost(exception)
                }
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

            override fun deliveryComplete(token: IMqttToken?) {
                // Intentionally empty
            }

            override fun mqttErrorOccurred(exception: MqttException?) {
                Log.e(tag, "MQTT error occurred: ${exception?.message}", exception)
                onProtocolError(exception)
            }

            override fun authPacketArrived(reasonCode: Int, properties: MqttProperties?) {
                // Intentionally empty, only used for enhanced Auth
            }
        })

        val activeClient = client ?: throw IllegalStateException("V5 Client initialization failed")
        activeClient.connect(optionsV5)
    }


    override fun disconnect(quiesceTime: Long?) {
        if (quiesceTime != null) {
            client?.disconnect(quiesceTime)
        } else {
            client?.disconnect()
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
    ): MqttConnectionOptions {

        return MqttConnectionOptions().apply {
            isAutomaticReconnect = automaticReconnect
            connectionTimeout = connectionTimeoutSec
            isCleanStart = connection.cleanStart
            sessionExpiryInterval = connection.sessionExpiryInterval
            keepAliveInterval = connection.keepAlive

            // Set username and password if provided
            if(connection.username.isNotBlank()) {
                userName = connection.username
            }
            if(connection.password.isNotBlank()) {
                password = connection.password.toByteArray(Charsets.UTF_8)
            }

            // Set will message if provided
            if(connection.willTopic.isNotBlank()) {
                val willMessage = MqttMessage().apply {
                    qos = connection.willQos
                    isRetained = connection.willRetain
                    payload = connection.willMessage.toByteArray(Charsets.UTF_8)
                }

                setWill(connection.willTopic, willMessage)
            }
        }
    }
}