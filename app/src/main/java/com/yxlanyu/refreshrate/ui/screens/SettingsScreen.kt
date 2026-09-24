package com.yxlanyu.refreshrate.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.yxlanyu.refreshrate.R
import com.yxlanyu.refreshrate.ui.components.FicIcon
import com.yxlanyu.refreshrate.ui.components.PageTransitionContent
import com.yxlanyu.refreshrate.ui.components.RefreshPageScaffold
import com.yxlanyu.refreshrate.ui.components.RefrSheetDialog
import com.yxlanyu.refreshrate.ui.components.UpdateController
import com.yxlanyu.refreshrate.util.AccessibilityUtils
import com.yxlanyu.refreshrate.util.LanguageUtils
import com.yxlanyu.refreshrate.util.RootUtils
import com.yxlanyu.refreshrate.util.ShizukuUtils
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.RadioButton
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.ChevronForward
import top.yukonga.miuix.kmp.icon.extended.ConvertFile
import top.yukonga.miuix.kmp.icon.extended.File
import top.yukonga.miuix.kmp.icon.extended.Info
import top.yukonga.miuix.kmp.icon.extended.Link
import top.yukonga.miuix.kmp.icon.extended.Refresh
import top.yukonga.miuix.kmp.icon.extended.Translate
import top.yukonga.miuix.kmp.icon.extended.Update
import top.yukonga.miuix.kmp.theme.MiuixTheme

private enum class SettingsPage { Main, Language, About }

private data class LangOption(val key: String, val labelRes: Int)

private val LANG_OPTIONS = listOf(
    LangOption(LanguageUtils.LANG_SYSTEM, R.string.lang_system),
    LangOption(LanguageUtils.LANG_ZH, R.string.lang_zh),
    LangOption(LanguageUtils.LANG_EN, R.string.lang_en),
)

private val THEME_COLORS = listOf(
    0xFF1976D2.toInt(),
    0xFF5B6CFF.toInt(),
    0xFF8E24AA.toInt(),
    0xFFE53935.toInt(),
    0xFFFB8C00.toInt(),
    0xFFD81B60.toInt(),
    0xFF34A853.toInt(),
    0xFF00B6A3.toInt(),
)

