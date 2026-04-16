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
     * Called when the activity is no longer visible to the user.
     */
    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop() called")

        if( isFinishing ) {
            Log.d(TAG, "App is closing (isFinishing)")
            if( viewModel.uiState.value.isConnected ) {
                Log.d(TAG, "Sending graceful disconnect")
                viewModel.disconnect()
            }
        } else {
            Log.d(TAG, "App is losing focus. No graceful disconnect needed")
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.d(TAG, "onDestroy() called")
    }
}