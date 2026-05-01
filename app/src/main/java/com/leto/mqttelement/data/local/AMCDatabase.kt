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

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.leto.mqttelement.data.model.AMCServerConnection
import com.leto.mqttelement.data.model.AMCSubscription

/**
 * Room database for the MQTT client.
 *
 * This database contains the server connections and their subscriptions.
 */
@Database(
    entities = [
        AMCServerConnection::class,
        AMCSubscription::class
    ],
    version = 6,
    exportSchema = false)
abstract class AMCDatabase : RoomDatabase() {

    // DAO for the server connections
    abstract fun serverConnectionDao(): AMCServerConnectionDao
    // DAO for the subscriptions
    abstract fun subscriptionDao(): AMCSubscriptionDao

    // Singleton instance of the database
    companion object {
        @Volatile
        private var Instance: AMCDatabase? = null

        /**
         * Migration from version 6 to version 7, for future use.
         * Not used in the current version!
         */
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Future schema changes go here.
            }
        }

        fun getDatabase(context: Context): AMCDatabase {
            return Instance ?: synchronized(this) {
                // Load SQLCipher library
                System.loadLibrary("sqlcipher")
                // Get shared preferences with the encryption key
                val sharedPrefs = context.applicationContext.getSharedPreferences(
                    "sqlcipher_prefs",
                    Context.MODE_PRIVATE
                )
                // Initialize the key manager
                val keyManager = SqlCipherKeyManager(sharedPrefs)
                // Get the SQLCipher encryption factory
                val factory = keyManager.getSupportFactory()

                // Build the database
                Room.databaseBuilder(
                    context.applicationContext,
                    AMCDatabase::class.java,
                    "mqtt_element_database"
                )
                    // Apply the encryption factory
                    .openHelperFactory(factory)
                    // Do not drop tables if the schema changes
                    .fallbackToDestructiveMigration(false)
                    // Add migration object when the schema changes
                    // .addMigrations(MIGRATION_6_7)
                    .build()
                    .also { Instance = it }
            }
        }
    }
}