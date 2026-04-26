package com.example.androidmqttclient.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.androidmqttclient.R
import com.example.androidmqttclient.data.model.AMCMessage
import com.example.androidmqttclient.data.model.formatTimestamp

/**
 * Composable function for displaying a single message item.
 *
 * @param message The message to display.
 */
@Composable
fun MessageItem(
    message: AMCMessage
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
        val subscriptionColor = message.subscriptionColor ?: Color.Gray

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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Text(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .weight(1f, fill = false),
                    text = message.topic,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.MiddleEllipsis
                )
                Text(
                    text = stringResource(R.string.qos) + ": " + message.qos,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
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

@Preview(showBackground = true, widthDp = 400)
@Composable
fun MessageItemPreview() {
    MessageItem(
        message = AMCMessage(
            topic = "very/long/test/topic/that/should/be/truncated",
            payload = "Test message",
            qos = 0,
            retain = false,
            timestamp = 123456789
        )
    )
}