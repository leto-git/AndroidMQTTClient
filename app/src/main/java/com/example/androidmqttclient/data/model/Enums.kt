/*
 * Copyright 2026 Tobias Leikam (RheinMain University of Applied Sciences)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package com.example.androidmqttclient.data.model

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