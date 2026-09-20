package com.yxlanyu.refreshrate.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yxlanyu.refreshrate.R
import com.yxlanyu.refreshrate.adb.AdbConnectionManager
import com.yxlanyu.refreshrate.model.DisplayMode
import com.yxlanyu.refreshrate.service.RemoteKeepAliveService
import com.yxlanyu.refreshrate.ui.components.RefreshPageScaffold
import io.github.muntashirakon.adb.AbsAdbConnectionManager
import io.github.muntashirakon.adb.AdbStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.LinkedHashMap
import java.util.LinkedHashSet
import java.util.regex.Matcher
import java.util.regex.Pattern

private val GREEN = Color(0xFF2ECC71)
private val AMBER = Color(0xFFFFB300)
private val RED = Color(0xFFE57373)
private const val MAX_DEV = 3

private class RemoteDev(
    val ip: String,
    val port: String,
) {
    var model: String = ""
    var dump: String = ""
    var devInfo: String = ""
    var mgr: AbsAdbConnectionManager? = null
}

@Composable
fun RemoteScreen(outerContentPadding: PaddingValues) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("s", Context.MODE_PRIVATE)
    val scope = rememberCoroutineScope()

    var wirelessMode by remember { mutableStateOf(true) }
    var ip by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("") }
    var pairPort by remember { mutableStateOf("") }
    var pairCode by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(context.getString(R.string.remote_status_idle)) }
    var statusColor by remember { mutableStateOf(AMBER) }
    var busy by remember { mutableStateOf(false) }
    var blurIp by remember { mutableStateOf(prefs.getBoolean("remote_blur_ip", false)) }
    var hub by remember { mutableStateOf(listOf<RemoteDev>()) }
    var activeIdx by remember { mutableStateOf(-1) }
    var deviceName by remember { mutableStateOf("") }
    var remoteModes by remember { mutableStateOf(listOf<Any>()) }
    var history by remember { mutableStateOf(listOf<String>()) }

    fun refreshHistory() {
        val raw = prefs.getString("remote_history", "")
        history = if (raw.isNullOrEmpty()) emptyList() else raw.split(";").filter { it.isNotEmpty() }
    }

    fun setStatus(s: String, c: Color) {
        status = s
        statusColor = c
    }

    fun saveHistory(eip: String, eport: String) {
        val entry = "$eip,$eport"
        val raw = prefs.getString("remote_history", "")
        val list = mutableListOf(entry)
        if (!raw.isNullOrEmpty()) {
            for (e in raw.split(";")) {
                if (e.isNotEmpty() && e != entry && list.size < 6) list.add(e)
            }
        }
        prefs.edit().putString("remote_history", list.joinToString(";")).apply()
        refreshHistory()
    }

    fun deleteHistory(entry: String) {
        val raw = prefs.getString("remote_history", "")
        val list = mutableListOf<String>()
        if (!raw.isNullOrEmpty()) {
            for (e in raw.split(";")) if (e.isNotEmpty() && e != entry) list.add(e)
        }
        prefs.edit().putString("remote_history", list.joinToString(";")).apply()
        refreshHistory()
    }

    fun parseModes(dump: String): List<Any> {
        val p = Pattern.compile("id=(\\d+),\\s*width=(\\d+),\\s*height=(\\d+),\\s*fps=([0-9.]+)")
        val seen = LinkedHashSet<String>()
        val groups = LinkedHashMap<String, MutableList<DisplayMode>>()
        var sfIdx = 0
        var usedRecords = false
        for (line in dump.split("\n")) {
            if (!line.contains("DisplayModeRecord")) continue
            val m = p.matcher(line)
            if (m.find()) {
                usedRecords = true
                addMode(groups, seen, m, sfIdx)
                sfIdx++
            }
        }
        if (!usedRecords) {
            val m = p.matcher(dump)
            while (m.find()) addMode(groups, seen, m, -1)
        }
        if (groups.isEmpty()) return emptyList()
        val items = mutableListOf<Any>()
        for (en in groups) {
            val g = en.value.sortedByDescending { it.refreshRate }
            items.add(en.key)
            items.addAll(g)
        }
        return items
    }

    fun doPair() {
        if (!wirelessMode || busy) return
        val trimIp = ip.trim()
        val trimPairPort = pairPort.trim()
        val trimCode = pairCode.trim()
        if (trimIp.isEmpty() || trimPairPort.isEmpty() || trimCode.isEmpty()) {
            setStatus(context.getString(R.string.remote_need_pair_input), RED)
            return
        }
        busy = true
        setStatus(context.getString(R.string.remote_pairing), AMBER)
        scope.launch(Dispatchers.IO) {
            var ok = false
            var err: String? = null
            try {
                val m = AdbConnectionManager.newInstance(context)
                ok = m.pair(trimIp, Integer.parseInt(trimPairPort), trimCode)
            } catch (t: Throwable) {
                err = t.message?.toString()
            }
            val fok = ok
            val ferr = err
            withContext(Dispatchers.Main) {
                busy = false
                if (fok) setStatus(context.getString(R.string.remote_pair_ok), GREEN)
                else setStatus(context.getString(R.string.remote_pair_fail, ferr ?: ""), RED)
            }
        }
    }

    fun doConnect() {
        if (!wirelessMode || busy) return
        val trimIp = ip.trim()
        val trimPort = port.trim()
        if (trimIp.isEmpty() || trimPort.isEmpty()) {
            setStatus(context.getString(R.string.remote_status_need_input), RED)
            return
        }
        busy = true
        setStatus(context.getString(R.string.remote_conn_wait), AMBER)
        scope.launch(Dispatchers.IO) {
            var ok = false
            var err: String? = null
            var dump: String? = null
            var devInfo: String? = null
            var mgr: AbsAdbConnectionManager? = null
            try {
                mgr = AdbConnectionManager.newInstance(context)
                ok = mgr.connect(trimIp, Integer.parseInt(trimPort))
                if (ok && mgr != null) {
                    dump = exec(mgr, "dumpsys display")
                    devInfo = exec(mgr,
                        "getprop ro.product.marketname; getprop ro.vendor.oplus.market.name; getprop ro.product.model; getprop ro.build.version.release")
                }
            } catch (t: Throwable) {
                err = t.message?.toString()
            }
            val fok = ok
            val ferr = err
            val fm = mgr
            withContext(Dispatchers.Main) {
                busy = false
                if (fok) {
                    val hb = hub.toMutableList()
                    var slot = hb.indexOfFirst { it.ip == trimIp && it.port == trimPort }
                    if (slot < 0 && hb.size >= MAX_DEV) {
                        setStatus(context.getString(R.string.remote_max_devices), AMBER)
                        return@withContext
                    }
                    val d: RemoteDev
                    if (slot >= 0) {
                        d = hb[slot]
                        try { d.mgr?.close() } catch (ig: Throwable) {}
                        d.mgr = fm
                        activeIdx = slot
                    } else {
                        d = RemoteDev(trimIp, trimPort)
                        d.mgr = fm
                        hb.add(d)
                        activeIdx = hb.size - 1
                    }
                    d.dump = dump ?: ""
                    d.devInfo = devInfo ?: ""
                    d.model = parseName(d.devInfo)
                    hub = hb
                    saveHistory(trimIp, trimPort)
                    deviceName = parseDeviceName(d.devInfo)
                    remoteModes = parseModes(d.dump)
                    setStatus(if (blurIp) context.getString(R.string.remote_connected,
                        context.getString(R.string.remote_wireless_device))
                        else context.getString(R.string.remote_connected, trimIp), GREEN)
                    RemoteKeepAliveService.start(context)
                } else {
                    setStatus(context.getString(R.string.remote_conn_fail, ferr ?: ""), RED)
                }
            }
        }
    }

    fun selectDevice(idx: Int) {
        if (idx < 0 || idx >= hub.size) return
        activeIdx = idx
        val d = hub[idx]
        ip = d.ip
        port = d.port
        deviceName = parseDeviceName(d.devInfo)
        remoteModes = parseModes(d.dump)
        setStatus(if (blurIp) context.getString(R.string.remote_connected,
            context.getString(R.string.remote_wireless_device))
            else context.getString(R.string.remote_connected, d.ip), GREEN)
    }

    fun disconnectDevice(idx: Int) {
        if (idx < 0 || idx >= hub.size) return
        val hb = hub.toMutableList()
        try { hb[idx].mgr?.close() } catch (ig: Throwable) {}
        hb.removeAt(idx)
        if (hb.isEmpty()) {
            activeIdx = -1
            hub = emptyList()
            deviceName = ""
            remoteModes = emptyList()
            setStatus(context.getString(R.string.remote_status_idle), AMBER)
            RemoteKeepAliveService.stop(context)
        } else {
            hub = hb
            selectDevice(0)
        }
    }

    fun applyRemoteMode(mode: DisplayMode) {
        if (activeIdx < 0 || activeIdx >= hub.size) return
        val mgr = hub[activeIdx].mgr ?: return
        val rr = mode.rateInt
        setStatus(context.getString(R.string.remote_switching, rr), AMBER)
        scope.launch(Dispatchers.IO) {
            var active = -1
            var err: String? = null
            try {
                val setCmd = "cmd display set-user-preferred-display-mode ${mode.width} ${mode.height} $rr 2>/dev/null; " +
                        "settings put system peak_refresh_rate $rr.0; " +
                        "settings put system min_refresh_rate $rr.0; " +
                        "settings put system user_refresh_rate $rr; " +
                        "settings put secure miui_refresh_rate $rr; " +
                        "settings put system thermal_limit_refresh_rate $rr 2>/dev/null"
                var finalCmd = setCmd
                if (mode.sfIndex >= 0) finalCmd += "; service call SurfaceFlinger 1035 i32 ${mode.sfIndex}"
                exec(mgr, finalCmd)
                Thread.sleep(600)
                active = parseActiveFps(exec(mgr, "dumpsys display"))
            } catch (t: Throwable) {
                err = t.message?.toString()
            }
            val fa = active
            val ferr = err
            withContext(Dispatchers.Main) {
                when {
                    ferr != null -> setStatus(context.getString(R.string.remote_switch_fail, ferr), RED)
                    fa <= 0 -> setStatus(context.getString(R.string.remote_switch_sent), AMBER)
                    fa == rr -> setStatus(context.getString(R.string.remote_switch_ok, rr), GREEN)
                    else -> setStatus(context.getString(R.string.remote_switch_actual, fa), AMBER)
                }
            }
        }
    }

    RefreshPageScaffold(
        title = stringResource(R.string.nav_remote),
        outerContentPadding = outerContentPadding,
        largeTitle = stringResource(R.string.nav_remote),
        subtitle = stringResource(R.string.remote_hint),
    ) {
        item(key = "mode_tabs") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                ModeTab(
                    label = stringResource(R.string.remote_mode_wireless),
                    selected = wirelessMode,
                    modifier = Modifier.weight(1f),
                    onClick = { wirelessMode = true },
                )
                Spacer(Modifier.width(8.dp))
                ModeTab(
                    label = stringResource(R.string.remote_mode_usb),
                    selected = !wirelessMode,
                    modifier = Modifier.weight(1f),
                    onClick = { wirelessMode = false },
                )
            }
        }

        if (wirelessMode) {
            item(key = "pair_section") {
                SmallTitle(
                    text = stringResource(R.string.remote_section_pair),
                    insideMargin = PaddingValues(28.dp, 8.dp),
                )
            }
            item(key = "pair_card") {
                Card(
                    cornerRadius = 16.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp, 14.dp, 14.dp, 0.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp, 14.dp)) {
                        TextField(
                            value = ip,
                            onValueChange = { ip = it },
                            label = stringResource(R.string.remote_ip_hint),
                            useLabelAsPlaceholder = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.size(10.dp))
                        TextField(
                            value = pairPort,
                            onValueChange = { pairPort = it },
                            label = stringResource(R.string.remote_pair_port_hint),
                            useLabelAsPlaceholder = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.size(10.dp))
                        TextField(
                            value = pairCode,
                            onValueChange = { pairCode = it },
                            label = stringResource(R.string.remote_pair_hint),
                            useLabelAsPlaceholder = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.size(12.dp))
                        Button(
                            onClick = { doPair() },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(stringResource(R.string.remote_pair_btn))
                        }
                    }
                }
            }
        }

        item(key = "connect_section") {
            SmallTitle(
                text = stringResource(R.string.remote_section_connect),
                insideMargin = PaddingValues(28.dp, 8.dp),
            )
        }
        item(key = "connect_card") {
            Card(
                cornerRadius = 16.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp, 14.dp, 14.dp, 0.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp, 14.dp)) {
                    if (wirelessMode) {
                        TextField(
                            value = port,
                            onValueChange = { port = it },
                            label = stringResource(R.string.remote_port_hint),
                            useLabelAsPlaceholder = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.size(10.dp))
                    } else {
                        Text(
                            text = stringResource(R.string.remote_usb_hint),
                            fontSize = 13.sp,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                        Spacer(Modifier.size(12.dp))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = status,
                            fontSize = 12.sp,
                            color = statusColor,
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (wirelessMode) doConnect()
                                else Toast.makeText(context, R.string.remote_usb_hint, Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.width(96.dp),
                        ) {
                            Text(stringResource(R.string.remote_connect_btn))
                        }
                    }
                }
            }
        }

        if (deviceName.isNotEmpty()) {
            item(key = "device_section") {
                SmallTitle(
                    text = stringResource(R.string.remote_modes_title),
                    insideMargin = PaddingValues(28.dp, 8.dp),
                )
            }
            item(key = "device_name") {
                Text(
                    text = deviceName,
                    fontSize = 13.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp),
                )
            }
        }
        if (hub.isNotEmpty()) {
            item(key = "hub") {
                Column {
                    hub.forEachIndexed { idx, d ->
                        DeviceChip(
                            dev = d,
                            active = idx == activeIdx,
                            onClick = { selectDevice(idx) },
                            onDelete = { disconnectDevice(idx) },
                        )
                    }
                }
            }
        }
        if (remoteModes.isNotEmpty()) {
            items(remoteModes, key = { it.hashCode().toString() + remoteModes.indexOf(it) }) { item ->
                when (item) {
                    is String -> SmallTitle(
                        text = item,
                        insideMargin = PaddingValues(28.dp, 8.dp),
                    )
                    is DisplayMode -> RemoteRateCard(mode = item, onClick = { applyRemoteMode(item) })
                }
            }
        }

        item(key = "history_section") {
            if (history.isNotEmpty()) {
                SmallTitle(
                    text = stringResource(R.string.remote_history_title),
                    insideMargin = PaddingValues(28.dp, 8.dp),
                )
            }
        }
        items(history, key = { it }) { entry ->
            val parts = entry.split(",")
            if (parts.size >= 2) {
                HistoryRow(
                    ipx = parts[0],
                    portx = parts[1],
                    onClick = {
                        ip = parts[0]
                        port = parts[1]
                    },
                    onDelete = { deleteHistory(entry) },
                )
            }
        }
        if (history.isEmpty()) {
            item(key = "history_empty") {
                Text(
                    text = stringResource(R.string.remote_status_none),
                    fontSize = 13.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp),
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshHistory()
        if (hub.isNotEmpty() && activeIdx in hub.indices) {
            val d = hub[activeIdx]
            ip = d.ip
            port = d.port
            deviceName = parseDeviceName(d.devInfo)
            remoteModes = parseModes(d.dump)
            setStatus(if (blurIp) context.getString(R.string.remote_connected,
                context.getString(R.string.remote_wireless_device))
                else context.getString(R.string.remote_connected, d.ip), GREEN)
        }
    }
}

