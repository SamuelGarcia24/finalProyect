package com.ud.finalproyect.ui.diary

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import com.ud.finalproyect.data.*
import java.time.*
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DiaryScreen() {
    val medications = remember { getMockMedications() }
    val days = remember { (-3..3).map { LocalDate.now().plusDays(it.toLong()) } }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Selector de fecha horizontal
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(days) { date ->
                val isSelected = date == selectedDate
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
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
                            text = date.month.name.take(3),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) Color.White else Color.Gray
                        )
                        Text(
                            text = date.dayOfMonth.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else Color.Black
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Lista de medicamentos con línea de tiempo
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(medications.filter { it.isActive }) { med ->
                Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                    // Indicador de línea de tiempo
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(2.dp)
                                .background(Color.LightGray)
                        )
                    }

                    // Tarjeta del medicamento
                    Card(
                        modifier = Modifier
                            .padding(start = 16.dp, bottom = 16.dp)
                            .fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(4.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(med.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Text("${med.dose} - ${med.time}", color = Color.Gray)
                            }
                            // Icono de estado (Según mockup)
                            Icon(
                                imageVector = if (med.status == "Tomado") Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (med.status == "Tomado") Color.Green else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}