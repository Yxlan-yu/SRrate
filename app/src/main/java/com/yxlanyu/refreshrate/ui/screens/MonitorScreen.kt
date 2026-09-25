package com.yxlanyu.refreshrate.ui.screens

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yxlanyu.refreshrate.R
import com.yxlanyu.refreshrate.model.DisplayMode
import com.yxlanyu.refreshrate.ui.components.RefreshPageScaffold
import com.yxlanyu.refreshrate.util.AutoOverclockManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

private const val HISTORY_POINTS = 120
private const val SAMPLE_MS = 500L

@Composable
fun MonitorScreen(outerContentPadding: PaddingValues) {
    val context = LocalContext.current
    var rate by remember { mutableStateOf(0f) }
    var res by remember { mutableStateOf("--") }
    var history by remember { mutableStateOf(listOf<Float>()) }
    var modes by remember { mutableStateOf<List<Any>>(emptyList()) }
    var activeModeId by remember { mutableIntStateOf(-1) }
    var subtitle by remember { mutableStateOf("") }

    fun sample(pushHistory: Boolean) {
        val r = AutoOverclockManager.getCurrentRate(context)
        val s = AutoOverclockManager.getCurrentResolution(context)
        if (r != rate) rate = r
        if (s != res) res = s
        if (pushHistory && r > 0f) {
            history = (history + r).takeLast(HISTORY_POINTS)
        }
        val a = buildActiveModeId(context)
        if (a != activeModeId) activeModeId = a
        val sub = buildSubtitle(context)
        if (sub != subtitle) subtitle = sub
    }

    LaunchedEffect(Unit) {
        val list = withContext(Dispatchers.IO) {
            buildSortedList(context, AutoOverclockManager.getSupportedModes(context).ifEmpty { null })
        }
        modes = list
        sample(false)
        while (true) {
            delay(SAMPLE_MS)
            sample(true)
        }
    }

    RefreshPageScaffold(
        title = stringResource(R.string.monitor_title),
        outerContentPadding = outerContentPadding,
        largeTitle = stringResource(R.string.monitor_title),
        subtitle = if (subtitle.isEmpty()) "-" else subtitle,
    ) {
        item(key = "big_rate") {
            BigRateCard(rate = rate, res = res, context = context)
        }
        item(key = "history") {
            HistoryCard(points = history)
        }
        if (modes.isNotEmpty()) {
            item(key = "modes_title") {
                SmallTitle(
                    text = stringResource(R.string.monitor_modes_title),
                    insideMargin = PaddingValues(28.dp, 8.dp),
                )
            }
            modes.forEach { item ->
                when (item) {
                    is String -> item(key = item) {
                        SmallTitle(
                            text = item,
                            insideMargin = PaddingValues(28.dp, 8.dp),
                        )
                    }
                    is DisplayMode -> {
                        val isCurrent = if (item.modeId >= 0) item.modeId == activeModeId else false
                        item(key = itemKey(item)) {
                            MonitorModeCard(mode = item, isCurrent = isCurrent)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BigRateCard(rate: Float, res: String, context: Context) {
    val rateName = DisplayMode(0, 0, rate, -1).getRateName(context)
    val hz = rate.toInt()
    Card(
        cornerRadius = 16.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp, 14.dp, 14.dp, 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = hz.toString(),
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = MiuixTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Hz",
                    fontSize = 20.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    modifier = Modifier.padding(bottom = 10.dp),
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = rateName,
                fontSize = 15.sp,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = res,
                fontSize = 14.sp,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
    }
}

@Composable
private fun HistoryCard(points: List<Float>) {
    Card(
        cornerRadius = 16.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp, 14.dp, 14.dp, 0.dp),
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                text = stringResource(R.string.monitor_history_title),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MiuixTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(10.dp))
            val gridColor = MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.18f)
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
            ) {
                val h = size.height
                val w = size.width
                for (i in 1..4) {
                    val y = h * i / 5f
                    drawLine(gridColor, androidx.compose.ui.geometry.Offset(0f, y), androidx.compose.ui.geometry.Offset(w, y), 1f)
                }
                if (points.size < 2) return@Canvas
                var minR = points.minOrNull() ?: 0f
                var maxR = points.maxOrNull() ?: 0f
                if (maxR - minR < 1f) {
                    minR -= 0.5f
                    maxR += 0.5f
                }
                val span = maxR - minR
                val path = Path()
                val n = points.size
                points.forEachIndexed { i, v ->
                    val x = w * i / (n - 1)
                    val y = h - (v - minR) / span * h
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(
                    path = path,
                    color = Color(0xFF3B76FD),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                )
                val last = points.last()
                val lx = w
                val ly = h - (last - minR) / span * h
                drawCircle(Color(0xFF3B76FD), radius = 4.dp.toPx(), center = androidx.compose.ui.geometry.Offset(lx, ly))
            }
        }
    }
}

@Composable
private fun MonitorModeCard(mode: DisplayMode, isCurrent: Boolean) {
    val ctx = LocalContext.current
    val name = mode.getRateName(ctx)
    val desc = mode.getRateDesc(ctx)
    Card(
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
                                .background(Color(0xFF2ECC71), RoundedCornerShape(8.dp))
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
            Text(
                text = "${mode.getResolutionLabel()} · ${mode.getRateInt()}Hz",
                fontSize = 13.sp,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
    }
}