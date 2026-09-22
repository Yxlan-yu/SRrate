package com.yxlanyu.refreshrate.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.yxlanyu.refreshrate.R
import com.yxlanyu.refreshrate.model.DisplayMode
import com.yxlanyu.refreshrate.ui.components.FicIcon
import com.yxlanyu.refreshrate.ui.components.RefreshPageScaffold
import com.yxlanyu.refreshrate.util.AccessibilityUtils
import com.yxlanyu.refreshrate.util.AutoOverclockManager
import com.yxlanyu.refreshrate.util.RootUtils
import com.yxlanyu.refreshrate.util.ShizukuUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.RadioButton
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.ChevronForward
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme

private enum class CustomPage { Main, AppList, AppConfig }

private data class AppEntry(
    val name: String,
    val pkg: String,
    val systemApp: Boolean,
)

@Composable
fun CustomScreen(outerContentPadding: PaddingValues) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("s", Context.MODE_PRIVATE) }
    val scope = rememberCoroutineScope()

    var page by remember { mutableStateOf(CustomPage.Main) }
    var configPkg by remember { mutableStateOf("") }

    RefreshPageScaffold(
        title = stringResource(R.string.nav_custom_app_refresh),
        outerContentPadding = outerContentPadding,
        largeTitle = when (page) {
            CustomPage.Main -> stringResource(R.string.nav_custom_app_refresh)
            CustomPage.AppList -> stringResource(R.string.app_list_title)
            CustomPage.AppConfig -> stringResource(R.string.app_refresh_config_title)
        },
        navigationIcon = if (page != CustomPage.Main) {
            {
                top.yukonga.miuix.kmp.basic.IconButton(onClick = {
                    page = if (page == CustomPage.AppConfig) CustomPage.AppList else CustomPage.Main
                }) {
                    top.yukonga.miuix.kmp.basic.Icon(
                        imageVector = MiuixIcons.Back,
                        contentDescription = "back",
                    )
                }
            }
        } else null,
    ) {
        when (page) {
            CustomPage.Main -> item(key = "main") {
                MainContent(
                    context = context,
                    prefs = prefs,
                    scope = scope,
                    openAppList = { page = CustomPage.AppList },
                    openAppConfig = { pkg ->
                        configPkg = pkg
                        page = CustomPage.AppConfig
                    },
                )
            }
            CustomPage.AppList -> item(key = "applist") {
                AppListContent(
                    context = context,
                    prefs = prefs,
                    openAppConfig = { pkg ->
                        configPkg = pkg
                        page = CustomPage.AppConfig
                    },
                )
            }
            CustomPage.AppConfig -> item(key = "appcfg") {
                AppConfigContent(
                    context = context,
                    prefs = prefs,
                    pkg = configPkg,
                )
            }
        }
    }
}

