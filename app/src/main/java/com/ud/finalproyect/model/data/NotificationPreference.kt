package com.ud.finalproyect.model.data

import com.google.firebase.database.PropertyName

data class NotificationPreference(
    val userId: String = "",
    val enableNotifications: Boolean = true,
    val doNotDisturbStart: String = "21:00",
    val doNotDisturbEnd: String = "08:00",
    val enableDoNotDisturb: Boolean = false,
    val snoozeIntervalMinutes: Int = 15,
    val lastUpdated: Long = System.currentTimeMillis()
)

