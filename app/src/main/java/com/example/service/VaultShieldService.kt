package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.db.AegisDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class VaultShieldService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var monitorJob: Job? = null

    private var temporarilyAllowedPackage: String? = null
    private var lastInterceptedPackage: String? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        startAppMonitoring()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val allowedPkg = intent?.getStringExtra(EXTRA_ALLOW_PACKAGE)
        if (allowedPkg != null) {
            temporarilyAllowedPackage = allowedPkg
        }
        return START_STICKY
    }

    private fun startAppMonitoring() {
        monitorJob?.cancel()
        monitorJob = serviceScope.launch {
            val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            val database = AegisDatabase.getInstance(applicationContext)

            while (isActive) {
                try {
                    val hiddenApps = database.hiddenAppDao().getAllHiddenApps().first()
                    val hiddenPackageSet = hiddenApps.map { it.packageName }.toSet()

                    if (hiddenPackageSet.isNotEmpty() && usageStatsManager != null) {
                        val currentPkg = getForegroundPackage(usageStatsManager)
                        if (currentPkg != null && currentPkg != packageName) {
                            if (hiddenPackageSet.contains(currentPkg)) {
                                if (currentPkg != temporarilyAllowedPackage && currentPkg != lastInterceptedPackage) {
                                    lastInterceptedPackage = currentPkg
                                    interceptApp(currentPkg)
                                }
                            } else {
                                // Moved to a non-protected app, reset last intercepted
                                lastInterceptedPackage = null
                                temporarilyAllowedPackage = null
                            }
                        }
                    }
                } catch (_: Exception) {}

                delay(600) // check interval
            }
        }
    }

    private fun getForegroundPackage(usageStatsManager: UsageStatsManager): String? {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 3000
        val events = usageStatsManager.queryEvents(startTime, endTime)
        val event = UsageEvents.Event()
        var lastForegroundApp: String? = null

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                lastForegroundApp = event.packageName
            }
        }
        return lastForegroundApp
    }

    private fun interceptApp(targetPackage: String) {
        val interceptIntent = Intent(applicationContext, MainActivity::class.java).apply {
            action = MainActivity.ACTION_INTERCEPT_PROTECTED_APP
            putExtra(MainActivity.EXTRA_LOCKED_PACKAGE, targetPackage)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        startActivity(interceptIntent)
    }

    private fun buildNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Aegis Shield Guard Active")
            .setContentText("Real-time privacy protection monitoring hidden apps")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Aegis Shield Protection Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors and intercepts unauthorized launches of hidden apps"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        const val CHANNEL_ID = "aegis_shield_guard_channel"
        const val NOTIFICATION_ID = 9001
        const val EXTRA_ALLOW_PACKAGE = "extra_allow_package"

        fun startService(context: Context) {
            try {
                val intent = Intent(context, VaultShieldService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (_: Exception) {}
        }

        fun allowSessionForPackage(context: Context, packageName: String) {
            try {
                val intent = Intent(context, VaultShieldService::class.java).apply {
                    putExtra(EXTRA_ALLOW_PACKAGE, packageName)
                }
                context.startService(intent)
            } catch (_: Exception) {}
        }
    }
}