@Composable
private fun MainContent(
    context: Context,
    prefs: android.content.SharedPreferences,
    scope: CoroutineScope,
    openAppList: () -> Unit,
    openAppConfig: (String) -> Unit,
) {
    var ocOn by remember { mutableStateOf(false) }
    var customOn by remember { mutableStateOf(prefs.getBoolean("custom_app_refresh", false)) }
    var ocRes by remember { mutableStateOf(prefs.getString("oc_target_res", "") ?: "") }
    var ocHz by remember { mutableStateOf(prefs.getInt("oc_target_hz", -1)) }
    var guardLog by remember { mutableStateOf("") }
    var showResPicker by remember { mutableStateOf(false) }
    var showHzPicker by remember { mutableStateOf(false) }
    var enabledApps by remember { mutableStateOf<List<AppEntry>>(emptyList()) }

    suspend fun refreshGuard() {
        val running = AutoOverclockManager.isRunning()
        ocOn = prefs.getBoolean("auto_overclock", false) && running
        guardLog = if (running) AutoOverclockManager.getLastLog() else ""
        ocHz = prefs.getInt("oc_target_hz", ocHz)
        ocRes = prefs.getString("oc_target_res", ocRes) ?: ocRes
    }

    suspend fun refreshEnabled() {
        val list = withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val all = prefs.all
            val out = mutableListOf<AppEntry>()
            for ((k, v) in all) {
                if (!k.startsWith("app_refresh_enabled_")) continue
                if (v != true) continue
                val pkg = k.substring("app_refresh_enabled_".length)
                if (pkg.isEmpty() || pkg.contains(":u")) continue
                val label = try {
                    pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
                } catch (e: Exception) { pkg }
                val sys = try {
                    (pm.getApplicationInfo(pkg, 0).flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
                } catch (e: Exception) { false }
                out.add(AppEntry(label, pkg, sys))
            }
            out.sortedBy { it.name.lowercase() }
        }
        enabledApps = list
    }

    LaunchedEffect(Unit) {
        ocOn = prefs.getBoolean("auto_overclock", false) && AutoOverclockManager.isRunning()
        while (true) {
            refreshGuard()
            refreshEnabled()
            delay(1500L)
        }
    }

    fun updateRunningTarget(res: String, hz: Int) {
        if (res.isEmpty() || hz <= 0) return
        if (!AutoOverclockManager.isRunning()) return
        val wh = res.replace("×", "x").split("x")
        if (wh.size != 2) return
        try {
            AutoOverclockManager.updateTarget(wh[0].toInt(), wh[1].toInt(), hz)
        } catch (e: Exception) {}
    }

    fun ensurePermission(): Boolean {
        if (!AccessibilityUtils.isKeepAliveServiceEnabled(context)) {
            Toast.makeText(context, R.string.accessibility_required, Toast.LENGTH_LONG).show()
            try {
                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            } catch (e: Exception) {
                Toast.makeText(context, R.string.accessibility_open_failed, Toast.LENGTH_SHORT).show()
            }
            return false
        }
        val mode = prefs.getString("auth_mode", "") ?: ""
        if (mode.isEmpty()) {
            Toast.makeText(context, R.string.no_any_permission, Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    fun applyOcSwitch(checked: Boolean) {
        ocOn = false
        prefs.edit().putBoolean("auto_overclock", false).apply()
        if (checked) {
            val act = context as? Activity
            if (Build.VERSION.SDK_INT >= 33 && act != null &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) {
                act.requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 200)
                Toast.makeText(context, R.string.no_any_permission, Toast.LENGTH_SHORT).show()
                return
            }
            if (!ensurePermission()) return
            val res = prefs.getString("oc_target_res", "") ?: ""
            var hz = prefs.getInt("oc_target_hz", -1)
            if (res.isEmpty() || hz <= 0) {
                val first = firstValidTarget(context)
                if (first == null) {
                    Toast.makeText(context, context.getString(R.string.guard_no_res_format, "?"), Toast.LENGTH_SHORT).show()
                    return
                }
                val (w, h, hzz) = first
                prefs.edit().putString("oc_target_res", "$w" + "x" + h).putInt("oc_target_hz", hzz).apply()
                ocRes = "$w" + "x" + h
                ocHz = hzz
                AutoOverclockManager.startService(context, prefs.getString("auth_mode", "") ?: "", w, h, hzz)
                ocOn = true
                prefs.edit().putBoolean("auto_overclock", true).apply()
                return
            }
            val wh = res.replace("×", "x").split("x")
            if (wh.size != 2) {
                Toast.makeText(context, context.getString(R.string.guard_no_res_format, res), Toast.LENGTH_SHORT).show()
                return
            }
            try {
                val w = wh[0].toInt(); val h = wh[1].toInt()
                AutoOverclockManager.startService(context, prefs.getString("auth_mode", "") ?: "", w, h, hz)
                ocOn = true
                prefs.edit().putBoolean("auto_overclock", true).apply()
            } catch (e: Exception) {
                Toast.makeText(context, context.getString(R.string.guard_no_res_format, res), Toast.LENGTH_SHORT).show()
            }
        } else {
            AutoOverclockManager.stopService(context)
            guardLog = ""
        }
    }

    SettingsSectionCard(
        title = stringResource(R.string.custom_section_global),
        children = {
            SettingsRow(
                title = stringResource(R.string.auto_overclock_title),
                desc = stringResource(R.string.auto_overclock_desc),
                leading = {
                    FicIcon(R.drawable.ic_fic_radar, accent = true)
                    Spacer(Modifier.width(12.dp))
                },
                trailing = {
                    Switch(checked = ocOn, onCheckedChange = { applyOcSwitch(it) })
                },
            )
            Divider(start = 52.dp)
            SettingsRow(
                title = stringResource(R.string.target_resolution_label),
                desc = if (ocRes.isEmpty()) "-" else ocRes.replace("x", "×"),
                onClick = { showResPicker = true },
                trailing = {
                    top.yukonga.miuix.kmp.basic.Icon(
                        modifier = Modifier.size(16.dp),
                        imageVector = MiuixIcons.ChevronForward,
                        contentDescription = "chevron",
                        tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                },
            )
            Divider()
            SettingsRow(
                title = stringResource(R.string.target_rate_label),
                desc = if (ocHz > 0) "$ocHz Hz" else "-",
                onClick = { showHzPicker = true },
                trailing = {
                    top.yukonga.miuix.kmp.basic.Icon(
                        modifier = Modifier.size(16.dp),
                        imageVector = MiuixIcons.ChevronForward,
                        contentDescription = "chevron",
                        tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                },
            )
            Divider()
            SettingsRow(
                title = stringResource(R.string.guard_enabled),
                desc = guardLog.ifEmpty { stringResource(R.string.guard_stopped) },
                descColor = if (ocOn) Color(0xFF2ECC71) else Color(0xFF888888),
            )
        },
    )

    SettingsSectionCard(
        title = stringResource(R.string.custom_app_master_title),
        children = {
            SettingsRow(
                title = stringResource(R.string.custom_app_master_title),
                desc = stringResource(R.string.custom_app_refresh_desc),
                leading = {
                    FicIcon(R.drawable.ic_fic_checkbox, accent = true)
                    Spacer(Modifier.width(12.dp))
                },
                onClick = {
                    if (!customOn) {
                        if (!ensurePermission()) return@SettingsRow
                        customOn = true
                        prefs.edit().putBoolean("custom_app_refresh", true).apply()
                    } else {
                        customOn = false
                        prefs.edit().putBoolean("custom_app_refresh", false).apply()
                        AutoOverclockManager.clearCustomOverride()
                    }
                },
                trailing = {
                    Switch(checked = customOn, onCheckedChange = {
                        if (it) {
                            if (!ensurePermission()) return@Switch
                            customOn = true
                            prefs.edit().putBoolean("custom_app_refresh", true).apply()
                        } else {
                            customOn = false
                            prefs.edit().putBoolean("custom_app_refresh", false).apply()
                            AutoOverclockManager.clearCustomOverride()
                        }
                    })
                },
            )
        },
    )

    SettingsSectionCard(
        title = stringResource(R.string.enabled_app_list_title),
        children = {
            if (enabledApps.isEmpty()) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    text = stringResource(R.string.enabled_list_empty),
                    fontSize = 14.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            } else {
                enabledApps.forEachIndexed { idx, app ->
                    SettingsRow(
                        title = app.name,
                        desc = app.pkg,
                        descColor = if (idx == 0 && app.pkg == app.name) Color(0xFF888888) else Color(0xFF888888),
                        onClick = { openAppConfig(app.pkg) },
                        leading = {
                            AppAvatar(app.name, app.pkg)
                            Spacer(Modifier.width(12.dp))
                        },
                        trailing = {
                            Text(
                                text = stringResource(R.string.app_enabled_badge),
                                fontSize = 12.sp,
                                color = Color(0xFF2ECC71),
                            )
                            Spacer(Modifier.width(2.dp))
                            top.yukonga.miuix.kmp.basic.Icon(
                                modifier = Modifier.size(16.dp),
                                imageVector = MiuixIcons.ChevronForward,
                                contentDescription = "chevron",
                                tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            )
                        },
                    )
                    if (idx < enabledApps.lastIndex) Divider(start = 52.dp)
                }
            }
        },
    )

    SettingsSectionCard(
        title = stringResource(R.string.custom_section_manage),
        children = {
            SettingsRow(
                title = stringResource(R.string.app_list_title),
                desc = stringResource(R.string.custom_app_master_desc),
                onClick = openAppList,
                leading = {
                    FicIcon(R.drawable.ic_fic_grid)
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
        },
    )

    val modes = remember { AutoOverclockManager.getSupportedModes(context) }
    TargetPickerDialog(
        show = showResPicker,
        title = stringResource(R.string.target_resolution_label),
        options = remember(modes) { buildResLabels(modes) },
        selected = ocRes.replace("x", "×"),
        onSelect = { res ->
            val nr = res.replace("×", "x")
            ocRes = nr
            prefs.edit().putString("oc_target_res", nr).apply()
            val rates = ratesForRes(modes, nr)
            if (rates.isNotEmpty()) {
                val cur = prefs.getInt("oc_target_hz", -1)
                val hz = if (rates.contains(cur)) cur else rates.last()
                ocHz = hz
                prefs.edit().putInt("oc_target_hz", hz).apply()
            }
            updateRunningTarget(nr, ocHz)
        },
        onDismiss = { showResPicker = false },
    )
    TargetPickerDialog(
        show = showHzPicker,
        title = stringResource(R.string.target_rate_label),
        options = remember(ocRes) { buildHzLabels(modes, ocRes) },
        selected = if (ocHz > 0) "$ocHz Hz" else "",
        onSelect = { label ->
            val hz = label.replace(" Hz", "").trim().toIntOrNull()
            if (hz != null && hz > 0) {
                ocHz = hz
                prefs.edit().putInt("oc_target_hz", hz).apply()
                updateRunningTarget(ocRes, hz)
            }
        },
        onDismiss = { showHzPicker = false },
    )
}

@Composable
private fun AppListContent(
    context: Context,
    prefs: android.content.SharedPreferences,
    openAppConfig: (String) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var showSystem by remember { mutableStateOf(prefs.getBoolean("show_system_apps_in_list", false)) }
    var allApps by remember { mutableStateOf<List<AppEntry>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val list = withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val intents = if (Build.VERSION.SDK_INT >= 33) {
                pm.queryIntentActivities(
                    Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),
                    PackageManager.ResolveInfoFlags.of(0L),
                )
            } else {
                @Suppress("DEPRECATION")
                pm.queryIntentActivities(
                    Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),
                    0,
                )
            }
            val seen = mutableSetOf<String>()
            val out = mutableListOf<AppEntry>()
            for (ri in intents) {
                val pkg = ri.activityInfo?.packageName ?: continue
                if (pkg in seen) continue
                seen.add(pkg)
                val label = ri.loadLabel(pm)?.toString() ?: pkg
                val sys = try {
                    (pm.getApplicationInfo(pkg, 0).flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
                } catch (e: Exception) { false }
                out.add(AppEntry(label, pkg, sys))
            }
            out.filter { it.name.isNotBlank() }.distinctBy { it.pkg }.sortedBy { it.name.lowercase() }
        }
        allApps = list
        loading = false
    }

    val filtered = remember(query, showSystem, allApps) {
        val base = if (showSystem) allApps else allApps.filter { !it.systemApp }
        if (query.isBlank()) base
        else base.filter {
            it.name.contains(query, ignoreCase = true) || it.pkg.contains(query, ignoreCase = true)
        }
    }

    TextField(
        value = query,
        onValueChange = { query = it },
        label = stringResource(R.string.app_list_search_hint),
        useLabelAsPlaceholder = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 14.dp, end = 14.dp, top = 4.dp, bottom = 4.dp),
    )
    SettingsRow(
        title = stringResource(R.string.show_system_apps),
        desc = stringResource(if (showSystem) R.string.app_list_all_hint else R.string.app_list_third_party_hint),
        onClick = {
            showSystem = !showSystem
            prefs.edit().putBoolean("show_system_apps_in_list", showSystem).apply()
        },
        trailing = {
            Switch(checked = showSystem, onCheckedChange = {
                showSystem = it
                prefs.edit().putBoolean("show_system_apps_in_list", it).apply()
            })
        },
    )
    Divider()
    if (loading) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            text = "-",
            fontSize = 14.sp,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
    } else if (filtered.isEmpty()) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            text = stringResource(R.string.no_apps_found),
            fontSize = 14.sp,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
    } else {
        filtered.forEach { app ->
            SettingsRow(
                title = app.name,
                desc = app.pkg,
                onClick = { openAppConfig(app.pkg) },
                leading = {
                    AppAvatar(app.name, app.pkg)
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
    }
}

@Composable
private fun AppConfigContent(
    context: Context,
    prefs: android.content.SharedPreferences,
    pkg: String,
) {
    var enabled by remember(pkg) {
        mutableStateOf(prefs.getBoolean("app_refresh_enabled_" + pkg, false))
    }
    var res by remember(pkg) { mutableStateOf(prefs.getString("app_refresh_res_" + pkg, "") ?: "") }
    var hz by remember(pkg) { mutableStateOf(prefs.getInt("app_refresh_hz_" + pkg, -1)) }
    var showResPicker by remember { mutableStateOf(false) }
    var showHzPicker by remember { mutableStateOf(false) }
    var appName by remember(pkg) { mutableStateOf(pkg) }

    LaunchedEffect(pkg) {
        val name = withContext(Dispatchers.IO) {
            try {
                context.packageManager.getApplicationLabel(
                    context.packageManager.getApplicationInfo(pkg, 0)
                ).toString()
            } catch (e: Exception) { pkg }
        }
        appName = name
    }

    fun ensurePermission(): Boolean {
        if (!AccessibilityUtils.isKeepAliveServiceEnabled(context)) {
            Toast.makeText(context, R.string.accessibility_required, Toast.LENGTH_LONG).show()
            try {
                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            } catch (e: Exception) {}
            return false
        }
        val mode = prefs.getString("auth_mode", "") ?: ""
        if (mode.isEmpty()) {
            Toast.makeText(context, R.string.no_any_permission, Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    fun applyDisplay(res: String, hz: Int) {
        if (res.isEmpty() || hz <= 0) return
        val wh = res.replace("×", "x").split("x")
        if (wh.size != 2) return
        val authMode = prefs.getString("auth_mode", "") ?: ""
        try {
            val w = wh[0].toInt(); val h = wh[1].toInt()
            val modes = AutoOverclockManager.getSupportedModes(context)
            var target: DisplayMode? = null
            for (m in modes) {
                if (m.width == w && m.height == h && m.rateInt == hz) { target = m; break }
            }
            if (target == null) {
                for (m in modes) {
                    if (m.width == w && m.height == h) {
                        if (target == null || kotlin.math.abs(m.rateInt - hz) < kotlin.math.abs(target.rateInt - hz)) {
                            target = m
                        }
                    }
                }
            }
            if (target != null) {
                if ("root" == authMode) RootUtils.setDisplayMode(target.width, target.height, target.rateInt, target.sfIndex)
                else if ("shizuku" == authMode) ShizukuUtils.setDisplayMode(target.width, target.height, target.rateInt, target.sfIndex)
            }
        } catch (e: Exception) {}
    }

    fun toggleApp(checked: Boolean) {
        if (checked) {
            if (!prefs.getBoolean("custom_app_refresh", false)) {
                Toast.makeText(context, R.string.custom_app_master_required, Toast.LENGTH_LONG).show()
                enabled = false
                return
            }
            if (!ensurePermission()) {
                enabled = false
                return
            }
            if (res.isEmpty() || hz <= 0) {
                Toast.makeText(context, R.string.custom_app_master_required, Toast.LENGTH_LONG).show()
                enabled = false
                return
            }
            prefs.edit().putBoolean("app_refresh_enabled_" + pkg, true).apply()
            applyDisplay(res, hz)
        } else {
            prefs.edit().putBoolean("app_refresh_enabled_" + pkg, false).apply()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppAvatar(appName, pkg)
        Spacer(Modifier.width(14.dp))
        Column {
            Text(
                text = appName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MiuixTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.size(3.dp))
            Text(
                text = pkg,
                fontSize = 12.sp,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
    }
    SettingsSectionCard(
        title = stringResource(R.string.enable_single_app_refresh),
        children = {
            SettingsRow(
                title = stringResource(R.string.enable_single_app_refresh),
                desc = stringResource(R.string.single_app_refresh_desc),
                onClick = { toggleApp(!enabled) },
                trailing = {
                    Switch(checked = enabled, onCheckedChange = { toggleApp(it) })
                },
            )
        },
    )
    SettingsSectionCard(
        title = stringResource(R.string.custom_section_global),
            children = {
                SettingsRow(
                    title = stringResource(R.string.target_resolution_label),
                    desc = if (res.isEmpty()) "-" else res.replace("x", "×"),
                    onClick = { showResPicker = true },
                    trailing = {
                        top.yukonga.miuix.kmp.basic.Icon(
                            modifier = Modifier.size(16.dp),
                            imageVector = MiuixIcons.ChevronForward,
                            contentDescription = "chevron",
                            tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    },
                )
                Divider()
                SettingsRow(
                    title = stringResource(R.string.target_rate_label),
                    desc = if (hz > 0) "$hz Hz" else "-",
                    onClick = { showHzPicker = true },
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

    val modes = remember { AutoOverclockManager.getSupportedModes(context) }
    TargetPickerDialog(
        show = showResPicker,
        title = stringResource(R.string.target_resolution_label),
        options = remember(modes) { buildResLabels(modes) },
        selected = res.replace("x", "×"),
        onSelect = { r ->
            val nr = r.replace("×", "x")
            res = nr
            prefs.edit().putString("app_refresh_res_" + pkg, nr).apply()
            val rates = ratesForRes(modes, nr)
            if (rates.isNotEmpty()) {
                val cur = prefs.getInt("app_refresh_hz_" + pkg, -1)
                val h = if (rates.contains(cur)) cur else rates.last()
                hz = h
                prefs.edit().putInt("app_refresh_hz_" + pkg, h).apply()
            }
            if (enabled) applyDisplay(nr, hz)
        },
        onDismiss = { showResPicker = false },
    )
    TargetPickerDialog(
        show = showHzPicker,
        title = stringResource(R.string.target_rate_label),
        options = remember(res) { buildHzLabels(modes, res) },
        selected = if (hz > 0) "$hz Hz" else "",
        onSelect = { label ->
            val h = label.replace(" Hz", "").trim().toIntOrNull()
            if (h != null && h > 0) {
                hz = h
                prefs.edit().putInt("app_refresh_hz_" + pkg, h).apply()
                if (enabled) applyDisplay(res, h)
            }
        },
        onDismiss = { showHzPicker = false },
    )
}

@Composable
private fun TargetPickerDialog(
    show: Boolean,
    title: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    if (!show) return
    OverlayDialog(
        show = show,
        title = title,
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 360.dp)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 8.dp),
        ) {
            if (options.isEmpty()) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    text = stringResource(R.string.enabled_list_empty),
                    fontSize = 14.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            } else {
                options.forEachIndexed { idx, opt ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(opt) }
                            .padding(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = opt,
                            fontSize = 17.sp,
                            color = MiuixTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                        RadioButton(selected = opt == selected, onClick = { onSelect(opt) })
                    }
                    if (show && idx < options.lastIndex) {
                        top.yukonga.miuix.kmp.basic.HorizontalDivider(
                            modifier = Modifier.padding(start = 12.dp, end = 12.dp),
                            thickness = 1.dp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppAvatar(name: String, pkg: String) {
    val hs = pkg.hashCode().and(0xFFFFFF)
    val r = ((hs ushr 16) and 0xFF).toFloat() / 255f
    val g = ((hs ushr 8) and 0xFF).toFloat() / 255f
    val b = (hs and 0xFF).toFloat() / 255f
    val c1 = Color(r, g, b, alpha = 0.45f)
    val c2 = Color(r, g, b, alpha = 0.8f)
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(
                Brush.linearGradient(listOf(c1, c2)),
                RoundedCornerShape(20.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = name.ifEmpty { "?" }.firstOrNull()?.toString() ?: "?",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
    }
}

private fun firstValidTarget(context: Context): Triple<Int, Int, Int>? {
    val modes = AutoOverclockManager.getSupportedModes(context)
    if (modes.isEmpty()) return null
    val group = LinkedHashMap<String, MutableList<DisplayMode>>()
    for (m in modes) {
        val k = "${m.width}x${m.height}"
        group.getOrPut(k) { mutableListOf() }.add(m)
    }
    val key = group.keys.maxByOrNull {
        (it.split("x")[0].toIntOrNull() ?: 0) * (it.split("x")[1].toIntOrNull() ?: 0)
    } ?: return null
    val ms = group[key] ?: return null
    val max = ms.maxByOrNull { it.refreshRate } ?: return null
    return Triple(max.width, max.height, max.rateInt)
}

private fun buildResLabels(modes: List<DisplayMode>): List<String> {
    val set = LinkedHashSet<String>()
    for (m in modes) set.add("${m.width}x${m.height}")
    return set.sortedByDescending {
        (it.split("x")[0].toIntOrNull() ?: 0) * (it.split("x")[1].toIntOrNull() ?: 0)
    }.map { it.replace("x", "×") }
}

private fun buildHzLabels(modes: List<DisplayMode>, res: String): List<String> {
    val rk = res.replace("×", "x")
    val set = LinkedHashSet<Int>()
    for (m in modes) {
        if ("${m.width}x${m.height}" == rk) set.add(m.rateInt)
    }
    return set.sortedDescending().map { "$it Hz" }
}

private fun ratesForRes(modes: List<DisplayMode>, res: String): List<Int> {
    val rk = res.replace("×", "x")
    val set = LinkedHashSet<Int>()
    for (m in modes) {
        if ("${m.width}x${m.height}" == rk) set.add(m.rateInt)
    }
    return set.sortedDescending()
}

@Composable
private fun SettingsSectionCard(
    title: String,
    children: @Composable () -> Unit,
) {
    SmallTitle(
        text = title,
        insideMargin = PaddingValues(28.dp, 12.dp),
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
private fun Divider(start: Dp = 14.dp, end: Dp = 14.dp) {
    top.yukonga.miuix.kmp.basic.HorizontalDivider(
        modifier = Modifier.padding(start = start, end = end),
        thickness = 1.dp,
    )
}