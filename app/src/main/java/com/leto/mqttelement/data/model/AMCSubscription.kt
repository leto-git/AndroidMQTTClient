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
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Data class for representing MQTT subscriptions stored in a Room database.
 * Linked to a specific [AMCServerConnection]
 */
@Entity(
    tableName = "subscriptions",
    foreignKeys = [
        ForeignKey(
            entity = AMCServerConnection::class,
            parentColumns = ["id"],
            childColumns = ["serverConnectionId"],
            // If the server is deleted, delete the subscriptions as well
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["serverConnectionId"])]
)
data class AMCSubscription (
    // Primary key for the Room database
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    // Foreign key to the linked server connection
    val serverConnectionId: Int,

    // Subscription parameters
    val qos: Int,
    val topic: String,
    val color: Long
)