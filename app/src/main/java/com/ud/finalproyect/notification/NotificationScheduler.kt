package com.ud.finalproyect.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.WorkManager
import com.ud.finalproyect.model.data.Medication
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

class NotificationScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_ID = "medication_reminder_channel"
        const val CHANNEL_NAME = "Recordatorios de Medicamentos"
        const val CHANNEL_DESCRIPTION = "Notificaciones para recordar tomar medicamentos"
        const val ACTION_SCHEDULE_MEDICATION_REMINDER = "com.ud.finalproyect.SCHEDULE_MEDICATION_REMINDER"
    }

    fun createNotificationChannel() {
        // minSdk is 26, so this check is technically not needed but good practice
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleMedicationReminder(medication: Medication) {
        val startTime = LocalTime.parse(medication.startTime)
        val startDate = LocalDate.parse(medication.startDate)
        val endDate = LocalDate.parse(medication.endDate)

        var currentNotificationDate = startDate

        while (!currentNotificationDate.isAfter(endDate)) {
            when {
                medication.intervalHours < 24 && medication.intervalHours > 0 -> {
                    // HOURLY frequency
                    var currentDoseTime = startTime
                    val maxDosesPerDay = 24 / medication.intervalHours
                    for (i in 0 until maxDosesPerDay) {
                        val notificationTimeMillis = getNotificationTimeMillis(
                            currentNotificationDate,
                            currentDoseTime
                        )

                        if (notificationTimeMillis > System.currentTimeMillis()) {
                            scheduleAlarm(
                                medication,
                                currentNotificationDate,
                                currentDoseTime,
                                notificationTimeMillis,
                                // Unique request code for hourly: medicationId + date + dose index
                                medication.id.hashCode() + currentNotificationDate.toEpochDay().toInt() + i
                            )
                        }
                        currentDoseTime = currentDoseTime.plusHours(medication.intervalHours.toLong())
                    }
                }
                medication.intervalHours % 24 == 0 && medication.intervalHours > 0 -> {
                    val daysInterval = medication.intervalHours / 24
                    // DAILY or WEEKLY frequency (treated as daily increment for simplicity in scheduling loop)
                    // Schedule one alarm for the start time of the day
                    val notificationTimeMillis = getNotificationTimeMillis(
                        currentNotificationDate,
                        startTime
                    )

                    if (notificationTimeMillis > System.currentTimeMillis()) {
                        scheduleAlarm(
                            medication,
                            currentNotificationDate,
                            startTime,
                            notificationTimeMillis,
                            // Unique request code: medicationId + date
                            medication.id.hashCode() + currentNotificationDate.toEpochDay().toInt()
                        )
                    }
                    currentNotificationDate = currentNotificationDate.plusDays(daysInterval.toLong() - 1) // -1 because loop increments by 1 at the end
                }
                else -> {
                    // Treat as a single dose for the duration if frequency is unknown or 0
                    val notificationTimeMillis = getNotificationTimeMillis(
                        currentNotificationDate,
                        startTime
                    )

                    if (notificationTimeMillis > System.currentTimeMillis()) {
                        scheduleAlarm(
                            medication,
                            currentNotificationDate,
                            startTime,
                            notificationTimeMillis,
                            medication.id.hashCode() + currentNotificationDate.toEpochDay().toInt()
                        )
                    }
                }
            }
            currentNotificationDate = currentNotificationDate.plusDays(1)
        }
    }

    fun cancelMedicationReminder(medication: Medication) {
        val startTime = LocalTime.parse(medication.startTime)
        val startDate = LocalDate.parse(medication.startDate)
        val endDate = LocalDate.parse(medication.endDate)

        var currentNotificationDate = startDate

        while (!currentNotificationDate.isAfter(endDate)) {
            when {
                medication.intervalHours < 24 && medication.intervalHours > 0 -> {
                    // HOURLY frequency
                    var currentDoseTime = startTime
                    val maxDosesPerDay = 24 / medication.intervalHours
                    for (i in 0 until maxDosesPerDay) {
                        val requestCode = medication.id.hashCode() + currentNotificationDate.toEpochDay().toInt() + i
                        cancelAlarm(medication, requestCode, currentNotificationDate, currentDoseTime)
                        currentDoseTime = currentDoseTime.plusHours(medication.intervalHours.toLong())
                    }
                }
                medication.intervalHours % 24 == 0 && medication.intervalHours > 0 -> {
                    val daysInterval = medication.intervalHours / 24
                    val requestCode = medication.id.hashCode() + currentNotificationDate.toEpochDay().toInt()
                    cancelAlarm(medication, requestCode, currentNotificationDate, startTime)
                    currentNotificationDate = currentNotificationDate.plusDays(daysInterval.toLong() - 1)
                }
                else -> {
                    val requestCode = medication.id.hashCode() + currentNotificationDate.toEpochDay().toInt()
                    cancelAlarm(medication, requestCode, currentNotificationDate, startTime)
                }
            }
            currentNotificationDate = currentNotificationDate.plusDays(1)
        }
    }

    private fun scheduleAlarm(
        medication: Medication,
        notificationDate: LocalDate,
        notificationTime: LocalTime,
        triggerAtMillis: Long,
        requestCode: Int
    ) {
        val intent = Intent(context, MedicationReminderReceiver::class.java).apply {
            action = ACTION_SCHEDULE_MEDICATION_REMINDER
            putExtra("MEDICATION_NAME", medication.name)
            putExtra("MEDICATION_DOSE", medication.dose)
            putExtra("MEDICATION_ID", medication.id)
            putExtra("NOTIFICATION_DATE", notificationDate.toString())
            putExtra("NOTIFICATION_TIME", notificationTime.toString())
            putExtra("REQUEST_CODE", requestCode)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
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
        } catch (e: Exception) {
            // If AlarmManager fails, use WorkManager as fallback
            val delayMillis = triggerAtMillis - System.currentTimeMillis()
            val delayMinutes = (delayMillis / (1000 * 60)).coerceAtLeast(0)

            NotificationWorker.schedule(
                context,
                medication.name,
                medication.dose,
                medication.id,
                notificationTime.toString(),
                notificationDate.toString(),
                requestCode,
                delayMinutes
            )
        }

        // Also schedule with WorkManager as backup for maximum reliability
        val delayMillis = triggerAtMillis - System.currentTimeMillis()
        val delayMinutes = (delayMillis / (1000 * 60)).coerceAtLeast(0)

        NotificationWorker.schedule(
            context,
            medication.name,
            medication.dose,
            medication.id,
            notificationTime.toString(),
            notificationDate.toString(),
            requestCode,
            delayMinutes
        )
    }

    private fun cancelAlarm(
        medication: Medication,
        requestCode: Int,
        notificationDate: LocalDate,
        notificationTime: LocalTime
    ) {
        val intent = Intent(context, MedicationReminderReceiver::class.java).apply {
            action = ACTION_SCHEDULE_MEDICATION_REMINDER
            putExtra("MEDICATION_NAME", medication.name)
            putExtra("MEDICATION_DOSE", medication.dose)
            putExtra("MEDICATION_ID", medication.id)
            putExtra("NOTIFICATION_DATE", notificationDate.toString())
            putExtra("NOTIFICATION_TIME", notificationTime.toString())
            putExtra("REQUEST_CODE", requestCode)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        // Also cancel any WorkManager backup scheduled for this requestCode
        try {
            WorkManager.getInstance(context).cancelUniqueWork("medication_notification_$requestCode")
        } catch (e: Exception) {
            // ignore
        }
        // Also cancel potential snooze alarm/work (uses requestCode + 10000)
        try {
            val snoozePending = PendingIntent.getBroadcast(
                context,
                requestCode + 10000,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(snoozePending)
        } catch (e: Exception) {
            // ignore
        }
        try {
            WorkManager.getInstance(context).cancelUniqueWork("medication_notification_${requestCode + 10000}")
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun getNotificationTimeMillis(date: LocalDate, time: LocalTime): Long {
        return date.atTime(time).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}
