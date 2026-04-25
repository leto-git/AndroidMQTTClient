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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.androidmqttclient.R
import com.example.androidmqttclient.data.MQTTVersion
import com.example.androidmqttclient.data.TransportProtocol
import com.example.androidmqttclient.ui.theme.AndroidMQTTClientTheme

/**
 * Composable function for displaying the server connection details.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerConnectionDetails(
    mqttVersion: MQTTVersion,
    serverName: String,
    serverAddress: String,
    webSocketPath: String,
    serverPort: Int,
    protocol: TransportProtocol,
    clientId: String,
    username: String,
    password: String,
    keepAlive: Int,
    cleanSession: Boolean,
    cleanStart: Boolean,
    sessionExpiryInterval: Long,
    willQos: Int,
    willRetain: Boolean,
    willTopic: String,
    willMessage: String,

    isHostValid: Boolean,
    isWebSockethPathValid: Boolean,
    isPortValid: Boolean,
    isClientIdValid: Boolean,
    isLastWillQosValid: Boolean,
    isLastWillTopicValid: Boolean,

    editingEnabled: Boolean,

    onMqttVersionChange: (MQTTVersion) -> Unit,
    onServerNameChange: (String) -> Unit,
    onServerAddressChange: (String) -> Unit,
    onWebsocketPathChange: (String) -> Unit,
    onServerPortChange: (Int) -> Unit,
    onProtocolChange: (TransportProtocol) -> Unit,
    onClientIDChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onKeepAliveChange: (Int) -> Unit,
    onCleanSessionChange: (Boolean) -> Unit,
    onCleanStartChange: (Boolean) -> Unit,
    onSessionExpiryIntervalChange: (Long) -> Unit,
    onWillQosChange: (Int) -> Unit,
    onWillRetainChange: (Boolean) -> Unit,
    onWillTopicChange: (String) -> Unit,
    onWillMessageChange: (String) -> Unit
) {
    // Local focus manager to jump to next field on enter
    val focusManager = LocalFocusManager.current

    // State initialization
    var expanded by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var selectedIndex by remember {
        mutableIntStateOf(if(mqttVersion == MQTTVersion.V3_1_1) 0 else 1)
    }
    val options = listOf(MQTTVersion.V3_1_1.name, MQTTVersion.V5.name)

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Server name input
        OutlinedTextField(
            value = serverName,
            onValueChange = onServerNameChange,
            label = { Text(stringResource(R.string.server_name) + "*") },
            isError = serverName.isBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            enabled = editingEnabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )

        // MQTT version selection
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            options.forEachIndexed { index, label ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                    onClick = {
                        selectedIndex = index
                        onMqttVersionChange(if(index == 0) MQTTVersion.V3_1_1 else MQTTVersion.V5)
                    },
                    selected = index == selectedIndex,
                    enabled = editingEnabled
                ) {
                    Text(label)
                }
            }
        }

        FormSectionHeader(stringResource(R.string.connection))

        // Server address (host) and ws path input
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Server address (host) input
            OutlinedTextField(
                value = serverAddress,
                onValueChange = onServerAddressChange,
                label = { Text(stringResource(R.string.host) + "*") },
                prefix = if (!serverAddress.contains("://")) {
                    {
                        Text(
                            text = protocol.prefix,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        )
                    }
                } else null,
                modifier = Modifier
                    .weight(0.75f)
                    .height(64.dp),
                enabled = editingEnabled,
                singleLine = true,
                isError = serverAddress.isBlank() || (!isHostValid && serverAddress.isNotEmpty()),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Next,
                    capitalization = KeyboardCapitalization.None,
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Next) }
                )
            )
            // Path for ws or wss connections
            if (protocol == TransportProtocol.WS || protocol == TransportProtocol.WSS) {
                OutlinedTextField(
                    value = webSocketPath,
                    onValueChange = onWebsocketPathChange,
                    label = { Text(stringResource(R.string.path)) },
                    modifier = Modifier
                        .weight(0.25f)
                        .height(64.dp),
                    enabled = editingEnabled,
                    singleLine = true,
                    isError = !isWebSockethPathValid && webSocketPath.isNotEmpty(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Next,
                        capitalization = KeyboardCapitalization.None,
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Next) }
                    )
                )
            }

        }
        // Server port and protocol
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
                label = { Text(stringResource(R.string.port) + "*") },
                modifier = Modifier
                    .weight(0.5f)
                    .height(64.dp),
                enabled = editingEnabled,
                singleLine = true,
                isError = !isPortValid,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )
            // Protocol selection
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { if (editingEnabled) expanded = !expanded },
                modifier = Modifier
                    .weight(0.5f)
                    .height(64.dp)
            ) {
                OutlinedTextField(
                    value = protocol.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.protocol)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier.menuAnchor(
                        type = MenuAnchorType.PrimaryNotEditable,
                        enabled = editingEnabled
                    ),
                    enabled = editingEnabled,
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    TransportProtocol.entries.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption.name) },
                            onClick = {
                                onProtocolChange(selectionOption)
                                expanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }

        }
        // Client ID
        OutlinedTextField(
            value = clientId,
            onValueChange = onClientIDChange,
            label = { Text(stringResource(R.string.client_id) + "*") },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            enabled = editingEnabled,
            singleLine = true,
            isError = clientId.isBlank() || (!isClientIdValid && clientId.isNotEmpty()),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )

        FormSectionHeader(stringResource(R.string.credentials))

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
            visualTransformation =
                if( passwordVisible ) VisualTransformation.None
                else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible)
                    Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff

                val description = if (passwordVisible) "Hide password" else "Show password"

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = description)
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )

        FormSectionHeader(stringResource(R.string.session))

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
            // Clean session (MQTT 3.1.1)
            if( mqttVersion == MQTTVersion.V3_1_1 ) {
                // Clean session checkbox with label
                Row(
                    modifier = Modifier
                        .weight(0.5f)
                        .clickable(enabled = editingEnabled) { onCleanSessionChange(!cleanSession) },
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
        }
        // Clean start and session expiry interval (MQTT 5)
        if( mqttVersion == MQTTVersion.V5 ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Session expiry interval
                OutlinedTextField(
                    value = if (sessionExpiryInterval < 0) "" else sessionExpiryInterval.toString(),
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() }) {
                            onSessionExpiryIntervalChange(newValue.toLongOrNull() ?: 0L)
                        }
                    },
                    label = { Text(stringResource(R.string.session_expiry_s)) },
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
                // Clean start checkbox with label
                Row(
                    modifier = Modifier
                        .weight(0.5f)
                        .clickable(enabled = editingEnabled) { onCleanStartChange(!cleanStart) },
                    horizontalArrangement = Arrangement.spacedBy(0.5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = cleanStart,
                        onCheckedChange = onCleanStartChange,
                        enabled = editingEnabled,
                    )
                    Text(
                        text = stringResource(R.string.clean_start),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        FormSectionHeader(stringResource(R.string.last_will))

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
                isError = !isLastWillQosValid,
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
        // Last will topic input
        OutlinedTextField(
            value = willTopic,
            onValueChange = onWillTopicChange,
            label = { Text(stringResource(R.string.last_will_topic)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            enabled = editingEnabled,
            isError = !isLastWillTopicValid && willTopic.isNotEmpty(),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )
        // Last will message input
        OutlinedTextField(
            value = willMessage,
            onValueChange = onWillMessageChange,
            label = { Text(stringResource(R.string.last_will_message)) },
            modifier = Modifier
                .fillMaxWidth()
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

/**
 * Composable function for displaying a form section header.
 *
 * @param title The title of the section.
 */
