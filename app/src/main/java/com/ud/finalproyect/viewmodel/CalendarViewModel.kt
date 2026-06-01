package com.ud.finalproyect.viewmodel

import androidx.lifecycle.ViewModel

import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.ud.finalproyect.model.data.Medication
import com.ud.finalproyect.model.repository.MedicationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoUnit
import java.util.Locale
import com.ud.finalproyect.model.data.ScheduledDose
import com.ud.finalproyect.viewmodel.enums.FrequencyType

class CalendarViewModel : ViewModel() {
    private val repository = MedicationRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _medications = MutableStateFlow<List<Medication>>(emptyList())
    val medications: StateFlow<List<Medication>> = _medications.asStateFlow()

    private val formatters = listOf(
        DateTimeFormatter.ofPattern("hh:mm a", Locale.US),
        DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault()),
        DateTimeFormatter.ofPattern("h:mm a", Locale.US),
        DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()),
        DateTimeFormatter.ofPattern("HH:mm", Locale.US),
        DateTimeFormatter.ofPattern("H:mm", Locale.US),
        DateTimeFormatter.ofPattern("hh:mm a").withLocale(Locale.getDefault()),
        DateTimeFormatterBuilder()
            .appendPattern("h:mm")
            .optionalStart()
            .appendPattern(" ")
            .optionalEnd()
            .appendPattern("a")
            .toFormatter(Locale.US)
    )

    init {
        fetchMedications()
    }

    private fun fetchMedications() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            repository.getMedications(userId).collect {
                _medications.value = it
            }
        }
    }

    private fun parseTimeSafely(timeStr: String): LocalTime? {
        val cleanTime = timeStr.trim()
            .replace("a.m.", "AM")
            .replace("p.m.", "PM")
            .replace("a. m.", "AM")
            .replace("p. m.", "PM")
            .replace("am", "AM")
            .replace("pm", "PM")

        for (formatter in formatters) {
            try {
                return LocalTime.parse(cleanTime, formatter)
            } catch (e: Exception) {
                // Continuar intentando con el siguiente formateador
            }
        }

        // Intento de fallback de emergencia si contiene AM/PM o formato simple
        return try {
            val parts = cleanTime.split(" ")[0].split(":")
            val h = parts[0].toInt()
            val m = parts[1].toInt()
            var hour = h
            if (cleanTime.contains("PM", ignoreCase = true) && h < 12) hour += 12
            if (cleanTime.contains("AM", ignoreCase = true) && h == 12) hour = 0
            LocalTime.of(hour, m)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Determina el tipo de frecuencia basándose en el campo intervalHours
     * Horas: intervalHours < 24
     * Días: intervalHours es múltiplo de 24 (pero no de 7*24)
     * Semanas: intervalHours es múltiplo de 7*24
     */
    private fun determineFrequencyType(intervalHours: Int): FrequencyType {
        return when {
            intervalHours < 24 -> FrequencyType.HOURLY
            intervalHours % (7 * 24) == 0 && intervalHours > 0 -> FrequencyType.WEEKLY
            intervalHours % 24 == 0 && intervalHours > 0 -> FrequencyType.DAILY
            else -> FrequencyType.UNKNOWN
        }
    }

    private fun shouldTakeMedicationOnDate(
        medication: Medication,
        date: LocalDate,
        startDate: LocalDate
    ): Boolean {
        val frequencyType = determineFrequencyType(medication.intervalHours)

        return when (frequencyType) {
            FrequencyType.HOURLY -> {
                true
            }
            FrequencyType.DAILY -> {
                // Medicamento cada X días
                val daysInterval = medication.intervalHours / 24
                if (daysInterval > 0) {
                    val daysBetween = ChronoUnit.DAYS.between(startDate, date)
                    daysBetween % daysInterval == 0L
                } else {
                    false
                }
            }
            FrequencyType.WEEKLY -> {
                // Medicamento cada X semanas
                val weeksInterval = medication.intervalHours / (7 * 24)
                if (weeksInterval > 0) {
                    val weeksBetween = ChronoUnit.WEEKS.between(startDate, date)
                    weeksBetween % weeksInterval == 0L
                } else {
                    false
                }
            }
            FrequencyType.UNKNOWN -> false
        }
    }

    /**
     * Genera los horarios de toma para una fecha específica
     */
    fun getDosesForDate(date: LocalDate): List<ScheduledDose> {
        val doses = mutableListOf<ScheduledDose>()
        val outputFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())

        medications.value.forEach { med ->
            try {
                val startDate = LocalDate.parse(med.startDate)
                val endDate = LocalDate.parse(med.endDate)

                // Validar que la fecha esté dentro del rango de tratamiento
                if (!date.isBefore(startDate) && !date.isAfter(endDate)) {
                    // Validar si el medicamento se debe tomar en esta fecha
                    if (shouldTakeMedicationOnDate(med, date, startDate)) {
                        val startTime = parseTimeSafely(med.startTime) ?: return@forEach
                        val frequencyType = determineFrequencyType(med.intervalHours)

                        when (frequencyType) {
                            FrequencyType.HOURLY -> {
                                // Generar múltiples tomas durante el día
                                if (med.intervalHours > 0) {
                                    var currentDoseTime = startTime
                                    val maxDoses = 24 / med.intervalHours
                                    repeat(maxDoses) {
                                        doses.add(
                                            ScheduledDose(
                                                medicationName = med.name,
                                                time = currentDoseTime.format(outputFormatter),
                                                dose = med.dose
                                            )
                                        )
                                        currentDoseTime = currentDoseTime.plusHours(med.intervalHours.toLong())
                                    }
                                } else {
                                    doses.add(ScheduledDose(med.name, med.startTime, med.dose))
                                }
                            }
                            FrequencyType.DAILY, FrequencyType.WEEKLY -> {
                                // Una única toma en la hora programada
                                doses.add(ScheduledDose(med.name, med.startTime, med.dose))
                            }
                            FrequencyType.UNKNOWN -> {
                                // Fallback: toma única
                                doses.add(ScheduledDose(med.name, med.startTime, med.dose))
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignorar medicamentos con datos de fechas mal estructurados
            }
        }
        return doses.sortedBy { parseTimeSafely(it.time) ?: LocalTime.MIN }
    }

    fun hasMedicationOnDate(date: LocalDate): Boolean = getDosesForDate(date).isNotEmpty()
}
