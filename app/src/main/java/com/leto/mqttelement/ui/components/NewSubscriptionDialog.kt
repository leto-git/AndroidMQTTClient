/*
 * Copyright 2026 Tobias Leikam (RheinMain University of Applied Sciences)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package com.leto.mqttelement.ui.components

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.leto.mqttelement.R
import com.leto.mqttelement.data.model.isValidForSubscribing
import com.leto.mqttelement.ui.theme.MqttElementTheme

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

    val isTopicValid = remember(topic) { isValidForSubscribing(topic) }

    // Define the dialog content
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.new_subscription),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            NewSubscriptionContent(
                qos,
                topic,
                subscriptionColor,
                isTopicValid,
                onQoSChange = { qos = it },
                onTopicChange = { topic = it },
                onColorChange = { subscriptionColor = it },
            )
        },

        // Confirm (Subscribe) button
        confirmButton = {
            Button(
                onClick = { onConfirm(qos, topic, subscriptionColor) },
                enabled = isTopicValid
            ) {
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
    isTopicValid: Boolean,
    onQoSChange: (Int) -> Unit,
    onTopicChange: (String) -> Unit,
    onColorChange: (Long) -> Unit,
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

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Column{
            // QoS input
            Text(
                text = stringResource(R.string.quality_of_service_qos),
                style = MaterialTheme.typography.titleMedium
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
        }

        // Topic input
        OutlinedTextField(
            value = topic,
            onValueChange = onTopicChange,
            label = { Text(stringResource(R.string.topic)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = !isTopicValid && topic.isNotEmpty(),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Uri
            )
        )

        Column {
            // Color input
            Text(
                text = stringResource(R.string.color_for_subscription_messages),
                style = MaterialTheme.typography.titleMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
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
}

@Preview(showBackground = true)
@Composable
fun NewSubscriptionContentPreview() {
    MqttElementTheme {
        Surface(
            modifier = Modifier.padding(16.dp),
        ) {
            NewSubscriptionContent(
                qos = 0,
                topic = "test/topic",
                subscriptionColor = 0xFFFF0000,
                isTopicValid = true,
                onQoSChange = {},
                onTopicChange = {},
                onColorChange = {}
            )
        }
    }
}
