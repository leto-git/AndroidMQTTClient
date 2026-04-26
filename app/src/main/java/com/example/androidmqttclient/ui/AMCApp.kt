package com.example.androidmqttclient.ui

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.androidmqttclient.R
import com.example.androidmqttclient.viewmodel.AMCUiState
import com.example.androidmqttclient.data.model.MQTTConnectionState
import com.example.androidmqttclient.ui.screens.AddServerConnectionScreen
import com.example.androidmqttclient.ui.screens.ConnectScreen
import com.example.androidmqttclient.ui.screens.EditServerConnectionScreen
import com.example.androidmqttclient.ui.screens.PublishScreen
import com.example.androidmqttclient.ui.screens.StatusScreen
import com.example.androidmqttclient.ui.screens.SubscribeScreen
import com.example.androidmqttclient.viewmodel.AMCViewModel

/**
 * Sealed class representing different MQTT screens.
 *
 * @property route The route string for navigation.
 * @property title The string resource for the screen title.
 * @property icon The image vector for the screen icon.
 */
sealed class MQTTScreen (
    val route: String,
    @StringRes val title: Int,
    val icon: ImageVector
) {
    object Connect : MQTTScreen("connect", R.string.connect, Icons.Default.Call)
    object Subscribe : MQTTScreen("subscribe", R.string.subscribe, Icons.Default.Add)
    object AddServer: MQTTScreen("add_server", R.string.add_server_screen, Icons.Default.Add)
    object EditServer: MQTTScreen("edit_server/{serverId}", R.string.edit_server_screen, Icons.Default.Edit) {
        fun createRoute(serverId: Int) = "edit_server/$serverId"
    }
    object Publish : MQTTScreen("publish", R.string.publish, Icons.AutoMirrored.Default.Send)
    object Status : MQTTScreen("status", R.string.status, Icons.AutoMirrored.Filled.List)
    object Info : MQTTScreen("info", R.string.info, Icons.Default.Info)

    companion object {
        // Helper to find the screen object based on the current route
        fun fromRoute(route: String?): MQTTScreen {
            val baseRoute = route?.substringBefore("/")
            return when (baseRoute) {
                Connect.route -> Connect
                AddServer.route -> AddServer
                "edit_server" -> EditServer
                Subscribe.route -> Subscribe
                Publish.route -> Publish
                Status.route -> Status
                Info.route -> Info
                else -> Connect
            }
        }
    }
}

/**
 * Composable function defining the general app layout
 */
