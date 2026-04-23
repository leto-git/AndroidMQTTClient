package com.example.androidmqttclient.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

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
    version = 3,
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

        fun getDatabase(context: Context): AMCDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, AMCDatabase::class.java, "amc_database")
                    // Wipes and rebuilds database instead of migrating if no Migration object.
                    // TODO: Set this to false in production and add a migration object
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
