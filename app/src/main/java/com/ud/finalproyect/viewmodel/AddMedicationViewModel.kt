package com.ud.finalproyect.viewmodel

import androidx.lifecycle.ViewModel
import com.ud.finalproyect.model.data.Medication
import com.ud.finalproyect.model.repository.MedicationRepository
import java.time.LocalDate

class AddMedicationViewModel : ViewModel() {

    private val repository = MedicationRepository()

    fun saveMedication(
        userId: String,
        name: String,
        doseValue: String,
        doseUnit: String,
        frequency: String,
        intervalHours: Int,
        intervalMinutes: Int,
        startTime: String,
        durationDays: Int
    ) {
        val today = LocalDate.now()
        val endDate = today.plusDays(durationDays.toLong())

        val medication = Medication(
            userId = userId,
            name = name,
            dose = "$doseValue$doseUnit",
            doseUnit = doseUnit,
            frequency = frequency,
            intervalHours = intervalHours,
            intervalMinutes = intervalMinutes,
            startTime = startTime,
            durationDays = durationDays,
            startDate = today.toString(),
            endDate = endDate.toString(),
            isActive = true,
            status = "Pendiente"
        )

        repository.saveMedication(medication)
    }
}
