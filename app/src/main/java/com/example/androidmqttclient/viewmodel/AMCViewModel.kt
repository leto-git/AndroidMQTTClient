/*
 * Copyright 2026 Tobias Leikam (RheinMain University of Applied Sciences)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package com.example.androidmqttclient.viewmodel

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidmqttclient.data.model.AMCLogEntry
import com.example.androidmqttclient.data.model.AMCMessage
import com.example.androidmqttclient.data.model.AMCServerConnection
import com.example.androidmqttclient.data.model.AMCSubscription
import com.example.androidmqttclient.data.model.LogEntryType
import com.example.androidmqttclient.data.model.isValidConnection
import com.example.androidmqttclient.data.repository.AMCRepository
import com.example.androidmqttclient.data.model.isValidForSubscribing
import com.example.androidmqttclient.data.model.topicMatchesPattern
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the MQTT client.
 *
 * @param amcRepository The repository for the MQTT client.
 */
class AMCViewModel(private val amcRepository: AMCRepository): ViewModel() {
    // StateFlow holding the current state of the MQTT client
    // [_uiState] is private to prevent external modification
    private val _uiState = MutableStateFlow(AMCUiState())
    // Read-only property to exposes the current state
    val uiState: StateFlow<AMCUiState> = _uiState.asStateFlow()

    // Tag for logging
    private val tag: String = "MQTTViewModel"

    /**
     * Initialize the ViewModel.
     */
    init {
        // Observer repository errors
        observeProtocolErrors()
        // Observe server connections from the database
        observeServerConnections()
        // Observe connection state from the repository
        observeConnectionState()
        // Observe active subscriptions
        observeActiveSubscriptions()
        // Observe incoming messages from the MQTT client
        observeIncomingMessages()
    }

