package com.ud.finalproyect.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ud.finalproyect.viewmodel.AuthViewModel
import com.ud.finalproyect.viewmodel.SettingsViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.ud.finalproyect.R

@Composable
fun SettingsScreen(
    onSignOut: () -> Unit = {},
    authViewModel: AuthViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val user by authViewModel.user.collectAsState(initial = null)
    val notificationsEnabled by settingsViewModel.notificationsEnabled.collectAsState()
    val darkModeEnabled by settingsViewModel.darkModeEnabled.collectAsState()
    val doNotDisturbEnabled by settingsViewModel.doNotDisturbEnabled.collectAsState()
    val doNotDisturbStart by settingsViewModel.doNotDisturbStart.collectAsState()
    val doNotDisturbEnd by settingsViewModel.doNotDisturbEnd.collectAsState()
    val snoozeIntervalMinutes by settingsViewModel.snoozeIntervalMinutes.collectAsState()
    val currentLang = LocalConfiguration.current.locales[0].language

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Profile Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(modifier = Modifier.size(48.dp), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primary) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = user?.displayName ?: stringResource(id = R.string.settings_default_user),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = user?.email ?: stringResource(id = R.string.settings_default_email),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(id = R.string.settings_section_general),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Notifications
        item {
            SettingItem(
                title = stringResource(id = R.string.settings_notifications),
                icon = Icons.Default.Notifications,
                hasSwitch = true,
                checked = notificationsEnabled
            ) {
                settingsViewModel.toggleNotifications(it)
            }
        }

        // Language
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = stringResource(id = R.string.settings_language), style = MaterialTheme.typography.bodyLarge)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LanguageButton("Español", currentLang == "es") { settingsViewModel.changeLanguage("es") }
                    LanguageButton("Inglés", currentLang == "en") { settingsViewModel.changeLanguage("en") }
                }
            }
        }

        // Dark Mode
        item {
            SettingItem(
                title = stringResource(id = R.string.settings_dark_mode),
                icon = Icons.Default.DarkMode,
                hasSwitch = true,
                checked = darkModeEnabled
            ) {
                settingsViewModel.toggleDarkMode(it)
            }
        }

        // Do Not Disturb Section
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Notificaciones Avanzadas",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Do Not Disturb Toggle
        item {
            SettingItem(
                title = "Silencio Automático",
                icon = Icons.Default.Schedule,
                hasSwitch = true,
                checked = doNotDisturbEnabled
            ) {
                settingsViewModel.toggleDoNotDisturb(it)
            }
        }

        // Do Not Disturb Time Range
        if (doNotDisturbEnabled) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Horario sin notificaciones",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TimePickerField(
                                label = "Desde",
                                value = doNotDisturbStart,
                                onValueChange = { settingsViewModel.updateDoNotDisturbStart(it) },
                                modifier = Modifier.weight(1f)
                            )
                            TimePickerField(
                                label = "Hasta",
                                value = doNotDisturbEnd,
                                onValueChange = { settingsViewModel.updateDoNotDisturbEnd(it) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Text(
                            text = "Ejemplo: Sin notificaciones de $doNotDisturbStart a $doNotDisturbEnd",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }

        // Snooze Interval
        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Intervalo de Posponer",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(5, 10, 15, 30).forEach { minutes ->
                            FilterChip(
                                selected = snoozeIntervalMinutes == minutes,
                                onClick = { settingsViewModel.updateSnoozeInterval(minutes) },
                                label = { Text("$minutes min") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Text(
                        text = "Las notificaciones se pospondrán $snoozeIntervalMinutes minutos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onSignOut,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(text = stringResource(id = R.string.settings_sign_out), fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun LanguageButton(label: String, isSelected: Boolean, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray)
    }
}

@Composable
fun SettingItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    hasSwitch: Boolean = false,
    checked: Boolean = false,
    onCheckedChange: (Boolean) -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, style = MaterialTheme.typography.bodyLarge)
        }
        if (hasSwitch) Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun TimePickerField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val showTimePicker = remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = { },
        label = { Text(label) },
        readOnly = true,
        modifier = modifier.fillMaxWidth(),
        trailingIcon = {
            IconButton(onClick = { showTimePicker.value = true }) {
                Icon(Icons.Default.Schedule, contentDescription = null)
            }
        }
    )

    if (showTimePicker.value) {
        TimePickerDialog(
            initialTime = value,
            onConfirm = {
                onValueChange(it)
                showTimePicker.value = false
            },
            onDismiss = { showTimePicker.value = false }
        )
    }
}

@Composable
fun TimePickerDialog(
    initialTime: String = "00:00",
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val parts = initialTime.split(":")
    val initialHour = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val initialMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0

    val hour = remember { mutableStateOf(initialHour) }
    val minute = remember { mutableStateOf(initialMinute) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar hora") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hour Spinner
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Hora", style = MaterialTheme.typography.bodySmall)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(
                                onClick = { hour.value = (hour.value - 1 + 24) % 24 },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Text("-", style = MaterialTheme.typography.headlineSmall)
                            }
                            Text(
                                String.format("%02d", hour.value),
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.width(50.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            IconButton(
                                onClick = { hour.value = (hour.value + 1) % 24 },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Text("+", style = MaterialTheme.typography.headlineSmall)
                            }
                        }
                    }

                    Text(":", style = MaterialTheme.typography.headlineMedium)

                    // Minute Spinner
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Minuto", style = MaterialTheme.typography.bodySmall)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(
                                onClick = { minute.value = (minute.value - 5 + 60) % 60 },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Text("-", style = MaterialTheme.typography.headlineSmall)
                            }
                            Text(
                                String.format("%02d", minute.value),
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.width(50.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            IconButton(
                                onClick = { minute.value = (minute.value + 5) % 60 },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Text("+", style = MaterialTheme.typography.headlineSmall)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val timeString = String.format("%02d:%02d", hour.value, minute.value)
                onConfirm(timeString)
            }) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
