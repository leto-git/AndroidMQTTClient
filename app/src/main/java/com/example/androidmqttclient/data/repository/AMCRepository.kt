package com.example.androidmqttclient.data.repository

import android.util.Log
import com.example.androidmqttclient.data.AMCMessage
import com.example.androidmqttclient.data.AMCServerConnection
import com.example.androidmqttclient.data.AMCServerConnectionDao
import com.example.androidmqttclient.data.AMCSubscription
import com.example.androidmqttclient.data.AMCSubscriptionDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.forEach
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
 * It is responsible for managing the MQTT client and the server connections.
 */
class AMCRepository(
    private val serverConnectionDao: AMCServerConnectionDao,
    private val subscriptionDao: AMCSubscriptionDao
) {
    // Paho MQTT client instance
    private var mqttClient: MqttClient? = null

    // Coroutine scope for repository operations
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Flow for server connections
    val serverConnections: Flow<List<AMCServerConnection>> =
        serverConnectionDao.getAllServerConnections()

    // Track the current server connection
    private val _connectedServerId = MutableStateFlow<Int?>(null)

    // Flow for active subscriptions
    // Any change to the subscription database will trigger a new query and update this variable
    @OptIn(ExperimentalCoroutinesApi::class)
    val activeSubscriptions: Flow<List<AMCSubscription>> = _connectedServerId.flatMapLatest { id ->
        if (id == null) {
            flowOf(emptyList())
        } else {
            subscriptionDao.getSubscriptionsForServer(id)
        }
    }

    // Shared flow for incoming messages
    private val _incomingMessages = MutableSharedFlow<AMCMessage>(
        replay = 0,
        extraBufferCapacity = 32,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    // Expose shared flow as read-only property
    val incomingMessages = _incomingMessages.asSharedFlow()

    // Tag for logging
    private val tag: String = "MQTTRepository"

    /**
     * Insert a new server connection into the database.
     *
     * @param connection The server connection to insert.
     */
    suspend fun insertServerConnection(connection: AMCServerConnection) {
        serverConnectionDao.insertServerConnection(connection)
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
                    // Intentionally empty
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
                    // Intentionally empty
                }
            })

            // Connect to server
            Log.d(tag, "Connecting to $address")
            mqttClient?.connect(options)

            _connectedServerId.value = connection.id

            // Clear subscriptions from the database, if clean session is true
            // If the client gracefully disconnected this operation is not necessary, but
            // it is safer to delete all subscriptions here in case it did not.
            if (connection.cleanSession) {
                subscriptionDao.deleteAllSubscriptionsForServer(connection.id)
            }

            Log.d(tag, "Connected to $address")
            Result.success(Unit)
        } catch (e: Exception) {
            // Reset state
            _connectedServerId.value = null
            mqttClient?.setCallback(null)
            mqttClient = null

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
     *
     * @return A [Result] object indicating the success or failure of the disconnection.
     */
    suspend fun disconnect(
        connection: AMCServerConnection
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val client = mqttClient
        if (client == null) {
            // Can't disconnect, return success since desired disconnect state is achieved.
            return@withContext Result.success(Unit)
        }

        try {
            if(client.isConnected) {
                // Disconnect from the server
                client.disconnect()

                // Clear subscriptions from the database if clean session is true
                if (connection.cleanSession) {
                    subscriptionDao.deleteAllSubscriptionsForServer(connection.id)
                }
            }

            // Reset connected ID, lear callback listener and set mqttClient to null
            _connectedServerId.value = null
            client.setCallback(null)
            mqttClient = null

            Log.d(tag, "Disconnected")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Error disconnecting", e)

            // In case of error also reset the client state
            _connectedServerId.value = null
            client.setCallback(null)
            mqttClient = null

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
        requireClient().fold(
            onSuccess = { client ->
                try {
                    Log.d(tag, "Trying to publish to $topic: $message")
                    val mqttMessage = MqttMessage(message.toByteArray()).apply {
                        this.qos = qos
                        this.isRetained = retain
                    }
                    client.publish(topic, mqttMessage)

                    Log.d(tag, "Successfully published.")
                    Result.success(Unit)
                } catch (e: Exception) {
                    Log.e(tag, "Error publishing to $topic", e)
                    Result.failure(e)
                }
            },
            onFailure = { Result.failure(it) }
        )
    }

    /**
     * Require an initialized MQTT client.
     *
     * @return A [Result] object containing the MQTT client if it is initialized and connected,
     *         or a failure otherwise.
     */
    private fun requireClient(): Result<MqttClient> {
        val client = mqttClient
        return if (client != null && client.isConnected) {
            Result.success(client)
        } else {
            Result.failure(
                IllegalStateException("MQTT Client not initialized or disconnected")
            )
        }
    }
}