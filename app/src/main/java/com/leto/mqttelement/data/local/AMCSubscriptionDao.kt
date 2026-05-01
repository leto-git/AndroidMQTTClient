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
import com.leto.mqttelement.data.model.AMCSubscription
import kotlinx.coroutines.flow.Flow

/**
 * Data access object (DAO) for the subscriptions database.
 */
@Dao
interface AMCSubscriptionDao {
    /**
     * Insert a new subscription into the database.
     *
     * @param subscription The subscription to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(subscription: AMCSubscription)

    /**
     * Delete a subscription from the database.
     *
     * @param subscription The subscription to delete.
     */
    @Delete
    suspend fun deleteSubscription(subscription: AMCSubscription)

    /**
     * Delete a subscription by its topic.
     *
     * @param serverConnectionId The ID of the server connection.
     * @param topic The topic to delete.
     */
    @Query("DELETE FROM subscriptions WHERE serverConnectionId = :serverConnectionId AND topic = :topic")
    suspend fun deleteByTopic(serverConnectionId: Int, topic: String)

    /**
     * Delete all subscriptions for a specific server.
     *
     * @param serverConnectionId The ID of the server connection.
     */
    @Query("DELETE FROM subscriptions WHERE serverConnectionId = :serverConnectionId")
    suspend fun deleteAllSubscriptionsForServer(serverConnectionId: Int)

    /**
     * Get all subscriptions for a specific server.
     *
     * @return A flow of all subscriptions for a specific server.
     */
    @Query("SELECT * FROM subscriptions WHERE serverConnectionId = :serverConnectionId")
    fun getSubscriptionsForServer(serverConnectionId: Int): Flow<List<AMCSubscription>>
}