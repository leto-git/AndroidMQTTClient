/*
 * Copyright 2026 Tobias Leikam (RheinMain University of Applied Sciences)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package com.leto.mqttelement.viewmodel

import com.leto.mqttelement.data.model.MQTTConnectionState
import com.leto.mqttelement.data.model.AMCLogEntry
import com.leto.mqttelement.data.model.AMCMessage
import com.leto.mqttelement.data.model.AMCServerConnection
import com.leto.mqttelement.data.model.AMCSubscription

/**
 * Data class for representing the UI state of the MQTT client.
 */
data class AMCUiState(
    // Connection related properties
    val connectedServer: AMCServerConnection? = null,
    val connectionState: MQTTConnectionState = MQTTConnectionState.DISCONNECTED,

    val serverConnections: List<AMCServerConnection> = listOf(),
    val takenConnectionNames: List<String> = listOf(),

    // Subscription related properties
    val activeSubscriptions: List<AMCSubscription> = listOf(),

    val receivedMessagesLimit: Int = 200,
    val numReceivedMessages: Int = 0,
    val receivedMessages: List<AMCMessage> = listOf(),

    // Publish related properties
    val publishTopic: String = "",
    val publishQos: Int = 0,
    val publishRetain: Boolean = false,
    val publishMessage: String = "",

    val publishedMessagesLimit: Int = 200,
    val numPublishedMessages: Int = 0,
    val publishedMessages: List<AMCMessage> = listOf(),

    // Status related properties
    val isSubscribing: Boolean = false,
    val isUnsubscribing: Boolean = false,
    val isPublishing: Boolean = false,

    // Status messages and logging
    val errorMessage: String? = null,
    val infoMessage: String? = null,

    val logMessagesLimit: Int = 200,
    val logMessages: List<AMCLogEntry> = listOf()
)