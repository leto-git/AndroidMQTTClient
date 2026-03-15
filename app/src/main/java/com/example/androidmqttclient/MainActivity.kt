package com.example.androidmqttclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.androidmqttclient.ui.AMCApp
import com.example.androidmqttclient.ui.theme.AndroidMQTTClientTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidMQTTClientTheme {
                AMCApp()
            }
        }
    }
}