@Composable
fun AMCApp(
    navController: NavHostController = rememberNavController(),
    viewModel: AMCViewModel
) {
    // UI state from ViewModel
    val uiState by viewModel.uiState.collectAsState()

    // Snackbar host state
    val snackBarHostState = remember { SnackbarHostState() }

    // Navigation
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentScreen = MQTTScreen.fromRoute(currentRoute)
    val canNavigateBack =
        (navController.previousBackStackEntry != null) &&
        (currentScreen is MQTTScreen.AddServer || currentScreen is MQTTScreen.EditServer)

    // Show snackbar if error or info message is set
    LaunchedEffect(uiState.errorMessage, uiState.infoMessage) {
        val message = uiState.errorMessage ?: uiState.infoMessage
        val isError = uiState.errorMessage != null

        message?.let {
            snackBarHostState.showSnackbar(
                message = it,
                duration = if (isError) SnackbarDuration.Long else SnackbarDuration.Short,
                withDismissAction = true
            )
            // Clear message after showing it
            viewModel.clearStatusMessage()
        }
    }

    // Scaffold with top app bar, bottom navigation bar and snackbar host
    Scaffold(
        topBar = {
            MQTTAppBar(
                currentScreen = currentScreen,
                canNavigateBack = canNavigateBack,
                navigateUp = { navController.navigateUp() }
            )
        },
        bottomBar = {
            MQTTBottomBar(
                currentRoute = currentRoute,
                navController = navController,
                isConnected = uiState.connectionState == MQTTConnectionState.CONNECTED,
                onShowErrorMessage = { viewModel.showErrorMessage(it) }
            )
        },
        snackbarHost = {
            // Snackbar composable for showing error and info messages
            SnackbarHost(hostState = snackBarHostState) { data ->
                val isError = data.visuals.message == uiState.errorMessage
                Snackbar(
                    snackbarData = data,
                    containerColor = if (isError) {
                        MaterialTheme.colorScheme.errorContainer
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    },
                    contentColor = if (isError) {
                        MaterialTheme.colorScheme.onErrorContainer
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    },
                    dismissActionContentColor = if (isError) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                ReconnectIndicator(uiState.connectionState)

                NavigationHost(
                    navController = navController,
                    uiState = uiState,
                    viewModel = viewModel
                )
            }

            ConnectionIndicator(uiState.connectionState)
        }
    }
}

/**
 * Composable function for the top app bar showing current screen title etc.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MQTTAppBar(
    currentScreen: MQTTScreen,
    canNavigateBack: Boolean,
    modifier: Modifier = Modifier,
    navigateUp: () -> Unit = {},
) {
    TopAppBar(
        title = { Text(
            text = stringResource(currentScreen.title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
        ) },
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button)
                    )
                }
            }
        }
    )
}

/**
 * Composable showing different icons for screen navigation
 */
@Composable
fun MQTTBottomBar(
    currentRoute: String?,
    navController: NavHostController,
    isConnected: Boolean,
    onShowErrorMessage: (String) -> Unit,
) {
    NavigationBar {
        val bottomTabScreens = listOf(
            MQTTScreen.Connect,
            MQTTScreen.Subscribe,
            MQTTScreen.Publish,
            MQTTScreen.Status,
            MQTTScreen.Info
        )
        bottomTabScreens.forEach { screen ->
            val isSelected = currentRoute?.substringBefore("/") ==
                    screen.route.substringBefore("/")
            val connectFirstMessage = stringResource(R.string.please_connect_first)

            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = null) },
                selected = isSelected,
                enabled = if (screen == MQTTScreen.Subscribe || screen == MQTTScreen.Publish) {
                    isConnected
                } else {
                    true
                },
                onClick = {
                    // Prevent navigation to subscribe and publish if not connected
                    if( (screen == MQTTScreen.Subscribe || screen == MQTTScreen.Publish) &&
                        !isConnected ) {
                        onShowErrorMessage(connectFirstMessage)
                        return@NavigationBarItem
                    }

                    if (currentRoute?.substringBefore("/") !=
                        screen.route.substringBefore("/")
                    ) {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

/**
 * Composable function for the navigation host showing different screens
 */
@Composable
fun NavigationHost(
    navController: NavHostController,
    uiState: AMCUiState,
    viewModel: AMCViewModel
) {
    // NavHost composable for app navigation
    NavHost(
        navController = navController,
        startDestination = MQTTScreen.Connect.route,
    ) {
        // Connect screen
        composable(route = MQTTScreen.Connect.route) {
            val cannotEditMessage =
                stringResource(R.string.cannot_view_connection_while_connected)
            val alreadyConnectedMessage = stringResource(R.string.already_connected_to_a_server)

            ConnectScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(dimensionResource(R.dimen.padding_small)),
                uiState = uiState,
                onAddConnection = { navController.navigate(MQTTScreen.AddServer.route) },
                onConnect = { connection ->
                    // Prevent connecting if already connected
                    if (uiState.connectionState == MQTTConnectionState.CONNECTED ||
                        uiState.connectionState == MQTTConnectionState.RECONNECTING) {
                        viewModel.showErrorMessage(alreadyConnectedMessage)
                    } else {
                        viewModel.connect(connection)
                    }
                },
                onViewConnectionDetails = { connection ->
                    // Prevent editing connection of currently connected server
                    if (uiState.connectionState == MQTTConnectionState.CONNECTED &&
                        uiState.connectedServer?.id == connection.id) {
                        viewModel.showErrorMessage(cannotEditMessage)
                        return@ConnectScreen
                    }
                    navController.navigate(
                        MQTTScreen.EditServer.createRoute(connection.id)
                    )
                },
                onDisconnect = { viewModel.disconnect() },
                onDeleteConnection = { connection ->
                    viewModel.removeServer(connection)
                },
            )
        }

        // Add server screen
        composable(route = MQTTScreen.AddServer.route) {
            AddServerConnectionScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(dimensionResource(R.dimen.padding_small)),
                takenConnectionNames = uiState.takenConnectionNames,
                onAddConnection = {
                    viewModel.addServer(it)
                    navController.popBackStack()
                },
                onCancel = { navController.navigateUp() }
            )
        }

        // Edit server screen
        composable(
            route = MQTTScreen.EditServer.route,
            arguments = listOf(navArgument("serverId") { type = NavType.IntType })
        ) { backStackEntry ->
            val serverId = backStackEntry.arguments?.getInt("serverId")
            val connection = uiState.serverConnections.find { it.id == serverId }

            // Only show if connection is found to avoid crashes
            connection?.let {
                EditServerConnectionScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(dimensionResource(R.dimen.padding_small)),
                    takenConnectionNames = uiState.takenConnectionNames,
                    connection = it,
                    onSave = { connection ->
                        viewModel.updateServer(connection)
                    },
                    onDelete = { connection ->
                        viewModel.removeServer(connection)
                        navController.popBackStack()
                    },
                    onBack = { navController.navigateUp() }
                )
            }
        }

        // Subscribe screen
        composable(route = MQTTScreen.Subscribe.route) {
            SubscribeScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(dimensionResource(R.dimen.padding_small)),
                uiState = uiState,
                onAddSubscription = { viewModel.addSubscription(it) },
                onUnsubscribe = { viewModel.removeSubscription(it) },
                onClearReceivedMessagesLog = { viewModel.clearReceivedMessages() }
            )
        }

        // Publish screen
        composable(route = MQTTScreen.Publish.route) {
            PublishScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(dimensionResource(R.dimen.padding_small)),
                uiState = uiState,
                onTopicChange = { viewModel.updatePublishTopic(it) },
                onQosChange = { viewModel.updatePublishQos(it) },
                onRetainToggle = { viewModel.togglePublishRetain() },
                onMessageChange = { viewModel.updatePublishMessage(it) },
                onPublish = { viewModel.publish(it) },
                onClearPublishedMessagesLog = { viewModel.clearPublishedMessages() }
            )
        }

        // Status screen
        composable(route = MQTTScreen.Status.route) {
            StatusScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(dimensionResource(R.dimen.padding_small)),
                uiState = uiState,
                onShowCopyConfirmation = { confirmMessage ->
                    viewModel.showInfoMessage(confirmMessage)
                },
                onClearLog = { viewModel.clearLog() }
            )
        }

        // TODO: Replace placeholders with actual screens
        composable(route = MQTTScreen.Info.route) { PlaceholderScreen("Info") }
    }
}

/**
 * Composable function for the connection indicator
 */
@Composable
fun ConnectionIndicator(connectionState: MQTTConnectionState) {
    // Connecting or disconnecting indicator as animated visibility
    val isBusy =
        connectionState == MQTTConnectionState.CONNECTING ||
        connectionState == MQTTConnectionState.DISCONNECTING
    AnimatedVisibility(
        visible = isBusy,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                // Dim background
                .background(Color.Black.copy(alpha = 0.4f))
                // Block touch events to screens below
                .pointerInput(Unit) {},
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text =
                            if (connectionState == MQTTConnectionState.CONNECTING)
                                stringResource(R.string.connecting)
                            else
                                stringResource(R.string.disconnecting),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

/**
 * Composable function for the reconnect indicator
 */
@Composable
fun ReconnectIndicator(connectionState: MQTTConnectionState) {
    // Reconnecting indicator
    AnimatedVisibility(
        visible = connectionState == MQTTConnectionState.RECONNECTING,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.WifiOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.connection_lost_reconnecting),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(Modifier.width(12.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}


// TODO: Remove placeholder
@Composable
fun PlaceholderScreen(name: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = name, style = MaterialTheme.typography.headlineLarge)
    }
}