@Composable
private fun ModeTab(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .background(
                if (selected) MiuixTheme.colorScheme.primary
                else MiuixTheme.colorScheme.secondaryVariant,
                RoundedCornerShape(16.dp),
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) MiuixTheme.colorScheme.onPrimary
            else MiuixTheme.colorScheme.onSecondaryVariant,
        )
    }
}

@Composable
private fun DeviceChip(
    dev: RemoteDev,
    active: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .background(
                if (active) MiuixTheme.colorScheme.primary
                else MiuixTheme.colorScheme.secondaryVariant,
                RoundedCornerShape(16.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (dev.model.isEmpty()) dev.ip else dev.model,
            fontSize = 13.sp,
            color = if (active) MiuixTheme.colorScheme.onPrimary
            else MiuixTheme.colorScheme.onSecondaryVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = dev.ip + ":" + dev.port,
            fontSize = 12.sp,
            color = if (active) MiuixTheme.colorScheme.onPrimary
            else MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = "✕",
            fontSize = 15.sp,
            color = if (active) MiuixTheme.colorScheme.onPrimary else RED,
            modifier = Modifier.clickable(onClick = onDelete),
        )
    }
}

@Composable
private fun HistoryRow(
    ipx: String,
    portx: String,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .background(MiuixTheme.colorScheme.secondaryVariant, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "$ipx : $portx",
            fontSize = 14.sp,
            color = MiuixTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "✕",
            fontSize = 15.sp,
            color = RED,
            modifier = Modifier.clickable(onClick = onDelete),
        )
    }
}

