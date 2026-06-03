package com.ud.finalproyect.notification

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.ud.finalproyect.model.repository.NotificationHistoryRepository
import com.ud.finalproyect.model.repository.MedicationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val action = intent.action
        val notificationId = intent.getIntExtra("NOTIFICATION_ID", 0)
        val logId = intent.getStringExtra("LOG_ID") ?: ""
        val medicationName = intent.getStringExtra("MEDICATION_NAME") ?: ""
        val medicationDose = intent.getStringExtra("MEDICATION_DOSE") ?: ""
        val notificationTime = intent.getStringExtra("NOTIFICATION_TIME") ?: ""
        val requestCode = intent.getIntExtra("REQUEST_CODE", 0)
        val medicationId = intent.getStringExtra("MEDICATION_ID") ?: ""
        val notificationDate = intent.getStringExtra("NOTIFICATION_DATE") ?: ""

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val repository = NotificationHistoryRepository()
        val medicationRepository = MedicationRepository()

        when (action) {
            ACTION_SNOOZE -> {
                notificationManager.cancel(notificationId)

                val currentActualTime = LocalTime.now()
                    .format(DateTimeFormatter.ofPattern("HH:mm"))

                CoroutineScope(Dispatchers.IO).launch {
                    val snoozeUntil = System.currentTimeMillis() + SNOOZE_DURATION_MILLIS
                    repository.snoozeNotification(logId, snoozeUntil)

                    // Actualizar la hora real de posponer en el repositorio
                    val notificationTime = intent.getStringExtra("NOTIFICATION_TIME") ?: ""
                    medicationRepository.updateActualTakenTime(
                        medicationId,
                        notificationDate,
                        "$currentActualTime (Pospuesto)",
                        notificationTime
                    )
                }

                // Schedule new alarm for 15 minutes later
                scheduleSnoozeAlarm(
                    context,
                    medicationName,
                    medicationDose,
                    notificationTime,
                    requestCode,
                    logId,
                    medicationId,
                    notificationDate
                )
            }

            ACTION_CONFIRM -> {
                // Dismiss notification
                notificationManager.cancel(notificationId)

                // Get current actual time when confirmed
                val currentActualTime = LocalTime.now()
                    .format(DateTimeFormatter.ofPattern("HH:mm"))

                // Update status as confirmed and save actual time
                CoroutineScope(Dispatchers.IO).launch {
                    repository.confirmNotification(logId)

                    // Guardar la hora real exacta en que se tomó el medicamento
                    val notificationTime = intent.getStringExtra("NOTIFICATION_TIME") ?: ""
                    medicationRepository.updateActualTakenTime(
                        medicationId,
                        notificationDate,
                        currentActualTime,
                        notificationTime
                    )
                }
            }

            ACTION_SNOOZE_ALARM -> {
                val mainIntent = Intent(context, com.ud.finalproyect.MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }

                val pendingIntent = PendingIntent.getActivity(
                    context,
                    requestCode,
                    mainIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val builder = androidx.core.app.NotificationCompat.Builder(
                    context,
                    NotificationScheduler.CHANNEL_ID
                )
                    .setSmallIcon(com.ud.finalproyect.R.mipmap.ic_launcher)
                    .setContentTitle("Recordatorio: Medicamento Pospuesto")
                    .setContentText("Es momento de tomar: $medicationName ($medicationDose) - $notificationTime")
                    .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(androidx.core.app.NotificationCompat.DEFAULT_ALL)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .addAction(
                        android.R.drawable.ic_menu_recent_history,
                        "Posponer",
                        createSnoozeAction(context, notificationId, logId, medicationName, medicationDose, notificationTime, requestCode, medicationId, notificationDate)
                    )
                    .addAction(
                        android.R.drawable.ic_menu_view,
                        "Tomar",
                        createConfirmAction(context, notificationId, logId, medicationId, notificationDate)
                    )

                notificationManager.notify(notificationId, builder.build())
            }
        }
    }

    private fun scheduleSnoozeAlarm(
        context: Context,
        medicationName: String,
        medicationDose: String,
        notificationTime: String,
        requestCode: Int,
        logId: String,
        medicationId: String,
        notificationDate: String
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_SNOOZE_ALARM
            putExtra("MEDICATION_NAME", medicationName)
            putExtra("MEDICATION_DOSE", medicationDose)
            putExtra("NOTIFICATION_TIME", notificationTime)
            putExtra("REQUEST_CODE", requestCode)
            putExtra("LOG_ID", logId)
            putExtra("NOTIFICATION_ID", requestCode)
            putExtra("MEDICATION_ID", medicationId)
            putExtra("NOTIFICATION_DATE", notificationDate)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode + 10000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtMillis = System.currentTimeMillis() + SNOOZE_DURATION_MILLIS

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    private fun createSnoozeAction(
        context: Context,
        notificationId: Int,
        logId: String,
        medicationName: String,
        medicationDose: String,
        notificationTime: String,
        requestCode: Int,
        medicationId: String,
        notificationDate: String
    ): PendingIntent {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_SNOOZE
            putExtra("NOTIFICATION_ID", notificationId)
            putExtra("LOG_ID", logId)
            putExtra("MEDICATION_NAME", medicationName)
            putExtra("MEDICATION_DOSE", medicationDose)
            putExtra("NOTIFICATION_TIME", notificationTime)
            putExtra("REQUEST_CODE", requestCode)
            putExtra("MEDICATION_ID", medicationId)
            putExtra("NOTIFICATION_DATE", notificationDate)
        }

        return PendingIntent.getBroadcast(
            context,
            requestCode + 1000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createConfirmAction(
        context: Context,
        notificationId: Int,
        logId: String,
        medicationId: String,
        notificationDate: String
    ): PendingIntent {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_CONFIRM
            putExtra("NOTIFICATION_ID", notificationId)
            putExtra("LOG_ID", logId)
            putExtra("MEDICATION_ID", medicationId)
            putExtra("NOTIFICATION_DATE", notificationDate)
        }

        return PendingIntent.getBroadcast(
            context,
            notificationId + 2000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val ACTION_SNOOZE = "com.ud.finalproyect.SNOOZE_NOTIFICATION"
        const val ACTION_CONFIRM = "com.ud.finalproyect.CONFIRM_NOTIFICATION"
        const val ACTION_SNOOZE_ALARM = "com.ud.finalproyect.SNOOZE_ALARM"
        const val SNOOZE_DURATION_MILLIS = 15 * 60 * 1000L // 15 minutes

        fun createSnoozeActionIntent(
            context: Context,
            requestCode: Int,
            medicationName: String,
            medicationDose: String,
            notificationTime: String,
            medicationId: String,
            notificationDate: String
        ): PendingIntent {
            val intent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = ACTION_SNOOZE
                putExtra("NOTIFICATION_ID", requestCode)
                putExtra("LOG_ID", "")
                putExtra("MEDICATION_NAME", medicationName)
                putExtra("MEDICATION_DOSE", medicationDose)
                putExtra("NOTIFICATION_TIME", notificationTime)
                putExtra("REQUEST_CODE", requestCode)
                putExtra("MEDICATION_ID", medicationId)
                putExtra("NOTIFICATION_DATE", notificationDate)
            }

            return PendingIntent.getBroadcast(
                context,
                requestCode + 1000,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        fun createConfirmActionIntent(
            context: Context,
            requestCode: Int,
            medicationId: String,
            notificationDate: String,
            notificationTime: String
        ): PendingIntent {
            val intent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = ACTION_CONFIRM
                putExtra("NOTIFICATION_ID", requestCode)
                putExtra("LOG_ID", "")
                putExtra("MEDICATION_ID", medicationId)
                putExtra("NOTIFICATION_DATE", notificationDate)
                putExtra("NOTIFICATION_TIME", notificationTime)
            }

            return PendingIntent.getBroadcast(
                context,
                requestCode + 2000,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}