    /**
     * Observe errors from the repository.
     *
     * This function collects errors from the repository and shows them in the UI.
     */
    private fun observeProtocolErrors() {
        viewModelScope.launch {
            amcRepository.protocolErrors.collect { error ->
                showErrorMessage("MQTT Protocol Error", error)
            }
        }
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
                    currentState.copy(
                        serverConnections = connections,
                        takenConnectionNames = connections.map { it.connectionName }
                    )
                }
            }
        }
    }

    /**
     * Observe the current connection state.
     *
     * This function collects the current connection state from the repository and updates
     * the UI state accordingly.
     */
    private fun observeConnectionState() {
        viewModelScope.launch {
            combine(amcRepository.connectionState, amcRepository.connectedServer) { state, connection ->
                Pair(state, connection)
            }.collect { (state, connection) ->
                // Update the UI State
                _uiState.update { currentState ->
                    currentState.copy(
                        connectedServer = connection,
                        connectionState = state,
                    )
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
                    // Determine subscription color of messages that have not been assigned yet
                    // This can be necessary if the message is received before the subscription is added,
                    // for example with retained messages that are sent immediately after subscribing
                    val messages = currentState.receivedMessages.map { msg ->
                        if (msg.subscriptionColor == null ) {
                            msg.copy(subscriptionColor = determineSubscriptionColor(msg, activeSubscriptions))
                        } else {
                            msg
                        }
                    }

                    currentState.copy(
                        activeSubscriptions = activeSubscriptions,
                        receivedMessages = messages
                    )
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
                // Determine the subscription color for the message
                val color = determineSubscriptionColor(newMessage)
                val messageWithColor = newMessage.copy(subscriptionColor = color)

                // Update the UI State
                _uiState.update { currentState ->
                    val updatedReceivedMessages = (currentState.receivedMessages + messageWithColor)
                        .takeLast(currentState.receivedMessagesLimit)

                    currentState.copy(
                        numReceivedMessages = currentState.numReceivedMessages + 1,
                        receivedMessages = updatedReceivedMessages
                    )
                }
                // Log message
                addLogEntry(AMCLogEntry(
                    timestamp = messageWithColor.timestamp,
                    type = LogEntryType.PUBLISH_RECEIVED,
                    message = "Received message with topic: ${messageWithColor.topic}",
                ))
            }
        }
    }

    /**
     * Add a new server to the list of available servers.
     *
     * @param connection The server to add.
     */
    fun addServer(connection: AMCServerConnection) {
        if( !isValidConnection(connection) ) {
            Log.e(tag, "Invalid connection: ${connection.connectionName}")
            showErrorMessage("Invalid connection: ${connection.connectionName}")
            return
        }

        viewModelScope.launch {
            // Insert server into database
            amcRepository.insertServerConnection(connection).onSuccess {
                showInfoMessage("Successfully added ${connection.connectionName}")
            }.onFailure { error ->
                Log.e(tag, "Error adding ${connection.connectionName}", error)
                showErrorMessage("Could not add ${connection.connectionName}", error)
            }
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
        // Connect to server inside coroutine to prevent blocking the Main thread
        viewModelScope.launch {
            Log.d(tag, "Connecting to ${connection.connectionName}")

            val result = amcRepository.connect(connection)
            result.onSuccess {
                Log.d(tag, "Successfully connected to ${connection.connectionName}")
                showInfoMessage("Connected to ${connection.connectionName}")

                // Clear received and published messages
                clearReceivedMessages()
                clearPublishedMessages()

                // Log connect
                addLogEntry(AMCLogEntry(
                    timestamp = System.currentTimeMillis(),
                    type = LogEntryType.CONNECT,
                    message = "Connected to server: ${connection.connectionName}"
                ))
            }.onFailure { error ->
                Log.e(tag, "Error connecting to ${connection.connectionName}", error)
                showErrorMessage("Could not connect to ${connection.connectionName}", error)
            }
        }
    }

    /**
     * Disconnect from the current server.
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

                // Log disconnect
                addLogEntry(AMCLogEntry(
                    timestamp = System.currentTimeMillis(),
                    type = LogEntryType.DISCONNECT,
                    message = "Disconnected from server: $connectionName"
                ))
            }.onFailure { error ->
                Log.e(tag, "Error disconnecting from server", error)
                showErrorMessage("Could not disconnect from $connectionName", error)
            }
        }
    }

    /**
     * Subscribe to a new subscription.
     *
     * @param subscription The subscription to add.
     */
    fun addSubscription(subscription: AMCSubscription) {
        // Check if topic and qos are valid
        if( !isValidForSubscribing(subscription.topic) ) {
            Log.e(tag, "Invalid topic: ${subscription.topic}")
            showErrorMessage("Invalid topic: ${subscription.topic}")
            return
        }
        if( subscription.qos < 0 || subscription.qos > 2 ) {
            Log.e(tag, "Invalid Qos: ${subscription.qos}")
            showErrorMessage("Invalid Qos: ${subscription.qos}")
            return
        }
        // Check if subscription already exists
        val alreadyExists = uiState.value.activeSubscriptions.any { it.topic == subscription.topic }
        if( alreadyExists ) {
            Log.e(tag, "Already subscribed to topic: ${subscription.topic}")
            showErrorMessage("Already subscribed to topic: ${subscription.topic}")
            return
        }

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
                        isSubscribing = false
                    )
                }

                // Log subscription
                addLogEntry(AMCLogEntry(
                    timestamp = System.currentTimeMillis(),
                    type = LogEntryType.SUBSCRIBE,
                    message = "Subscribed to topic: ${subscription.topic}"
                ))
            }.onFailure { error ->
                _uiState.update { it.copy(isSubscribing = false) }
                Log.e(tag, "Error subscribing to ${subscription.topic}", error)
                showErrorMessage("Could not subscribe to ${subscription.topic}", error)

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
                    )
                }

                // Log unsubscribe
                addLogEntry(AMCLogEntry(
                    timestamp = System.currentTimeMillis(),
                    type = LogEntryType.UNSUBSCRIBE,
                    message = "Unsubscribed from topic: ${subscription.topic}"
                ))
            }.onFailure { error ->
                _uiState.update { it.copy(isUnsubscribing = false) }
                Log.e(tag, "Error unsubscribing from ${subscription.topic}", error)
                showErrorMessage("Could not unsubscribe from ${subscription.topic}", error)
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

            val result = amcRepository.publish(message)
            result.onSuccess {
                Log.d(tag, "Successfully published to ${message.topic}")
                showInfoMessage("Published to ${message.topic}")

                // Update UI state
                _uiState.update { currentState ->
                    val updatedPublishedMessages = (currentState.publishedMessages + message)
                        .takeLast(currentState.publishedMessagesLimit)

                    currentState.copy(
                        isPublishing = false,
                        numPublishedMessages = currentState.numPublishedMessages + 1,
                        publishedMessages = updatedPublishedMessages
                    )
                }

                // Log publish
                addLogEntry(AMCLogEntry(
                    timestamp = System.currentTimeMillis(),
                    type = LogEntryType.PUBLISH_SENT,
                    message = "Published message with topic: ${message.topic}"
                ))
            }.onFailure { error ->
                _uiState.update { it.copy(isPublishing = false) }
                Log.e(tag, "Error publishing to ${message.topic}", error)
                showErrorMessage("Could not publish to ${message.topic}", error)
            }
        }
    }

    /**
     * Clear the list of received messages.
     */
    fun clearReceivedMessages() {
        _uiState.update { currentState ->
            currentState.copy(
                numReceivedMessages = 0,
                receivedMessages = emptyList()
            )
        }
    }

    /**
     * Clear the list of published messages.
     */
    fun clearPublishedMessages() {
        _uiState.update { currentState ->
            currentState.copy(
                numPublishedMessages = 0,
                publishedMessages = emptyList()
            )
        }
    }

    /**
     * Add a new log entry to the list of log messages.
     *
     * @param newLogEntry Log entry to add
     */
    fun addLogEntry(newLogEntry: AMCLogEntry) {
        _uiState.update { currentState ->
            // Limit the number of log entries
            val updatedLogEntries = (currentState.logMessages + newLogEntry)
                .takeLast(currentState.logMessagesLimit)
            currentState.copy(
                logMessages = updatedLogEntries
            )
        }
    }

    /**
     * Clear the list of log messages.
     */
    fun clearLog() {
        _uiState.update { it.copy(logMessages = emptyList()) }
    }

    /**
     * Updates the error message in the UI state.
     *
     * @param message The error message to display.
     * @param error The error that caused the message.
     */
    fun showErrorMessage(message: String, error: Throwable? = null) {
        val errorMessage = if( error == null ) {
            message
        } else {
            "$message: ${formatErrorMessage(error)}"
        }

        _uiState.update { it.copy(errorMessage = errorMessage) }
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

    /**
     * Update the publish topic in the UI state.
     */
    fun updatePublishTopic(topic: String) {
        _uiState.update { it.copy(publishTopic = topic) }
    }

    /**
     * Update the publish Qos in the UI state.
     */
    fun updatePublishQos(qos: Int) {
        _uiState.update { it.copy(publishQos = qos) }
    }

    /**
     * Toggle the publish retain in the UI state.
     */
    fun togglePublishRetain() {
        _uiState.update { it.copy(publishRetain = !it.publishRetain) }
    }

    /**
     * Update the publish message in the UI state.
     */
    fun updatePublishMessage(message: String) {
        _uiState.update { it.copy(publishMessage = message) }
    }

    /**
     * Determine the subscription color for a message based on the list of active subscriptions.
     *
     * @param message The message to determine the color for.
     * @param subscriptions The list of active subscriptions.
     *
     * @return The subscription color for the message, or null if no matching subscription is found.
     */
    private fun determineSubscriptionColor(
        message: AMCMessage,
        subscriptions: List<AMCSubscription> = uiState.value.activeSubscriptions
    ): Color? {
        // Determine all potentially matching subscriptions
        val matchingSubscription = subscriptions.filter { sub ->
            topicMatchesPattern(message.topic, sub.topic)
        }
        // Sort matching subscriptions by topic length and absence of wildcard
        val bestMatch = matchingSubscription.maxWithOrNull(
            compareBy<AMCSubscription> { it.topic.split("/").size }
                .thenBy { !it.topic.contains("#")}
                .thenBy { !it.topic.contains("+")}
        )
        // Set the subscription color
        return if( bestMatch != null ) Color(bestMatch.color) else null
    }

    /**
     * Format an error message for display in the UI.
     *
     * @param error The error to format.
     *
     * @return The formatted error message.
     */
    private fun formatErrorMessage(error: Throwable): String {
        val baseMessage = error.message ?: "Unknown error"
        val causeMessage = error.cause?.message

        return if (causeMessage != null && causeMessage != baseMessage) {
            "$baseMessage ($causeMessage)"
        } else {
            baseMessage
        }
    }
}