package com.example.androidmqttclient.ui

import androidx.lifecycle.ViewModel
import com.example.androidmqttclient.data.MQTTUiState
import com.example.androidmqttclient.data.Subscription
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
    fun addSubscription(subscription: Subscription) {
        _uiState.update { currentState ->
            currentState.copy(
                subscriptions = currentState.subscriptions + subscription
            )
        }
    }

    /**
     * [removeSubscription] removes a subscription from the list of subscriptions.
     */
    fun removeSubscription(subscription: Subscription) {
        // TODO: Implement removeSubscription
        // _uiState.update { currentState ->
        //     currentState.copy(
        //         subscriptions = currentState.subscriptions - subscription
        //     )
        // }
    }
}
