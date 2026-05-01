/*
 * Copyright 2026 Tobias Leikam (RheinMain University of Applied Sciences)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package com.leto.mqttelement.data.remote

import com.leto.mqttelement.data.model.AMCMessage
import com.leto.mqttelement.data.model.AMCServerConnection

/**
 * Interface for a wrapper around the Paho MQTT client.
 *
 * This interface provides a simplified API for interacting with the MQTT client,
 * allowing support for both 3.1.1 and 5.0.0 versions of Paho.
 */
interface MqttClientWrapper {
    /**
     * Connect to the server.
     *
     * @param options The connection options to use.
     */
    fun connect(options: Any?)

    /**
     * Disconnect from the server.
     *
     * @param quiesceTime The time in milliseconds to allow for existing work to finish
     * before disconnecting. A value of null will use the paho default behaviour
     * of waiting up to 30 seconds.
     */
    fun disconnect(quiesceTime: Long?)

    /**
     * Subscribe to a topic.
     *
     * @param topic The topic to subscribe to.
     */
    fun subscribe(topic: String, qos: Int)

    /**
     * Unsubscribe from a topic.
     *
     * @param topic The topic to unsubscribe from.
     */
    fun unsubscribe(topic: String)

    /**
     * Publish a message.
     *
     * @param message The message to publish.
     */
    fun publish(message: AMCMessage)

    /**
     * Check if the client is connected to the server.
     *
     * @return `true` if the client is connected, `false` otherwise.
     */
    fun isConnected(): Boolean

    /**
     * Close the client and release all resources.
     */
    fun close()

    /**
     * Translate an internal [AMCServerConnection] to a Paho connection option (either V3 or V5).
     *
     * @param connection The internal server connection to translate.
     * @param automaticReconnect Whether automatic reconnection should be enabled.
     * @param connectionTimeoutSec The connection timeout in seconds.
     *
     * @return A [Any] object containing the translated options (either V3 or V5).
     */
    fun translateConnectOptions(
        connection: AMCServerConnection,
        automaticReconnect: Boolean = true,
        connectionTimeoutSec: Int = 10
    ): Any
}