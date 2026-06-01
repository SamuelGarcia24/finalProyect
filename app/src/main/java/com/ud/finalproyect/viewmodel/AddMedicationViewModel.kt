package com.ud.finalproyect.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import com.ud.finalproyect.model.data.Medication
import com.ud.finalproyect.model.repository.MedicationRepository
import com.ud.finalproyect.notification.NotificationScheduler
import java.time.LocalDate

class AddMedicationViewModel : ViewModel() {

    private val repository = MedicationRepository()
    private lateinit var notificationScheduler: NotificationScheduler

    // Initialize NotificationScheduler with Application context, as ViewModels shouldn't hold Activity context
    fun init(application: Application) {
        notificationScheduler = NotificationScheduler(application.applicationContext)
    }

    fun saveMedication(
        application: Application,
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

        // Capture the returned medication with the Firebase-generated ID
        val savedMedication = repository.saveMedication(medication)

        // Schedule notification after saving medication
        // Ensure notificationScheduler is initialized before calling scheduleMedicationReminder
        if (!::notificationScheduler.isInitialized) {
            notificationScheduler = NotificationScheduler(application.applicationContext)
        }
        notificationScheduler.scheduleMedicationReminder(savedMedication)
    }
}
