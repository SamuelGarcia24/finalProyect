package com.ud.finalproyect.model.data

import com.google.firebase.database.PropertyName

data class Medication(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val dose: String = "",
    val doseUnit: String = "mg",
    val frequency: String = "",
    val intervalHours: Int = 0,
    val intervalMinutes: Int = 0,
    val startTime: String = "",
    val durationDays: Int = 0,
    val startDate: String = "",
    val endDate: String = "",
    @get:PropertyName("isActive")
    @set:PropertyName("isActive")
    var isActive: Boolean = true,
    val status: String = "Pendiente",
    val takenDates: List<String> = emptyList(),
    val actualTakenTimes: Map<String, String> = emptyMap()
)