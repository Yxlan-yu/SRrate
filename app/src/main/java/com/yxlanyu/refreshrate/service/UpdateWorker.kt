package com.yxlanyu.refreshrate.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.yxlanyu.refreshrate.R

object UpdateWorker {

    const val EXTRA_SHOW_UPDATE = "extra_show_update"
    const val CHANNEL_UPDATE = "update_channel"

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
}