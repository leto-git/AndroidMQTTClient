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

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Data class for representing a MQTT server connection stored in a Room database.
 */
@Entity(
    tableName = "server_connections",
    indices = [Index(value = ["connectionName"], unique = true)]
)
data class AMCServerConnection(
    // Primary key for the Room database
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    // Connection name (must be unique!)
    val connectionName: String = "",

    // MQTT version
    val mqttVersion: MQTTVersion = MQTTVersion.V3_1_1,

    // Connection parameters
    val protocol: String = "",
    val serverAddress: String = "",
    val serverPort: Int = 1883,
    val webSocketPath: String = "",
    val clientID: String = "",
    val username: String = "",
    val password: String = "",
    val keepAlive: Int = 60,
    val cleanSession: Boolean = true,
    val cleanStart: Boolean = true,
    val sessionExpiryInterval: Long = 0L,
    val willQos: Int = 0,
    val willRetain: Boolean = false,
    val willTopic: String = "",
    val willMessage: String = ""
)
