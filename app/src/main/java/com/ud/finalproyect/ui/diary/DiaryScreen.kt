package com.ud.finalproyect.ui.diary

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
// ...existing code...
import androidx.compose.ui.res.stringResource
import com.ud.finalproyect.R
import com.ud.finalproyect.model.data.ScheduledDose
import com.ud.finalproyect.viewmodel.CalendarViewModel
import com.ud.finalproyect.viewmodel.DiaryViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.LocalDate
import androidx.compose.ui.platform.LocalConfiguration
import java.time.format.TextStyle

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DiaryScreen(
    userId: String,
    modifier: Modifier = Modifier,
    viewModel: DiaryViewModel = viewModel()
) {
    val calendarViewModel: CalendarViewModel = viewModel()
    val calendarMeds by calendarViewModel.medications.collectAsState()

    val days = remember { (-3..3).map { LocalDate.now().plusDays(it.toLong()) } }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val locale = LocalConfiguration.current.locales[0]

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) viewModel.loadForUser(userId)
    }

    // Obtener dosis programadas para el día seleccionado usando CalendarViewModel
    val dosesForDate: List<ScheduledDose> = remember(selectedDate, calendarMeds) {
        calendarViewModel.getDosesForDate(selectedDate)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(days) { date ->
                val isSelected = date == selectedDate
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .width(60.dp)
                        .clickable { selectedDate = date },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = date.month.getDisplayName(TextStyle.SHORT, locale),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = date.dayOfMonth.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (dosesForDate.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.diary_no_meds),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                items(dosesForDate) { dose ->
                            DiaryDoseItem(
                                dose = dose,
                                date = selectedDate,
                                onToggleTaken = { medId, dateStr, scheduledTime, currentlyTaken ->
                                    if (currentlyTaken) {
                                        // unmark
                                        viewModel.unmarkTaken(medId, dateStr, scheduledTime)
                                    } else {
                                        // mark with current time
                                        val now = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
                                        viewModel.markTaken(medId, dateStr, scheduledTime, now)
                                    }
                                }
                            )
                }
            }
        }
    }
}

@Composable
fun DiaryDoseItem(
    dose: ScheduledDose,
    date: LocalDate,
    onToggleTaken: (String, String, String, Boolean) -> Unit
) {
    val scheduledTime = dose.time
    val isAlreadyTaken = !dose.actualTime.isNullOrEmpty()

    // Habilitar el botón solo si la hora actual está dentro de la ventana permitida (±10 minutos)
    // y si la fecha seleccionada es hoy.
    fun isWithinWindow(timeStr: String, date: LocalDate, windowMinutes: Long = 10): Boolean {
        try {
            if (date != LocalDate.now()) return false
            val formatter = DateTimeFormatter.ofPattern("hh:mm a")
            val scheduled = LocalTime.parse(timeStr, formatter)
            val now = LocalTime.now()
            val diff = kotlin.math.abs(ChronoUnit.MINUTES.between(now, scheduled))
            return diff <= windowMinutes
        } catch (e: Exception) {
            return false
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(bottom = 12.dp)
    ) {
        // Tiempo a la izquierda
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(80.dp)
        ) {
            Text(text = scheduledTime, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Box(modifier = Modifier
                .width(4.dp)
                .height(60.dp)
                .background(MaterialTheme.colorScheme.primary))
        }

        Card(
            modifier = Modifier
                .padding(start = 12.dp)
                .fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = dose.medicationName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(text = dose.dose, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                    if (isAlreadyTaken) {
                        Text(text = stringResource(id = R.string.diary_taken_at, dose.actualTime!!), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                    }
                }

                val canMark = !isAlreadyTaken && isWithinWindow(scheduledTime, date)
                val statusColor = if (isAlreadyTaken) {
                    if (isSystemInDarkTheme()) Color(0xFF8CF39A) else Color(0xFF2E7D32)
                } else if (canMark) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline
                }

                IconButton(
                    onClick = {
                        val isoKey = try {
                            LocalTime.parse(scheduledTime, DateTimeFormatter.ofPattern("hh:mm a")).format(DateTimeFormatter.ofPattern("HH:mm"))
                        } catch (e: Exception) {
                            try { LocalTime.parse(scheduledTime, DateTimeFormatter.ofPattern("HH:mm")).format(DateTimeFormatter.ofPattern("HH:mm")) } catch (ex: Exception) { scheduledTime }
                        }
                        if (isAlreadyTaken) {
                            onToggleTaken(dose.medicationId, date.toString(), isoKey, true)
                        } else if (canMark) {
                            onToggleTaken(dose.medicationId, date.toString(), isoKey, false)
                        }
                    },
                    enabled = true
                ) {
                    Icon(
                        imageVector = if (isAlreadyTaken) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = stringResource(id = R.string.diary_mark_as_taken),
                        tint = statusColor,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    }
}