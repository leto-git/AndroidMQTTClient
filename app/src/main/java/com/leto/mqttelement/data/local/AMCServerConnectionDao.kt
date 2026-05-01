/*
 * Copyright 2026 Tobias Leikam (RheinMain University of Applied Sciences)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package com.leto.mqttelement.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.leto.mqttelement.data.model.AMCServerConnection
import kotlinx.coroutines.flow.Flow

/**
 * Data access object (DAO) for the server connections database.
 */
@Dao
interface AMCServerConnectionDao {
    /**
     * Insert a new server connection into the database.
     *
     * @param connection The server connection to insert.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertServerConnection(connection: AMCServerConnection)

    /**
     * Update an existing server connection in the database.
     *
     * @param connection The server connection to update.
     */
    @Update
    suspend fun updateServerConnection(connection: AMCServerConnection)

    /**
     * Delete a server connection from the database.
     *
     * @param connection The server connection to delete.
     */
    @Delete
    suspend fun deleteServerConnection(connection: AMCServerConnection)

    /**
     * Get all server connections from the database.
     *
     * @return A flow of all server connections.
     */
    @Query("SELECT * FROM server_connections ORDER BY connectionName ASC")
    fun getAllServerConnections(): Flow<List<AMCServerConnection>>

    /**
     * Get a server connection by its ID.
     *
     * @param id The ID of the server connection to retrieve.
     *
     * @return A flow of the server connection.
     */
    @Query("SELECT * FROM server_connections WHERE id = :id")
    fun getServerConnectionById(id: Int): Flow<AMCServerConnection>
}