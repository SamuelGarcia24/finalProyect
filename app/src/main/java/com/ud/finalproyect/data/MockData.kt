package com.ud.finalproyect.data

data class Medication(
    val id: Int,
    val name: String,
    val dose: String,
    val time: String,
    val status: String // 🟢 tomado, 🔴 pendiente, etc.
)

// Datos iniciales
fun getMockMedications(): List<Medication> {
    return listOf(
        Medication(1, "Ibuprofen", "400mg", "7:00 AM", "🟢"),
        Medication(2, "Amoxicillin", "500mg", "11:15 AM", "🟢"),
        Medication(3, "Loratadina", "10mg", "2:30 PM", "🟢")
    )
}