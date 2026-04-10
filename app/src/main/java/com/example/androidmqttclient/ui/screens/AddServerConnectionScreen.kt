package com.example.androidmqttclient.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.androidmqttclient.data.AMCServerConnection
import com.example.androidmqttclient.data.MQTTVersion
import com.example.androidmqttclient.ui.components.ServerConnectionDetails
import com.example.androidmqttclient.ui.theme.AndroidMQTTClientTheme
import com.example.androidmqttclient.R
import com.example.androidmqttclient.ui.components.ServerConnectionForm

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