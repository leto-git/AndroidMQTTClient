package com.example.androidmqttclient.ui

import androidx.lifecycle.ViewModel
import com.example.androidmqttclient.data.MQTTUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * [MQTTViewModel] holds the state of the MQTT client.
 */
class MQTTViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(MQTTUiState())
    val uiState: StateFlow<MQTTUiState> = _uiState.asStateFlow()
}
