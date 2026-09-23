package com.yxlanyu.refreshrate.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.yxlanyu.refreshrate.R
import com.yxlanyu.refreshrate.service.UpdateWorker
import com.yxlanyu.refreshrate.util.UpdateChecker
import com.yxlanyu.refreshrate.util.UpdateInfo
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.theme.MiuixTheme

class UpdateController(
    private val context: Context,
    private val scope: CoroutineScope,
) {
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
        scope.launch {
            val result = withContext(Dispatchers.IO) { UpdateChecker.fetchLatestRelease() }
            checking = false
            if (result == null) {
                Toast.makeText(context, R.string.update_check_fail, Toast.LENGTH_SHORT).show()
                return@launch
            }
            if (!UpdateChecker.isNewerThan(UpdateWorker.currentVersionName(context), result.tagName)) {
                Toast.makeText(context, R.string.update_no_new, Toast.LENGTH_SHORT).show()
                return@launch
            }
            info = result
            visible = true
        }
    }

    fun dismiss() {
        if (downloading) return
        visible = false
    }

    fun downloadAndInstall() {
        val entry = info ?: return
        if (downloading) return
        downloading = true
        progress = 0f
        scope.launch {
            val dest = File(context.cacheDir, "update/${fileSafeTag(entry.tagName)}.apk")
            val mainHandler = Handler(Looper.getMainLooper())
            val ok = withContext(Dispatchers.IO) {
                UpdateChecker.downloadApk(entry.apkUrl, dest) { fraction ->
                    mainHandler.post { progress = fraction }
                }
            }
            downloading = false
            if (!ok) {
                Toast.makeText(context, R.string.update_download_fail, Toast.LENGTH_LONG).show()
                return@launch
            }
            visible = false
            installApk(dest)
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
            if (controller.downloading) {
                Spacer(Modifier.width(16.dp))
                Text(
                    text = "${(controller.progress * 100).toInt()}%",
                    fontSize = 13.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
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