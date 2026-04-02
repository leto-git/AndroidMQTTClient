package com.example.androidmqttclient.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room database for the MQTT client.
 *
 * This database contains the server connections.
 */
@Database(entities = [AMCServerConnection::class], version = 1, exportSchema = false)
abstract class AMCDatabase : RoomDatabase() {

    // DAO for the server connections
    abstract fun serverConnectionDao(): AMCServerConnectionDao

    // Singleton instance of the database
    companion object {
        @Volatile
        private var Instance: AMCDatabase? = null

        fun getDatabase(context: Context): AMCDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, AMCDatabase::class.java, "amc_database")
                    .fallbackToDestructiveMigration(false)
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
