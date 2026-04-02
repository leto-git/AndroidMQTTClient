package com.example.androidmqttclient.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data access object (DAO) for the server connections database.
 */
@Dao
interface AMCServerConnectionDao {
    @Insert
    suspend fun insertServerConnection(connection: AMCServerConnection)

    @Update
    suspend fun updateServerConnection(connection: AMCServerConnection)

    @Delete
    suspend fun deleteServerConnection(connection: AMCServerConnection)

    @Query("SELECT * FROM server_connections ORDER BY connectionName ASC")
    fun getAllServerConnections(): Flow<List<AMCServerConnection>>

    @Query("SELECT * FROM server_connections WHERE id = :id")
    fun getServerConnectionById(id: Int): Flow<AMCServerConnection>
}