package com.example.androidmqttclient.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.androidmqttclient.R
import com.example.androidmqttclient.ui.theme.AndroidMQTTClientTheme

/**
 * Composable function for creating a new subscription.
 *
 * @param onDismiss The callback to invoke when the dialog is dismissed.
 * @param onConfirm The callback to invoke when the user confirms the subscription.
 */
@Composable
fun NewSubscriptionDialog(
    onDismiss: () -> Unit,
    onConfirm: (qos: Int, topic: String, color: Long) -> Unit
) {
    // Subscription parameters
    var qos by remember { mutableIntStateOf(0) }
    var topic by remember { mutableStateOf("") }
    var subscriptionColor by remember { mutableLongStateOf(0xFFFF0000) }

    // Define the dialog content
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.new_subscription)) },
        text = {
            NewSubscriptionContent(
                qos,
                topic,
                subscriptionColor,
                onQoSChange = { qos = it },
                onTopicChange = { topic = it },
                onColorChange = { subscriptionColor = it }
            )
        },

        // Confirm (Subscribe) button
        confirmButton = {
            TextButton(onClick = { onConfirm(qos, topic, subscriptionColor) }) {
                Text(stringResource(R.string.subscribe))
            }
        },

        // Cancel button
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

/**
 * Composable function for displaying the content of the new subscription dialog.
 *
 * @param qos The current QoS level.
 * @param topic The current topic.
 * @param subscriptionColor The current subscription color.
 * @param onQoSChange The callback to invoke when the QoS level changes.
 * @param onTopicChange The callback to invoke when the topic changes
 * @param onColorChange The callback to invoke when the subscription color changes.
 */
@Composable
fun NewSubscriptionContent(
    qos: Int,
    topic: String,
    subscriptionColor: Long,
    onQoSChange: (Int) -> Unit,
    onTopicChange: (String) -> Unit,
    onColorChange: (Long) -> Unit
) {
    // List of available colors for subscription messages
    val subscriptionColors = listOf(
        0xFFFF0000, // Red
        0xFFFFA500, // Orange
        0xFFFFFF00, // Yellow
        0xFF008000, // Green
        0xFF0000FF, // Blue
        0xFF800080, // Purple
    )

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // QoS input
        Text(
            text = "Quality of Service (QoS)",
            style = MaterialTheme.typography.bodyMedium
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            listOf(0, 1, 2).forEach { qosLevel ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .selectable(
                            selected = (qos == qosLevel),
                            onClick = { onQoSChange(qosLevel) },
                            role = Role.RadioButton
                        )
                        .padding(4.dp)
                ) {
                    RadioButton(
                        selected = qos == qosLevel,
                        onClick = null
                    )
                    Text(
                        text ="QoS $qosLevel",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }

        HorizontalDivider()

        // Topic input
        TextField(
            value = topic,
            onValueChange = onTopicChange,
            label = { Text(stringResource(R.string.topic)) },
            modifier = Modifier.fillMaxWidth()
        )

        HorizontalDivider()

        // Color input
        Text(
            text = stringResource(R.string.color_for_subscription_messages),
            style = MaterialTheme.typography.bodyMedium
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            subscriptionColors.forEach { colorValue ->
                val color = Color(colorValue)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(color, CircleShape)
                        .clickable { onColorChange(colorValue) }
                        .border(
                            width = if (subscriptionColor == colorValue) 3.dp else 0.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NewSubscriptionContentPreview() {
    AndroidMQTTClientTheme {
        Surface(
            modifier = Modifier.padding(16.dp),
        ) {
            NewSubscriptionContent(
                qos = 0,
                topic = "test/topic",
                subscriptionColor = 0xFFFF0000,
                onQoSChange = {},
                onTopicChange = {},
                onColorChange = {}
            )
        }
    }
}
