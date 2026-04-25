package com.example.androidmqttclient.data

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
    val clientID: String = "AMC_" + System.currentTimeMillis().toString().takeLast(6),
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
