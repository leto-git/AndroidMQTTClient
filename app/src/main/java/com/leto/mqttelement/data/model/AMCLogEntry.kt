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

/**
 * Data class for representing MQTT log entries.
 */
data class AMCLogEntry (
    val timestamp: Long = System.currentTimeMillis(),
    val type: LogEntryType,
    val message: String,
) {
    fun getLogEntry(): String {
        return "${formatTimestamp(timestamp)} - ${type.label} - $message"
    }
}
