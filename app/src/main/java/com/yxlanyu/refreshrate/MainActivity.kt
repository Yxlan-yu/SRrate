package com.yxlanyu.refreshrate

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.yxlanyu.refreshrate.ui.AppRoot
import com.yxlanyu.refreshrate.ui.AppTheme
import com.yxlanyu.refreshrate.util.LanguageUtils
import com.yxlanyu.refreshrate.service.UpdateWorker

class MainActivity : ComponentActivity() {

    private var appliedLang: String? = null

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageUtils.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        LanguageUtils.applyLanguage(this)
        appliedLang = LanguageUtils.getCurrentLang(this)
        createNotificationChannel()
        setContent {
            AppTheme {
                AppRoot()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (appliedLang != null && LanguageUtils.getCurrentLang(this) != appliedLang) {
            recreate()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "overclock_channel",
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW,
            )
            getSystemService(NotificationManager::class.java)
                ?.createNotificationChannel(channel)
            UpdateWorker.ensureChannel(this)
        }
    }
}