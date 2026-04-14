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

// Más medicamentos para "Show more"
fun getMoreMockMedications(): List<Medication> {
    return listOf(
        Medication(4, "Paracetamol", "500mg", "8:00 AM", "🟢"),
        Medication(5, "Omeprazol", "20mg", "9:00 AM", "🔴"),
        Medication(6, "Metformina", "850mg", "10:30 AM", "🟢"),
        Medication(7, "Losartán", "50mg", "1:00 PM", "🔴"),
        Medication(8, "Aspirina", "100mg", "6:00 PM", "🟢")
    )
}