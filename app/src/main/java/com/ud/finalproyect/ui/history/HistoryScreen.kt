package com.ud.finalproyect.ui.history

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.res.stringResource
import com.ud.finalproyect.R
import com.ud.finalproyect.model.data.Medication
import com.ud.finalproyect.viewmodel.HistoryViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.ui.platform.LocalConfiguration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: HistoryViewModel = viewModel()) {
    val medications by viewModel.medications.collectAsState()
    val locale = LocalConfiguration.current.locales[0]
    
    // Filtro de mes seleccionado (null significa "Todos")
    var selectedMonth by remember { mutableStateOf<YearMonth?>(null) }

    // Generar dinámicamente los últimos 12 meses
    val filterMonths = remember {
        val list = mutableListOf<YearMonth>()
        var current = YearMonth.now()
        repeat(12) {
            list.add(current)
            current = current.minusMonths(1)
        }
        list
    }

    // Filtrar medicamentos por el mes seleccionado
    val filteredMedications = remember(medications, selectedMonth) {
        if (selectedMonth == null) {
            medications
        } else {
            medications.filter { med ->
                try {
                    val start = LocalDate.parse(med.startDate)
                    val end = LocalDate.parse(med.endDate)
                    val medStartMonth = YearMonth.from(start)
                    val medEndMonth = YearMonth.from(end)
                    
                    // El medicamento está activo en el mes seleccionado si el mes está en el rango
                    !selectedMonth!!.isBefore(medStartMonth) && !selectedMonth!!.isAfter(medEndMonth)
                } catch (e: Exception) {
                    false
                }
            }
        }
    }

    // Agrupar los medicamentos filtrados por el mes de inicio para una visualización seccionada
    val groupedMedications = remember(filteredMedications) {
        filteredMedications.groupBy { med ->
            try {
                val start = LocalDate.parse(med.startDate)
                YearMonth.from(start)
            } catch (e: Exception) {
                YearMonth.now() // Fallback
            }
        }.toList().sortedByDescending { it.first }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.history_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Barra de Filtros por Mes (Scrollable horizontal)
        Text(
            text = stringResource(id = R.string.history_filter_label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Opción "Todos"
            item {
                FilterChip(
                    selected = selectedMonth == null,
                    onClick = { selectedMonth = null },
                    label = { Text(stringResource(id = R.string.history_filter_all), fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Opciones de cada mes
            items(filterMonths) { month ->
                val monthLabel = month.month.getDisplayName(TextStyle.SHORT, locale)
                    .replaceFirstChar { it.uppercase() }
                val label = "$monthLabel ${month.year}"
                
                FilterChip(
                    selected = selectedMonth == month,
                    onClick = { selectedMonth = month },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredMedications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(id = R.string.history_empty),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            // Lista seccionada por meses
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                groupedMedications.forEach { (month, medsInMonth) ->
                    item {
                        val monthName = month.month.getDisplayName(TextStyle.FULL, locale)
                            .replaceFirstChar { it.uppercase() }
                        Text(
                            text = "$monthName ${month.year}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 4.dp)
                        )
                    }

                    items(medsInMonth) { medication ->
                        HistoryCard(medication = medication)
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryCard(medication: Medication) {
    val locale = LocalConfiguration.current.locales[0]
    val formattedPeriod = remember(medication.startDate, medication.endDate, locale) {
        try {
            val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val outputFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", locale)
            val start = LocalDate.parse(medication.startDate, inputFormatter).format(outputFormatter)
            val end = LocalDate.parse(medication.endDate, inputFormatter).format(outputFormatter)
            "$start → $end"
        } catch (e: Exception) {
            "${medication.startDate} → ${medication.endDate}"
        }
    }

    val friendlyTime = remember(medication.startTime) {
        try {
            LocalTime.parse(medication.startTime)
                .format(DateTimeFormatter.ofPattern("hh:mm a", locale))
        } catch (e: Exception) {
            medication.startTime
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (medication.isActive)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = medication.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (medication.isActive)
                            MaterialTheme.colorScheme.onSurface
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${medication.dose} • ${medication.frequency}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        // Badge visual para la hora amigable
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = friendlyTime,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
                StatusBadge(isActive = medication.isActive)
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.history_period, formattedPeriod),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StatusBadge(isActive: Boolean) {
    val containerColor = if (isActive) {
        if (isSystemInDarkTheme()) Color(0xFF00390A) else Color(0xFFE8F5E9)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    
    val contentColor = if (isActive) {
        if (isSystemInDarkTheme()) Color(0xFF8CF39A) else Color(0xFF2E7D32)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = if (isActive) stringResource(id = R.string.history_status_active) else stringResource(id = R.string.history_status_finished),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
    }
}