@Composable
private fun RemoteRateCard(
    mode: DisplayMode,
    onClick: () -> Unit,
) {
    val ctx = LocalContext.current
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${mode.width} × ${mode.height}",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MiuixTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.size(3.dp))
                Text(
                    text = mode.getRateName(ctx) + "  ·  ${mode.rateInt} Hz",
                    fontSize = 13.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                Text(
                    text = if (mode.sfIndex >= 0) "idx=${mode.sfIndex}" else "",
                    fontSize = 11.sp,
                    color = Color(0x66888888),
                )
            }
        }
    }
}

private fun parseName(raw: String): String {
    if (raw.isEmpty()) return ""
    val l = raw.split("\n").map { it.trim() }
    return when {
        l.isNotEmpty() && l[0].isNotEmpty() -> l[0]
        l.size > 1 && l[1].isNotEmpty() -> l[1]
        l.size > 2 && l[2].isNotEmpty() -> l[2]
        else -> ""
    }
}

private fun parseDeviceName(raw: String): String {
    if (raw.isEmpty()) return ""
    val l = raw.split("\n").map { it.trim() }
    val name = when {
        l.isNotEmpty() && l[0].isNotEmpty() -> l[0]
        l.size > 1 && l[1].isNotEmpty() -> l[1]
        l.size > 2 && l[2].isNotEmpty() -> l[2]
        else -> ""
    }
    if (name.isEmpty()) return ""
    val ver = if (l.size > 3) l[3] else ""
    return if (ver.isNotEmpty()) "$name  ·  Android $ver" else name
}

