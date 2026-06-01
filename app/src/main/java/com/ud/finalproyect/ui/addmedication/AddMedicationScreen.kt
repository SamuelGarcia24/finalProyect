package com.ud.finalproyect.ui.addmedication

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ud.finalproyect.viewmodel.AddMedicationViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationScreen(
    navController: NavController,
    userId: String,
    viewModel: AddMedicationViewModel = viewModel()
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    // Obtenemos la hora actual y un formateador para AM/PM
    val calendarInstance = remember { Calendar.getInstance() }
    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    
    // Estados del formulario
    var name by remember { mutableStateOf("") }
    var doseValue by remember { mutableStateOf("") }
    var doseUnit by remember { mutableStateOf("mg") }
    
    // Frecuencia: inicia vacía para que el usuario no tenga que borrar valores por defecto
    var frequencyValue by remember { mutableStateOf("") }
    var frequencyUnit by remember { mutableStateOf("Horas") }
    val frequencyUnits = listOf("Horas", "Días", "Semanas")
    
    // Hora de inicio: inicia con la hora actual formateada en AM/PM
    var startText by remember { mutableStateOf(timeFormatter.format(calendarInstance.time)) }
    var duration by remember { mutableStateOf("") }

    val isFormValid = name.isNotBlank() && doseValue.isNotBlank() && 
                      frequencyValue.isNotBlank() && duration.isNotBlank()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Nuevo Medicamento", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Sección: Información Básica
            InfoSection(title = "Información General") {
                CustomTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Nombre del medicamento",
                    icon = Icons.Default.Medication,
                    placeholder = "Ej: Ibuprofeno"
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    CustomTextField(
                        value = doseValue,
                        onValueChange = { if (it.all { char -> char.isDigit() }) doseValue = it },
                        label = "Dosis",
                        icon = Icons.Default.Scale,
                        modifier = Modifier.weight(1f),
                        keyboardType = KeyboardType.Number
                    )
                    
                    var expanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.padding(top = 8.dp)) {
                        FilterChip(
                            selected = true,
                            onClick = { expanded = true },
                            label = { Text(doseUnit) },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) }
                        )
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            listOf("mg", "g", "ml", "gotas").forEach { unit ->
                                DropdownMenuItem(text = { Text(unit) }, onClick = {
                                    doseUnit = unit; expanded = false
                                })
                            }
                        }
                    }
                }
            }

            // Sección: Horarios Flexible (soporta Horas, Días, Semanas)
            InfoSection(title = "Configuración de Horarios") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CustomTextField(
                        value = frequencyValue,
                        onValueChange = { if (it.all { char -> char.isDigit() }) frequencyValue = it },
                        label = "Cada cuanto",
                        icon = Icons.Default.Update,
                        modifier = Modifier.weight(1f),
                        keyboardType = KeyboardType.Number,
                        placeholder = "Ej: 8"
                    )
                    
                    var unitExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.padding(top = 8.dp)) {
                        FilterChip(
                            selected = true,
                            onClick = { unitExpanded = true },
                            label = { Text(frequencyUnit) },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) }
                        )
                        DropdownMenu(expanded = unitExpanded, onDismissRequest = { unitExpanded = false }) {
                            frequencyUnits.forEach { unit ->
                                DropdownMenuItem(text = { Text(unit) }, onClick = {
                                    frequencyUnit = unit; unitExpanded = false
                                })
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TimePickerField(
                    label = "Hora de la primera toma",
                    value = startText,
                    icon = Icons.Default.AccessTime,
                    onClick = {
                        val calendar = Calendar.getInstance()
                        TimePickerDialog(context, { _, h, m -> 
                            val selectedCal = Calendar.getInstance()
                            selectedCal.set(Calendar.HOUR_OF_DAY, h)
                            selectedCal.set(Calendar.MINUTE, m)
                            startText = timeFormatter.format(selectedCal.time)
                        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()
                    }
                )
            }

            // Sección: Duración
            InfoSection(title = "Duración del Tratamiento") {
                CustomTextField(
                    value = duration,
                    onValueChange = { if (it.all { char -> char.isDigit() }) duration = it },
                    label = "Número de días",
                    icon = Icons.Default.CalendarToday,
                    placeholder = "Ej: 7",
                    keyboardType = KeyboardType.Number
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón de Guardar: incluye cálculo de intervalo y salida de pantalla
            Button(
                onClick = {
                    val fVal = frequencyValue.toIntOrNull() ?: 0
                    // Calculamos el intervalo base en horas para la lógica interna
                    val totalHours = when (frequencyUnit) {
                        "Días" -> fVal * 24
                        "Semanas" -> fVal * 24 * 7
                        else -> fVal
                    }
                    val frequencyString = "Cada $frequencyValue ${frequencyUnit.lowercase()}"

                    viewModel.saveMedication(
                        userId = userId,
                        name = name,
                        doseValue = doseValue,
                        doseUnit = doseUnit,
                        frequency = frequencyString,
                        intervalHours = totalHours,
                        intervalMinutes = 0,
                        startTime = startText,
                        durationDays = duration.toIntOrNull() ?: 1
                    )
                    // Salimos de la pantalla inmediatamente tras guardar
                    navController.popBackStack()
                },
                enabled = isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(4.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Guardar Medicamento", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun InfoSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.Transparent
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerField(label: String, value: String, icon: ImageVector, onClick: () -> Unit) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.outline)
        }
    }
}
