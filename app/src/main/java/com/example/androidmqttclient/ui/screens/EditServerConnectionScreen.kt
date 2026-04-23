package com.example.androidmqttclient.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.androidmqttclient.R
import com.example.androidmqttclient.data.AMCServerConnection
import com.example.androidmqttclient.ui.components.ServerConnectionForm
import com.example.androidmqttclient.ui.theme.AndroidMQTTClientTheme

/**
 * Composable function for editing an existing server connection.
 *
 * @param connection The server connection to edit.
 * @param onSave The callback to invoke when the user wants to save the changes to the connection.
 * @param onDelete The callback to invoke when the user wants to delete the connection.
 * @param onBack The callback to invoke when the user cancels the operation.
 * @param modifier The modifier to apply to this layout.
 */
@Composable
fun EditServerConnectionScreen(
    connection: AMCServerConnection,
    onSave: (AMCServerConnection) -> Unit,
    onDelete: (AMCServerConnection) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // State initialization
    var showDeleteDialog by remember { mutableStateOf(false) }

    ServerConnectionForm(
        modifier = modifier,
        existingConnection = connection
    ) { isValid, isEditMode, onToggleEdit, currentData ->
        // Back, and Edit/Save buttons
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.back))
            }

            Button(
                onClick = {
                    if( isEditMode ) {
                        // Save changes and exit edit mode
                        onSave(currentData())
                        onToggleEdit()
                    } else {
                        // Enter edit mode
                        onToggleEdit()
                    }
                },
                modifier = Modifier.weight(1f),
                // In edit mode enable save only when valid, otherwise edit is always enabled
                enabled = if (isEditMode) isValid else true
            ) {
                Text(
                    text =
                        if (isEditMode) stringResource(R.string.save)
                        else stringResource(R.string.edit)
                )
            }
        }

        // Delete Button
        OutlinedButton (
            onClick = { showDeleteDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
        ) {
            Text(stringResource(R.string.delete_connection))
        }

        // Confirmation Dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text(stringResource(R.string.delete_connection)) },
                text = { Text(stringResource(
                    R.string.are_you_sure_you_want_to_delete,
                    connection.connectionName)
                ) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            onDelete(connection)
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(stringResource(R.string.delete))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }
}

@Preview
@Composable
fun EditServerConnectionScreenPreview() {
    AndroidMQTTClientTheme {
        EditServerConnectionScreen(
            connection = AMCServerConnection(),
            onSave = {},
            onDelete = {},
            onBack = {}
        )
    }
}