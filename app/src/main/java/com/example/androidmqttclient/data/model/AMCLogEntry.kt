package com.example.androidmqttclient.data.model

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
