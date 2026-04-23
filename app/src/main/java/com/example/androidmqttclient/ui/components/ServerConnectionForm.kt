package com.example.androidmqttclient.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.androidmqttclient.data.AMCServerConnection
import com.example.androidmqttclient.data.MQTTVersion

/**
 * Composable function for displaying the server connection form.
 *
 * This is a base composable, that can be used by other server connection screens e.g. to add
 * or edit a server connection.
 *
 * @param modifier The modifier to apply to this layout.
 * @param existingConnection The existing server connection to edit, or null if creating a new one.
 * @param actions The composable content for the buttons and actions.
 */
@Composable
fun ServerConnectionForm(
    modifier: Modifier = Modifier,
    existingConnection: AMCServerConnection? = null,
    actions: @Composable (
        isValid: Boolean,
        isEditMode: Boolean,
        onToggleEdit: () -> Unit,
        currentData: () -> AMCServerConnection
    ) -> Unit
) {
    // State initialization
    var editingEnabled by remember { mutableStateOf(existingConnection == null) }

    var serverName by remember { mutableStateOf(existingConnection?.connectionName ?: "") }
    var serverAddress by remember { mutableStateOf(existingConnection?.serverAddress ?: "") }
    var serverPort by remember { mutableIntStateOf(existingConnection?.serverPort ?: 1883) }
    var useSSL by remember { mutableStateOf(existingConnection?.useSSL ?: false) }
    var clientID by remember {
        mutableStateOf(existingConnection?.clientID ?:
    "AMC_${System.currentTimeMillis().toString().takeLast(6)}")
    }
    var username by remember { mutableStateOf(existingConnection?.username ?: "") }
    var password by remember { mutableStateOf(existingConnection?.password ?: "") }
    var keepAlive by remember { mutableIntStateOf(existingConnection?.keepAlive ?: 60) }
    var cleanSession by remember { mutableStateOf(existingConnection?.cleanSession ?: true) }
    var willQos by remember { mutableIntStateOf(existingConnection?.willQos ?: 0) }
    var willRetain by remember { mutableStateOf(existingConnection?.willRetain ?: false) }
    var willTopic by remember { mutableStateOf(existingConnection?.willTopic ?: "") }
    var willMessage by remember { mutableStateOf(existingConnection?.willMessage ?: "") }

    val isValid =
        serverName.isNotBlank() &&
        serverAddress.isNotBlank() &&
        serverPort > 0 &&
        clientID.isNotBlank()

    val currentConnectionData = {
        AMCServerConnection(
            id = existingConnection?.id ?: 0,
            connectionName = serverName,
            mqttVersion = MQTTVersion.V3_1_1,
            serverAddress = serverAddress,
            serverPort = serverPort,
            useSSL = useSSL,
            clientID = clientID,
            username = username,
            password = password,
            keepAlive = keepAlive,
            cleanSession = cleanSession,
            willQos = willQos,
            willRetain = willRetain,
            willTopic = willTopic,
            willMessage = willMessage
        )
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            // Inject specific buttons from the parent composable
            Surface(
                tonalElevation = 0.dp,
                shadowElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    actions(
                        isValid,
                        editingEnabled,
                        { editingEnabled = !editingEnabled },
                        currentConnectionData
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // Server connection details
            ServerConnectionDetails(
                serverName, serverAddress, serverPort,
                useSSL,clientID, username, password, keepAlive, cleanSession,
                willQos, willRetain, willTopic, willMessage,
                editingEnabled = editingEnabled,
                onServerNameChange = { serverName = it },
                onServerAddressChange = { serverAddress = it },
                onServerPortChange = { serverPort = it },
                onUseSSLChange = {
                    useSSL = it
                    if( it && serverPort == 1883 ) serverPort = 8883
                    else if( !it && serverPort == 8883 ) serverPort = 1883
                },
                onClientIDChange = { clientID = it },
                onUsernameChange = { username = it },
                onPasswordChange = { password = it },
                onKeepAliveChange = { keepAlive = it },
                onCleanSessionChange = { cleanSession = it },
                onWillQosChange = { willQos = it },
                onWillRetainChange = { willRetain = it },
                onWillTopicChange = { willTopic = it },
                onWillMessageChange = { willMessage = it }
            )
            // Spacer
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}