@Composable
fun SettingsScreen(
    outerContentPadding: androidx.compose.foundation.layout.PaddingValues,
    updateController: UpdateController,
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("s", android.content.Context.MODE_PRIVATE) }
    val scope = rememberCoroutineScope()
    val versionName = remember {
        runCatching {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: ""
        }.getOrDefault("")
    }

    var page by remember { mutableStateOf(SettingsPage.Main) }
    var authMode by remember { mutableStateOf(prefs.getString("auth_mode", "") ?: "") }
    var hasRoot by remember { mutableStateOf(false) }
    var shizukuAvail by remember { mutableStateOf(false) }
    var shizukuPerm by remember { mutableStateOf(false) }
    var a11yEnabled by remember { mutableStateOf(false) }
    var nativeOverlay by remember { mutableStateOf(prefs.getBoolean("native_refresh_overlay", false)) }
    var switchToast by remember { mutableStateOf(prefs.getBoolean("switch_toast_enabled", true)) }
    var autoCheck by remember { mutableStateOf(prefs.getBoolean("auto_check_update", true)) }
    var monet by remember { mutableStateOf(prefs.getBoolean("monet", true)) }
    var themeColor by remember { mutableStateOf(prefs.getInt("theme_color", 0xFF1976D2.toInt())) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showLog by remember { mutableStateOf(false) }
    var logText by remember { mutableStateOf("") }

    fun recreateActivity() {
        val activity = context as? Activity
        activity?.recreate()
    }

    BackHandler(enabled = page != SettingsPage.Main) {
        page = SettingsPage.Main
    }

    suspend fun refreshA11y() {
        val ok = withContext(Dispatchers.IO) { AccessibilityUtils.isKeepAliveServiceEnabled(context) }
        a11yEnabled = ok
    }

    suspend fun refreshAuth() {
        val root = withContext(Dispatchers.IO) { RootUtils.isRooted() }
        val avail = withContext(Dispatchers.IO) { ShizukuUtils.isAvailable() }
        val perm = withContext(Dispatchers.IO) {
            if (avail) ShizukuUtils.hasPermission() else false
        }
        hasRoot = root
        shizukuAvail = avail
        shizukuPerm = perm
    }

    LaunchedEffect(Unit) {
        while (true) {
            refreshA11y()
            refreshAuth()
            kotlinx.coroutines.delay(1500L)
        }
    }

    val openA11y: () -> Unit = {
        try {
            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        } catch (e: Exception) {
            Toast.makeText(context, R.string.accessibility_open_failed, Toast.LENGTH_SHORT).show()
        }
    }

    PageTransitionContent(
        targetState = page,
        depth = { p ->
            when (p) {
                SettingsPage.Main -> 0
                SettingsPage.Language -> 1
                SettingsPage.About -> 2
            }
        },
    ) { p ->
    RefreshPageScaffold(
        title = stringResource(R.string.settings_title),
        outerContentPadding = outerContentPadding,
        largeTitle = when (p) {
            SettingsPage.Main -> stringResource(R.string.settings_title)
            SettingsPage.Language -> stringResource(R.string.language_page_title)
            SettingsPage.About -> stringResource(R.string.about_title)
        },
        navigationIcon = if (page != SettingsPage.Main) {
            {
                top.yukonga.miuix.kmp.basic.IconButton(onClick = { page = SettingsPage.Main }) {
                    top.yukonga.miuix.kmp.basic.Icon(
                        imageVector = MiuixIcons.Back,
                        contentDescription = "back",
                    )
                }
            }
        } else null,
    ) {
        when (p) {
            SettingsPage.Main -> {
                item(key = "auth") {
                    SettingsSectionCard(
                        title = stringResource(R.string.settings_root_title),
                        children = {
                            RootRow(
                                hasRoot = hasRoot,
                                checked = authMode == "root",
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        authMode = "root"
                                        prefs.edit().putString("auth_mode", "root").apply()
                                    } else {
                                        if (authMode != "shizuku") {
                                            authMode = ""
                                            prefs.edit().putString("auth_mode", "").apply()
                                        }
                                    }
                                },
                            )
                            ShizukuRow(
                                avail = shizukuAvail,
                                perm = shizukuPerm,
                                checked = authMode == "shizuku",
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        authMode = "shizuku"
                                        prefs.edit().putString("auth_mode", "shizuku").apply()
                                    } else {
                                        if (authMode != "root") {
                                            authMode = ""
                                            prefs.edit().putString("auth_mode", "").apply()
                                        }
                                    }
                                },
                                onAuthorize = {
                                    if (shizukuAvail) {
                                        ShizukuUtils.requestPermission()
                                        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                                            kotlinx.coroutines.delay(1000L)
                                            val perm = ShizukuUtils.hasPermission()
                                            withContext(Dispatchers.Main) { shizukuPerm = perm }
                                        }
                                    } else {
                                        Toast.makeText(context, R.string.shizuku_install_hint, Toast.LENGTH_LONG).show()
                                    }
                                },
                            )
                            AccessibilityRow(a11yEnabled = a11yEnabled, onClick = openA11y)
                            NativeOverlayRow(
                                checked = nativeOverlay,
                                onCheckedChange = { checked ->
                                    val mode = prefs.getString("auth_mode", "")
                                    val useRoot = "root" == mode
                                    val useShizuku = "shizuku" == mode
                                    if (!useRoot && !useShizuku) {
                                        Toast.makeText(context, R.string.rate_lock_need_auth, Toast.LENGTH_SHORT).show()
                                        nativeOverlay = false
                                    } else {
                                        nativeOverlay = checked
                                        prefs.edit().putBoolean("native_refresh_overlay", checked).apply()
                                        scope.launch(Dispatchers.IO) {
                                            if (useRoot) RootUtils.setNativeRefreshOverlay(checked)
                                            else ShizukuUtils.setNativeRefreshOverlay(checked)
                                        }
                                    }
                                },
                            )
                        },
                    )
                }
                item(key = "general") {
                    SettingsSectionCard(
                        title = stringResource(R.string.settings_section_general),
                        children = {
                            ToggleRow(
                                leadingIcon = R.drawable.ic_fic_bell,
                                title = stringResource(R.string.settings_switch_toast),
                                desc = stringResource(R.string.settings_switch_toast_desc),
                                checked = switchToast,
                                onCheckedChange = { checked ->
                                    switchToast = checked
                                    prefs.edit().putBoolean("switch_toast_enabled", checked).apply()
                                },
                            )
                            ToggleRow(
                                leadingIcon = R.drawable.ic_update,
                                title = stringResource(R.string.settings_auto_check),
                                desc = stringResource(R.string.settings_auto_check_desc),
                                checked = autoCheck,
                                onCheckedChange = { checked ->
                                    autoCheck = checked
                                    prefs.edit().putBoolean("auto_check_update", checked).apply()
                                },
                            )
                            ToggleRow(
                                leadingIcon = R.drawable.ic_fic_radar,
                                title = stringResource(R.string.settings_monet_title),
                                desc = stringResource(R.string.settings_monet_desc),
                                checked = monet,
                                onCheckedChange = { checked ->
                                    monet = checked
                                    prefs.edit().putBoolean("monet", checked).apply()
                                    recreateActivity()
                                },
                            )
                            ChevRow(
                                title = stringResource(R.string.settings_color_title),
                                desc = stringResource(R.string.settings_color_desc),
                                icon = R.drawable.ic_fic_grid,
                                onClick = { showColorPicker = true },
                            )
                            ChevRow(
                                title = stringResource(R.string.language_page_title),
                                desc = stringResource(R.string.language_row_desc),
                                icon = R.drawable.ic_translate,
                                onClick = { page = SettingsPage.Language },
                            )
                            ChevRow(
                                title = stringResource(R.string.about_title),
                                desc = stringResource(R.string.version_label, versionName),
                                onClick = { page = SettingsPage.About },
                            )
                        },
                    )
                }
            }
            SettingsPage.Language -> {
                val current = LanguageUtils.getCurrentLang(context)
                item(key = "lang") {
                    Card(
                        cornerRadius = 16.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp, 14.dp, 14.dp, 0.dp),
                    ) {
                        LANG_OPTIONS.forEachIndexed { index, opt ->
                            val selected = opt.key == current
                            val onClick = {
                                val activity = context as? Activity
                                if (activity != null) {
                                    LanguageUtils.setLanguageAndRecreate(activity, opt.key)
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 12.dp, end = 14.dp, top = 8.dp, bottom = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = stringResource(opt.labelRes),
                                    fontSize = 17.sp,
                                    color = MiuixTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f),
                                )
                                RadioButton(selected = selected, onClick = onClick)
                            }
                            if (index < LANG_OPTIONS.lastIndex) {
                                top.yukonga.miuix.kmp.basic.HorizontalDivider(
                                    modifier = Modifier.padding(start = 12.dp, end = 12.dp),
                                    thickness = 1.dp,
                                )
                            }
                        }
                    }
                }
            }
            SettingsPage.About -> {
                item(key = "about_logo") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 22.dp, bottom = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_srrate),
                            contentDescription = null,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(18.dp)),
                        )
                        Spacer(Modifier.size(12.dp))
                        Text(
                            text = stringResource(R.string.about_app_brand),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MiuixTheme.colorScheme.onSurface,
                        )
                        Spacer(Modifier.size(4.dp))
                        Text(
                            text = stringResource(R.string.about_version, versionName),
                            fontSize = 12.sp,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    }
                }
                item(key = "more") {
                    SettingsSectionCard(
                        title = stringResource(R.string.about_menu_more),
                        children = {
                            AboutMenuRow(
                                icon = MiuixIcons.Update,
                                title = stringResource(R.string.check_update),
                                desc = stringResource(R.string.about_check_update_desc),
                                onClick = { updateController.checkAndShow() },
                            )
                            AboutMenuRow(
                                icon = MiuixIcons.Link,
                                title = stringResource(R.string.about_github_project),
                                desc = stringResource(R.string.about_github_project_desc),
                                onClick = {
                                    openInBrowser(context, "https://github.com/Yxlan-yu/SRrate")
                                },
                            )
                            AboutMenuRow(
                                icon = MiuixIcons.ConvertFile,
                                title = stringResource(R.string.about_source_code),
                                desc = stringResource(R.string.about_source_desc),
                                onClick = {
                                    openInBrowser(context, "https://www.coolapk.com/u/31452988")
                                },
                            )
                        },
                    )
                }
                item(key = "author") {
                    SettingsSectionCard(
                        title = stringResource(R.string.contributors_title),
                        children = {
                            ContributorRow(
                                avatar = "葉",
                                type = stringResource(R.string.contrib_name_yxlanyu),
                                info = stringResource(R.string.contrib_info_yxlanyu),
                                url = "https://github.com/Yxlan-yu",
                                avatarImage = R.drawable.avatar_yxlanyu,
                            )
                        },
                    )
                }
                item(key = "diag") {
                    SettingsSectionCard(
                        title = stringResource(R.string.about_diag_title),
                        children = {
                            SettingsRow(
                                leading = {
                                    FicIcon(R.drawable.ic_fic_doc, accent = false)
                                    Spacer(Modifier.width(12.dp))
                                },
                                title = stringResource(R.string.about_gen_log),
                                desc = stringResource(R.string.generate_log_desc),
                                onClick = {
                                    scope.launch(Dispatchers.IO) {
                                        val log = RootUtils.generateRuntimeLog(context)
                                        withContext(Dispatchers.Main) {
                                            logText = log
                                            showLog = true
                                        }
                                    }
                                },
                                trailing = {
                                    top.yukonga.miuix.kmp.basic.Icon(
                                        modifier = Modifier.size(16.dp),
                                        imageVector = MiuixIcons.ChevronForward,
                                        contentDescription = "chevron",
                                        tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                    )
                                },
                            )
                        },
                    )
                }
            }
        }
    }

    RefrSheetDialog(
        show = showLog,
        title = stringResource(R.string.log_dialog_title),
        onDismissRequest = { showLog = false },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 320.dp)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Text(
                text = logText.ifEmpty { "-" },
                fontSize = 12.sp,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End,
        ) {
            TextButton(
                text = stringResource(R.string.log_dialog_close),
                onClick = { showLog = false },
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
                shareLog(context, logText)
                showLog = false
            }) {
                Text(text = stringResource(R.string.log_dialog_share))
            }
        }
    }

    RefrSheetDialog(
        show = showColorPicker,
        title = stringResource(R.string.settings_color_title),
        onDismissRequest = { showColorPicker = false },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            THEME_COLORS.chunked(4).forEach { rowColors ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                ) {
                    rowColors.forEach { c ->
                        val selected = themeColor == c
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(c))
                                .then(
                                    if (selected) {
                                        Modifier.border(3.dp, MiuixTheme.colorScheme.surface, CircleShape)
                                    } else {
                                        Modifier
                                    },
                                )
                                .clickable {
                                    themeColor = c
                                    showColorPicker = false
                                    prefs.edit().putInt("theme_color", c).apply()
                                    recreateActivity()
                                },
                        )
                    }
                }
            }
        }
    }
}
}

