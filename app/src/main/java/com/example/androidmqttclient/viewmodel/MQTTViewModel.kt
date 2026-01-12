package com.example.androidmqttclient.viewmodel

import androidx.lifecycle.ViewModel
import com.example.androidmqttclient.data.MQTTUiState
import com.example.androidmqttclient.data.MqttServerConnection
import com.example.androidmqttclient.data.MqttSubscription
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * [MQTTViewModel] holds the state of the MQTT client.
 */
class MQTTViewModel: ViewModel() {
    // StateFlow holding the current state of the MQTT client
    // _uiState is private to prevent external modification
    // uiState is a read-only property that exposes the current state
    private val _uiState = MutableStateFlow(MQTTUiState())
    val uiState: StateFlow<MQTTUiState> = _uiState.asStateFlow()

    /**
     * [addSubscription] adds a new subscription to the list of subscriptions.
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
     * [removeSubscription] removes a subscription from the list of subscriptions.
     */
    fun removeSubscription(subscription: MqttSubscription) {
        TODO("Not yet implemented")
    }

    /**
     * [addServer] adds a new server to the list of available servers.
     */
    fun addServer(server: MqttServerConnection) {
        // TODO: Check for validity of server data
        // TODO: Check if server already exists

        _uiState.update { currentState ->
            currentState.copy(
                serversConnections = currentState.serversConnections + server
            )
        }
    }

    fun removeServer(server: MqttServerConnection) {
        TODO("Not yet implemented")
    }

    /**
     * [connect] connects to a server.
     */
    fun connect(server: MqttServerConnection) {
        TODO("Not yet implemented")
    }
}