package com.ud.finalproyect.ui.addmedication

import android.app.TimePickerDialog
import android.widget.TimePicker
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ud.finalproyect.navigation.Screen
import com.ud.finalproyect.viewmodel.AddMedicationViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationScreen(
    navController: NavController,
    userId: String,
    viewModel: AddMedicationViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }

    var doseValue by remember { mutableStateOf("") }
    var doseUnit by remember { mutableStateOf("mg") }
    val doseUnits = listOf("mg", "g", "ml", "µg")
    var doseError by remember { mutableStateOf(false) }

    var intervalHours by remember { mutableStateOf(0) }
    var intervalMinutes by remember { mutableStateOf(0) }
    var intervalText by remember { mutableStateOf("Select time") }
    var intervalError by remember { mutableStateOf(false) }

    var startHours by remember { mutableStateOf(0) }
    var startMinutes by remember { mutableStateOf(0) }
    var startText by remember { mutableStateOf("Select time") }
    var startError by remember { mutableStateOf(false) }

    var duration by remember { mutableStateOf("") }
    var durationError by remember { mutableStateOf(false) }

    var savedSuccess by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val isFormValid = run {
        val nameValid = name.trim().length >= 2
        val doseValid = doseValue.isNotEmpty() && doseValue.toIntOrNull() != null && doseValue.toInt() > 0
        val intervalValid = intervalText != "Select time"
        val startValid = startText != "Select time"
        val durationValid = duration.isNotEmpty() && duration.toIntOrNull() != null && duration.toInt() in 1..365
        nameValid && doseValid && intervalValid && startValid && durationValid
    }

    fun showIntervalTimePicker() {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            context,
            { _: TimePicker, hourOfDay: Int, minute: Int ->
                intervalHours = hourOfDay
                intervalMinutes = minute
                intervalText = String.format("%02d:%02d", hourOfDay, minute)
                intervalError = false
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    fun showStartTimePicker() {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            context,
            { _: TimePicker, hourOfDay: Int, minute: Int ->
                startHours = hourOfDay
                startMinutes = minute
                startText = String.format("%02d:%02d", hourOfDay, minute)
                startError = false
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    LaunchedEffect(savedSuccess) {
        if (savedSuccess) {
            navController.navigate(Screen.Home.route) {
                popUpTo("add_medication") { inclusive = true }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Add Medication") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = it.trim().length < 2 && it.isNotEmpty()
                },
                label = { Text("Medication Name") },
                isError = nameError,
                supportingText = { if (nameError) Text("Minimum 2 characters") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = doseValue,
                    onValueChange = {
                        if (it.isEmpty() || it.all { c -> c.isDigit() }) {
                            doseValue = it
                            doseError = it.isNotEmpty() && (it.toIntOrNull() == null || it.toInt() <= 0)
                        }
                    },
                    label = { Text("Dose") },
                    isError = doseError,
                    supportingText = { if (doseError) Text("Enter a positive number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )

                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    TextField(
                        value = doseUnit,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .width(80.dp)
                            .menuAnchor(),
                        shape = MaterialTheme.shapes.small
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        doseUnits.forEach { unit ->
                            DropdownMenuItem(
                                text = { Text(unit) },
                                onClick = {
                                    doseUnit = unit
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = intervalText,
                onValueChange = {},
                readOnly = true,
                label = { Text("Interval (HH:MM)") },
                isError = intervalError,
                supportingText = { if (intervalError) Text("Select an interval") },
                trailingIcon = {
                    IconButton(onClick = { showIntervalTimePicker() }) {
                        Icon(Icons.Default.AccessTime, contentDescription = "Select interval")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = startText,
                onValueChange = {},
                readOnly = true,
                label = { Text("Start Time (HH:MM)") },
                isError = startError,
                supportingText = { if (startError) Text("Select a start time") },
                trailingIcon = {
                    IconButton(onClick = { showStartTimePicker() }) {
                        Icon(Icons.Default.AccessTime, contentDescription = "Select start time")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = duration,
                onValueChange = {
                    if (it.isEmpty() || it.all { c -> c.isDigit() }) {
                        duration = it
                        val d = it.toIntOrNull()
                        durationError = d != null && (d < 1 || d > 365)
                    }
                },
                label = { Text("Duration (days)") },
                isError = durationError,
                supportingText = { if (durationError) Text("Enter days between 1 and 365") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    viewModel.saveMedication(
                        userId = userId,
                        name = name,
                        doseValue = doseValue,
                        doseUnit = doseUnit,
                        intervalHours = intervalHours,
                        intervalMinutes = intervalMinutes,
                        startTime = startText,
                        durationDays = duration.toInt()
                    )
                    savedSuccess = true
                },
                enabled = isFormValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Medication")
            }
        }
    }
}