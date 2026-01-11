package com.example.androidmqttclient.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.androidmqttclient.R
import com.example.androidmqttclient.data.MqttServerConnection
import com.example.androidmqttclient.ui.theme.AndroidMQTTClientTheme

@Composable
fun AddServerDialog(
    onDismiss: () -> Unit,
    onConfirm: (MqttServerConnection) -> Unit
) {
    // Connection parameters
    // TODO: MQTT 5.0 connection parameters
    var serverName by remember { mutableStateOf("") }
    var serverAddress by remember { mutableStateOf("") }
    var serverPort by remember { mutableIntStateOf(0) }
    var clientID by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var keepAlive by remember { mutableIntStateOf(0) }
    var cleanSession by remember { mutableStateOf(false) }
    var willQos by remember { mutableIntStateOf(0) }
    var willRetain by remember { mutableStateOf(false) }
    var willTopic by remember { mutableStateOf("") }
    var willMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_server)) },
        text = {
            AddServerContent(
                serverName, serverAddress, serverPort,
                clientID, username, password, keepAlive, cleanSession,
                willQos, willRetain, willTopic, willMessage,
                onServerNameChange = { serverName = it },
                onServerAddressChange = { serverAddress = it},
                onServerPortChange = { serverPort = it },
                onClientIDChange = { clientID = it },
                onUsernameChange = { username = it },
                onPasswordChange = { password = it },
                onKeepAliveChange = { keepAlive = it },
                onCleanSessionChange = { cleanSession = it },
                onWillQosChange = { willQos = it },
                onWillRetainChange = { willRetain = it },
                onWillTopicChange = { willTopic = it },
                onWillMessageChange = { willMessage = it }
            )
        },

        // Confirm (Subscribe) button
        confirmButton = {

            TextButton(onClick = { onConfirm(MqttServerConnection(false, serverName, serverAddress, serverPort,
                clientID, username, password, keepAlive, cleanSession,
                willQos, willRetain, willTopic, willMessage)) }
            ) {
                Text(stringResource(R.string.add))
            }
        },

        // Cancel button
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

/**
 * [AddServerContent] is the composable function for creating a new server.
 * The composable had to be moved outside of the Dialog composable for the preview to work properly.
 */
@Composable
fun AddServerContent(
    serverName: String, serverAddress: String, serverPort: Int,
    clientID: String, username: String, password: String, keepAlive: Int, cleanSession: Boolean,
    willQos: Int, willRetain: Boolean, willTopic: String, willMessage: String,
    onServerNameChange: (String) -> Unit,
    onServerAddressChange: (String) -> Unit,
    onServerPortChange: (Int) -> Unit,
    onClientIDChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onKeepAliveChange: (Int) -> Unit,
    onCleanSessionChange: (Boolean) -> Unit,
    onWillQosChange: (Int) -> Unit,
    onWillRetainChange: (Boolean) -> Unit,
    onWillTopicChange: (String) -> Unit,
    onWillMessageChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Server name input
        TextField(
            value = serverName,
            onValueChange = onServerNameChange,
            label = { Text(stringResource(R.string.server_name)) },
            modifier = Modifier.fillMaxWidth()
        )
        // Server address and port input
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Server address (host) input
            TextField(
                value = serverAddress,
                onValueChange = onServerAddressChange,
                label = { Text(stringResource(R.string.host)) },
                modifier = Modifier.weight(0.5f)
            )
            // Server port input
            TextField(
                value = if (serverPort == 0) "" else serverPort.toString(),
                onValueChange = { newValue ->
                    // Only allow numeric input and limit to 5 characters (port max is 65535)
                    if (newValue.all { it.isDigit() } && newValue.length <= 5) {
                        onServerPortChange(newValue.toIntOrNull() ?: 0)
                    }
                },
                label = { Text(stringResource(R.string.port)) },
                modifier = Modifier.weight(0.5f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
        // Client ID
        TextField(
            value = clientID,
            onValueChange = onClientIDChange,
            label = { Text(stringResource(R.string.client_id)) },
            modifier = Modifier.fillMaxWidth()
        )
        // Username
        TextField(
            value = username,
            onValueChange = onUsernameChange,
            label = { Text(stringResource(R.string.user_name_optional)) },
            modifier = Modifier.fillMaxWidth()
        )
        // Password
        TextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text(stringResource(R.string.password_optional)) },
            modifier = Modifier.fillMaxWidth()
        )
        // Keep alive and clean session
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Keep alive
            TextField(
                value = if (keepAlive == 0) "" else keepAlive.toString(),
                onValueChange = { newValue ->
                    // Only allow numeric input and limit to 5 characters (keep alive max is 65535)
                    if (newValue.all { it.isDigit() } && newValue.length <= 5) {
                        onKeepAliveChange(newValue.toIntOrNull() ?: 0)
                    }
                },
                label = { Text(stringResource(R.string.keep_alive_seconds)) },
                modifier = Modifier.weight(0.5f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            // Clean session checkbox with label
            Row(
                modifier = Modifier.weight(0.5f),
                horizontalArrangement = Arrangement.spacedBy(0.5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = cleanSession,
                    onCheckedChange = onCleanSessionChange
                )
                Text(
                    text =stringResource(R.string.clean_session),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        // Last will QoS and retain flag
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Last will QoS
            TextField(
                value = if (willQos == -1) "" else willQos.toString(),
                onValueChange = { newValue ->
                    // Only allow numeric input and limit to 1 character (QoS max is 2)
                    if (newValue.all { it.isDigit() } && newValue.length <= 1) {
                        onWillQosChange(newValue.toIntOrNull() ?: -1)
                    }
                },
                label = { Text(stringResource(R.string.last_will_qos)) },
                modifier = Modifier.weight(0.5f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            // Last will retain checkbox and label
            Row(
                horizontalArrangement = Arrangement.spacedBy(0.5.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(0.5f)
            ) {
                Checkbox(
                    checked = willRetain,
                    onCheckedChange = onWillRetainChange
                )
                Text(
                    text = stringResource(R.string.last_will_retain),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        // Last will topic and message
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Last will topic input
            TextField(
                value = willTopic,
                onValueChange = onWillTopicChange,
                label = { Text(stringResource(R.string.last_will_topic)) },
                modifier = Modifier.weight(0.5f)
            )
            // Last will message input
            TextField(
                value = willMessage,
                onValueChange = onWillMessageChange,
                label = { Text(stringResource(R.string.last_will_message)) },
                modifier = Modifier.weight(0.5f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddServerContentPreview() {
    AndroidMQTTClientTheme {
        Surface(
            modifier = Modifier.padding(16.dp),
        ) {
            AddServerContent(
                serverName = "Test Server",
                serverAddress = "localhost",
                serverPort = 1883,
                clientID = "test",
                username = "",
                password = "",
                keepAlive = 60,
                cleanSession = true,
                willQos = 0,
                willRetain = false,
                willTopic = "",
                willMessage = "",
                onServerNameChange = {},
                onServerAddressChange = {},
                onServerPortChange = {},
                onClientIDChange = {},
                onUsernameChange = {},
                onPasswordChange = {},
                onKeepAliveChange = {},
                onCleanSessionChange = {},
                onWillQosChange = {},
                onWillRetainChange = {},
                onWillTopicChange = {},
                onWillMessageChange = {}
            )
        }
    }
}