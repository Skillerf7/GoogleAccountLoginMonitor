package com.galm.monitor.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.galm.monitor.R
import com.galm.monitor.data.SecurityEvent
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    uiState: UiState,
    events: List<SecurityEvent>,
    onSignInClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onAddManualEvent: (String, String?, String?, String?, Boolean) -> Unit,
    onOpenGoogleSecurity: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onClearError: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showLimitations by remember { mutableStateOf(false) }

    val filteredEvents = remember(events, uiState.searchQuery) {
        if (uiState.searchQuery.isBlank()) events
        else events.filter {
            it.description.contains(uiState.searchQuery, ignoreCase = true) ||
                    it.deviceName?.contains(uiState.searchQuery, ignoreCase = true) == true ||
                    it.eventType.contains(uiState.searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = { showLimitations = true }) {
                        Icon(Icons.Default.Info, contentDescription = "Einschränkungen")
                    }
                    if (uiState.isSignedIn) {
                        IconButton(onClick = onSignOutClick) {
                            Icon(Icons.Default.Logout, contentDescription = stringResource(R.string.sign_out))
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState.isSignedIn) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_manual_event))
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Account status card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (uiState.isSignedIn) {
                        Text(
                            text = "Angemeldet als",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = uiState.accountName ?: uiState.accountEmail ?: "Unbekannt",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        uiState.accountEmail?.let {
                            Text(text = it, style = MaterialTheme.typography.bodySmall)
                        }
                    } else {
                        Text(
                            text = "Nicht angemeldet",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onSignInClick,
                            enabled = !uiState.isLoading
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(stringResource(R.string.sign_in_with_google))
                        }
                    }
                }
            }

            // Open official Google Security page
            OutlinedButton(
                onClick = onOpenGoogleSecurity,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Icon(Icons.Default.Security, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.open_google_security))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Search
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = { Text(stringResource(R.string.search)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Event list
            Text(
                text = stringResource(R.string.security_events),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (filteredEvents.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.no_events))
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredEvents, key = { it.id }) { event ->
                        EventCard(event)
                    }
                }
            }
        }
    }

    // Error snackbar
    uiState.errorMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = onClearError,
            title = { Text("Fehler") },
            text = { Text(msg) },
            confirmButton = {
                TextButton(onClick = onClearError) { Text("OK") }
            }
        )
    }

    // Limitations dialog
    if (showLimitations) {
        AlertDialog(
            onDismissRequest = { showLimitations = false },
            title = { Text(stringResource(R.string.limitations_title)) },
            text = { Text(stringResource(R.string.limitations_text)) },
            confirmButton = {
                TextButton(onClick = { showLimitations = false }) { Text("Verstanden") }
            }
        )
    }

    // Add manual event dialog
    if (showAddDialog) {
        AddEventDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { desc, device, ip, loc, isNew ->
                onAddManualEvent(desc, device, ip, loc, isNew)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun EventCard(event: SecurityEvent) {
    val dateFormat = remember {
        SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.GERMAN)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (event.eventType) {
                "LIMITATION_NOTE" -> MaterialTheme.colorScheme.errorContainer
                "APP_SIGN_IN" -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = event.eventType,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                if (event.isNewDevice) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Neues Gerät") },
                        leadingIcon = {
                            Icon(Icons.Default.PhoneAndroid, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = dateFormat.format(Date(event.timestamp)),
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = event.description,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Details
            event.deviceName?.let {
                DetailRow(Icons.Default.PhoneAndroid, "Gerät", it)
            }
            event.deviceOs?.let {
                DetailRow(Icons.Default.Android, "OS", it)
            }
            event.ipAddress?.let {
                DetailRow(Icons.Default.Language, "IP", it)
            } ?: DetailRow(Icons.Default.Language, "IP", stringResource(R.string.not_available))
            event.location?.let {
                DetailRow(Icons.Default.LocationOn, "Standort", it)
            } ?: DetailRow(Icons.Default.LocationOn, "Standort", stringResource(R.string.not_available))

            Text(
                text = "Quelle: ${event.source}",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun AddEventDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String?, String?, String?, Boolean) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var deviceName by remember { mutableStateOf("") }
    var ipAddress by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var isNewDevice by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_manual_event)) },
        text = {
            Column {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Beschreibung *") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = deviceName,
                    onValueChange = { deviceName = it },
                    label = { Text("Gerätename (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = ipAddress,
                    onValueChange = { ipAddress = it },
                    label = { Text("IP-Adresse (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Standort (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isNewDevice, onCheckedChange = { isNewDevice = it })
                    Text("Als neues Gerät markieren")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (description.isNotBlank()) {
                        onConfirm(
                            description,
                            deviceName.ifBlank { null },
                            ipAddress.ifBlank { null },
                            location.ifBlank { null },
                            isNewDevice
                        )
                    }
                },
                enabled = description.isNotBlank()
            ) {
                Text("Hinzufügen")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Abbrechen") }
        }
    )
}
