package com.example.androidmqttclient.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidmqttclient.data.MQTTUiState
import com.example.androidmqttclient.data.MqttMessage
import com.example.androidmqttclient.data.MqttServerConnection
import com.example.androidmqttclient.data.MqttSubscription
import com.example.androidmqttclient.data.repository.MQTTRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the MQTT client.
 *
 * @param mqttRepository The repository for the MQTT client.
 */
class MQTTViewModel(
    private val mqttRepository: MQTTRepository
): ViewModel() {
    // StateFlow holding the current state of the MQTT client
    // _uiState is private to prevent external modification
    private val _uiState = MutableStateFlow(MQTTUiState())
    // uiState is a read-only property that exposes the current state
    val uiState: StateFlow<MQTTUiState> = _uiState.asStateFlow()
    // Tag for logging
    private val tag: String = "MQTTViewModel"

    /**
     * Add a new server to the list of available servers.
     *
     * @param server The server to add.
     */
    fun addServer(server: MqttServerConnection) {
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
    fun removeServer(server: MqttServerConnection) {
        TODO("Not yet implemented")
    }

    /**
     * Connect to a server.
     *
     * @param server The server to connect to.
     */
    fun connect(server: MqttServerConnection) {
        // Update UI state to indicate that connection is in progress
        _uiState.update { it.copy(isConnecting = true) }

        // Connect to server inside coroutine to prevent blocking the Main thread
        viewModelScope.launch {
            Log.d(tag, "Connecting to ${server.serverName}")
            val result = mqttRepository.connect(server)

            result.onSuccess {
                // Update UI state to indicate that connection was successful
                _uiState.update { currentState ->
                    currentState.copy(
                        isConnecting = false,
                        isConnected = true,
                        connectedServer = server.serverName,
                        serverConnections = currentState.serverConnections.map {
                            if (it.serverName == server.serverName) it.copy(isConnected = true) else it
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
    fun addSubscription(subscription: MqttSubscription) {
        // TODO: Check for validity of subscription data (topic, qos, etc)
        // TODO: Check if subscription already exists
        _uiState.update { currentState ->
            currentState.copy(
                subscriptions = currentState.subscriptions + subscription
            )
        }
    }

    /**
     * Remove a subscription from the list of subscriptions.
     *
     * @param subscription The subscription to remove.
     */
    fun removeSubscription(subscription: MqttSubscription) {
        TODO("Not yet implemented")
    }

    /**
     * Publish a message.
     *
     * @param message The message to publish.
     */
    fun publish(message: MqttMessage) {
        // Update UI state to indicate that publishing is in progress
        _uiState.update { it.copy(isPublishing = true) }

        // Publish message inside coroutine to prevent blocking the Main thread
        viewModelScope.launch {
            Log.d(tag, "Publishing to ${message.topic}")
            val result = mqttRepository.publish(
                message.topic,
                message.message,
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