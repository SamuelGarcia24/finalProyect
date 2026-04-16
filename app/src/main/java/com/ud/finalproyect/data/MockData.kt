package com.ud.finalproyect.data

import java.time.LocalDate

data class Medication(
    val id: Int,
    val name: String,
    val dose: String,
    val frequency: String,
    val time: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val isActive: Boolean,
    val status: String = "Pendiente"
)

fun getMockMedications(): List<Medication> {
    val today = LocalDate.now()
    return listOf(
        Medication(
            id = 1,
            name = "Ibuprofeno",
            dose = "400mg",
            frequency = "Cada 8 horas",
            time = "08:00 AM",
            startDate = today.minusDays(5),
            endDate = today.plusDays(5),
            isActive = true
        ),
        Medication(
            id = 2,
            name = "Amoxicilina",
            dose = "500mg",
            frequency = "Cada 12 horas",
            time = "10:00 AM",
            startDate = today.minusDays(10),
            endDate = today.minusDays(3),
            isActive = false
        ),
        Medication(
            id = 3,
            name = "Loratadina",
            dose = "10mg",
            frequency = "Cada 24 horas",
            time = "09:00 PM",
            startDate = today,
            endDate = today.plusDays(15),
            isActive = true
        ),
        Medication(
            id = 4,
            name = "Paracetamol",
            dose = "500mg",
            frequency = "Cada 6 horas",
            time = "12:00 PM",
            startDate = today.minusDays(20),
            endDate = today.minusDays(15),
            isActive = false
        )
    )
}

// Días que tienen medicación programada (simulación)
fun getScheduledDates(): List<LocalDate> {
    val today = LocalDate.now()
    return listOf(
        today.minusDays(2),
        today.minusDays(1),
        today,
        today.plusDays(1),
        today.plusDays(2),
        today.plusDays(5),
        today.plusDays(8)
    )
}
