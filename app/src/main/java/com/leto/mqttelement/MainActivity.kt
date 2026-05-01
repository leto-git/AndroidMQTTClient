/*
 * Copyright 2026 Tobias Leikam (RheinMain University of Applied Sciences)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package com.leto.mqttelement

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.leto.mqttelement.data.local.AMCDatabase
import com.leto.mqttelement.data.model.MQTTConnectionState
import com.leto.mqttelement.data.repository.AMCRepository
import com.leto.mqttelement.ui.AMCApp
import com.leto.mqttelement.ui.theme.MqttElementTheme
import com.leto.mqttelement.viewmodel.AMCViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

private const val TAG = "MainActivity"

/**
 * Main activity of the application.
 */
class MainActivity : ComponentActivity() {
    // Create repository
    private val repository: AMCRepository by lazy {
        val database = AMCDatabase.getDatabase(applicationContext)
        AMCRepository(
            serverConnectionDao = database.serverConnectionDao(),
            subscriptionDao = database.subscriptionDao()
        )
    }

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

        enableEdgeToEdge()
        setContent {
            MqttElementTheme {
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
            if( currentState.connectionState == MQTTConnectionState.CONNECTED &&
                currentState.connectedServer != null ) {
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