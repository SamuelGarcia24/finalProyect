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
            FrequencyType.HOURLY -> true
            FrequencyType.DAILY -> {
                val daysInterval = medication.intervalHours / 24
                if (daysInterval > 0) {
                    val daysBetween = ChronoUnit.DAYS.between(startDate, date)
                    daysBetween >= 0 && daysBetween % daysInterval == 0L
                } else true
            }
            FrequencyType.WEEKLY -> {
                val weeksInterval = medication.intervalHours / (7 * 24)
                if (weeksInterval > 0) {
                    val weeksBetween = ChronoUnit.WEEKS.between(startDate, date)
                    weeksBetween >= 0 && weeksBetween % weeksInterval == 0L
                } else true
            }
            FrequencyType.UNKNOWN -> false
        }
    }

    fun getDosesForDate(date: LocalDate): List<ScheduledDose> {
        val doses = mutableListOf<ScheduledDose>()
        val outputFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())

        medications.value.forEach { med ->
            try {
                val startDate = LocalDate.parse(med.startDate)
                val endDate = LocalDate.parse(med.endDate)
                val startTime = parseTimeSafely(med.startTime) ?: return@forEach
                val friendlyTime = startTime.format(outputFormatter)

                if (!date.isBefore(startDate) && !date.isAfter(endDate)) {
                    val frequencyType = determineFrequencyType(med.intervalHours)

                    when (frequencyType) {
                        FrequencyType.HOURLY -> {
                            if (med.intervalHours > 0) {
                                // Lógica mejorada: Calcular desde el inicio absoluto del tratamiento
                                var currentDateTime = startDate.atTime(startTime)
                                val endOfSelectedDay = date.atTime(LocalTime.MAX)
                                
                                // Optimización: Si la fecha seleccionada es muy posterior al inicio, saltar intervalos
                                if (currentDateTime.toLocalDate().isBefore(date)) {
                                    val hoursBetween = ChronoUnit.HOURS.between(currentDateTime, date.atStartOfDay())
                                    val intervalsToSkip = (hoursBetween + med.intervalHours - 1) / med.intervalHours
                                    currentDateTime = currentDateTime.plusHours(intervalsToSkip * med.intervalHours)
                                }

                                while (!currentDateTime.isAfter(endOfSelectedDay)) {
                                    if (currentDateTime.toLocalDate() == date) {
                                        val formattedTime = currentDateTime.toLocalTime().format(outputFormatter)
                                        val isoTime = currentDateTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))
                                        val perDoseKey = "${date}|${isoTime}"
                                        val actual = med.actualTakenTimes?.get(perDoseKey) ?: med.actualTakenTimes?.get(date.toString())
                                        doses.add(
                                            ScheduledDose(
                                                medicationName = med.name,
                                                medicationId = med.id,
                                                time = formattedTime,
                                                dose = med.dose,
                                                actualTime = actual
                                            )
                                        )
                                    }
                                    currentDateTime = currentDateTime.plusHours(med.intervalHours.toLong())
                                    // Evitar bucle infinito si por error llega un intervalo de 0
                                    if (med.intervalHours == 0) break
                                }
                             } else {
                                 val actualSimple = med.actualTakenTimes?.get(date.toString())
                                 doses.add(ScheduledDose(med.name, med.id, friendlyTime, med.dose, actualTime = actualSimple))
                             }
                        }
                        FrequencyType.DAILY, FrequencyType.WEEKLY -> {
                                if (shouldTakeMedicationOnDate(med, date, startDate)) {
                                    val actualSimple2 = med.actualTakenTimes?.get(date.toString())
                                    doses.add(ScheduledDose(med.name, med.id, friendlyTime, med.dose, actualTime = actualSimple2))
                                }
                        }
                        FrequencyType.UNKNOWN -> {
                            if (date == startDate) {
                                doses.add(ScheduledDose(med.name, med.id, friendlyTime, med.dose))
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignorar errores de parseo
            }
        }
        return doses.sortedBy { parseTimeSafely(it.time) ?: LocalTime.MIN }
    }

    fun hasMedicationOnDate(date: LocalDate): Boolean = getDosesForDate(date).isNotEmpty()
}
