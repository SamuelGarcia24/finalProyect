package com.ud.finalproyect.ui.calendar
// creacion rama
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ud.finalproyect.data.getScheduledDates
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

//creacion rama
@Composable
fun CalendarScreen() {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val scheduledDates = getScheduledDates()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Calendario de Medicación",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                CalendarHeader(
                    currentMonth = currentMonth,
                    onMonthChange = { currentMonth = it }
                )
                Spacer(modifier = Modifier.height(16.dp))
                CalendarGrid(
                    currentMonth = currentMonth,
                    selectedDate = selectedDate,
                    scheduledDates = scheduledDates,
                    onDateSelected = { selectedDate = it }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Resumen del día seleccionado (Visual solamente)
        Text(
            text = "Programación para el ${selectedDate.dayOfMonth} de ${selectedDate.month.getDisplayName(TextStyle.FULL, Locale("es", "ES"))}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        if (scheduledDates.contains(selectedDate)) {
            MedicationSummaryCard()
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay medicamentos programados para este día",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CalendarHeader(
    currentMonth: YearMonth,
    onMonthChange: (YearMonth) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onMonthChange(currentMonth.minusMonths(1)) }) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Mes anterior")
        }
        Text(
            text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale("es", "ES")).replaceFirstChar { it.uppercase() }} ${currentMonth.year}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = { onMonthChange(currentMonth.plusMonths(1)) }) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Mes siguiente")
        }
    }
}

@Composable
fun CalendarGrid(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    scheduledDates: List<LocalDate>,
    onDateSelected: (LocalDate) -> Unit
) {
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfMonth = currentMonth.atDay(1).dayOfWeek.value % 7 // Ajuste para que Domingo sea 0 o Lunes sea 0
    val days = (1..daysInMonth).toList()
    
    val dayNames = listOf("Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb")

    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            dayNames.forEach { dayName ->
                Text(
                    text = dayName,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(280.dp)
        ) {
            // Espacios vacíos para el inicio del mes
            items(firstDayOfMonth) { Spacer(modifier = Modifier.fillMaxSize()) }
            
            items(days) { day ->
                val date = currentMonth.atDay(day)
                val isSelected = date == selectedDate
                val hasMedication = scheduledDates.contains(date)

                DayItem(
                    day = day,
                    isSelected = isSelected,
                    hasMedication = hasMedication,
                    onClick = { onDateSelected(date) }
                )
            }
        }
    }
}

@Composable
fun DayItem(
    day: Int,
    isSelected: Boolean,
    hasMedication: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .clip(CircleShape)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary 
                else Color.Transparent
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            if (hasMedication) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

@Composable
fun MedicationSummaryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text("💊", fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = "Ibuprofeno", fontWeight = FontWeight.Bold)
                Text(text = "08:00 AM - 400mg", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Ver detalles",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { /* Simulación de navegación a Diary */ }
            )
        }
    }
}
