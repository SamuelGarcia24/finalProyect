package com.ud.finalproyect

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.ud.finalproyect.navigation.NavGraph
import com.ud.finalproyect.notification.NotificationScheduler
import com.ud.finalproyect.theme.MedicControlTheme

class MainActivity : ComponentActivity() {

    private lateinit var notificationScheduler: NotificationScheduler

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            // Permission not granted. You might want to show a rationale or guide the user to settings.
            // For example, navigate to app settings if user denied permanently
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                !shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // User denied and checked "Don't ask again" or denied twice
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        notificationScheduler = NotificationScheduler(this)
        notificationScheduler.createNotificationChannel()

        requestNotificationPermission()

        // Note: For SCHEDULE_EXACT_ALARM (API 31+), the user must explicitly grant permission
        // through system settings. There's no direct runtime permission dialog for it.
        // You can check the permission status and guide the user if needed:
        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        //     val alarmManager = getSystemService(AlarmManager::class.java)
        //     if (!alarmManager.canScheduleExactAlarms()) {
        //         val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
        //         startActivity(intent)
        //     }
        // }

        setContent {
            MedicControlTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavGraph()
                }
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
