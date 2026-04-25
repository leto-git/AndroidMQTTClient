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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.androidmqttclient.data.AMCServerConnection
import com.example.androidmqttclient.data.MQTTVersion
import com.example.androidmqttclient.data.TransportProtocol
import com.example.androidmqttclient.data.isValidClientId
import com.example.androidmqttclient.data.isValidForPublishing
import com.example.androidmqttclient.data.isValidServerAddress
import com.example.androidmqttclient.data.isValidWebSocketPath

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

    var mqttVersion by remember { mutableStateOf(existingConnection?.mqttVersion ?: MQTTVersion.V3_1_1) }
    var serverName by remember { mutableStateOf(existingConnection?.connectionName ?: "") }
    var protocol by remember {
        mutableStateOf(
            when (existingConnection?.protocol) {
                TransportProtocol.SSL.prefix -> TransportProtocol.SSL
                TransportProtocol.WS.prefix -> TransportProtocol.WS
                TransportProtocol.WSS.prefix -> TransportProtocol.WSS
                else -> TransportProtocol.TCP
            }
        )
    }
    var serverAddress by remember {mutableStateOf(existingConnection?.serverAddress ?: "") }
    var webSocketPath by remember { mutableStateOf(existingConnection?.webSocketPath ?: "/mqtt") }
    var serverPort by remember { mutableIntStateOf(existingConnection?.serverPort ?: 1883) }
    var clientId by remember {
        mutableStateOf(existingConnection?.clientID ?:
    "AMC_${System.currentTimeMillis().toString().takeLast(6)}")
    }
    var username by remember { mutableStateOf(existingConnection?.username ?: "") }
    var password by remember { mutableStateOf(existingConnection?.password ?: "") }
    var keepAlive by remember { mutableIntStateOf(existingConnection?.keepAlive ?: 60) }
    var cleanSession by remember { mutableStateOf(existingConnection?.cleanSession ?: true) }
    var cleanStart by remember { mutableStateOf(existingConnection?.cleanSession ?: true) }
    var sessionExpiryInterval by remember { mutableLongStateOf(existingConnection?.keepAlive?.toLong() ?: 0L) }
    var willQos by remember { mutableIntStateOf(existingConnection?.willQos ?: 0) }
    var willRetain by remember { mutableStateOf(existingConnection?.willRetain ?: false) }
    var willTopic by remember { mutableStateOf(existingConnection?.willTopic ?: "") }
    var willMessage by remember { mutableStateOf(existingConnection?.willMessage ?: "") }

    val isHostValid = remember(serverAddress) { isValidServerAddress(serverAddress) }
    val isWebSockethPathValid = remember(webSocketPath) { isValidWebSocketPath(webSocketPath) }
    val isPortValid = remember(serverPort) { serverPort in 1..65535 }
    val isClientIdValid = remember(clientId) { isValidClientId(clientId) }
    val isLastWillQosValid = remember(willQos) { willQos in 0..2 }
    val isLastWillTopicValid = remember(willTopic) { isValidForPublishing(willTopic) }

    val isWillValid = willTopic.isEmpty() || isLastWillTopicValid

    val isValid = isHostValid &&
            isWebSockethPathValid &&
            isPortValid &&
            isClientIdValid &&
            isLastWillQosValid &&
            isWillValid

    val currentConnectionData = {
        AMCServerConnection(
            id = existingConnection?.id ?: 0,
            connectionName = serverName,
            mqttVersion = mqttVersion,
            protocol = protocol.prefix,
            serverAddress = serverAddress,
            serverPort = serverPort,
            webSocketPath =
                if (protocol == TransportProtocol.WS || protocol == TransportProtocol.WSS) webSocketPath
                else "",
            clientID = clientId,
            username = username,
            password = password,
            keepAlive = keepAlive,
            cleanSession = cleanSession,
            cleanStart = cleanStart,
            sessionExpiryInterval = sessionExpiryInterval,
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
                mqttVersion, serverName, serverAddress, webSocketPath,serverPort, protocol,
                clientId, username, password, keepAlive, cleanSession, cleanStart, sessionExpiryInterval,
                willQos, willRetain, willTopic, willMessage,
                isHostValid, isWebSockethPathValid, isPortValid, isClientIdValid,
                isLastWillQosValid, isLastWillTopicValid,
                editingEnabled = editingEnabled,
                onMqttVersionChange = { mqttVersion = it },
                onServerNameChange = { serverName = it },
                onServerAddressChange = { serverAddress = it },
                onWebsocketPathChange = { webSocketPath = it },
                onServerPortChange = { serverPort = it },
                onProtocolChange = { newProtocol ->
                    // If the user hasn't changed the port to a custom one, update it automatically
                    val knownDefaultPorts = TransportProtocol.entries.map { it.defaultPort }
                    if (serverPort in knownDefaultPorts || serverPort == 0) {
                        serverPort = newProtocol.defaultPort
                    }
                    // Default back to "/mqtt" if current path is empty
                    if( webSocketPath == "" &&
                        (newProtocol == TransportProtocol.WS ||newProtocol == TransportProtocol.WSS) ) {
                        webSocketPath = "/mqtt"
                    }
                    protocol = newProtocol
                },
                onClientIDChange = { clientId = it },
                onUsernameChange = { username = it },
                onPasswordChange = { password = it },
                onKeepAliveChange = { keepAlive = it },
                onCleanSessionChange = { cleanSession = it },
                onCleanStartChange = { cleanStart = it },
                onSessionExpiryIntervalChange = { sessionExpiryInterval = it },
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