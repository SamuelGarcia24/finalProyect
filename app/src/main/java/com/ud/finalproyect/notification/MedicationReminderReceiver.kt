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
                val medicationName = intent.getStringExtra("MEDICATION_NAME") ?: applicationContext.getString(R.string.notification_default_med)
                val medicationDose = intent.getStringExtra("MEDICATION_DOSE") ?: ""
                val medicationId = intent.getStringExtra("MEDICATION_ID") ?: ""
                val notificationTime = intent.getStringExtra("NOTIFICATION_TIME") ?: ""
                val notificationDate = intent.getStringExtra("NOTIFICATION_DATE") ?: ""
                val requestCode = intent.getIntExtra("REQUEST_CODE", 0)

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val prefRepo = NotificationPreferenceRepository()
                        val preferences = prefRepo.getPreferences().first()

                        if (shouldSuppressNotification(preferences)) {
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
            now.isAfter(startTime) || now.isBefore(endTime)
        } else {
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

        val snoozeAction = NotificationActionReceiver.createSnoozeActionIntent(
            context,
            requestCode,
            medicationName,
            medicationDose,
            notificationTime,
            medicationId,
            notificationDate
        )

        val confirmAction = NotificationActionReceiver.createConfirmActionIntent(
            context,
            requestCode,
            medicationId,
            notificationDate,
            notificationTime
        )

        val notificationBuilder = NotificationCompat.Builder(context, NotificationScheduler.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.notification_content, medicationName, medicationDose, friendlyTime))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                android.R.drawable.ic_menu_recent_history,
                context.getString(R.string.notification_action_snooze),
                snoozeAction
            )
            .addAction(
                android.R.drawable.ic_menu_view,
                context.getString(R.string.notification_action_take),
                confirmAction
            )

        notificationManager.notify(requestCode, notificationBuilder.build())
    }

    companion object {
        private fun isExternalClass(): Boolean = false
    }
}
