/*
 * Copyright 2026 Tobias Leikam (RheinMain University of Applied Sciences)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package com.example.androidmqttclient.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.androidmqttclient.R
import com.example.androidmqttclient.data.model.AMCLogEntry
import com.example.androidmqttclient.data.model.AMCServerConnection
import com.example.androidmqttclient.data.model.AMCSubscription
import com.example.androidmqttclient.data.model.LogEntryType
import com.example.androidmqttclient.data.model.MQTTConnectionState
import com.example.androidmqttclient.data.model.formatTimestamp
import com.example.androidmqttclient.ui.theme.AndroidMQTTClientTheme

/**
 * Composable function for showing the status screen.
 *
 * @param modifier The modifier to apply to the composable.
 * @param connectionState The current connection state.
 * @param connectedServer The currently connected server.
 * @param activeSubscriptions The list of active subscriptions.
 * @param numReceivedMessages The number of received messages.
 * @param numPublishedMessages The number of published messages.
 * @param logMessages The list of log messages.
 * @param onShowCopyConfirmation Callback for showing a copy to clipboard confirmation.
 * @param onClearLog Callback for clearing the log.
 */
@Composable
fun StatusScreen (
    modifier: Modifier = Modifier,
    connectionState: MQTTConnectionState,
    connectedServer: AMCServerConnection?,
    activeSubscriptions: List<AMCSubscription>,
    numReceivedMessages: Int,
    numPublishedMessages: Int,
    logMessages: List<AMCLogEntry>,
    onShowCopyConfirmation: (String) -> Unit = {},
    onClearLog: () -> Unit = {}
) {
    var showClearLogDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.padding_small))
    ) {
        // General information
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = dimensionResource(R.dimen.padding_small)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
            ) {
                DetailItem(
                    label = (stringResource(R.string.connection)),
                    value = connectedServer?.connectionName ?: stringResource(R.string.not_connected),
                    modifier = Modifier.weight(1f)
                )
                DetailItem(
                    label = (stringResource(R.string.subscriptions)),
                    value = "${activeSubscriptions.size} subscriptions",
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
            ) {
                DetailItem(
                    label = (stringResource(R.string.received)),
                    value = "$numReceivedMessages messages",
                    modifier = Modifier.weight(1f)
                )
                DetailItem(
                    label = (stringResource(R.string.published)),
                    value = "$numPublishedMessages messages",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        HorizontalDivider(Modifier.padding(top = dimensionResource(R.dimen.padding_small)))

        // Event log headline and buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = dimensionResource(R.dimen.padding_small)),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val context = LocalContext.current
            val confirmMessage = stringResource(R.string.copied_to_clipboard)

            Text(
                text = stringResource(R.string.event_log),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(
                    top = dimensionResource(R.dimen.padding_small),
                    bottom = dimensionResource(R.dimen.padding_small)
                )
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Clear log button
                IconButton(
                    onClick = { showClearLogDialog = true },
                    enabled = logMessages.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.clear_log)
                    )
                }

                // Vertical Divider between buttons
                VerticalDivider(
                    modifier = Modifier
                        .height(24.dp)
                        .padding(horizontal = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                // Copy to clipboard button
                IconButton (
                    onClick = {
                        val clipboard = context.getSystemService(
                            CLIPBOARD_SERVICE
                        ) as ClipboardManager
                        // Join all log entries into one large string
                        val logText = logMessages.joinToString("\n") { it.getLogEntry() }
                        val clip = ClipData.newPlainText("MQTT Event Log", logText)
                        clipboard.setPrimaryClip(clip)

                        onShowCopyConfirmation(confirmMessage)
                    },
                    enabled = logMessages.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = stringResource(R.string.copy_to_clipboard),
                    )
                }
            }
        }

        // Log entries
        LazyColumn (
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ){
            items(logMessages.asReversed() ) { logEntry ->
                LogEntryItem(logEntry)
            }
        }

        // Confirmation Dialog
        if (showClearLogDialog) {
            AlertDialog(
                onDismissRequest = { showClearLogDialog = false },
                title = { Text(stringResource(R.string.clear_event_log)) },
                text = { Text(stringResource(R.string.are_you_sure_you_want_to_delete_the_event_log)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showClearLogDialog = false
                            onClearLog()
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(stringResource(R.string.clear))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearLogDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }
}

/**
 * Helper composable to show a label and a value.
 *
 * @param label The label to show.
 * @param value The value to show.
 * @param modifier The modifier to apply to the composable.
 */
@Composable
fun DetailItem(
    label: String,
    value: String?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        if (value != null) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Composable function for showing a single log entry.
 *
 * @param logEntry The log entry to show.
 */
@Composable
fun LogEntryItem(
    logEntry: AMCLogEntry
) {
    // Format timestamp into a readable string
    val formattedDate = remember(logEntry.timestamp) {
        formatTimestamp(logEntry.timestamp)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
            .padding(dimensionResource(R.dimen.padding_small))
    ) {
        // Entry timestamp and type
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = logEntry.type.label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Entry message
        Text(
            text = logEntry.message,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StatusScreenPreview() {
    AndroidMQTTClientTheme {
        StatusScreen(
            connectionState = MQTTConnectionState.CONNECTED,
            connectedServer = AMCServerConnection(
                connectionName = "Test Server with a long name that should be truncated",
            ),
            activeSubscriptions = listOf(),
            numReceivedMessages = 0,
            numPublishedMessages = 0,
            logMessages = listOf(
                AMCLogEntry(
                    type = LogEntryType.CONNECT,
                    message = "Connected",
                    timestamp = System.currentTimeMillis()
                )
            )
        )
    }
}