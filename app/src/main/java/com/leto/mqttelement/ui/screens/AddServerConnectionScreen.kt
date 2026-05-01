/*
 * Copyright 2026 Tobias Leikam (RheinMain University of Applied Sciences)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package com.leto.mqttelement.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.leto.mqttelement.R
import com.leto.mqttelement.data.model.AMCServerConnection
import com.leto.mqttelement.ui.components.ServerConnectionForm
import com.leto.mqttelement.ui.theme.MqttElementTheme

/**
 * Composable function for adding a new server connection.
 *
 * @param onAddConnection The callback to invoke when a new connection is added.
 * @param onCancel The callback to invoke when the user cancels the operation.
 * @param modifier The modifier to apply to this layout.
 */
@Composable
fun AddServerConnectionScreen(
    modifier: Modifier = Modifier,
    takenConnectionNames: List<String> = emptyList(),
    onAddConnection: (AMCServerConnection) -> Unit,
    onCancel: () -> Unit,
) {
    ServerConnectionForm(
        modifier = modifier,
        existingConnection = null,
        takenConnectionNames = takenConnectionNames,
    ) { isValid, _, _, currentData ->
        // Add button
        Button(
            onClick = { onAddConnection(currentData()) },
            modifier = Modifier
                .fillMaxWidth(),
            enabled = isValid
        ) {
            Text(stringResource(R.string.add))
        }

        // Cancel button
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            Text(stringResource(R.string.cancel))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ServerConnectionScreenPreview() {
    MqttElementTheme {
        AddServerConnectionScreen(
            onAddConnection = {},
            onCancel = {}
        )
    }
}