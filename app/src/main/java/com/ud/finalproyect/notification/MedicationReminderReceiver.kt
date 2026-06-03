package com.ud.finalproyect.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.ud.finalproyect.MainActivity
import com.ud.finalproyect.R
import com.ud.finalproyect.model.data.NotificationLog
import com.ud.finalproyect.model.data.NotificationPreference
import com.ud.finalproyect.model.repository.MedicationRepository
import com.ud.finalproyect.model.repository.NotificationPreferenceRepository
import com.ud.finalproyect.model.repository.NotificationHistoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class MedicationReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val applicationContext = context?.applicationContext ?: return
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        when (intent?.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                val medicationRepository = MedicationRepository()
                val notificationScheduler = NotificationScheduler(applicationContext)

                CoroutineScope(Dispatchers.IO).launch {
                    val dummyUserId = "" 
                    medicationRepository.getMedications(dummyUserId).collect { list ->
                        list.filter { med -> med.isActive }.forEach { medication ->
                            notificationScheduler.scheduleMedicationReminder(medication)
                        }
                    }
                }
            }
            NotificationScheduler.ACTION_SCHEDULE_MEDICATION_REMINDER -> {
                val medicationName = intent.getStringExtra("MEDICATION_NAME") ?: "Tu Medicamento"
                val medicationDose = intent.getStringExtra("MEDICATION_DOSE") ?: ""
                val medicationId = intent.getStringExtra("MEDICATION_ID") ?: ""
                val notificationTime = intent.getStringExtra("NOTIFICATION_TIME") ?: ""
                val notificationDate = intent.getStringExtra("NOTIFICATION_DATE") ?: ""
                val requestCode = intent.getIntExtra("REQUEST_CODE", 0)

                // Check Do Not Disturb setting
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val prefRepo = NotificationPreferenceRepository()
                        val preferences = prefRepo.getPreferences().first()

                        // Check if notification should be suppressed due to Do Not Disturb
                        if (shouldSuppressNotification(preferences)) {
                            // Still log it but don't show
                            val historyRepo = NotificationHistoryRepository()
                            val log = NotificationLog(
                                medicationId = medicationId,
                                medicationName = medicationName,
                                medicationDose = medicationDose,
                                scheduledTime = System.currentTimeMillis(),
                                sentTime = System.currentTimeMillis(),
                                status = "PENDING",
                                notificationDate = notificationDate,
                                notificationTime = notificationTime
                            )
                            historyRepo.saveNotificationLog(log)
                            return@launch
                        }

                        // Show notification with actions
                        showNotificationWithActions(
                            applicationContext,
                            notificationManager,
                            medicationName,
                            medicationDose,
                            notificationTime,
                            requestCode,
                            medicationId,
                            notificationDate
                        )

                        // Log notification
                        val historyRepo = NotificationHistoryRepository()
                        val log = NotificationLog(
                            medicationId = medicationId,
                            medicationName = medicationName,
                            medicationDose = medicationDose,
                            scheduledTime = System.currentTimeMillis(),
                            sentTime = System.currentTimeMillis(),
                            status = "PENDING",
                            notificationDate = notificationDate,
                            notificationTime = notificationTime
                        )
                        historyRepo.saveNotificationLog(log)
                    } catch (e: Exception) {
                        // If error reading preferences, show notification anyway
                        showNotificationWithActions(
                            applicationContext,
                            notificationManager,
                            medicationName,
                            medicationDose,
                            notificationTime,
                            requestCode,
                            medicationId,
                            notificationDate
                        )
                    }
                }
            }
        }
    }

    private fun shouldSuppressNotification(preferences: NotificationPreference): Boolean {
        if (!preferences.enableDoNotDisturb) return false

        val now = LocalTime.now()
        val startTime = LocalTime.parse(preferences.doNotDisturbStart)
        val endTime = LocalTime.parse(preferences.doNotDisturbEnd)

        return if (startTime.isBefore(endTime)) {
            // Normal case: e.g., 21:00 to 08:00 spans across midnight
            now.isAfter(startTime) || now.isBefore(endTime)
        } else {
            // Spans midnight: e.g., 21:00 to 08:00
            now.isAfter(startTime) || now.isBefore(endTime)
        }
    }

    private fun showNotificationWithActions(
        context: Context,
        notificationManager: NotificationManager,
        medicationName: String,
        medicationDose: String,
        notificationTime: String,
        requestCode: Int,
        medicationId: String,
        notificationDate: String
    ) {
        // Convert ISO time to friendly format (hh:mm a)
        val friendlyTime = try {
            LocalTime.parse(notificationTime)
                .format(DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault()))
        } catch (e: Exception) {
            notificationTime
        }

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            requestCode,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Create Snooze action
        val snoozeAction = NotificationActionReceiver.createSnoozeActionIntent(
            context,
            requestCode,
            medicationName,
            medicationDose,
            notificationTime,
            medicationId,
            notificationDate
        )

        // Create Confirm action (include notificationTime so receiver can identify per-dose key)
        val confirmAction = NotificationActionReceiver.createConfirmActionIntent(
            context,
            requestCode,
            medicationId,
            notificationDate,
            notificationTime
        )

        val notificationBuilder = NotificationCompat.Builder(context, NotificationScheduler.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Hora de su medicina")
            .setContentText("Tomar: $medicationName ($medicationDose) - $friendlyTime")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                android.R.drawable.ic_menu_recent_history,
                "Posponer",
                snoozeAction
            )
            .addAction(
                android.R.drawable.ic_menu_view,
                "Tomar",
                confirmAction
            )

        notificationManager.notify(requestCode, notificationBuilder.build())
    }

    companion object {
        // Import NotificationPreference for the shouldSuppressNotification function
        private fun isExternalClass(): Boolean = false
    }
}
