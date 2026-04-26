package com.example.androidmqttclient.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.androidmqttclient.R
import com.example.androidmqttclient.data.model.AMCMessage
import com.example.androidmqttclient.data.model.AMCServerConnection
import com.example.androidmqttclient.data.model.AMCSubscription
import com.example.androidmqttclient.ui.components.MessageDetailsDialog
import com.example.androidmqttclient.ui.components.MessageItem
import com.example.androidmqttclient.ui.components.NewSubscriptionDialog
import com.example.androidmqttclient.ui.components.SubscriptionsOverviewDialog
import com.example.androidmqttclient.ui.theme.AndroidMQTTClientTheme

/**
 * Composable function for showing the subscribe screen.
 *
 * @param modifier The modifier to apply to the composable.
 * @param receivedMessages The list of received messages.
 * @param connectedServer The connected server connection.
 * @param activeSubscriptions The list of active subscriptions.
 * @param onAddSubscription The callback to invoke when a new subscription is added.
 * @param onUnsubscribe The callback to invoke when unsubscribing from an existing subscription.
 * @param onClearReceivedMessagesLog The callback to invoke when clearing the received messages log.
 * @param onShowCopyConfirmation The callback to invoke when showing a copy confirmation message.
 */
@Composable
fun SubscribeScreen(
    modifier: Modifier = Modifier,
    receivedMessages: List<AMCMessage>,
    connectedServer: AMCServerConnection?,
    activeSubscriptions: List<AMCSubscription>,
    onAddSubscription: (AMCSubscription) -> Unit = {},
    onUnsubscribe: (AMCSubscription) -> Unit = {},
    onClearReceivedMessagesLog: () -> Unit = {},
    onShowCopyConfirmation: (String) -> Unit = {},
) {
    // State to track if the new subscription dialog should be shown
    var showNewSubscriptionDialog by remember { mutableStateOf(false) }
    var showSubscriptionsOverviewDialog by remember { mutableStateOf(false) }
    var showClearLogDialog by remember { mutableStateOf(false) }
    var selectedMessageForDetails by remember { mutableStateOf<AMCMessage?>(null) }

    // Column holding buttons and list of messages
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.padding_small))
    ) {
        // New Subscription and View Subscriptions buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            // New Subscription button
            Button(
                onClick = { showNewSubscriptionDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                )
                Text(
                    text = stringResource(R.string.new_subscription),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge
                )
            }
            // View Subscriptions button
            Button(
                onClick = { showSubscriptionsOverviewDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.List,
                    contentDescription = null,
                )
                Text(
                    text = stringResource(R.string.view_subscriptions),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        HorizontalDivider(Modifier.padding(top = dimensionResource(R.dimen.padding_small)))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = dimensionResource(R.dimen.padding_small),bottom = dimensionResource(R.dimen.padding_small)),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val context = LocalContext.current
            val confirmMessage = stringResource(R.string.copied_to_clipboard)

            // Messages headline
            Text(
                text = stringResource(R.string.received_messages),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f)
            )
            // Clear log button
            IconButton(
                onClick = { showClearLogDialog = true },
                enabled = receivedMessages.isNotEmpty()
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
                    val clipboard = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    // Join all received messages into one large string
                    val logText = receivedMessages.joinToString("\n") { it.getMessageAsString() }
                    val clip = ClipData.newPlainText("MQTT Published Messages", logText)
                    clipboard.setPrimaryClip(clip)

                    onShowCopyConfirmation(confirmMessage)
                },
                enabled = receivedMessages.isNotEmpty()
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = stringResource(R.string.copy_to_clipboard),
                )
            }
        }

        // List of received messages
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(receivedMessages.asReversed()) { message ->
                // Show message item
                MessageItem(
                    message,
                    modifier = Modifier.clickable {
                        selectedMessageForDetails = message
                    }
                )
            }
        }
    }

    // Show the new subscription dialog if the state is true
    if( showNewSubscriptionDialog ) {
        NewSubscriptionDialog(
            onDismiss = { showNewSubscriptionDialog = false },
            onConfirm = { qos, topic, color ->
                val connectionId = connectedServer?.id ?: return@NewSubscriptionDialog

                // Create a new subscription
                val newSubscription = AMCSubscription(
                    serverConnectionId = connectionId,
                    qos = qos,
                    topic = topic,
                    color = color
                )

                // Callback with new subscription
                onAddSubscription(newSubscription)

                showNewSubscriptionDialog = false
            }
        )
    }

    // Show the subscriptions dialog if the state is true
    if( showSubscriptionsOverviewDialog ) {
        SubscriptionsOverviewDialog(
            subscriptions = activeSubscriptions,
            onDismiss = { showSubscriptionsOverviewDialog = false },
            onUnsubscribe = { subscription ->
                onUnsubscribe(subscription)
            }
        )
    }

    // Confirmation Dialog
    if (showClearLogDialog) {
        AlertDialog(
            onDismissRequest = { showClearLogDialog = false },
            title = { Text(stringResource(R.string.clear_received_messages)) },
            text = { Text(stringResource(R.string.are_you_sure_you_want_to_delete_the_received_messages)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearLogDialog = false
                        onClearReceivedMessagesLog()
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

    // Show message details dialog if selected
    selectedMessageForDetails?.let { message ->
        MessageDetailsDialog(
            message = message,
            onDismiss = { selectedMessageForDetails = null }
        )
    }
}

/**
 * Preview function for the SubscribeScreen composable.
 */
@Preview(showBackground = true)
@Composable
fun SubscribeScreenPreview() {
    AndroidMQTTClientTheme {
        SubscribeScreen(
            receivedMessages = listOf(
                AMCMessage("test/topic", "Test message 1",
                    0, false, timestamp = 0),
                AMCMessage("test/topic", "Test message 2",
                    1, false, timestamp = 12345),
                AMCMessage("test/topic", "Test message 3",
                    2, false, timestamp = 12345678)
            ),
            connectedServer = AMCServerConnection(),
            activeSubscriptions = listOf(),
            onAddSubscription = {}
        )
    }
}