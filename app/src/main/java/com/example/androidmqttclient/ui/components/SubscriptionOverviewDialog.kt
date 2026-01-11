package com.example.androidmqttclient.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.androidmqttclient.R
import com.example.androidmqttclient.data.MqttSubscription
import com.example.androidmqttclient.ui.theme.AndroidMQTTClientTheme

/**
 * [SubscriptionsOverviewDialog] is the composable function for showing the currently active subscriptions.
 */
@Composable
fun SubscriptionsOverviewDialog(
    subscriptions: List<MqttSubscription> = listOf(),
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(
            text = stringResource(R.string.active_subscriptions),
            style = MaterialTheme.typography.headlineSmall
        ) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                SubscriptionsOverviewContent(subscriptions)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

@Composable
fun SubscriptionsOverviewContent(subscriptions: List<MqttSubscription>) {
    HorizontalDivider()

    // List of subscriptions
    LazyColumn(
        modifier = Modifier.heightIn(max = 400.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // TODO: Make subscription items clickable so that the user can unsubscribe somehow
        items(subscriptions) { subscription ->
            SubscriptionItem(subscription)
        }
    }
}

@Composable
fun SubscriptionItem(subscription: MqttSubscription) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(R.dimen.padding_small))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Box showing the subscription color
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(Color(subscription.color))
            )
            // Subscription topic
            Text(
                text = subscription.topic,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .weight(1f)
            )
            // Subscription QoS level
            Text(
                text = "QoS: ${subscription.qos}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun SubscriptionsOverviewContentPreview() {
    AndroidMQTTClientTheme {
        Surface (
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            SubscriptionsOverviewContent(
                subscriptions = listOf(
                    MqttSubscription(
                        qos = 2,
                        topic = "test/topic/1",
                        color = 0xFFFF0000
                    ),
                    MqttSubscription(
                        qos = 0,
                        topic = "test/topic/2",
                        color = 0xFF008000
                    )
                )
            )
        }
    }
}