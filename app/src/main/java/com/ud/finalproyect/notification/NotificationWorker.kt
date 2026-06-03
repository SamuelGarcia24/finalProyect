package com.ud.finalproyect.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Worker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.ud.finalproyect.MainActivity
import com.ud.finalproyect.R
import com.ud.finalproyect.model.repository.MedicationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.TimeUnit

class NotificationWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        return try {
            val medicationName = inputData.getString("MEDICATION_NAME") ?: return Result.failure()
            val medicationDose = inputData.getString("MEDICATION_DOSE") ?: ""
            val medicationId = inputData.getString("MEDICATION_ID") ?: ""
            val notificationTime = inputData.getString("NOTIFICATION_TIME") ?: ""
            val notificationDate = inputData.getString("NOTIFICATION_DATE") ?: ""
            val requestCode = inputData.getInt("REQUEST_CODE", 0)

            if (requestCode == 0) {
                return Result.failure()
            }

            // Show notification with actions
            showNotificationWithActions(
                applicationContext,
                medicationName,
                medicationDose,
                notificationTime,
                requestCode,
                medicationId,
                notificationDate
            )

            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    private fun showNotificationWithActions(
        context: Context,
        medicationName: String,
        medicationDose: String,
        notificationTime: String,
        requestCode: Int,
        medicationId: String,
        notificationDate: String
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

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
        fun schedule(
            context: Context,
            medicationName: String,
            medicationDose: String,
            medicationId: String,
            notificationTime: String,
            notificationDate: String,
            requestCode: Int,
            delayMinutes: Long = 0
        ) {
            val inputData = Data.Builder()
                .putString("MEDICATION_NAME", medicationName)
                .putString("MEDICATION_DOSE", medicationDose)
                .putString("MEDICATION_ID", medicationId)
                .putString("NOTIFICATION_TIME", notificationTime)
                .putString("NOTIFICATION_DATE", notificationDate)
                .putInt("REQUEST_CODE", requestCode)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInputData(inputData)
                .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "medication_notification_$requestCode",
                ExistingWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}

