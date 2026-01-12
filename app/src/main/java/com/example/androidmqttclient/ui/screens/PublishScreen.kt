package com.example.androidmqttclient.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.androidmqttclient.R
import com.example.androidmqttclient.data.MqttMessage
import com.example.androidmqttclient.ui.theme.AndroidMQTTClientTheme
import kotlinx.coroutines.launch

@Composable
fun PublishScreen(
    modifier: Modifier = Modifier,
    onPublish: (MqttMessage) -> Unit = {}
) {
    // State variables for input fields
    var topic by remember { mutableStateOf("") }
    var qos by remember { mutableIntStateOf(0) }
    var retain by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TODO: Enable jumping to next field on enter with `keyboardOptions.imeAction`
            // Topic input
            OutlinedTextField(
                value = topic,
                onValueChange = { topic = it },
                label = { Text(stringResource(R.string.topic)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                singleLine = true
            )

            // QoS and Retain
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // QoS input
                OutlinedTextField(
                    value = if (qos < 0 || qos > 2) "" else qos.toString(),
                    onValueChange = { newValue ->
                        // Only allow numeric input and limit to 1 character (QoS max is 2)
                        if (newValue.all { it.isDigit() } && newValue.length <= 1) {
                            qos = newValue.toIntOrNull() ?: -1
                        }
                    },
                    label = { Text(stringResource(R.string.qos)) },
                    modifier = Modifier
                        .weight(0.5f)
                        .height(64.dp),
                    singleLine = true
                )

                // Retain checkbox
                Row(
                    modifier = Modifier
                        .weight(0.5f)
                        .height(64.dp)
                        .clickable { retain = !retain },
                    horizontalArrangement = Arrangement.spacedBy(0.5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = retain,
                        onCheckedChange = { retain = it }
                    )
                    Text(
                        text =stringResource(R.string.retain),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Message input
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text(stringResource(R.string.message)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(128.dp)
            )

            // Publish button
            Button(
                onClick = {
                    // TODO: Check for valid input
                    // Publish and show confirmation snackBar
                    onPublish(MqttMessage(topic, qos, retain, message))
                    scope.launch {
                        snackBarHostState.showSnackbar(
                            message = "Message published",
                            duration = SnackbarDuration.Short,
                            withDismissAction = true
                        )
                    }
                    // Reset input fields
                    topic = ""
                    qos = 0
                    retain = false
                    message = ""
                },
                modifier = Modifier
                    .padding(top = dimensionResource(R.dimen.padding_medium)),
                enabled = topic.isNotBlank() && message.isNotBlank()
            ) {
                Text(stringResource(R.string.publish))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PublishScreenPreview(

) {
    AndroidMQTTClientTheme {
        Surface(
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium)),
        ) {
            PublishScreen()
        }
    }
}