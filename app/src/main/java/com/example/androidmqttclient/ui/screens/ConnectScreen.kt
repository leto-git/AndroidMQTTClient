package com.example.androidmqttclient.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.example.androidmqttclient.R
import com.example.androidmqttclient.data.AMCServerConnection
import com.example.androidmqttclient.data.AMCUiState
import com.example.androidmqttclient.data.MQTTVersion
import com.example.androidmqttclient.ui.theme.AndroidMQTTClientTheme
import com.example.androidmqttclient.ui.theme.ConnectionGreen
import com.example.androidmqttclient.ui.theme.ConnectionRed

/**
 * Composable function for showing the connect screen.
 */
@Composable
fun ConnectScreen(
    modifier: Modifier = Modifier,
    uiState: AMCUiState,
    onAddServer: () -> Unit = {},
    onConnect: (AMCServerConnection) -> Unit = {},
    onViewConnectionDetails: (AMCServerConnection) -> Unit = {},
    onDisconnect: () -> Unit = {},
    onDeleteConnection: (AMCServerConnection) -> Unit = {},
    onErrorDismissed: () -> Unit = {},
) {
    // State initialization
    val snackBarHostState = remember { SnackbarHostState() }
    var connectionToDelete by remember { mutableStateOf<AMCServerConnection?>(null) }

    // Show error message if it is not null
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackBarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Long,
                withDismissAction = true
            )
            // Clear error message after showing it
            onErrorDismissed()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) },
        modifier = modifier
    ) { padding ->
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
                        // TODO: Handle clicks when already connected to a server
                        // TODO: Snackbar feedback after successful connection
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
                if( uiState.isConnected )
                {
                    ExtendedFloatingActionButton(
                        onClick = { onDisconnect() },
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
                    onClick = { onAddServer() },
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

            // Show loading indicator if connection is in progress
            if( uiState.isConnecting ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)), // Dim the background
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Connecting...")
                        }
                    }
                }
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
        }
    }
}

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
    val shape = MaterialTheme.shapes.medium

    Box {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
                .clip(shape)
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
                    // Server address and port and indication if connected
                    Column(
                        modifier = Modifier.padding(start = 24.dp)
                    ) {
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
                    modifier = Modifier.weight(0.5f),
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
                isConnected = true,
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