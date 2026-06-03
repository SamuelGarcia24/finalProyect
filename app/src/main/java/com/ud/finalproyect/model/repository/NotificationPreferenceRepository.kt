package com.ud.finalproyect.model.repository

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.FirebaseAuth
import com.ud.finalproyect.model.data.NotificationPreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class NotificationPreferenceRepository {
    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val preferencesRef = database.getReference("notification_preferences")

    fun getPreferences(): Flow<NotificationPreference> = flow {
        try {
            val userId = auth.currentUser?.uid ?: return@flow
            val snapshot = preferencesRef.child(userId).get().await()
            val preference = snapshot.getValue(NotificationPreference::class.java)
                ?: NotificationPreference(userId = userId)
            emit(preference)
        } catch (e: Exception) {
            emit(NotificationPreference(userId = auth.currentUser?.uid ?: ""))
        }
    }

    fun updatePreferences(preference: NotificationPreference) {
        try {
            val userId = auth.currentUser?.uid ?: return
            preferencesRef.child(userId).setValue(preference)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateDoNotDisturb(
        enableDoNotDisturb: Boolean,
        startTime: String,
        endTime: String
    ) {
        try {
            val userId = auth.currentUser?.uid ?: return
            val updates = mapOf(
                "enableDoNotDisturb" to enableDoNotDisturb,
                "doNotDisturbStart" to startTime,
                "doNotDisturbEnd" to endTime
            )
            preferencesRef.child(userId).updateChildren(updates)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateSnoozeInterval(minutes: Int) {
        try {
            val userId = auth.currentUser?.uid ?: return
            preferencesRef.child(userId).child("snoozeIntervalMinutes").setValue(minutes)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

