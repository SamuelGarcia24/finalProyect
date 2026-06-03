package com.ud.finalproyect.model.repository

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.FirebaseAuth
import com.ud.finalproyect.model.data.NotificationLog
import kotlinx.coroutines.tasks.await

class NotificationHistoryRepository {
    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val notificationLogsRef = database.getReference("notification_logs")

    fun saveNotificationLog(log: NotificationLog) {
        try {
            val userId = auth.currentUser?.uid ?: return
            val logId = notificationLogsRef.push().key ?: return
            val updatedLog = log.copy(id = logId, userId = userId)
            notificationLogsRef.child(userId).child(logId).setValue(updatedLog)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateNotificationStatus(
        logId: String,
        status: String,
        confirmedTime: Long? = null,
        snoozedUntil: Long? = null
    ) {
        try {
            val userId = auth.currentUser?.uid ?: return
            val updates = mutableMapOf<String, Any>("status" to status)
            confirmedTime?.let { updates["confirmedTime"] = it }
            snoozedUntil?.let { updates["snoozedUntil"] = it }
            notificationLogsRef.child(userId).child(logId).updateChildren(updates)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getNotificationLog(logId: String): NotificationLog? {
        return try {
            val userId = auth.currentUser?.uid ?: return null
            var result: NotificationLog? = null
            notificationLogsRef.child(userId).child(logId).get().addOnSuccessListener { snapshot ->
                result = snapshot.getValue(NotificationLog::class.java)
            }
            result
        } catch (e: Exception) {
            null
        }
    }

    fun confirmNotification(logId: String) {
        updateNotificationStatus(
            logId = logId,
            status = "CONFIRMED",
            confirmedTime = System.currentTimeMillis()
        )
    }

    fun snoozeNotification(logId: String, snoozedUntil: Long) {
        updateNotificationStatus(
            logId = logId,
            status = "SNOOZED",
            snoozedUntil = snoozedUntil
        )
    }
}

