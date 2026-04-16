package com.example.androidmqttclient.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidmqttclient.data.AMCLogEntry
import com.example.androidmqttclient.data.AMCUiState
import com.example.androidmqttclient.data.AMCMessage
import com.example.androidmqttclient.data.AMCServerConnection
import com.example.androidmqttclient.data.AMCSubscription
import com.example.androidmqttclient.data.LogEntryType
import com.example.androidmqttclient.data.repository.AMCRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the MQTT client.
 *
 * @param amcRepository The repository for the MQTT client.
 */
class AMCViewModel(private val amcRepository: AMCRepository): ViewModel() {
    // StateFlow holding the current state of the MQTT client
    // _uiState is private to prevent external modification
    private val _uiState = MutableStateFlow(AMCUiState())
    // Read-only property to exposes the current state
    val uiState: StateFlow<AMCUiState> = _uiState.asStateFlow()

    // Tag for logging
    private val tag: String = "MQTTViewModel"

    /**
     * Initialize the ViewModel.
     */
    init {
        // Observe server connections from the database
        observeServerConnections()
        // Observe active subscriptions
        observeActiveSubscriptions()
        // Observe incoming messages from the MQTT client
        observeIncomingMessages()
    }

    /**
     * Observe server connections from the database.
     *
     * This function collects server connections from the database and updates the UI state
     * accordingly.
     */
    private fun observeServerConnections() {
        viewModelScope.launch {
            amcRepository.serverConnections.collect { connections ->
                // Update the UI State
                _uiState.update { currentState ->
                    currentState.copy(serverConnections = connections)
                }
            }
        }
    }

    /**
     * Observe active subscriptions.
     *
     * This function collects active subscriptions from the database and updates the UI state
     * accordingly.
     */
    private fun observeActiveSubscriptions() {
        viewModelScope.launch {
            amcRepository.activeSubscriptions.collect { activeSubscriptions ->
                // Update the UI State
                _uiState.update { currentState ->
                    currentState.copy(activeSubscriptions = activeSubscriptions)
                }
            }
        }
    }

    /**
     * Observe incoming messages from the MQTT client.
     *
     * This function collects incoming messages from the shared flow in the repository
     * and updates the UI state accordingly.
     */
    private fun observeIncomingMessages() {
        viewModelScope.launch {
            amcRepository.incomingMessages.collect { newMessage ->
                // Update the UI State
                _uiState.update { currentState ->
                    currentState.copy(
                        receivedMessages = currentState.receivedMessages + newMessage,
                        logMessages = currentState.logMessages + AMCLogEntry(
                            timestamp = newMessage.timestamp,
                            type = LogEntryType.PUBLISH_RECEIVED,
                            message = "Received message with topic: ${newMessage.topic}",
                        )
                    )
                }
            }
        }
    }

    /**
     * Add a new server to the list of available servers.
     *
     * @param connection The server to add.
     */
    fun addServer(connection: AMCServerConnection) {
        // TODO: Check for validity of server data
        // TODO: Check if server already exists
        viewModelScope.launch {
            // Insert server into database
            amcRepository.insertServerConnection(connection)
            showInfoMessage("Successfully added ${connection.connectionName}")
        }
    }

    /**
     * Remove a server from the list of available servers.
     *
     * @param connection The server to remove.
     */
    fun removeServer(connection: AMCServerConnection) {
        viewModelScope.launch {
            amcRepository.deleteServer(connection)
            showInfoMessage("Successfully removed ${connection.connectionName}")
        }
    }

    /**
     * Update an existing server.
     *
     * @param server The server to update.
     */
    fun updateServer(server: AMCServerConnection) {
        viewModelScope.launch {
            amcRepository.updateServer(server)
            showInfoMessage("Successfully updated ${server.connectionName}")
        }
    }

    /**
     * Connect to a server.
     *
     * @param connection The server to connect to.
     */
    fun connect(connection: AMCServerConnection) {
        // Update UI state to indicate that connection is in progress
        _uiState.update { it.copy(isConnecting = true) }

        // Connect to server inside coroutine to prevent blocking the Main thread
        viewModelScope.launch {
            Log.d(tag, "Connecting to ${connection.connectionName}")

            val result = amcRepository.connect(connection)
            result.onSuccess {
                Log.d(tag, "Successfully connected to ${connection.connectionName}")
                showInfoMessage("Connected to ${connection.connectionName}")

                // Update UI state
                _uiState.update { currentState ->
                    currentState.copy(
                        isConnecting = false,
                        isConnected = true,
                        connectedServer = connection,
                        logMessages = currentState.logMessages + AMCLogEntry(
                            timestamp = System.currentTimeMillis(),
                            type = LogEntryType.CONNECT,
                            message = "Connected to server: ${connection.connectionName}"
                        )
                    )
                }
            }.onFailure { error ->
                _uiState.update { it.copy(isConnecting = false) }
                Log.e(tag, "Error connecting to ${connection.connectionName}", error)
                showErrorMessage("Could not connect to ${connection.connectionName}: ${error.message}")
            }
        }
    }

    /**
     * Disconnect from the current server.
     *
     */
    fun disconnect() {
        // Get current server and name
        val server = uiState.value.connectedServer ?: return
        val connectionName = server.connectionName

        // Update UI state to indicate that disconnection is in progress
        // Disconnect from server inside coroutine to prevent blocking the Main thread
        viewModelScope.launch {
            Log.d(tag, "Disconnecting from $connectionName")

            val result = amcRepository.disconnect(server)
            result.onSuccess {
                Log.d(tag, "Successfully disconnected from $connectionName")
                showInfoMessage("Disconnected from $connectionName")

                // Update UI state
                _uiState.update { currentState ->
                    currentState.copy(
                        isConnected = false,
                        connectedServer = null,
                        // Clear received and published messages
                        receivedMessages = emptyList(),
                        publishedMessages = emptyList(),
                        logMessages = currentState.logMessages + AMCLogEntry(
                            timestamp = System.currentTimeMillis(),
                            type  = LogEntryType.DISCONNECT,
                            message = "Disconnected from server: $connectionName"
                        )
                    )
                }
            }.onFailure { error ->
                Log.e(tag, "Error disconnecting from server", error)
                showErrorMessage("Could not disconnect from $connectionName: ${error.message}")
            }
        }
    }

