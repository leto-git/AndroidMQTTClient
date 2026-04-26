/*
 * Copyright 2026 Tobias Leikam (RheinMain University of Applied Sciences)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package com.example.androidmqttclient.data.repository

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import com.example.androidmqttclient.data.model.AMCMessage
import com.example.androidmqttclient.data.model.AMCServerConnection
import com.example.androidmqttclient.data.local.AMCServerConnectionDao
import com.example.androidmqttclient.data.model.AMCSubscription
import com.example.androidmqttclient.data.local.AMCSubscriptionDao
import com.example.androidmqttclient.data.model.MQTTConnectionState
import com.example.androidmqttclient.data.model.MQTTVersion
import com.example.androidmqttclient.data.model.TransportProtocol
import com.example.androidmqttclient.data.remote.MqttClientWrapper
import com.example.androidmqttclient.data.remote.MqttV3ClientWrapper
import com.example.androidmqttclient.data.remote.MqttV5ClientWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Repository for the MQTT client.
 *
 * It is responsible for managing the MQTT client and the server connections.
 */
class AMCRepository(
    private val serverConnectionDao: AMCServerConnectionDao,
    private val subscriptionDao: AMCSubscriptionDao
) {
    // Paho MQTT client instance
    private var mqttClient: MqttClientWrapper? = null

    // Coroutine scope for repository operations
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Track the list of server connections and expose it as a flow
    val serverConnections: Flow<List<AMCServerConnection>> =
        serverConnectionDao.getAllServerConnections()

    // Track the current server connection and expose it as a state flow
    private val _connectedServer = MutableStateFlow<AMCServerConnection?>(null)
    val connectedServer: StateFlow<AMCServerConnection?> = _connectedServer.asStateFlow()

    // Track the current MQTT connection state and expose it as a state flow
    private val _connectionState = MutableStateFlow(MQTTConnectionState.DISCONNECTED)
    val connectionState: StateFlow<MQTTConnectionState> = _connectionState.asStateFlow()


    // Track active subscriptions for the current server
    // Any change to the subscription database will trigger a new query and update this variable
    @OptIn(ExperimentalCoroutinesApi::class)
    val activeSubscriptions: Flow<List<AMCSubscription>> =
        _connectedServer.flatMapLatest { connection ->
            if (connection == null) {
                flowOf(emptyList())
            } else {
                subscriptionDao.getSubscriptionsForServer(connection.id)
            }
    }

    // Track incoming messages and expose them as a shared flow
    private val _incomingMessages = MutableSharedFlow<AMCMessage>(
        replay = 0,
        extraBufferCapacity = 32,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val incomingMessages = _incomingMessages.asSharedFlow()

    // Track errors that occur and expose them as a shared flow.
    private val _protocolErrors = MutableSharedFlow<Throwable>(extraBufferCapacity = 1)
    val protocolErrors = _protocolErrors.asSharedFlow()

    // Tag for logging
    private val tag: String = "MQTTRepository"

    /**
     * Insert a new server connection into the database.
     *
     * @param connection The server connection to insert.
     *
     * @return A [Result] object indicating the success or failure of the insertion.
     */
    suspend fun insertServerConnection(connection: AMCServerConnection): Result<Unit> {
        return try {
            serverConnectionDao.insertServerConnection(connection)
            Result.success(Unit)
        } catch (_: SQLiteConstraintException) {
            Result.failure(Exception("A connection with this name already exists"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete a server connection from the database.
     *
     * @param connection The server connection to delete.
     */
    suspend fun deleteServer(connection: AMCServerConnection) {
        serverConnectionDao.deleteServerConnection(connection)
    }

    /**
     * Update a server connection in the database.
     *
     * @param connection The server connection to update.
     */
    suspend fun updateServer(connection: AMCServerConnection) {
        serverConnectionDao.updateServerConnection(connection)
    }

    /**
     * Connect to a server.
     *
     * @param connection The connection object containing the server information.
     *
     * @return A [Result] object indicating the success or failure of the connection.
     */
    suspend fun connect(
        connection: AMCServerConnection
    ): Result<Unit> = withContext(Dispatchers.IO) {
        // Prevent overlapping connection attempts if already connected or reconnecting
        if (_connectionState.value == MQTTConnectionState.CONNECTED) {
            Log.d(tag, "Already connected or reconnecting to a server. Ignoring connect request.")
            return@withContext Result.success(Unit)
        }

        try {
            // Clean up old client (if it exists) to prevent errors from "zombie" clients
            // that failed to disconnect cleanly and could potentially try to reconnect.
            closeClient()

            // Update connection state
            _connectionState.value = MQTTConnectionState.CONNECTING

            // Build server address
            val address = buildAddress(connection)

            // Create MQTT client
            if( connection.mqttVersion == MQTTVersion.V3_1_1) {
                mqttClient = MqttV3ClientWrapper(
                    connection = connection,
                    serverUri = address,
                    onConnectComplete = { connection, reconnect, serverURI ->
                        onConnectComplete(connection, reconnect, serverURI)
                    },
                    onConnectionLost = { cause ->
                        onConnectionLost(cause)
                    },
                    onMessageArrived = { topic, payload, qos, retain ->
                        onMessageArrived(topic, payload, qos, retain)
                    }
                )
                val options = mqttClient?.translateConnectOptions(connection)
                mqttClient?.connect(options)
            } else {
                mqttClient = MqttV5ClientWrapper(
                    connection = connection,
                    serverUri = address,
                    onConnectComplete = { connection, reconnect, serverURI ->
                        onConnectComplete(connection, reconnect, serverURI)
                    },
                    onConnectionLost = { cause ->
                        onConnectionLost(cause)
                    },
                    onMessageArrived = { topic, payload, qos, retain ->
                        onMessageArrived(topic, payload, qos, retain)
                    },
                    onProtocolError = { error ->
                        onProtocolError(error)
                    }
                )
                val options = mqttClient?.translateConnectOptions(connection)
                mqttClient?.connect(options)
            }

            // NOTE: [_connectionState] will be set to CONNECTED in the callback listener
            _connectedServer.value = connection

            // Clear subscriptions from the database, if clean session/clean start is true
            // If the client gracefully disconnected this operation is not necessary, but
            // it is safer to delete all subscriptions here in case it did not.
            val shouldClearSubscriptions = if( connection.mqttVersion == MQTTVersion.V3_1_1 ) {
                connection.cleanSession
            } else {
                connection.cleanStart
            }
            if (shouldClearSubscriptions) {
                subscriptionDao.deleteAllSubscriptionsForServer(connection.id)
            }

            Log.d(tag, "Connected to $address")
            Result.success(Unit)
        } catch (e: Exception) {
            // Ensure client is closed even in case of error
            withContext(NonCancellable) {
                closeClient()
            }

            Log.e(tag, "Error connecting to server ${connection.connectionName}", e)
            Result.failure(e)
        }
    }

    /**
     * Disconnect from the server.
     *
     * This function will also delete all server subscriptions from the database, if
     * the clean session flag is set to true.
     *
     * @param connection The connection object containing the server information.
     * @param quiesceTime The time in milliseconds to allow for existing work to finish before
     * disconnecting. A value of null will use the paho default behaviour of waiting up to 30 seconds.
     *
     * @return A [Result] object indicating the success or failure of the disconnection.
     */
    suspend fun disconnect(
        connection: AMCServerConnection,
        quiesceTime: Long ?= null
    ): Result<Unit> = withContext(Dispatchers.IO + NonCancellable) {
        val client = mqttClient
        if (client == null) {
            // Even if client is null, ensure the UI state is reset to DISCONNECTED
            _connectionState.value = MQTTConnectionState.DISCONNECTED
            _connectedServer.value = null

            // Return success since desired state (no active connections) is achieved.
            return@withContext Result.success(Unit)
        }

        try {
            _connectionState.value = MQTTConnectionState.DISCONNECTING

            if(client.isConnected()) {
                client.disconnect(quiesceTime)

                // Clear subscriptions from the database if clean session is true
                val needToClearSubscriptions = if( connection.mqttVersion == MQTTVersion.V3_1_1 ) {
                    connection.cleanSession
                } else {
                    // TODO: For MQTT 5.0 the session expiry interval needs to be checked as well!
                    connection.cleanStart
                }
                if( needToClearSubscriptions ) {
                    subscriptionDao.deleteAllSubscriptionsForServer(connection.id)
                }
            }

            closeClient()

            Log.d(tag, "Disconnected")
            Result.success(Unit)
        } catch (e: Exception) {
            // In case of error also reset the client state
            closeClient()

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
    suspend fun subscribe(
        subscription: AMCSubscription
    ): Result<Unit> = withContext(Dispatchers.IO) {
        requireClient().fold(
            onSuccess = { client ->
                try {
                    // Subscribe to the topic on the server
                    client.subscribe(subscription.topic, subscription.qos)

                    // Save the subscription to the database
                    subscriptionDao.insertSubscription(subscription)

                    Log.d(tag, "Subscribed to ${subscription.topic}")
                    Result.success(Unit)
                } catch (e: Exception) {
                    Log.e(tag, "Error subscribing to ${subscription.topic}", e)
                    Result.failure(e)
                }
            },
            onFailure = { Result.failure(it) }
        )
    }

    /**
     * Unsubscribe from a topic.
     *
     * @param subscription The subscription object containing the topic.
     *
     * @return A [Result] object indicating the success or failure of the unsubscribe.
     */
    suspend fun unsubscribe(
        subscription: AMCSubscription
    ): Result<Unit> = withContext(Dispatchers.IO) {
        requireClient().fold(
            onSuccess = { client ->
                try {
                    // Unsubscribe from the topic on the server
                    client.unsubscribe(subscription.topic)

                    // Delete the subscription from the database
                    subscriptionDao.deleteByTopic(
                        subscription.serverConnectionId,
                        subscription.topic
                    )

                    Log.d(tag, "Unsubscribed from ${subscription.topic}")
                    Result.success(Unit)
                } catch (e: Exception) {
                    Log.e(tag, "Error unsubscribing from ${subscription.topic}", e)
                    Result.failure(e)
                }
            },
            onFailure = { Result.failure(it) }
        )
    }

    /**
     * Publish a message.
     *
     * @param message The message object containing the topic and payload.
     *
     * @return A [Result] object indicating the success or failure of the publish.
     */
    suspend fun publish(message: AMCMessage): Result<Unit> = withContext(Dispatchers.IO) {
        requireClient().fold(
            onSuccess = { client ->
                try {
                    client.publish(message)

                    Log.d(tag, "Successfully published.")
                    Result.success(Unit)
                } catch (e: Exception) {
                    Log.e(tag, "Error publishing to ${message.topic}", e)
                    Result.failure(e)
                }
            },
            onFailure = { Result.failure(it) }
        )
    }

    /**
     * Build an address string for the server connection, that can be used by Paho.
     *
     * @param connection The connection object containing the server information.
     *
     * @return A string representing the server address (e.g. "tcp://broker.example.com:1883").
     */
    private fun buildAddress(connection: AMCServerConnection): String {
        val isWebSocket = connection.protocol == TransportProtocol.WS.prefix ||
                connection.protocol == TransportProtocol.WSS.prefix

        // Only append the path if it's a WebSocket connection
        val pathSuffix = if (isWebSocket) connection.webSocketPath else ""

        return "${connection.protocol}${connection.serverAddress}:${connection.serverPort}$pathSuffix"
    }

    /**
     * Handle connection completion.
     *
     * This includes automatic reconnect attempts!
     *
     * @param connection The connection object containing the server information.
     * @param reconnect Whether the connection was a reconnect.
     * @param serverURI The URI of the server.
     */
    private fun onConnectComplete(
        connection: AMCServerConnection,
        reconnect: Boolean,
        serverURI: String?
    ) {
        Log.d(tag, "Connection complete. Reconnect: $reconnect, Server URI: $serverURI")

        // Resubscribe to all subscriptions on reconnect if clean session is true,
        // since the server will have reset the subscriptions.
        val needToReSubscribe = if( connection.mqttVersion == MQTTVersion.V3_1_1 ) {
            reconnect && connection.cleanSession
        } else {
            // TODO: For MQTT 5.0 the session expiry interval needs to be checked as well!
            reconnect && connection.cleanStart
        }

        if( needToReSubscribe ) {
            repositoryScope.launch {
                try {
                    val subscriptions = subscriptionDao
                        .getSubscriptionsForServer(connection.id)
                        .first()
                    subscriptions.forEach {
                        Log.d(tag, "Auto-resubscribing to: ${it.topic}")
                        mqttClient?.subscribe(it.topic, it.qos)
                    }
                } catch (e: Exception) {
                    Log.e(tag, "Failed to auto-resubscribe after reconnect", e)
                }
            }
        }
        // Update the connection state for the UI
        _connectionState.value = MQTTConnectionState.CONNECTED
    }

    /**
     * Handle connection loss.
     *
     * @param cause The cause of the connection loss.
     */
    private fun onConnectionLost(cause: Throwable?) {
        Log.d(tag, "Connection lost", cause)
        if (cause != null) {
            // Update the connection state (automatic reconnect is enabled!)
            _connectionState.value = MQTTConnectionState.RECONNECTING
        }
    }

    /**
     * Handle incoming V3 MQTT messages.
     *
     * @param topic The topic of the message.
     * @param payload The payload of the message.
     * @param qos The quality of service level.
     * @param retain Whether the message was retained.
     */
    private fun onMessageArrived(topic: String?, payload: ByteArray, qos: Int, retain: Boolean) {
        Log.d(tag, "Message arrived")

        val decodedPayload = payload.decodeToString()

        // Translate to internal message format
        val mqttMessage = AMCMessage(
            topic = topic ?: "",
            payload = decodedPayload,
            qos = qos,
            retain = retain,
            timestamp = System.currentTimeMillis()
        )
        // Emit message to shared flow, which will be collected by the ViewModel
        repositoryScope.launch {
            _incomingMessages.emit(mqttMessage)
        }
    }

    /**
     * Handle protocol errors.
     *
     * @param error The error that occurred.
     */
    private fun onProtocolError(error: Throwable?) {
        error?.let {
            Log.w(tag, "Protocol Error occurred: ${error.message}")
            _protocolErrors.tryEmit(error)
        }

    }

    /**
     * Require an initialized MQTT client.
     *
     * @return A [Result] object containing the MQTT client if it is initialized and connected,
     *         or a failure otherwise.
     */
    private fun requireClient(): Result<MqttClientWrapper> {
        val client = mqttClient
        return if (client != null && client.isConnected()) {
            Result.success(client)
        } else {
            Result.failure(
                IllegalStateException("MQTT Client not initialized or disconnected")
            )
        }
    }

    /**
     * Close the MQTT client and reset the client state.
     *
     * This function should be called when the client is no longer needed, to avoid
     * memory leaks, and to allow new connections to be established.
     *
     * @return A [Result] object indicating the success or failure of the operation.
     */
    private fun closeClient(): Result<Unit> {
        // If client is null, make sure the internal state is "Disconnected" and return
        if( mqttClient == null ) {
            _connectedServer.value = null
            _connectionState.value = MQTTConnectionState.DISCONNECTED
            return Result.success(Unit)
        }

        Log.d(tag, "Closing client")

        return try {
            mqttClient?.close()

            Log.d(tag, "Client closed successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Error cleaning up client", e)
            Result.failure(e)
        } finally {
            // Reset the client state
            mqttClient = null
            _connectedServer.value = null
            _connectionState.value = MQTTConnectionState.DISCONNECTED
        }
    }
}