package com.yxlanyu.refreshrate.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.yxlanyu.refreshrate.MainActivity
import com.yxlanyu.refreshrate.R
import com.yxlanyu.refreshrate.util.UpdateChecker
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UpdateWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        val prefs = context.getSharedPreferences("s", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("auto_check_update", true)) return Result.success()
        val now = System.currentTimeMillis()
        val lastCheck = prefs.getLong("last_auto_check_ts", 0L)
        if (lastCheck > 0 && now - lastCheck < DAY_MS) return Result.success()

        val info = withContext(Dispatchers.IO) { UpdateChecker.fetchLatestRelease() }
        if (info == null) return Result.success()
        prefs.edit().putLong("last_auto_check_ts", now).apply()

        val localVersion = currentVersionName(context)
        if (!UpdateChecker.isNewerThan(localVersion, info.tagName)) return Result.success()

        val notified = prefs.getString("last_notified_version", "") ?: ""
        if (notified == info.tagName) return Result.success()
        prefs.edit().putString("last_notified_version", info.tagName).apply()
        notifyUpdate(context, info.tagName)
        return Result.success()
    }

    private fun notifyUpdate(context: Context, tag: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_SHOW_UPDATE, true)
        }
        val pending = PendingIntent.getActivity(
            context,
            0,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            },
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_UPDATE)
            .setSmallIcon(R.drawable.ic_update)
            .setContentTitle(context.getString(R.string.update_notif_title, tag))
            .setContentText(context.getString(R.string.update_notif_content))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        ensureChannel(context)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val EXTRA_SHOW_UPDATE = "extra_show_update"
        const val CHANNEL_UPDATE = "update_channel"
        const val WORK_NAME = "auto_check_update"

        private const val NOTIFICATION_ID = 2001
        private const val DAY_MS = 24L * 60 * 60 * 1000

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<UpdateWorker>(24, TimeUnit.HOURS).build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, request)
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }

        fun ensureChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (manager.getNotificationChannel(CHANNEL_UPDATE) == null) {
                    val channel = NotificationChannel(
                        CHANNEL_UPDATE,
                        context.getString(R.string.update_notify_channel),
                        NotificationManager.IMPORTANCE_DEFAULT,
                    )
                    manager.createNotificationChannel(channel)
                }
            }
        }

        fun currentVersionName(context: Context): String =
            try {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: ""
            } catch (t: Throwable) {
                ""
            }
    }
}