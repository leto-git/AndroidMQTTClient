package com.example.androidmqttclient.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.example.androidmqttclient.R
import com.example.androidmqttclient.data.model.AMCServerConnection
import com.example.androidmqttclient.viewmodel.AMCUiState
import com.example.androidmqttclient.data.model.MQTTConnectionState
import com.example.androidmqttclient.data.model.MQTTVersion
import com.example.androidmqttclient.ui.theme.AndroidMQTTClientTheme
import com.example.androidmqttclient.ui.theme.ConnectionGreen
import com.example.androidmqttclient.ui.theme.ConnectionRed

/**
 * Composable function for showing the connect screen.
 *
 * @param modifier Modifier for styling.
 * @param uiState The current UI state.
 * @param onAddConnection Callback for adding a new connection.
 * @param onConnect Callback for connecting.
 * @param onViewConnectionDetails Callback for viewing connection details.
 * @param onDisconnect Callback for disconnecting.
 * @param onDeleteConnection Callback for deleting a connection.
 */
@Composable
fun ConnectScreen(
    modifier: Modifier = Modifier,
    uiState: AMCUiState,
    onAddConnection: () -> Unit = {},
    onConnect: (AMCServerConnection) -> Unit = {},
    onViewConnectionDetails: (AMCServerConnection) -> Unit = {},
    onDisconnect: () -> Unit = {},
    onDeleteConnection: (AMCServerConnection) -> Unit = {},
) {
    // State initialization
    var connectionToDelete by remember { mutableStateOf<AMCServerConnection?>(null) }
    var showDisconnectDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        // List of servers
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (uiState.serverConnections.isEmpty()) {
                // Empty State Hint
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = stringResource(R.string.no_servers_added),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = stringResource(R.string.tap_plus_to_start),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn (
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(uiState.serverConnections) { connection ->
                        val isCurrentConnection = uiState.connectedServer?.id == connection.id

                        ConnectionItem(
                            connection = connection,
                            isConnected = isCurrentConnection,
                            onClick = { onConnect(connection) },
                            onEditClick = { onViewConnectionDetails(connection) },
                            onDeleteClick = { connectionToDelete = connection }
                        )
                    }
                }
            }
        }

        // Floating action buttons
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(dimensionResource(R.dimen.padding_medium)),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Disconnect button if connected to a server
            if( uiState.connectionState == MQTTConnectionState.CONNECTED ||
                uiState.connectionState == MQTTConnectionState.RECONNECTING )
            {
                ExtendedFloatingActionButton(
                    onClick = { showDisconnectDialog = true },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null
                        )
                    },
                    text = {
                        Text(stringResource(R.string.disconnect))
                    }
                )
            }
            // Add server button
            ExtendedFloatingActionButton(
                onClick = { onAddConnection() },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                },
                text = {
                    Text(stringResource(R.string.add_server_screen))
                }
            )
        }

        // Delete connection confirmation dialog
        if (connectionToDelete != null) {
            AlertDialog(
                onDismissRequest = { connectionToDelete = null },
                title = { Text("Delete Connection") },
                text = { Text(stringResource(
                    R.string.are_you_sure_you_want_to_delete,
                    connectionToDelete!!.connectionName)
                ) },
                confirmButton = {
                    TextButton(onClick = {
                        onDeleteConnection(connectionToDelete!!)
                        connectionToDelete = null
                    }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = {
                    TextButton(onClick = { connectionToDelete = null }) { Text("Cancel") }
                }
            )
        }

        // Confirmation Dialog
        if (showDisconnectDialog) {
            AlertDialog(
                onDismissRequest = { showDisconnectDialog = false },
                title = { Text(stringResource(R.string.disconnect)) },
                text = { Text(stringResource(R.string.are_you_sure_you_want_to_disconnect)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDisconnectDialog = false
                            onDisconnect()
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(stringResource(R.string.disconnect))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDisconnectDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }
}

/**
 * Composable function for showing a single connection item.
 *
 * @param connection The connection to display.
 * @param isConnected Whether the connection is currently connected.
 * @param onClick Callback for when the item is clicked.
 * @param onEditClick Callback for when the edit button is clicked.
 * @param onDeleteClick Callback for when the delete button is clicked.
 */
@Composable
fun ConnectionItem(
    connection: AMCServerConnection,
    isConnected: Boolean,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    var showDropDownMenu by remember { mutableStateOf(false) }
    val color = if (isConnected) ConnectionGreen else ConnectionRed

    Box {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { showDropDownMenu = true }
                )
                .padding(dimensionResource(R.dimen.padding_small))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Server name
                    Text(
                        text = connection.connectionName,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    // MQTT version, server address and port
                    Column(
                        modifier = Modifier.padding(start = 24.dp)
                    ) {
                        Text(
                            text = "MQTT version: ${connection.mqttVersion.label}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        Text(
                            text = "Host: ${connection.serverAddress}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        Text(
                            text = "Port: ${connection.serverPort}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }

                // Edit and connection status
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Edit button
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { onEditClick() }
                            .clip(CircleShape)
                    )

                    // Connection status
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(color, CircleShape)
                            .border(
                                width = 3.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = CircleShape
                            )
                    )
                }
            }

            // Drop down menu after long click
            DropdownMenu(
                expanded = showDropDownMenu,
                onDismissRequest = { showDropDownMenu = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                offset = DpOffset(16.dp, 0.dp)
            ) {
                DropdownMenuItem(
                    text = { Text("Connect") },
                    onClick = {
                        showDropDownMenu = false
                        onClick()
                    },
                    leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null) }
                )
                DropdownMenuItem(
                    text = { Text("Edit") },
                    onClick = {
                        showDropDownMenu = false
                        onEditClick()
                    },
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                )
                DropdownMenuItem(
                    text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                    onClick = {
                        showDropDownMenu = false
                        onDeleteClick()
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ConnectScreenPreview() {
    AndroidMQTTClientTheme {
        ConnectScreen(
            uiState = AMCUiState(
                connectionState = MQTTConnectionState.CONNECTED,
                serverConnections = listOf(
                    AMCServerConnection(
                        mqttVersion = MQTTVersion.V3_1_1,
                        connectionName = "Test Server 1",
                        serverAddress = "localhost",
                        serverPort = 1234,
                        clientID = "test",
                        username = "",
                        password = "",
                        keepAlive = 60,
                        cleanSession = true,
                        willQos = 0,
                        willRetain = false,
                        willTopic = "",
                        willMessage = ""
                    ),
                    AMCServerConnection(
                        mqttVersion = MQTTVersion.V3_1_1,
                        connectionName = "Test Server 2",
                        serverAddress = "127.0.0.1",
                        serverPort = 5678,
                        clientID = "test",
                        username = "",
                        password = "",
                        keepAlive = 60,
                        cleanSession = true,
                        willQos = 0,
                        willRetain = false,
                        willTopic = "",
                        willMessage = ""
                    )
                )
            )
        )
    }
}