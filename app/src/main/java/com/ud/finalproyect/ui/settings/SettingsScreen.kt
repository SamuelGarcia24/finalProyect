package com.ud.finalproyect.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ud.finalproyect.viewmodel.AuthViewModel

@Composable
fun SettingsScreen(
    onSignOut: () -> Unit = {},
    authViewModel: AuthViewModel = viewModel()
) {
    val user by authViewModel.user.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Sección de Perfil
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Person, 
                            contentDescription = null, 
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = user?.displayName ?: "Usuario", 
                        style = MaterialTheme.typography.titleMedium, 
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = user?.email ?: "Sin correo", 
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Ajustes Generales", 
            style = MaterialTheme.typography.labelLarge, 
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Opción: Notificaciones
        SettingItem(title = "Notificaciones", icon = Icons.Default.Notifications, hasSwitch = true)

        // Opción: Idioma
        SettingItem(title = "Idioma (Español/Inglés)", icon = Icons.Default.Language)

        // Opción: Modo Oscuro
        SettingItem(title = "Modo Oscuro", icon = Icons.Default.DarkMode, hasSwitch = true)

        Spacer(modifier = Modifier.weight(1f))

        // Botón Cerrar Sesión
        Button(
            onClick = { onSignOut() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Cerrar Sesión", fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun SettingItem(title: String, icon: ImageVector, hasSwitch: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, style = MaterialTheme.typography.bodyLarge)
        }
        if (hasSwitch) {
            var checked by remember { mutableStateOf(true) }
            Switch(checked = checked, onCheckedChange = { checked = it })
        }
    }
}
