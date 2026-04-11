package com.example.androidmqttclient.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.androidmqttclient.R
import com.example.androidmqttclient.data.AMCMessage
import com.example.androidmqttclient.data.AMCSubscription
import com.example.androidmqttclient.data.AMCUiState
import com.example.androidmqttclient.data.formatTimestamp
import com.example.androidmqttclient.ui.components.NewSubscriptionDialog
import com.example.androidmqttclient.ui.components.SubscriptionsOverviewDialog
import com.example.androidmqttclient.ui.theme.AndroidMQTTClientTheme

/**
 * Composable function for showing the subscribe screen.
 *
 * @param modifier The modifier to apply to the composable.
 * @param uiState The current UI state.
 * @param onAddSubscription The callback to invoke when a new subscription is added.
 * @param onUnsubscribe The callback to invoke when unsubscribing from an existing subscription.
 */
@Composable
fun SubscribeScreen(
    modifier: Modifier = Modifier,
    uiState: AMCUiState,
    onAddSubscription: (AMCSubscription) -> Unit = {},
    onUnsubscribe: (AMCSubscription) -> Unit = {}
) {
    // State to track if the new subscription dialog should be shown
    var showNewSubscriptionDialog by remember { mutableStateOf(false) }
    var showSubscriptionsOverviewDialog by remember { mutableStateOf(false) }

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

        // Messages headline
        Text(
            text = stringResource(R.string.messages),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(
                top = dimensionResource(R.dimen.padding_small),
                bottom = dimensionResource(R.dimen.padding_small))
        )

        // List of received messages
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(uiState.receivedMessages.asReversed()) { message ->
                // Determine subscription color for message based on topic
                val subscriptionColorLong = uiState.subscriptions
                    .find { it.topic == message.topic }?.color ?: 0xFF808080
                val subscriptionColor = Color(subscriptionColorLong)
                // Show message item
                MessageItem(message, subscriptionColor)
            }
        }
    }

    // Show the new subscription dialog if the state is true
    if( showNewSubscriptionDialog ) {
        NewSubscriptionDialog(
            onDismiss = { showNewSubscriptionDialog = false },
            onConfirm = { qos, topic, color ->
                // Create a new subscription
                val newSubscription = AMCSubscription(qos, topic, color)

                // Callback with new subscription
                onAddSubscription(newSubscription)

                showNewSubscriptionDialog = false
            }
        )
    }

    // Show the subscriptions dialog if the state is true
    if( showSubscriptionsOverviewDialog ) {
        SubscriptionsOverviewDialog(
            subscriptions = uiState.subscriptions,
            onDismiss = { showSubscriptionsOverviewDialog = false },
            onUnsubscribe = { subscription ->
                onUnsubscribe(subscription)
            }
        )
    }
}

/**
 * Composable function for displaying a single message item.
 *
 * @param message The message to display.
 * @param subscriptionColor The color of the subscription.
 */
@Composable
fun MessageItem(
    message: AMCMessage,
    subscriptionColor: Color
) {
    // Format timestamp into a readable string
    val formattedDate = remember(message.timestamp) {
        formatTimestamp(message.timestamp)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        // Box showing the subscription color
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(dimensionResource(R.dimen.padding_small))
                .background(subscriptionColor)
        )

        // Message content
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(dimensionResource(R.dimen.padding_small))
        ) {
            // Message timestamp, topic and qos
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.topic) + ": " + message.topic,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.qos) + ": " + message.qos,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // Message payload
            Text(
                text = message.payload,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
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
            uiState = AMCUiState(
                subscriptions = listOf(
                    AMCSubscription(0, "test/topic", 0xFFFF0000)
                ),
                receivedMessages = listOf(
                    AMCMessage("test/topic", "Test message 1",
                        0, false, timestamp = 0),
                    AMCMessage("test/topic", "Test message 2",
                        1, false, timestamp = 12345),
                    AMCMessage("test/topic", "Test message 3",
                        2, false, timestamp = 123456789)
                )
            ),
            onAddSubscription = {}
        )
    }
}