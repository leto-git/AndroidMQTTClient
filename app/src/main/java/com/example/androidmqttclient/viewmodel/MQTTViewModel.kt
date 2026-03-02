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
 * [MQTTViewModel] holds the state of the MQTT client.
 */
class MQTTViewModel(
    private val mqttRepository: MQTTRepository
): ViewModel() {
    // StateFlow holding the current state of the MQTT client
    // _uiState is private to prevent external modification
    // uiState is a read-only property that exposes the current state
    private val _uiState = MutableStateFlow(MQTTUiState())
    val uiState: StateFlow<MQTTUiState> = _uiState.asStateFlow()

    /**
     * [addServer] adds a new server to the list of available servers.
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
     * [removeServer] removes a server from the list of available servers.
     */
    fun removeServer(server: MqttServerConnection) {
        TODO("Not yet implemented")
    }

    /**
     * [connect] connects to a server.
     * @param server The server to connect to.
     */
    fun connect(server: MqttServerConnection) {

        // Update UI state to indicate that connection is in progress
        _uiState.update { it.copy(isLoading = true) }

        // Connect to server inside coroutine to prevent blocking the Main thread
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("MQTTViewModel", "Connecting to ${server.serverName}")
            val result = mqttRepository.connect(server)

            result.onSuccess {
                // Update UI state to indicate that connection was successful
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
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
                    isLoading = false,
                    errorMessage = error.localizedMessage ?: "Connection failed") }
            }
        }
    }

    /**
     * [addSubscription] adds a new subscription to the list of subscriptions.
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
     * Clear the error message.
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * [removeSubscription] removes a subscription from the list of subscriptions.
     */
    fun removeSubscription(subscription: MqttSubscription) {
        TODO("Not yet implemented")
    }

    /**
     * [publish] publishes a message.
     */
    fun publish(message: MqttMessage) {
        // TODO("Not yet implemented")
    }
}