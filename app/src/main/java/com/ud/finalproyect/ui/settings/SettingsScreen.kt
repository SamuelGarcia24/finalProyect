package com.ud.finalproyect.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
    val currentLang = LocalConfiguration.current.locales[0].language

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Tarjeta de Perfil
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

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(id = R.string.settings_section_general),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Notificaciones
        SettingItem(
            title = stringResource(id = R.string.settings_notifications),
            icon = Icons.Default.Notifications,
            hasSwitch = true,
            checked = notificationsEnabled
        ) {
            settingsViewModel.toggleNotifications(it)
        }

        // Idioma (Selector de Botones Estilizados)
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

        // Modo Oscuro
        SettingItem(
            title = stringResource(id = R.string.settings_dark_mode),
            icon = Icons.Default.DarkMode,
            hasSwitch = true,
            checked = darkModeEnabled
        ) {
            settingsViewModel.toggleDarkMode(it)
        }

        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onSignOut,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(text = stringResource(id = R.string.settings_sign_out), fontWeight = FontWeight.Bold)
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
