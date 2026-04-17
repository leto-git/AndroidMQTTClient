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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.androidmqttclient.R
import com.example.androidmqttclient.data.AMCLogEntry
import com.example.androidmqttclient.data.AMCUiState
import com.example.androidmqttclient.data.LogEntryType
import com.example.androidmqttclient.data.formatTimestamp
import com.example.androidmqttclient.ui.theme.AndroidMQTTClientTheme

@Composable
fun StatusScreen (
    modifier: Modifier = Modifier,
    uiState: AMCUiState,
    onShowCopyConfirmation: (String) -> Unit = {},
    onClearLog: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.padding_small))
    ) {
        // Strings
        val connectionStatusString =
            if (uiState.isConnected) "Connected to server: ${uiState.connectedServer?.connectionName}"
            else "Not connected"
        val subscriptionCountString = "Subscribed to ${uiState.activeSubscriptions.size} topics"
        val receivedMessagesString = "Received ${uiState.numReceivedMessages} messages"
        val publishedMessagesString = "Published ${uiState.numPublishedMessages} messages"

        // General information
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {

            Text(
                text = connectionStatusString,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = subscriptionCountString,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = receivedMessagesString,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = publishedMessagesString,
                style = MaterialTheme.typography.bodyMedium,
            )
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
                    onClick = onClearLog,
                    enabled = uiState.logMessages.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.clear_log)
                    )
                }

                // Copy to clipboard button
                OutlinedButton (
                    onClick = {
                        val clipboard = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                        // Join all log entries into one large string
                        val logText =
                            connectionStatusString + "\n" +
                            subscriptionCountString + "\n" +
                            receivedMessagesString + "\n" +
                            publishedMessagesString + "\n\n" +
                            uiState.logMessages.joinToString("\n") { it.getLogEntry() }
                        val clip = ClipData.newPlainText("MQTT Event Log", logText)
                        clipboard.setPrimaryClip(clip)

                        onShowCopyConfirmation(confirmMessage)
                    },
                    enabled = uiState.logMessages.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                    )
                    Text(stringResource(R.string.copy_to_clipboard))
                }
            }
        }

        // Log entries
        LazyColumn (
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ){
            items(uiState.logMessages.asReversed() ) { logEntry ->
                LogEntryItem(logEntry)
            }
        }
    }
}

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
            uiState = AMCUiState(
                isConnected = true,
                connectedServer = null,
                logMessages = listOf(
                    AMCLogEntry(timestamp = 0, type = LogEntryType.CONNECT, message = "Connected to server"),
                    AMCLogEntry(timestamp = 12345, type = LogEntryType.SUBSCRIBE, message = "Subscribed to topic"),
                    AMCLogEntry(timestamp = 123456789, type = LogEntryType.PUBLISH_SENT, message = "Published to topic"),
                    AMCLogEntry(timestamp = 1234567890, type = LogEntryType.PUBLISH_RECEIVED, message = "Received message"),
                )
            )
        )
    }
}