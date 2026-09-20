package com.yxlanyu.refreshrate.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yxlanyu.refreshrate.R
import com.yxlanyu.refreshrate.model.DisplayMode
import com.yxlanyu.refreshrate.ui.components.RefreshPageScaffold
import com.yxlanyu.refreshrate.util.AutoOverclockManager
import com.yxlanyu.refreshrate.util.RootUtils
import com.yxlanyu.refreshrate.util.ShizukuUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

internal fun itemKey(mode: DisplayMode): String =
    "${mode.width}x${mode.height}x${mode.rateInt}"

internal fun buildSubtitle(context: Context): String {
    val hz = AutoOverclockManager.getCurrentRate(context).toInt()
    val res = AutoOverclockManager.getCurrentResolution(context)
    return String.format(context.getString(R.string.home_status_format), hz, res)
}

@Composable
fun HomeScreen(outerContentPadding: androidx.compose.foundation.layout.PaddingValues) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("s", Context.MODE_PRIVATE)
    val scope = rememberCoroutineScope()

    var hasRoot by remember { mutableStateOf(false) }
    var items by remember { mutableStateOf<List<Any>>(emptyList()) }
    var subtitle by remember { mutableStateOf("") }
    var activeModeId by remember { mutableIntStateOf(-1) }
    var selKey by remember { mutableStateOf("") }

    fun reload() {
        scope.launch(Dispatchers.IO) {
            val root = RootUtils.isRooted()
            val dumped = if (root) RootUtils.getDisplayModesFromDumpsys() else emptyList()
            val sorted = buildSortedList(context, dumped.ifEmpty { null })
            val selW = prefs.getInt("last_sel_w", 0)
            val selH = prefs.getInt("last_sel_h", 0)
            val selHz = prefs.getInt("last_sel_hz", 0)
            val cur = if (selHz > 0) intArrayOf(selW, selH, selHz) else null
            val activeId = buildActiveModeId(context)
            val sub = buildSubtitle(context)
            withContext(Dispatchers.Main) {
                hasRoot = root
                items = sorted
                activeModeId = activeId
                selKey = if (cur != null && cur[2] > 0) "${cur[0]}x${cur[1]}x${cur[2]}" else ""
                subtitle = sub
            }
        }
    }

    LaunchedEffect(Unit) {
        reload()
        while (true) {
            delay(2000L)
            val sub = buildSubtitle(context)
            if (sub != subtitle) subtitle = sub
            val activeId = buildActiveModeId(context)
            if (activeId != activeModeId) activeModeId = activeId
        }
    }

    RefreshPageScaffold(
        title = stringResource(R.string.home_title),
        outerContentPadding = outerContentPadding,
        largeTitle = stringResource(R.string.home_title),
        subtitle = if (subtitle.isEmpty()) "-" else subtitle,
    ) {
        items.forEach { item ->
            when (item) {
                is String -> item(key = item) {
                    SmallTitle(
                        text = item,
                        insideMargin = androidx.compose.foundation.layout.PaddingValues(28.dp, 8.dp),
                    )
                }
                is DisplayMode -> {
                    val isCurrent = if (item.modeId >= 0) {
                        item.modeId == activeModeId
                    } else {
                        selKey.isNotEmpty() && selKey == itemKey(item)
                    }
                    item(key = itemKey(item)) {
                        RateCard(
                            mode = item,
                            isCurrent = isCurrent,
                            onClick = {
                                onRateSelected(context, item, hasRoot, ::reload)
                            },
                        )
                    }
                }
            }
        }
    }
}

private fun onRateSelected(
    context: Context,
    mode: DisplayMode,
    hasRoot: Boolean,
    onSuccess: () -> Unit,
) {
    val prefs = context.getSharedPreferences("s", Context.MODE_PRIVATE)
    val authMode = prefs.getString("auth_mode", "")
    val useRoot = "root" == authMode && hasRoot
    val useShizuku = "shizuku" == authMode
            && ShizukuUtils.isAvailable()
            && ShizukuUtils.hasPermission()
    if (!useRoot && !useShizuku) {
        Toast.makeText(context, R.string.no_root_toast, Toast.LENGTH_SHORT).show()
        return
    }
    kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
        val ok = if (useRoot) {
            RootUtils.setDisplayMode(mode.width, mode.height, mode.rateInt, mode.sfIndex)
        } else {
            ShizukuUtils.setDisplayMode(mode.width, mode.height, mode.rateInt, mode.sfIndex)
        }
        if (ok) {
            prefs.edit()
                .putInt("last_sel_w", mode.width)
                .putInt("last_sel_h", mode.height)
                .putInt("last_sel_hz", mode.rateInt)
                .putInt("last_sel_sf", mode.sfIndex)
                .apply()
        }
        withContext(Dispatchers.Main) {
            if (ok) {
                Toast.makeText(context, context.getString(R.string.switch_success, mode.rateInt), Toast.LENGTH_SHORT).show()
                onSuccess()
            } else {
                Toast.makeText(context, R.string.switch_fail, Toast.LENGTH_SHORT).show()
            }
        }
    }
}

