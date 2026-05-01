/*
 * Copyright 2026 Tobias Leikam (RheinMain University of Applied Sciences)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package com.leto.mqttelement.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.SensorsOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.leto.mqttelement.R
import com.leto.mqttelement.data.model.AMCServerConnection
import com.leto.mqttelement.data.model.MQTTConnectionState
import com.leto.mqttelement.data.model.MQTTVersion
import com.leto.mqttelement.ui.theme.MqttElementTheme
import com.leto.mqttelement.ui.theme.ConnectionGreen

/**
 * Composable function for showing the connect screen.
 *
 * @param modifier Modifier for styling.
 * @param serverConnections List of server connections.
 * @param connectedServer The currently connected server.
 * @param connectionState The current connection state.
 * @param onAddConnection Callback for adding a new connection.
 * @param onConnect Callback for connecting.
 * @param onViewConnectionDetails Callback for viewing connection details.
 * @param onDisconnect Callback for disconnecting.
 * @param onDeleteConnection Callback for deleting a connection.
 */
@Composable
fun ConnectScreen(
    modifier: Modifier = Modifier,
    serverConnections: List<AMCServerConnection>,
    connectedServer: AMCServerConnection?,
    connectionState: MQTTConnectionState,
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
            if (serverConnections.isEmpty()) {
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
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(serverConnections) { connection ->
                        val isCurrentConnection = connectedServer?.id == connection.id

                        ConnectionItem(
                            connection = connection,
                            isConnected = isCurrentConnection,
                            onClick = { onConnect(connection) },
                            onDisconnect = { onDisconnect() },
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
            if( connectionState == MQTTConnectionState.CONNECTED ||
                connectionState == MQTTConnectionState.RECONNECTING )
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
 * @param isConnected Whether this connection is currently connected.
 * @param onClick Callback for when the item is clicked.
 * @param onEditClick Callback for when the edit button is clicked.
 * @param onDeleteClick Callback for when the delete button is clicked.
 */
@Composable
fun ConnectionItem(
    connection: AMCServerConnection,
    isConnected: Boolean,
    onClick: () -> Unit,
    onDisconnect: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    var showDropDownMenu by remember { mutableStateOf(false) }
    val color = if (isConnected) ConnectionGreen else MaterialTheme.colorScheme.outlineVariant

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .background(MaterialTheme.colorScheme.surface)
                .combinedClickable(
                    onClick = { },
                    onLongClick = { showDropDownMenu = true }
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(6.dp)
                    .background(color)
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Server name
                    Text(
                        text = connection.connectionName,
                        style = MaterialTheme.typography.titleMedium
                    )
                    // MQTT version, server address and port
                    Column(
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = "MQTT v${connection.mqttVersion.label}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "Address: ${connection.protocol}${connection.serverAddress}${connection.webSocketPath}:${connection.serverPort}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "ClientID: ${connection.clientID}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                // Buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Connect/Disconnect button
                    IconButton(onClick = if (isConnected) onDisconnect else onClick) {
                        Icon(
                            imageVector = if (isConnected) Icons.Default.Sensors else Icons.Default.SensorsOff,
                            contentDescription = "Toggle Connection",
                            tint = if (isConnected) ConnectionGreen else MaterialTheme.colorScheme.outline
                        )
                    }

                    // Vertical Divider between buttons
                    VerticalDivider(
                        modifier = Modifier
                            .height(24.dp)
                            .padding(horizontal = 4.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    // Edit button
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                    }
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 12.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )

        // Drop down menu after long click
        DropdownMenu(
            expanded = showDropDownMenu,
            onDismissRequest = { showDropDownMenu = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
            offset = DpOffset(16.dp, 0.dp)
        ) {
            if( isConnected ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.disconnect)) },
                    onClick = {
                        showDropDownMenu = false
                        onDisconnect()
                    },
                    leadingIcon = { Icon(Icons.Default.Sensors, contentDescription = null) }
                )
            } else {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.connect)) },
                    onClick = {
                        showDropDownMenu = false
                        onClick()
                    },
                    leadingIcon = { Icon(Icons.Default.SensorsOff, contentDescription = null) }
                )
            }
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

@Preview(showBackground = true)
@Composable
fun ConnectScreenPreview() {
    MqttElementTheme {
        ConnectScreen(
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
            ),
            connectionState = MQTTConnectionState.CONNECTED,
            connectedServer = null,
            onAddConnection = {},
            onConnect = {},
            onViewConnectionDetails = {},
            onDisconnect = {},
            onDeleteConnection = {}
        )
    }
}