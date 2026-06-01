package com.ud.finalproyect.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.ud.finalproyect.MainActivity
import com.ud.finalproyect.R
import com.ud.finalproyect.model.repository.MedicationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
                val notificationTime = intent.getStringExtra("NOTIFICATION_TIME") ?: ""
                val requestCode = intent.getIntExtra("REQUEST_CODE", 0)

                // Convertir hora ISO a formato amigable (AM/PM)
                val friendlyTime = try {
                    LocalTime.parse(notificationTime)
                        .format(DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault()))
                } catch (e: Exception) {
                    notificationTime
                }

                val mainIntent = Intent(applicationContext, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                
                val pendingIntent = PendingIntent.getActivity(
                    applicationContext,
                    requestCode,
                    mainIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val notificationBuilder = NotificationCompat.Builder(applicationContext, NotificationScheduler.CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("¡Hora de su medicina!")
                    .setContentText("Es momento de tomar: $medicationName ($medicationDose) - $friendlyTime")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)

                notificationManager.notify(requestCode, notificationBuilder.build())
            }
        }
    }
}
