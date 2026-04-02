package com.example.androidmqttclient.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidmqttclient.data.AMCUiState
import com.example.androidmqttclient.data.AMCMessage
import com.example.androidmqttclient.data.AMCServerConnection
import com.example.androidmqttclient.data.AMCSubscription
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
        // Observe incoming messages from the MQTT client
        observeIncomingMessages()
    }

    /**
     * Add a new server to the list of available servers.
     *
     * @param server The server to add.
     */
    fun addServer(server: AMCServerConnection) {
        // TODO: Check for validity of server data
        // TODO: Check if server already exists
        _uiState.update { currentState ->
            currentState.copy(
                serverConnections = currentState.serverConnections + server
            )
        }
    }

    /**
     * Remove a server from the list of available servers.
     *
     * @param server The server to remove.
     */
    fun removeServer(server: AMCServerConnection) {
        TODO("Not yet implemented")
    }

    /**
     * Connect to a server.
     *
     * @param server The server to connect to.
     */
    fun connect(server: AMCServerConnection) {
        // Update UI state to indicate that connection is in progress
        _uiState.update { it.copy(isConnecting = true) }

        // Connect to server inside coroutine to prevent blocking the Main thread
        viewModelScope.launch {
            Log.d(tag, "Connecting to ${server.connectionName}")
            val result = amcRepository.connect(server)

            result.onSuccess {
                // Update UI state to indicate that connection was successful
                _uiState.update { currentState ->
                    currentState.copy(
                        isConnecting = false,
                        isConnected = true,
                        connectedServer = server.connectionName,
                        serverConnections = currentState.serverConnections.map {
                            if (it.connectionName == server.connectionName) it.copy(isConnected = true) else it
                        }
                    )
                }
            }.onFailure { error ->
                // Update UI state to indicate that connection failed
                _uiState.update { it.copy(
                    isConnecting = false,
                    errorMessage = error.localizedMessage ?: "Connection failed") }
            }
        }
    }

    /**
     * Add a new subscription to the list of subscriptions.
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
                // Update UI state to indicate that subscription was successful
                _uiState.update { currentState ->
                    currentState.copy(
                        isSubscribing = false,
                        subscriptions = currentState.subscriptions + subscription
                    )
                }
            }.onFailure { error ->
                Log.e(tag, "Error subscribing to ${subscription.topic}", error)
                // Update UI state to indicate that subscription failed
                _uiState.update { currentState ->
                    currentState.copy(
                        isSubscribing = false,
                        errorMessage = error.localizedMessage ?: "Error subscribing"
                    )
                }
            }
        }
    }

    /**
     * Remove a subscription from the list of subscriptions.
     *
     * @param subscription The subscription to remove.
     */
    fun removeSubscription(subscription: AMCSubscription) {
        TODO("Not yet implemented")
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
                _uiState.update { currentState ->
                    currentState.copy(
                        receivedMessages = currentState.receivedMessages + newMessage
                    )
                }
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
                _uiState.update { it.copy(isPublishing = false) }
                Log.d(tag, "Successfully published to ${message.topic}")
            }.onFailure { error ->
                _uiState.update {
                    it.copy(isPublishing = false)
                    it.copy(errorMessage = error.localizedMessage ?: "Error publishing")
                }
                Log.e(tag, "Error publishing to ${message.topic}", error)
            }
        }
    }

    /**
     * Clear the error message.
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}