package com.example.androidmqttclient.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.androidmqttclient.R
import com.example.androidmqttclient.data.MQTTUiState
import com.example.androidmqttclient.data.Subscription
import com.example.androidmqttclient.ui.NewSubscriptionDialog
import com.example.androidmqttclient.ui.SubscriptionsOverviewDialog
import com.example.androidmqttclient.ui.theme.AndroidMQTTClientTheme

/**
 * [SubscribeScreen] is the composable function for showing the subscribe screen.
 */
@Composable
fun SubscribeScreen(
    modifier: Modifier = Modifier,
    uiState: MQTTUiState,
    onAddSubscription: (Subscription) -> Unit = {}
) {
    // State to track if the new subscription dialog should be shown
    var showNewSubscriptionDialog by remember { mutableStateOf(false) }
    var showSubscriptionsOverviewDialog by remember { mutableStateOf(false) }

    // Column holding buttons and list of messages
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.padding_medium))
    ) {
        // Row with two buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(
                dimensionResource(R.dimen.padding_small)
            )
        ) {
            // New Subscription button
            Button(
                onClick = { showNewSubscriptionDialog = true },
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.new_subscription))
            }
            // View Subscriptions button
            Button(
                onClick = { showSubscriptionsOverviewDialog = true },
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.view_subscriptions))
            }
        }

        HorizontalDivider()

        // Messages headline
        Text(
            text = stringResource(R.string.messages),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(
                top = dimensionResource(R.dimen.padding_medium),
                bottom = dimensionResource(R.dimen.padding_small))
        )

        // List of received messages
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(uiState.messages) { message ->
                MessageItem(message)
            }
        }
    }

    // Show the new subscription dialog if the state is true
    if( showNewSubscriptionDialog ) {
        NewSubscriptionDialog(
            onDismiss = { showNewSubscriptionDialog = false },
            onConfirm = { qos, topic, color ->
                // Create a new subscription
                val newSubscription = Subscription(qos, topic, color)

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
            onDismiss = { showSubscriptionsOverviewDialog = false }
        )
    }
}

@Composable
fun MessageItem(message: String) {
    // TODO: Message items should also display date and time of receipt, MQTT topic and QoS-Level
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(R.dimen.padding_small))
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium
        )
        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Preview(showBackground = true)
@Composable
fun SubscribeScreenPreview() {
    AndroidMQTTClientTheme {
        SubscribeScreen(
            uiState = MQTTUiState(
                subscriptions = listOf(Subscription(0, "test/topic", 0xFFFF0000))
            ),
            onAddSubscription = {}
        )
    }
}