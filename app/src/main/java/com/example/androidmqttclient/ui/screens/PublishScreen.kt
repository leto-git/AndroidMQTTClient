package com.example.androidmqttclient.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.androidmqttclient.R
import com.example.androidmqttclient.data.model.AMCMessage
import com.example.androidmqttclient.data.model.isValidForPublishing
import com.example.androidmqttclient.ui.components.MessageDetailsDialog
import com.example.androidmqttclient.ui.components.MessageItem
import com.example.androidmqttclient.ui.theme.AndroidMQTTClientTheme

/**
 * Composable function for showing the publish screen.
 *
 * @param modifier Modifier for styling.
 * @param publishTopic The topic to publish to.
 * @param publishQos The QoS to use.
 * @param publishRetain Whether to retain the message.
 * @param publishMessage The message to publish.
 * @param publishedMessages The list of published messages.
 * @param onTopicChange Callback for updating the topic.
 * @param onQosChange Callback for updating the QoS.
 * @param onRetainToggle Callback for toggling the retain flag.
 * @param onMessageChange Callback for updating the message.
 * @param onPublish Callback for publishing a message.
 * @param onClearPublishedMessagesLog Callback for clearing the published messages log.
 * @param onShowCopyConfirmation Callback for showing a confirmation message.
 */
@Composable
fun PublishScreen(
    modifier: Modifier = Modifier,
    publishTopic: String,
    publishQos: Int,
    publishRetain: Boolean,
    publishMessage: String,
    publishedMessages: List<AMCMessage>,
    onTopicChange: (String) -> Unit = {},
    onQosChange: (Int) -> Unit = {},
    onRetainToggle: () -> Unit = {},
    onMessageChange: (String) -> Unit = {},
    onPublish: (AMCMessage) -> Unit = {},
    onClearPublishedMessagesLog: () -> Unit = {},
    onShowCopyConfirmation: (String) -> Unit = {},
) {
    // Local focus manager to jump to next field on enter
    val focusManager = LocalFocusManager.current

    var showClearLogDialog by remember { mutableStateOf(false) }
    val isTopicValid by remember(publishTopic) {
        mutableStateOf(isValidForPublishing(publishTopic))
    }
    var selectedMessageForDetails by remember { mutableStateOf<AMCMessage?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Topic input
        OutlinedTextField(
            value = publishTopic,
            onValueChange = onTopicChange,
            label = { Text(stringResource(R.string.topic)) },
            isError = !isTopicValid && publishTopic.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )

        // QoS and Retain
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // QoS input
            OutlinedTextField(
                value =
                    if (publishQos < 0 || publishQos > 2) ""
                    else publishQos.toString(),
                onValueChange = { newValue ->
                    // Only allow numeric input and limit to 1 character (QoS max is 2)
                    if (newValue.isEmpty()) {
                        onQosChange(-1)
                    } else if (newValue.all { it.isDigit() } && newValue.length <= 1) {
                        val qos = newValue.toInt()
                        if (qos in 0..2) {
                            onQosChange(qos)
                        }
                    }
                },
                label = { Text(stringResource(R.string.qos)) },
                modifier = Modifier
                    .weight(0.5f)
                    .height(64.dp),
                singleLine = true,
                isError = publishQos < 0 || publishQos > 2,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            // Retain checkbox
            Row(
                modifier = Modifier
                    .weight(0.5f)
                    .height(64.dp)
                    .clickable { onRetainToggle() },
                horizontalArrangement = Arrangement.spacedBy(0.5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = publishRetain,
                    onCheckedChange = { onRetainToggle() }
                )
                Text(
                    text =stringResource(R.string.retain),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Message input
        OutlinedTextField(
            value = publishMessage,
            onValueChange = onMessageChange,
            label = { Text(stringResource(R.string.message)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            )
        )

        // Publish button
        Button(
            onClick = {
                // Close keyboard
                focusManager.clearFocus()

                // Publish and show confirmation snackBar
                onPublish(
                    AMCMessage(
                        publishTopic,
                        publishMessage,
                        publishQos,
                        publishRetain
                    )
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = dimensionResource(R.dimen.padding_small)),
            enabled = publishTopic.isNotBlank() && isTopicValid
        ) {
            Text(stringResource(R.string.publish))
        }

        HorizontalDivider(Modifier.padding(top = dimensionResource(R.dimen.padding_small)))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = dimensionResource(R.dimen.padding_small),bottom = dimensionResource(R.dimen.padding_small)),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val context = LocalContext.current
            val confirmMessage = stringResource(R.string.copied_to_clipboard)

            // Messages headline
            Text(
                text = stringResource(R.string.published_messages),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f)
            )
            // Clear log button
            IconButton(
                onClick = { showClearLogDialog = true },
                enabled = publishedMessages.isNotEmpty()
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.clear_log)
                )
            }

            // Vertical Divider between buttons
            VerticalDivider(
                modifier = Modifier
                    .height(24.dp)
                    .padding(horizontal = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Copy to clipboard button
            IconButton (
                onClick = {
                    val clipboard = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    // Join all published messages into one large string
                    val logText = publishedMessages.joinToString("\n") { it.getMessageAsString() }
                    val clip = ClipData.newPlainText("MQTT Published Messages", logText)
                    clipboard.setPrimaryClip(clip)

                    onShowCopyConfirmation(confirmMessage)
                },
                enabled = publishedMessages.isNotEmpty()
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = stringResource(R.string.copy_to_clipboard),
                )
            }
        }

        // List of published messages
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(publishedMessages.asReversed()) { message ->
                // Show message item
                MessageItem(
                    message,
                    modifier = Modifier.clickable {
                        selectedMessageForDetails = message
                    }
                )
            }
        }

        // Confirmation Dialog
        if (showClearLogDialog) {
            AlertDialog(
                onDismissRequest = { showClearLogDialog = false },
                title = { Text(stringResource(R.string.clear_published_messages)) },
                text = { Text(stringResource(R.string.are_you_sure_you_want_to_delete_the_published_messages)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showClearLogDialog = false
                            onClearPublishedMessagesLog()
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(stringResource(R.string.clear))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearLogDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }

        // Show message details dialog if selected
        selectedMessageForDetails?.let { message ->
            MessageDetailsDialog(
                message = message,
                onDismiss = { selectedMessageForDetails = null }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PublishScreenPreview() {
    AndroidMQTTClientTheme {
        Surface(
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium)),
        ) {
            PublishScreen(
                publishTopic = "test/topic",
                publishQos = 0,
                publishRetain = false,
                publishMessage = "Test message",
                publishedMessages = listOf(
                    AMCMessage("test/topic", "Test message 1",
                        0, false, timestamp = 0),
                    AMCMessage("test/topic", "Test message 2",
                        1, false, timestamp = 12345),
                    AMCMessage("test/topic", "Test message 3",
                        2, false, timestamp = 123456789)
                )
            )
        }
    }
}