private fun exec(m: AbsAdbConnectionManager, cmd: String): String {
    val stream: AdbStream = m.openStream("shell:$cmd")
    val bos = ByteArrayOutputStream()
    try {
        val is: InputStream = stream.openInputStream()
        val buf = ByteArray(4096)
        while (true) {
            val n = is.read(buf)
            if (n < 0) break
            bos.write(buf, 0, n)
        }
    } catch (e: Exception) {
    } finally {
        try { stream.close() } catch (ig: Exception) {}
    }
    return String(bos.toByteArray())
}

private fun addMode(
    groups: LinkedHashMap<String, MutableList<DisplayMode>>,
    seen: LinkedHashSet<String>,
    m: Matcher,
    sfIndex: Int,
) {
    val id = Integer.parseInt(m.group(1))
    val w = Integer.parseInt(m.group(2))
    val h = Integer.parseInt(m.group(3))
    val fps = Float.parseFloat(m.group(4))
    val key = "${w}x${h}@${Math.round(fps)}"
    if (!seen.add(key)) return
    val dm = DisplayMode(w, h, fps, id)
    if (sfIndex >= 0) dm.setSfIndex(sfIndex)
    val res = "$w × $h"
    groups.getOrPut(res) { mutableListOf() }.add(dm)
}

private fun parseActiveFps(dump: String): Int {
    if (dump.isEmpty()) return -1
    try {
        val a = Pattern.compile("mActiveModeId=(\\d+)").matcher(dump)
        if (a.find()) {
            val id = Integer.parseInt(a.group(1))
            val f = Pattern.compile("id=$id, width=\\d+, height=\\d+, fps=([0-9.]+)").matcher(dump)
            if (f.find()) return Math.round(Float.parseFloat(f.group(1)))
        }
    } catch (e: Exception) {
    }
    try {
        val r = Pattern.compile("refreshRate=?\\s?([0-9]+(?:\\.[0-9]+)?)").matcher(dump)
        if (r.find()) return Math.round(Float.parseFloat(r.group(1)))
    } catch (e: Exception) {
    }
    return -1
}