@Composable
private fun FormSectionHeader(title: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        HorizontalDivider(
            modifier = Modifier.padding(top = 4.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
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
                mqttVersion = MQTTVersion.V5,
                serverName = "Test Server",
                serverAddress = "localhost",
                webSocketPath = "/mqtt",
                serverPort = TransportProtocol.WS.defaultPort,
                protocol = TransportProtocol.WS,
                clientId = "test",
                username = "",
                password = "",
                keepAlive = 60,
                cleanSession = true,
                cleanStart = true,
                sessionExpiryInterval = 0L,
                willQos = 0,
                willRetain = false,
                willTopic = "",
                willMessage = "",
                isHostValid = true,
                isWebSockethPathValid = true,
                isPortValid = true,
                isClientIdValid = true,
                isLastWillQosValid = true,
                isLastWillTopicValid = true,
                editingEnabled = true,
                onMqttVersionChange = {},
                onServerNameChange = {},
                onServerAddressChange = {},
                onWebsocketPathChange = {},
                onServerPortChange = {},
                onProtocolChange = {},
                onClientIDChange = {},
                onUsernameChange = {},
                onPasswordChange = {},
                onKeepAliveChange = {},
                onCleanSessionChange = {},
                onCleanStartChange = {},
                onSessionExpiryIntervalChange = {},
                onWillQosChange = {},
                onWillRetainChange = {},
                onWillTopicChange = {},
                onWillMessageChange = {}
            )
        }
    }
}