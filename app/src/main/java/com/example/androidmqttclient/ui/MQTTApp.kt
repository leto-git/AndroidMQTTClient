package com.example.androidmqttclient.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.androidmqttclient.R
import com.example.androidmqttclient.data.repository.MQTTRepository
import com.example.androidmqttclient.ui.screens.ConnectScreen
import com.example.androidmqttclient.ui.screens.PublishScreen
import com.example.androidmqttclient.viewmodel.MQTTViewModel
import com.example.androidmqttclient.ui.screens.SubscribeScreen

enum class MQTTScreen(@StringRes val title: Int, val icon: ImageVector) {
    // TODO: Replace placeholder icons with custom ones
    Connect(R.string.connect, Icons.Default.Call),
    Subscribe(R.string.subscribe, Icons.Default.Add),
    Publish(R.string.publish, Icons.AutoMirrored.Default.Send),
    Stats(R.string.stats, Icons.AutoMirrored.Filled.List),
    Info(R.string.info, Icons.Default.Info)
}

/**
 * [MQTTApp] composable defining the general app layout
 */
@Composable
fun MQTTApp(
    navController: NavHostController = rememberNavController()
) {
    // Create MQTTRepository and ViewModel
    val mqttRepository = MQTTRepository(LocalContext.current)
    val viewModel: MQTTViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T: ViewModel> create (modelClass: Class<T>): T {
                return MQTTViewModel(mqttRepository) as T
            }
        }
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentScreen = MQTTScreen.valueOf(currentRoute ?: MQTTScreen.Connect.name)

    Scaffold(
        topBar = {
            MQTTAppBar(
                currentScreen = currentScreen,
                canNavigateBack = false
            )
        },
        bottomBar = {
            MQTTBottomBar(
                currentRoute = currentRoute,
                navController = navController
            )
        }
    ) { innerPadding ->
        val uiState by viewModel.uiState.collectAsState()

        // NavHost composable for app navigation
        NavHost(
            navController = navController,
            startDestination = MQTTScreen.Connect.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Connect screen
            composable(route = MQTTScreen.Connect.name) {
                ConnectScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(dimensionResource(R.dimen.padding_medium)),
                    uiState = uiState,
                    onAddServer = { viewModel.addServer(it) },
                    onConnect = { viewModel.connect(it) },
                    onErrorDismissed = { viewModel.clearError() }
                )
            }

            // Subscribe screen
            composable(route = MQTTScreen.Subscribe.name) {
                SubscribeScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(dimensionResource(R.dimen.padding_medium)),
                    uiState = uiState,
                    onAddSubscription = { viewModel.addSubscription(it) }
                )
            }

            // Publish screen
            composable(route = MQTTScreen.Publish.name) {
                PublishScreen (
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(dimensionResource(R.dimen.padding_medium)),
                    onPublish = { mqttMessage ->
                        viewModel.publish(mqttMessage)
                    }
                )
            }

            // TODO: Replace placeholders with actual screens
            composable(route = MQTTScreen.Stats.name) { PlaceholderScreen("Stats") }
            composable(route = MQTTScreen.Info.name) { PlaceholderScreen("Info") }
        }
    }
}

/**
 * [MQTTAppBar] composable for the top app bar showing current screen title etc.
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
            style = MaterialTheme.typography.headlineMedium
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
 * [MQTTBottomBar] composable showing different icons for screen navigation
 */
@Composable
fun MQTTBottomBar(
    currentRoute: String?,
    navController: NavHostController
) {
    NavigationBar {
        MQTTScreen.entries.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = null) },
                selected = currentRoute == screen.name,
                onClick = {
                    if (currentRoute != screen.name) {
                        navController.navigate(screen.name) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
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