    /**
     * Subscribe to a new subscription.
     *
     * @param subscription The subscription to add.
     */
    fun addSubscription(subscription: AMCSubscription) {
        // TODO: Check for validity of subscription data (topic, qos, etc)
        // TODO: Check if subscription already exists
        // Update UI state to indicate that subscription is in progress
        _uiState.update { it.copy(isSubscribing = true) }

        // Subscribe to topic inside coroutine to prevent blocking the Main thread
        viewModelScope.launch {
            Log.d(tag, "Subscribing to ${subscription.topic}")

            val result = amcRepository.subscribe(subscription)
            result.onSuccess {
                Log.d(tag, "Successfully subscribed to ${subscription.topic}")
                showInfoMessage("Subscribed to ${subscription.topic}")

                // Update UI state
                _uiState.update { currentState ->
                    currentState.copy(
                        isSubscribing = false,
                        logMessages = currentState.logMessages + AMCLogEntry(
                            timestamp = System.currentTimeMillis(),
                            type = LogEntryType.SUBSCRIBE,
                            message = "Subscribed to topic: ${subscription.topic}"
                        )
                    )
                }
            }.onFailure { error ->
                _uiState.update { it.copy(isSubscribing = false) }
                Log.e(tag, "Error subscribing to ${subscription.topic}", error)
                showErrorMessage("Could not subscribe to ${subscription.topic}: ${error.message}")

            }
        }
    }

    /**
     * Remove a subscription.
     *
     * @param subscription The subscription to remove.
     */
    fun removeSubscription(subscription: AMCSubscription) {
        // Update UI state to indicate that unsubscription is in progress
        _uiState.update { it.copy(isUnsubscribing = true) }

        // Unsubscribe from topic inside coroutine to prevent blocking the Main thread
        viewModelScope.launch {
            Log.d(tag, "Unsubscribing from ${subscription.topic}")

            val result = amcRepository.unsubscribe(subscription)
            result.onSuccess {
                Log.d(tag, "Successfully unsubscribed from ${subscription.topic}")
                showInfoMessage("Unsubscribed from ${subscription.topic}")

                // Update UI state
                _uiState.update { currentState ->
                    currentState.copy(
                        isUnsubscribing = false,
                        logMessages = currentState.logMessages + AMCLogEntry(
                            timestamp = System.currentTimeMillis(),
                            type = LogEntryType.UNSUBSCRIBE,
                            message = "Unsubscribed from topic: ${subscription.topic}"
                        )
                    )
                }
            }.onFailure { error ->
                _uiState.update { it.copy(isUnsubscribing = false) }
                Log.e(tag, "Error unsubscribing from ${subscription.topic}", error)
                showErrorMessage("Could not unsubscribe from ${subscription.topic}: ${error.message}")
            }
        }
    }

    /**
     * Publish a message.
     *
     * @param message The message to publish.
     */
    fun publish(message: AMCMessage) {
        // Update UI state to indicate that publishing is in progress
        _uiState.update { it.copy(isPublishing = true) }

        // Publish message inside coroutine to prevent blocking the Main thread
        viewModelScope.launch {
            Log.d(tag, "Publishing to ${message.topic}")

            val result = amcRepository.publish(
                message.topic,
                message.payload,
                message.qos,
                message.retain
            )
            result.onSuccess {
                Log.d(tag, "Successfully published to ${message.topic}")
                showInfoMessage("Published to ${message.topic}")

                // Update UI state
                _uiState.update { currentState ->
                    currentState.copy(
                        isPublishing = false,
                        publishedMessages = currentState.publishedMessages + message,
                        logMessages = currentState.logMessages + AMCLogEntry(
                            timestamp = System.currentTimeMillis(),
                            type = LogEntryType.PUBLISH_SENT,
                            message = "Published message with topic: ${message.topic}"
                        )
                    )
                }
            }.onFailure { error ->
                _uiState.update { it.copy(isPublishing = false) }
                Log.e(tag, "Error publishing to ${message.topic}", error)
                showErrorMessage("Could not publish to ${message.topic}: ${error.message}")
            }
        }
    }

    /**
     * Clear the list of received messages.
     */
    fun clearReceivedMessages() {
        _uiState.update { it.copy(receivedMessages = emptyList()) }
    }

    /**
     * Clear the list of published messages.
     */
    fun clearPublishedMessages() {
        _uiState.update { it.copy(publishedMessages = emptyList()) }
    }

    /**
     * Clear the list of log messages.
     */
    fun clearLog() {
        _uiState.update { it.copy(logMessages = emptyList()) }
    }

    /**
     * Updates the error message in the UI state.
     */
    fun showErrorMessage(message: String?) {
        _uiState.update { it.copy(errorMessage = message) }
    }

    /**
     * Updates the info message in the UI state.
     */
    fun showInfoMessage(message: String?) {
        _uiState.update { it.copy(infoMessage = message) }
    }

    /**
     * Clear the current error or info message.
     */
    fun clearStatusMessage() {
        _uiState.update {
            it.copy(
                errorMessage = null,
                infoMessage = null
            )
        }
    }
}