private fun shareLog(context: android.content.Context, content: String) {
    try {
        val logDir = File(context.cacheDir, "logs").apply { mkdirs() }
        val logFile = File(logDir, "refresh_rate_log.txt")
        logFile.writeText(content)
        val uri: Uri = FileProvider.getUriForFile(
            context,
            context.packageName + ".fileprovider",
            logFile,
        )
        val share = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(share, content.getShareTitle(context)))
    } catch (e: Exception) {
        val share = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, content)
        }
        context.startActivity(Intent.createChooser(share, content.getShareTitle(context)))
    }
}

private fun String.getShareTitle(context: android.content.Context): String =
    context.getString(R.string.log_dialog_share)

private fun openInBrowser(context: android.content.Context, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (e: Exception) {
        Toast.makeText(context, R.string.log_dialog_share, Toast.LENGTH_SHORT).show()
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    children: @Composable () -> Unit,
) {
    SmallTitle(
        text = title,
        insideMargin = androidx.compose.foundation.layout.PaddingValues(28.dp, 12.dp),
    )
    Card(
        cornerRadius = 16.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp, 0.dp, 14.dp, 0.dp),
    ) {
        children()
    }
}

@Composable
private fun SettingsRow(
    title: String,
    desc: String,
    descColor: Color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
    trailing: @Composable (() -> Unit)? = null,
    leading: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    val clickableModifier = if (onClick != null) {
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    } else {
        Modifier.fillMaxWidth()
    }
    Row(
        modifier = clickableModifier.padding(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            leading()
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 17.sp,
                color = MiuixTheme.colorScheme.onSurface,
            )
            if (desc.isNotEmpty()) {
                Spacer(Modifier.size(3.dp))
                Text(
                    text = desc,
                    fontSize = 13.sp,
                    color = descColor,
                )
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(10.dp))
            trailing()
        }
    }
}

