package com.ud.finalproyect.model.data

data class ScheduledDose(
    val medicationName: String,
    val medicationId: String,
    val time: String,
    val dose: String,
    val actualTime: String? = null // Hora real en que se tomó (si difiere de time)
)
