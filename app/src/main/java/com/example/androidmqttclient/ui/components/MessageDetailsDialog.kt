/*
 * Copyright 2026 Tobias Leikam (RheinMain University of Applied Sciences)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package com.example.androidmqttclient.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.androidmqttclient.R
import com.example.androidmqttclient.data.model.AMCMessage
import com.example.androidmqttclient.data.model.formatTimestamp
import com.example.androidmqttclient.ui.theme.AndroidMQTTClientTheme

/**
 * Composable function for showing a dialog with the message details.
 *
 * @param message The message to show the details for.
 * @param onDismiss The callback to invoke when the dialog is dismissed.
 */
@Composable
fun MessageDetailsDialog(
    message: AMCMessage,
    onDismiss: () -> Unit,
) {
    // Primary alert dialog to show the message detail
    AlertDialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .padding(vertical = 24.dp),
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        },
        title = { Text(
            text = stringResource(R.string.message_details),
            style = MaterialTheme.typography.titleLarge
        ) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                MessageDetailsContent(message)
            }
        },
    )
}

/**
 * Composable function for showing the message details content.
 *
 * @param message The message to show the details for.
 */
@Composable
fun MessageDetailsContent(
    message: AMCMessage
) {
    SelectionContainer {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
        ) {
            DetailItem(label = stringResource(R.string.topic), value = message.topic)

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                DetailItem(
                    label = stringResource(R.string.qos),
                    value = message.qos.toString(),
                    modifier = Modifier.weight(1f)
                )
                DetailItem(
                    label = stringResource(R.string.retain),
                    value = message.retain.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
            
            DetailItem(
                label = stringResource(R.string.timestamp),
                value = formatTimestamp(message.timestamp)
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Text(
                text = "Payload",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = MaterialTheme.shapes.small,
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Text(
                    text = message.payload,
                    modifier = Modifier
                        .padding(12.dp)
                        .horizontalScroll(rememberScrollState()),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    softWrap = false
                )
            }
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
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            softWrap = true
        )
    }
}

@Composable
@Preview(showBackground = true)
fun MessageDetailsContentPreview() {
    AndroidMQTTClientTheme {
        Surface (
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            MessageDetailsContent(
                message = AMCMessage(
                    topic = "test/topic",
                    payload = "{\n" +
                            "    \"name\": \"France\",\n" +
                            "    \"capital\": \"Paris\",\n" +
                            "    \"population\": 67364357,\n" +
                            "    \"area\": 551695,\n" +
                            "    \"currency\": \"Euro\",\n" +
                            "    \"languages\": [\"French\"],\n" +
                            "    \"region\": \"Europe\",\n" +
                            "    \"subregion\": \"Western Europe\",\n" +
                            "    \"flag\": \"https://upload.wikimedia.org/wikipedia/commons/c/c3/Flag_of_France.svg\"\n" +
                            "}",
                    qos = 0,
                    retain = false,
                    timestamp = 123456789
                )
            )
        }
    }
}