package com.example.androidmqttclient.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.androidmqttclient.R
import com.example.androidmqttclient.ui.theme.AndroidMQTTClientTheme

/**
 * Composable function for displaying the server connection details.
 */
@Composable
fun ServerConnectionDetails(
    serverName: String,
    serverAddress: String,
    serverPort: Int,
    useSSL: Boolean,
    clientID: String,
    username: String,
    password: String,
    keepAlive: Int,
    cleanSession: Boolean,
    willQos: Int,
    willRetain: Boolean,
    willTopic: String,
    willMessage: String,

    editingEnabled: Boolean,

    onServerNameChange: (String) -> Unit,
    onServerAddressChange: (String) -> Unit,
    onServerPortChange: (Int) -> Unit,
    onUseSSLChange: (Boolean) -> Unit,
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
    // Local focus manager to jump to next field on enter
    val focusManager = LocalFocusManager.current

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Server name input
        OutlinedTextField(
            value = serverName,
            onValueChange = onServerNameChange,
            label = { Text(stringResource(R.string.server_name)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            enabled = editingEnabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )
        // Server address (host) input
        OutlinedTextField(
            value = serverAddress,
            onValueChange = onServerAddressChange,
            label = { Text(stringResource(R.string.host)) },
            prefix = if (!serverAddress.contains("://")) {
                {
                    Text(
                        text = if (useSSL) "ssl://" else "tcp://",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    )
                }
            } else null,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            enabled = editingEnabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Next,
                capitalization = KeyboardCapitalization.None,
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Next) }
            )
        )
        // Server port and SSL input
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Server port input
            OutlinedTextField(
                value = if (serverPort == 0) "" else serverPort.toString(),
                onValueChange = { newValue ->
                    // Only allow numeric input and limit to 5 characters (port max is 65535)
                    if (newValue.all { it.isDigit() } && newValue.length <= 5) {
                        onServerPortChange(newValue.toIntOrNull() ?: 0)
                    }
                },
                label = { Text(stringResource(R.string.port)) },
                modifier = Modifier
                    .weight(0.5f)
                    .height(64.dp),
                enabled = editingEnabled,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Next) }
                )
            )
            // SSL checkbox and label
            Row(
                modifier = Modifier
                    .weight(0.5f)
                    .clickable { onUseSSLChange(!useSSL) },
                horizontalArrangement = Arrangement.spacedBy(0.5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = useSSL,
                    onCheckedChange = onUseSSLChange,
                    enabled = editingEnabled,
                )
                Text(
                    text = stringResource(R.string.SSL),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        // Client ID
        OutlinedTextField(
            value = clientID,
            onValueChange = onClientIDChange,
            label = { Text(stringResource(R.string.client_id)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            enabled = editingEnabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )
        // Username
        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            label = { Text(stringResource(R.string.user_name_optional)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            enabled = editingEnabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )
        // Password
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text(stringResource(R.string.password_optional)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            enabled = editingEnabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )
        // Keep alive and clean session
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Keep alive
            OutlinedTextField(
                value = if (keepAlive < 0) "" else keepAlive.toString(),
                onValueChange = { newValue ->
                    // Only allow numeric input and limit to 5 characters (keep alive max is 65535)
                    if (newValue.all { it.isDigit() } && newValue.length <= 5) {
                        onKeepAliveChange(newValue.toIntOrNull() ?: -1)
                    }
                },
                label = { Text(stringResource(R.string.keep_alive_seconds)) },
                modifier = Modifier
                    .weight(0.5f)
                    .height(64.dp),
                enabled = editingEnabled,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )
            // Clean session checkbox with label
            Row(
                modifier = Modifier
                    .weight(0.5f)
                    .clickable { onCleanSessionChange(!cleanSession) },
                horizontalArrangement = Arrangement.spacedBy(0.5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = cleanSession,
                    onCheckedChange = onCleanSessionChange,
                    enabled = editingEnabled,
                )
                Text(
                    text = stringResource(R.string.clean_session),
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
            OutlinedTextField(
                value = if (willQos < 0 || willQos > 2) "" else willQos.toString(),
                onValueChange = { newValue ->
                    // Only allow numeric input and limit to 1 character (QoS max is 2)
                    if (newValue.all { it.isDigit() } && newValue.length <= 1) {
                        onWillQosChange(newValue.toIntOrNull() ?: -1)
                    }
                },
                label = { Text(stringResource(R.string.last_will_qos)) },
                modifier = Modifier
                    .weight(0.5f)
                    .height(64.dp),
                enabled = editingEnabled,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )
            // Last will retain checkbox and label
            Row(
                horizontalArrangement = Arrangement.spacedBy(0.5.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(0.5f)
                    .clickable { onWillRetainChange(!willRetain) }
            ) {
                Checkbox(
                    checked = willRetain,
                    onCheckedChange = onWillRetainChange,
                    enabled = editingEnabled,
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
            OutlinedTextField(
                value = willTopic,
                onValueChange = onWillTopicChange,
                label = { Text(stringResource(R.string.last_will_topic)) },
                modifier = Modifier
                    .weight(0.5f)
                    .height(96.dp),
                enabled = editingEnabled,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Next) }
                )
            )
            // Last will message input
            OutlinedTextField(
                value = willMessage,
                onValueChange = onWillMessageChange,
                label = { Text(stringResource(R.string.last_will_message)) },
                modifier = Modifier
                    .weight(0.5f)
                    .height(96.dp),
                enabled = editingEnabled,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ServerConnectionDetailsPreview() {
    AndroidMQTTClientTheme {
        Surface(
            modifier = Modifier.padding(16.dp),
        ) {
            ServerConnectionDetails(
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
                editingEnabled = true,
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