package com.example.androidmqttclient.data.repository

import android.util.Log
import com.example.androidmqttclient.data.AMCMessage
import com.example.androidmqttclient.data.AMCServerConnection
import com.example.androidmqttclient.data.AMCServerConnectionDao
import com.example.androidmqttclient.data.AMCSubscription
import com.example.androidmqttclient.data.AMCSubscriptionDao
import com.example.androidmqttclient.data.MQTTConnectionState
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
            // Translate connection to MqttConnectOptions
            val options = translateToPahoConnectOptions(connection)
            // Generate client ID if none is provided
            val clientId = connection.clientID.ifBlank { MqttClient.generateClientId() }

            // Create MQTT client
            mqttClient = MqttClient(address, clientId, MemoryPersistence())

            // Set callback listener for asynchronous events
            setupCallbacks(connection)

            Log.d(tag, "Connecting to $address")

            // Connect to server
            mqttClient?.connect(options)

            // NOTE: [_connectionState] will be set to CONNECTED in the callback listener
            _connectedServer.value = connection

            // Clear subscriptions from the database, if clean session is true
            // If the client gracefully disconnected this operation is not necessary, but
            // it is safer to delete all subscriptions here in case it did not.
            if (connection.cleanSession) {
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

            if(client.isConnected) {
                // Disconnect from the server
                if(quiesceTime == null){
                    client.disconnect()
                } else {
                    client.disconnect(quiesceTime)
                }

                // Clear subscriptions from the database if clean session is true
                if (connection.cleanSession) {
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
     * Build an address string for the server connection.
     *
     * @param connection The connection object containing the server information.
     *
     * @return A string representing the server address.
     */
    private fun buildAddress(connection: AMCServerConnection): String {
        val rawAddress = connection.serverAddress

        // Determine default protocol if not provided
        val protocol = if (connection.useSSL) "ssl://" else "tcp://"
        val prefix = if (rawAddress.contains("://")) {
            ""
        } else {
            protocol
        }

        // Only add port if the address doesn't already have one
        val suffix = if (rawAddress.substringAfter("://").contains(":")) {
            ""
        } else {
            ":${connection.serverPort}"
        }

        return "$prefix$rawAddress$suffix"
    }

    /**
     * Translate an internal [AMCServerConnection] to a Paho [MqttConnectOptions].
     *
     * @param connection The internal server connection to translate.
     * @param automaticReconnect Whether automatic reconnection should be enabled.
     * @param connectionTimeoutSec The connection timeout in seconds.
     * @param version The MQTT version to use.
     *
     * @return A [MqttConnectOptions] object containing the translated options.
     */
    private fun translateToPahoConnectOptions(
        connection: AMCServerConnection,
        automaticReconnect: Boolean = true,
        connectionTimeoutSec: Int = 10,
        version: Int = MqttConnectOptions.MQTT_VERSION_3_1_1
    ): MqttConnectOptions {

        return MqttConnectOptions().apply {
            isAutomaticReconnect = automaticReconnect
            connectionTimeout = connectionTimeoutSec
            mqttVersion = version
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
                    connection.willMessage.toByteArray(),
                    connection.willQos,
                    connection.willRetain)
            }
        }
    }

    /**
     * Set up callbacks for the MQTT client used for asynchronous events.
     *
     * @param connection The connection object containing the server information.
     */
    private fun setupCallbacks(connection: AMCServerConnection) {

        mqttClient?.setCallback(object: MqttCallbackExtended {
            /**
             * Called when the connection to the server is complete.
             *
             * This includes automatic reconnect attempts.
             */
            override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                Log.d(tag, "Connection complete. Reconnect: $reconnect, Server URI: $serverURI")

                // Resubscribe to all subscriptions on reconnect if clean session is true,
                // since the server will have reset the subscriptions.
                if( reconnect && connection.cleanSession ) {
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
                _connectionState.value = MQTTConnectionState.CONNECTED
            }

            /**
             * Called when the connection to the server is lost unexpectedly.
             */
            override fun connectionLost(cause: Throwable?) {
                Log.d(tag, "Connection lost", cause)

                // Update the connection state (automatic reconnect is enabled!)
                _connectionState.value = MQTTConnectionState.RECONNECTING
            }

            /**
             * Called when a message is received from the server.
             */
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

            /**
             * Called when the delivery of a message is complete.
             */
            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                // Intentionally empty
            }
        })
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

    /**
     * Close the MQTT client and reset the client state.
     *
     * This function should be called when the client is no longer needed, to avoid
     * memory leaks, and to allow new connections to be established.
     *
     * @return A [Result] object indicating the success or failure of the operation.
     */
    private fun closeClient(): Result<Unit> {
        val existingClient = mqttClient

        // If client is null, make sure the internal state is "Disconnected" and return
        if( existingClient == null ) {
            _connectedServer.value = null
            _connectionState.value = MQTTConnectionState.DISCONNECTED
            return Result.success(Unit)
        }

        Log.d(tag, "Closing client")

        return try {
            // NOTE: Do NOT call `setCallback(null)` after `close()`, otherwise the app crashes!
            existingClient.setCallback(null)

            // Disconnect forcibly if still connected. This will kill all existing
            // background threads (like a reconnect attempt) and prevent "zombie clients".
            if (existingClient.isConnected) {
                try {
                    existingClient.disconnectForcibly(500,500)
                } catch (e: Exception) {
                    Log.e(tag, "Forcible disconnect failed (likely already disconnected): ${e.message}")
                }
            }

            // Close the client and release all resources
            existingClient.close()

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