@Composable
private fun RootRow(
    hasRoot: Boolean,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    SettingsRow(
        title = stringResource(R.string.root_running_label),
        desc = stringResource(if (hasRoot) R.string.settings_root_granted else R.string.settings_root_denied),
        descColor = if (hasRoot) Color(0xFF2ECC71) else Color(0xFFE74C3C),
        leading = {
            FicIcon(R.drawable.ic_fic_shield, accent = true)
            Spacer(Modifier.width(12.dp))
        },
        trailing = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        },
    )
    Divider(start = 52.dp)
}

@Composable
private fun ShizukuRow(
    avail: Boolean,
    perm: Boolean,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onAuthorize: () -> Unit,
) {
    val descRes = when {
        !avail -> R.string.shizuku_not_running
        !perm -> R.string.shizuku_no_perm
        else -> R.string.shizuku_authorized
    }
    val descColor = when {
        !avail -> Color(0xFF888888)
        !perm -> Color(0xFFE74C3C)
        else -> Color(0xFF2ECC71)
    }
    SettingsRow(
        title = stringResource(R.string.shizuku_running_label),
        desc = stringResource(descRes),
        descColor = descColor,
        onClick = if (avail && !perm) onAuthorize else null,
        leading = {
            FicIcon(R.drawable.ic_fic_lock, accent = true)
            Spacer(Modifier.width(12.dp))
        },
        trailing = {
            if (avail && !perm && !checked) {
                top.yukonga.miuix.kmp.basic.TextButton(
                    text = stringResource(R.string.btn_authorize_shizuku),
                    onClick = onAuthorize,
                )
            } else {
                Switch(checked = checked, onCheckedChange = onCheckedChange)
            }
        },
    )
    Divider(start = 52.dp)
}

