package com.example.androidmqttclient.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SettingsEthernet
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidmqttclient.R
import com.example.androidmqttclient.ui.theme.AndroidMQTTClientTheme

/**
 * Composable function for the Info screen.
 *
 * @param modifier The modifier for the composable.
 */
@Composable
fun InfoScreen(
    modifier: Modifier = Modifier
) {
    // Uri handler for opening links
    val uriHandler = LocalUriHandler.current

    // URLs
    val githubUrl = stringResource(R.string.url_github)
    val pahoUrl = stringResource(R.string.paho_url)
    val sqlcipherUrl = stringResource(R.string.sqlcipher_url)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Logo
        Surface(
            modifier = Modifier.size(100.dp),
            color = colorResource(id = R.color.ic_launcher_background),
            shape = MaterialTheme.shapes.extraLarge,
            shadowElevation = 4.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier.size(125.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // App Name & Version
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = stringResource(R.string.current_version),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Description Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Text(
                text = stringResource(R.string.app_description),
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Links and Documentation
        Text(
            text = stringResource(R.string.links_documentation),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            color = MaterialTheme.colorScheme.primary
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            InfoActionItem(
                icon = Icons.Default.Code,
                label = stringResource(R.string.source_code_github),
                onClick = {
                    uriHandler.openUri(githubUrl)
                }
            )
            // TODO: Mail to developer?
            InfoActionItem(
                icon = Icons.Default.Description,
                label = stringResource(R.string.eclipse_paho),
                onClick = { uriHandler.openUri(pahoUrl) }
            )
            InfoActionItem(
                icon = Icons.Default.Description,
                label = stringResource(R.string.sqlcipher),
                onClick = { uriHandler.openUri(sqlcipherUrl) }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Libraries and Licenses
        Text(
            text = stringResource(R.string.libraries_licenses),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            color = MaterialTheme.colorScheme.primary
        )
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

            // Eclipse Paho
            LibraryCard(
                title = stringResource(R.string.eclipse_paho),
                version = "1.2.5",
                license = stringResource(R.string.paho_license_notice),
                icon = Icons.Default.SettingsEthernet
            )

            // SQLCipher
            LibraryCard(
                title = stringResource(R.string.sqlcipher),
                version = "4.14.1",
                license = stringResource(R.string.sqlcipher_license_notice),
                icon = Icons.Default.Lock,
                iconTint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(32.dp))

        // Credits
        Text(
            text = stringResource(R.string.credits),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Composable function for an action item in the Info screen.
 * (e.g. Documentation, Source Code, etc.)
 *
 * @param icon The icon for the action.
 * @param label The label for the action.
 * @param onClick The action to perform when the item is clicked.
 */
@Composable
fun InfoActionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

/**
 * Composable function for a library card in the Info screen.
 *
 * @param title The title of the library.
 * @param version The version of the library.
 * @param license The license for the library.
 * @param icon The icon for the library.
 * @param iconTint The tint color for the icon.
 */
@Composable
fun LibraryCard(
    title: String,
    version: String,
    license: String,
    icon: ImageVector,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = iconTint)
                Spacer(Modifier.width(8.dp))
                Text(text = title, style = MaterialTheme.typography.titleSmall)
            }
            Text(text = "Version: $version", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp))
            Text(
                text = license,
                style = MaterialTheme.typography.bodySmall,
                lineHeight = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
fun InfoScreenPreview() {
    AndroidMQTTClientTheme {
        InfoScreen()
    }
}