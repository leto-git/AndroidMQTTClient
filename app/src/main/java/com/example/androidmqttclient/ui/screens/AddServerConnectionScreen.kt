package com.example.androidmqttclient.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.androidmqttclient.R
import com.example.androidmqttclient.data.AMCServerConnection
import com.example.androidmqttclient.ui.components.ServerConnectionForm
import com.example.androidmqttclient.ui.theme.AndroidMQTTClientTheme

/**
 * Composable function for adding a new server connection.
 *
 * @param onAddConnection The callback to invoke when a new connection is added.
 * @param onCancel The callback to invoke when the user cancels the operation.
 * @param modifier The modifier to apply to this layout.
 */
@Composable
fun AddServerConnectionScreen(
    onAddConnection: (AMCServerConnection) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    ServerConnectionForm(
        modifier = modifier,
        existingConnection = null
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
    AndroidMQTTClientTheme {
        AddServerConnectionScreen(
            onAddConnection = {},
            onCancel = {}
        )
    }
}