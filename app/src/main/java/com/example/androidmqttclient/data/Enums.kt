package com.example.androidmqttclient.data

/**
 * Enum class for representing the MQTT version.
 */
enum class MQTTVersion(val label: String) {
    V3_1_1("3.1.1"),
    V5("5.0")
}

enum class TransportProtocol(val prefix: String, val defaultPort: Int) {
    TCP("tcp://", 1883),
    SSL("ssl://", 8883),
    WS("ws://", 8080),
    WSS("wss://", 8081)
}

/**
 * Enum class for representing the MQTT connection state.
 */
enum class MQTTConnectionState {
    CONNECTING,
    CONNECTED,
    DISCONNECTED,
    DISCONNECTING,
    RECONNECTING
}

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