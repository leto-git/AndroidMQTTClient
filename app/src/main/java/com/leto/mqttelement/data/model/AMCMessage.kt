/*
 * Copyright 2026 Tobias Leikam (RheinMain University of Applied Sciences)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package com.leto.mqttelement.data.model

import androidx.compose.ui.graphics.Color

/**
 * Data class for representing MQTT messages.
 */
data class AMCMessage(
    // MQTT message properties
    val topic: String,
    val payload: String,
    val qos: Int,
    val retain: Boolean,
    // Timestamp for arrival or delivery
    val timestamp: Long = System.currentTimeMillis(),
    // Subscription color for message
    var subscriptionColor: Color ?= null
) {
    /**
     * Returns a string representation of the message including timestamp, QoS, retain,
     * topic, and payload.
     *
     * @return A string representation of the message.
     */
    fun getMessageAsString(): String {
        return "${formatTimestamp(timestamp)} - (q${qos}, r${if (retain) "1" else "0"}) - $topic - $payload"
    }
}
