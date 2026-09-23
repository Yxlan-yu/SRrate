package com.yxlanyu.refreshrate.ui.components

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.yxlanyu.refreshrate.MainActivity
import com.yxlanyu.refreshrate.R
import com.yxlanyu.refreshrate.service.UpdateWorker
import com.yxlanyu.refreshrate.util.UpdateChecker
import com.yxlanyu.refreshrate.util.UpdateInfo
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.LinearProgressIndicator
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.theme.MiuixTheme

class UpdateController(
    private val context: Context,
    private val scope: CoroutineScope,
) {
    companion object {
        private const val TAG = "SRrate_Upd"
        const val CHANNEL_DOWNLOAD = "update_download_channel"
        private const val NOTIFICATION_ID_DOWNLOAD = 2002

        fun ensureDownloadChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (manager.getNotificationChannel(CHANNEL_DOWNLOAD) == null) {
                    val channel = NotificationChannel(
                        CHANNEL_DOWNLOAD,
                        context.getString(R.string.update_downloading),
                        NotificationManager.IMPORTANCE_LOW,
                    )
                    manager.createNotificationChannel(channel)
                }
            }
        }
    }
    var checking by mutableStateOf(false)
        private set
    var downloading by mutableStateOf(false)
        private set
    var progress by mutableStateOf(0f)
        private set
    var visible by mutableStateOf(false)
        private set
    var info: UpdateInfo? by mutableStateOf(null)
        private set

    fun checkAndShow() {
        if (checking || downloading) return
        checking = true
        Log.i(TAG, "checkAndShow: start, checking=true at ${System.currentTimeMillis()}")
        Toast.makeText(context, R.string.update_checking_toast, Toast.LENGTH_SHORT).show()
        scope.launch {
            val result = try {
                withTimeoutOrNull(30_000) {
                    withContext(Dispatchers.IO) { UpdateChecker.fetchLatestRelease(context) }
                }
            } finally {
                checking = false
                Log.i(TAG, "checkAndShow: network done, checking=false at ${System.currentTimeMillis()}")
            }
            if (result == null) {
                Log.i(TAG, "checkAndShow: result=null -> fail toast")
                Toast.makeText(context, R.string.update_check_fail, Toast.LENGTH_SHORT).show()
                return@launch
            }
            if (!UpdateChecker.isNewerThan(UpdateWorker.currentVersionName(context), result.tagName)) {
                Log.i(TAG, "checkAndShow: not newer (${result.tagName}) -> no_new toast")
                Toast.makeText(context, R.string.update_no_new, Toast.LENGTH_SHORT).show()
                return@launch
            }
            Log.i(TAG, "checkAndShow: newer ${result.tagName} -> visible=true")
            info = result
            visible = true
        }
    }

    fun dismiss() {
        visible = false
    }

    fun reshow() {
        if (downloading) visible = true
    }

    fun openInBrowser() {
        val entry = info ?: return
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(entry.apkUrl))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, R.string.update_download_fail, Toast.LENGTH_SHORT).show()
        }
    }

    fun downloadAndInstall() {
        val entry = info ?: return
        if (downloading) return
        downloading = true
        progress = 0f
        showProgressNotification(0f)
        scope.launch {
            val dest = File(context.cacheDir, "update/${fileSafeTag(entry.tagName)}.apk")
            val mainHandler = Handler(Looper.getMainLooper())
            var lastNotifyPct = -1
            val ok = withContext(Dispatchers.IO) {
                UpdateChecker.downloadApk(entry.apkUrl, dest) { fraction ->
                    mainHandler.post {
                        progress = fraction
                        val pct = (fraction * 100).toInt()
                        if (pct != lastNotifyPct) {
                            lastNotifyPct = pct
                            showProgressNotification(fraction)
                        }
                    }
                }
            }
            downloading = false
            cancelProgressNotification()
            if (!ok) {
                Toast.makeText(context, R.string.update_download_fail, Toast.LENGTH_LONG).show()
                openInBrowser()
                return@launch
            }
            visible = false
            installApk(dest)
        }
    }

    private fun showProgressNotification(fraction: Float) {
        try {
            ensureDownloadChannel(context)
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(UpdateWorker.EXTRA_SHOW_UPDATE, true)
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
            val pct = (fraction.coerceIn(0f, 1f) * 100).toInt()
            val notification = NotificationCompat.Builder(context, CHANNEL_DOWNLOAD)
                .setSmallIcon(R.drawable.ic_update)
                .setContentTitle(context.getString(R.string.update_downloading))
                .setContentText("${pct}%")
                .setContentIntent(pending)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setProgress(100, pct, false)
                .build()
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(NOTIFICATION_ID_DOWNLOAD, notification)
        } catch (e: Exception) {
            Log.w(TAG, "showProgressNotification failed: ${e.message}")
        }
    }

    private fun cancelProgressNotification() {
        try {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(NOTIFICATION_ID_DOWNLOAD)
        } catch (e: Exception) {
            Log.w(TAG, "cancelProgressNotification failed: ${e.message}")
        }
    }

    private fun installApk(file: File) {
        try {
            val canInstall = Build.VERSION.SDK_INT < 26 || context.packageManager.canRequestPackageInstalls()
            if (!canInstall) {
                Toast.makeText(context, R.string.update_allow_install, Toast.LENGTH_LONG).show()
                val intent = Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:${context.packageName}"),
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return
            }
            val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, R.string.update_download_fail, Toast.LENGTH_SHORT).show()
        }
    }

    private fun fileSafeTag(tag: String): String =
        tag.replace(Regex("[^a-zA-Z0-9._-]"), "_")
}

@Composable
fun rememberUpdateController(): UpdateController {
    val context = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()
    return remember(context, scope) { UpdateController(context, scope) }
}

@Composable
fun UpdateDialog(controller: UpdateController) {
    val context = LocalContext.current
    val title = controller.info?.let { context.getString(R.string.update_found_title, it.tagName) } ?: ""
    RefrSheetDialog(
        show = controller.visible,
        title = title,
        onDismissRequest = { controller.dismiss() },
    ) {
        AnimatedContent(
            targetState = controller.downloading,
            label = "update_dialog_content",
        ) { downloading ->
            if (downloading) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 140.dp, max = 300.dp)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    LinearProgressIndicator(progress = controller.progress)
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = "${(controller.progress * 100).toInt()}%",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MiuixTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = context.getString(R.string.update_downloading),
                        fontSize = 13.sp,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                ) {
                    val notes = controller.info?.releaseNotes?.trim().orEmpty()
                    Text(
                        text = if (notes.isEmpty()) context.getString(R.string.update_notif_content) else notes,
                        fontSize = 13.sp,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(
                text = stringResource(R.string.update_btn_later),
                onClick = { controller.dismiss() },
            )
            Spacer(Modifier.width(8.dp))
            TextButton(
                text = stringResource(R.string.update_btn_browser),
                onClick = { controller.openInBrowser() },
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = { controller.downloadAndInstall() }) {
                Text(
                    text = if (controller.downloading) {
                        stringResource(R.string.update_downloading)
                    } else {
                        stringResource(R.string.update_btn_install)
                    },
                )
            }
        }
    }
}