package com.example.androidmqttclient.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.androidmqttclient.R
import com.example.androidmqttclient.data.AMCSubscription
import com.example.androidmqttclient.ui.theme.AndroidMQTTClientTheme

/**
 * Composable function for showing the currently active subscriptions.
 *
 * @param subscriptions The list of subscriptions to show.
 * @param onDismiss The callback to invoke when the dialog is dismissed.
 * @param onUnsubscribe The callback to invoke when unsubscribing.
 */
@Composable
fun SubscriptionsOverviewDialog(
    subscriptions: List<AMCSubscription> = listOf(),
    onDismiss: () -> Unit,
    onUnsubscribe: (AMCSubscription) -> Unit
) {
    // State to track the subscription to delete
    var subscriptionToRemove by remember { mutableStateOf<AMCSubscription?>(null) }

    // Primary alert dialog to show the currently active subscriptions
    AlertDialog(
        modifier = Modifier.fillMaxWidth(),
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        },
        title = { Text(
            text = stringResource(R.string.active_subscriptions),
            style = MaterialTheme.typography.headlineSmall
        ) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                SubscriptionsOverviewContent(
                    subscriptions = subscriptions,
                    onUnsubscribe = { subscriptionToRemove = it }
                )
            }
        },
    )

    // Secondary alert dialog to ask for confirmation to unsubscribe
    subscriptionToRemove?.let { subscription ->
        AlertDialog(
            onDismissRequest = { subscriptionToRemove = null },
            title = { Text(stringResource(R.string.unsubscribe)) },
            text = {
                Text(stringResource(R.string.are_you_sure_you_want_to_unsubscribe))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onUnsubscribe(subscription)
                        subscriptionToRemove = null
                    }
                ) {
                    Text(stringResource(R.string.unsubscribe), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { subscriptionToRemove = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

/**
 * Composable function for showing the currently active subscriptions.
 *
 * @param subscriptions The list of subscriptions to show.
 * @param onUnsubscribe The callback to invoke when when unsubscribing.
 */
@Composable
fun SubscriptionsOverviewContent(
    subscriptions: List<AMCSubscription>,
    onUnsubscribe: (AMCSubscription) -> Unit
) {
    if (subscriptions.isEmpty()) {
        Text(
            text = stringResource(R.string.no_subscriptions),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
    else {
        HorizontalDivider()

        // List of subscriptions
        LazyColumn(
            modifier = Modifier.heightIn(max = 400.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(subscriptions) { subscription ->
                SubscriptionItem(
                    subscription = subscription,
                    onUnsubscribe = onUnsubscribe
                )
            }
        }
    }
}

/**
 * Composable function for showing a single subscription item.
 *
 * @param subscription The subscription to show.
 * @param onUnsubscribe The callback to invoke when unsubscribing.
 */
@Composable
fun SubscriptionItem(
    subscription: AMCSubscription,
    onUnsubscribe: (AMCSubscription) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Box showing the subscription color
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(8.dp)
                .background(Color(subscription.color))
        )
        // Subscription QoS level and topic
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
        ) {
            // Subscription QoS level
            Text(
                text = "QoS: ${subscription.qos}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            // Subscription topic
            Text(
                text = subscription.topic,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.MiddleEllipsis
            )
        }
        // Unsubscribe button
        IconButton(
            onClick = { onUnsubscribe(subscription) },
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.unsubscribe),
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
}

@Preview(showBackground = true)
@Composable
fun SubscriptionsOverviewContentPreview() {
    AndroidMQTTClientTheme {
        Surface (
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            SubscriptionsOverviewContent(
                subscriptions = listOf(
                    AMCSubscription(
                        qos = 2,
                        topic = "test/topic/1",
                        color = 0xFFFF0000
                    ),
                    AMCSubscription(
                        qos = 0,
                        topic = "very/long/test/topic/that/will/likely/overflow/the/area/provided/by/the/alert/box/123",
                        color = 0xFF008000
                    )
                ),
                onUnsubscribe = {}
            )
        }
    }
}