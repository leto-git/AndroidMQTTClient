package com.example.androidmqttclient.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.androidmqttclient.data.model.AMCServerConnection
import com.example.androidmqttclient.data.model.AMCSubscription

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
                    "amc_database"
                )
                    // Apply the encryption factory
                    .openHelperFactory(factory)
                    // Wipes and rebuilds database instead of migrating if no Migration object.
                    // TODO: Set this to false in production and add a migration object
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { Instance = it }
            }
        }
    }
}