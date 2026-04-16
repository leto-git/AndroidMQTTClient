package com.example.androidmqttclient

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.androidmqttclient.data.AMCDatabase
import com.example.androidmqttclient.data.repository.AMCRepository
import com.example.androidmqttclient.ui.AMCApp
import com.example.androidmqttclient.ui.theme.AndroidMQTTClientTheme
import com.example.androidmqttclient.viewmodel.AMCViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

private const val TAG = "MainActivity"

/**
 * Main activity of the application.
 */
class MainActivity : ComponentActivity() {
    /**
     * Called when the activity is starting.
     */
    // Create repository
    private lateinit var repository: AMCRepository
    // Create ViewModel
    val viewModel: AMCViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if( modelClass.isAssignableFrom(AMCViewModel::class.java) ) {
                    @Suppress("UNCHECKED_CAST")
                    return AMCViewModel(repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

    /**
     * Called when the activity is starting.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate() called")

        // Create Database instance
        val database = AMCDatabase.getDatabase(this)
        // Create repository
        repository = AMCRepository(
            serverConnectionDao = database.serverConnectionDao(),
            subscriptionDao = database.subscriptionDao()
        )

        enableEdgeToEdge()
        setContent {
            AndroidMQTTClientTheme {
                AMCApp(viewModel = viewModel)
            }
        }
    }

    /**
     * Called when the activity is losing focus but may still be visible.
     */
    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause() called")
    }

    /**
     * Called when the activity is no longer visible to the user.
     */
    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop() called")
    }

    /**
     * Called when the activity is no longer used and is being destroyed.
     */
    override fun onDestroy() {
        super.onDestroy()

        Log.d(TAG, "onDestroy() called")

        if( isFinishing ) {
            Log.d(TAG, "App is closing (isFinishing)")

            val currentState = viewModel.uiState.value
            if( currentState.isConnected && currentState.connectedServer != null ) {
                Log.d(TAG, "Sending graceful disconnect")
                // Graceful disconnect inside runBlocking to make sure disconnect is sent.
                try {
                    runBlocking(Dispatchers.IO) {
                        repository.disconnect(
                            connection = currentState.connectedServer,
                            quiesceTime = 0
                        )
                    }
                    Log.d(TAG, "Graceful disconnect packet sent.")
                } catch (e: Exception) {
                    Log.e(TAG, "Error during disconnect", e)
                }
            }
        } else {
            Log.d(TAG, "App is losing focus. No graceful disconnect needed")
        }
    }
}