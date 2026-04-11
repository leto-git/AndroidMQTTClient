package com.example.androidmqttclient.data

/**
 * Enum class representing log entry types.
 */
enum class LogEntryType(val label: String) {
    CONNECT("CONNECT"),
    DISCONNECT("DISCONNECT"),
    SUBSCRIBE("SUBSCRIBE"),
    UNSUBSCRIBE("UNSUBSCRIBE"),
    PUBLISH_SENT("PUBLISH SENT"),
    PUBLISH_RECEIVED("PUBLISH RECEIVED"),
}

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
