package com.example.androidmqttclient.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.androidmqttclient.R
import com.example.androidmqttclient.data.MQTTUiState
import com.example.androidmqttclient.data.MQTTVersion
import com.example.androidmqttclient.data.MqttServerConnection
import com.example.androidmqttclient.ui.components.AddServerDialog
import com.example.androidmqttclient.ui.theme.AndroidMQTTClientTheme
import com.example.androidmqttclient.ui.theme.ConnectionGreen
import com.example.androidmqttclient.ui.theme.ConnectionRed

/**
 * Composable function for showing the connect screen.
 */
@Composable
fun ConnectScreen(
    modifier: Modifier = Modifier,
    uiState: MQTTUiState,
    onAddServer: (MqttServerConnection) -> Unit = {},
    onConnect: (MqttServerConnection) -> Unit = {},
    onErrorDismissed: () -> Unit = {},
) {
    // State for snack bar used to show errors
    val snackBarHostState = remember { SnackbarHostState() }
    // State to track if the add server dialog should be shown
    var showAddServerDialog by remember { mutableStateOf(false) }

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
                    .padding(dimensionResource(R.dimen.padding_small))
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
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = stringResource(R.string.tap_plus_to_start),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }
                } else {
                    LazyColumn (
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // TODO: Handle clicks when already connected to a server
                        // TODO: Handle disconnections
                        // TODO: Handle long press to remove or edit server

                        items(uiState.serverConnections) { connection ->
                            ConnectionItem(
                                connection,
                                onClick = { onConnect(connection) }
                            )
                        }
                    }
                }
            }

            // Floating action button to add a new server
            FloatingActionButton(
                onClick = { showAddServerDialog = true },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(dimensionResource(R.dimen.padding_medium))
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_server)
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
        }
    }

    // Show add server dialog
    if( showAddServerDialog ) {
        AddServerDialog(
            onDismiss = { showAddServerDialog = false },
            onAdd = { newConnection ->
                onAddServer(newConnection)
                showAddServerDialog = false
            },
            onAddAndConnect = { newConnection ->
                onAddServer(newConnection)
                onConnect(newConnection)
                showAddServerDialog = false
            }
        )
    }
}

@Composable
fun ConnectionItem(
    connection: MqttServerConnection,
    onClick: () -> Unit
) {
    val color = if (connection.isConnected) ConnectionGreen else ConnectionRed
    val shape = MaterialTheme.shapes.medium

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(R.dimen.padding_small))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
            .clip(shape)
            .clickable { onClick() }
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
                    text = connection.serverName,
                    style = MaterialTheme.typography.headlineSmall
                )
                // Server address and port and indication if connected
                // Server address and port
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
}


@Preview(showBackground = true)
@Composable
fun ConnectScreenPreview() {
    AndroidMQTTClientTheme {
        ConnectScreen(
            uiState = MQTTUiState(
                serverConnections = listOf(
                    MqttServerConnection(
                        isConnected = false,
                        mqttVersion = MQTTVersion.V3_1_1,
                        serverName = "Test Server 1",
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
                    MqttServerConnection(
                        isConnected = true,
                        mqttVersion = MQTTVersion.V3_1_1,
                        serverName = "Test Server 2",
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