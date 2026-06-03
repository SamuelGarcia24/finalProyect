package com.ud.finalproyect.model.data

import com.google.firebase.database.PropertyName

data class NotificationLog(
    val id: String = "",
    val userId: String = "",
    val medicationId: String = "",
    val medicationName: String = "",
    val medicationDose: String = "",
    val scheduledTime: Long = 0,
    val sentTime: Long = 0,
    val status: String = "PENDING", // PENDING, CONFIRMED, SNOOZED, MISSED, COMPLETED
    val confirmedTime: Long? = null,
    val snoozedUntil: Long? = null,
    val notificationDate: String = "",
    val notificationTime: String = ""
)