@Composable
private fun AccessibilityRow(a11yEnabled: Boolean, onClick: () -> Unit) {
    SettingsRow(
        title = stringResource(R.string.accessibility_running_label),
        desc = stringResource(if (a11yEnabled) R.string.accessibility_enabled else R.string.accessibility_disabled),
        descColor = if (a11yEnabled) Color(0xFF2ECC71) else Color(0xFFE74C3C),
        onClick = onClick,
        leading = {
            FicIcon(R.drawable.ic_fic_bell, accent = true)
            Spacer(Modifier.width(12.dp))
        },
        trailing = {
            Switch(
                checked = a11yEnabled,
                onCheckedChange = { onClick() },
                enabled = false,
            )
        },
    )
    Divider(start = 52.dp)
}

@Composable
private fun NativeOverlayRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    SettingsRow(
        leading = {
            FicIcon(R.drawable.ic_fic_bolt, accent = false)
            Spacer(Modifier.width(12.dp))
        },
        title = stringResource(R.string.native_overlay_title),
        desc = stringResource(R.string.native_overlay_desc),
        trailing = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        },
    )
}

@Composable
private fun ToggleRow(
    leadingIcon: Int,
    title: String,
    desc: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    SettingsRow(
        title = title,
        desc = desc,
        leading = {
            FicIcon(leadingIcon, accent = false)
            Spacer(Modifier.width(12.dp))
        },
        trailing = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        },
    )
    Divider(start = 52.dp)
}

@Composable
private fun ChevRow(
    title: String,
    desc: String,
    icon: Int = R.drawable.ic_fic_info,
    onClick: () -> Unit,
) {
    SettingsRow(
        title = title,
        desc = desc,
        onClick = onClick,
        leading = {
            FicIcon(icon, accent = false)
            Spacer(Modifier.width(12.dp))
        },
        trailing = {
            top.yukonga.miuix.kmp.basic.Icon(
                modifier = Modifier.size(16.dp),
                imageVector = MiuixIcons.ChevronForward,
                contentDescription = "chevron",
                tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        },
    )
    Divider(start = 52.dp)
}

@Composable
private fun AboutMenuRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String,
    onClick: () -> Unit,
) {
    SettingsRow(
        title = title,
        desc = desc,
        onClick = onClick,
        trailing = {
            top.yukonga.miuix.kmp.basic.Icon(
                modifier = Modifier.size(16.dp),
                imageVector = MiuixIcons.ChevronForward,
                contentDescription = "chevron",
                tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        },
        leading = {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFFE8EEF3), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                top.yukonga.miuix.kmp.basic.Icon(
                    modifier = Modifier.size(20.dp),
                    imageVector = icon,
                    contentDescription = title,
                    tint = MiuixTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.width(12.dp))
        },
    )
    Divider(start = 52.dp)
}

@Composable
private fun ContributorRow(
    avatar: String,
    type: String,
    info: String,
    url: String,
    avatarImage: Int? = null,
) {
    val context = LocalContext.current
    SettingsRow(
        title = type,
        desc = info,
        onClick = {
            openInBrowser(context, url)
        },
        leading = {
            if (avatarImage != null) {
                Image(
                    painter = painterResource(avatarImage),
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            Brush.linearGradient(listOf(Color(0xFF9C6BFF), Color(0xFF7C6CFF))),
                            RoundedCornerShape(20.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = avatar,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
        },
        trailing = {
            top.yukonga.miuix.kmp.basic.Icon(
                modifier = Modifier.size(16.dp),
                imageVector = MiuixIcons.ChevronForward,
                contentDescription = "chevron",
                tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        },
    )
}

@Composable
private fun Divider(start: Dp = 14.dp, end: Dp = 14.dp) {
    top.yukonga.miuix.kmp.basic.HorizontalDivider(
        modifier = Modifier.padding(start = start, end = end),
        thickness = 1.dp,
    )
}