internal fun buildActiveModeId(context: Context): Int =
    runCatching {
        val dm = context.getSystemService(Context.DISPLAY_SERVICE)
                as android.hardware.display.DisplayManager
        dm.getDisplay(android.view.Display.DEFAULT_DISPLAY).mode.modeId
    }.getOrDefault(-1)

private fun buildSortedList(context: Context, dumpedModes: List<DisplayMode>?): List<Any> {
    if (dumpedModes.isNullOrEmpty()) return fallback(context)
    val grouped = LinkedHashMap<String, MutableList<DisplayMode>>()
    for (m in dumpedModes) {
        val k = "${m.width}x${m.height}"
        grouped.getOrPut(k) { mutableListOf() }.add(m)
    }
    val keys = grouped.keys.sortedByDescending { key ->
        val p = key.split("x")
        (p[0].toIntOrNull() ?: 0) * (p[1].toIntOrNull() ?: 0)
    }
    val items = mutableListOf<Any>()
    for ((i, key) in keys.withIndex()) {
        val ms = grouped[key] ?: continue
        ms.sortByDescending { it.refreshRate }
        val label = context.getString(
            if (i == 0) R.string.high_resolution else R.string.low_resolution,
        ) + key
        items.add(label)
        items.addAll(ms)
    }
    return items
}

private fun fallback(context: Context): List<Any> {
    val rates = intArrayOf(60, 90, 120, 144, 165, 170, 175, 177)
    val list = mutableListOf<Any>()
    list.add(context.getString(R.string.fallback_high_res))
    for (r in rates) list.add(DisplayMode(1272, 2772, r.toFloat(), -1))
    list.add(context.getString(R.string.fallback_low_res))
    for (r in rates) list.add(DisplayMode(1080, 2354, r.toFloat(), -1))
    return list
}

private fun gradientFor(hz: Int): Pair<Color, Color> = when {
    hz >= 185 -> Color(0xFFF5A623) to Color(0xFFFF7A00)
    hz >= 165 -> Color(0xFFE74C3C) to Color(0xFFD4356C)
    hz >= 144 -> Color(0xFFD4356C) to Color(0xFFF5A623)
    hz >= 120 -> Color(0xFF0A84FF) to Color(0xFF00B6F0)
    hz >= 90 -> Color(0xFF3BA8FF) to Color(0xFF7C6CFF)
    hz >= 60 -> Color(0xFF00A86B) to Color(0xFF34C77B)
    else -> Color(0xFF07C160) to Color(0xFF00B96B)
}

@Composable
private fun RateCard(
    mode: DisplayMode,
    isCurrent: Boolean,
    onClick: () -> Unit,
) {
    val ctx = LocalContext.current
    val (c1, c2) = gradientFor(mode.rateInt)
    val name = mode.getRateName(ctx)
    val desc = mode.getRateDesc(ctx)
    val isFallback = mode.modeId < 0

    Card(
        onClick = onClick,
        cornerRadius = 16.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp, 14.dp, 14.dp, 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Brush.linearGradient(listOf(c1, c2)), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = name.take(1),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MiuixTheme.colorScheme.onSurface,
                    )
                    if (isCurrent) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.badge_current),
                            fontSize = 12.sp,
                            color = Color.White,
                            modifier = Modifier
                                .background(Color(0xFF2ECC71), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                }
                Spacer(Modifier.height(3.dp))
                Text(
                    text = desc,
                    fontSize = 13.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${mode.rateInt} Hz",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = MiuixTheme.colorScheme.primary,
                )
                Text(
                    text = if (isFallback) "·" else "${mode.width}×${mode.height}",
                    fontSize = 11.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        }
    }
}