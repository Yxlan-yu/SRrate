package com.yxlanyu.refreshrate.ui.screens

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yxlanyu.refreshrate.R
import com.yxlanyu.refreshrate.ui.components.RefreshPageScaffold
import kotlin.math.abs
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

private const val REFRESH_MS = 500L
private const val MAX_FRAMES = 30

@Composable
fun MonitorScreen(outerContentPadding: PaddingValues) {
    val context = LocalContext.current
    val activity = context as? Activity
    val fpsState = remember { mutableIntStateOf(0) }
    val fps = fpsState.intValue

    val onFrame = remember {
        val intervals = java.util.ArrayDeque<Float>()
        var prev = 0L
        var lastShown = 0L
        android.view.Window.OnFrameMetricsAvailableListener { _, _, dropCount ->
            val now = SystemClock.elapsedRealtime()
            if (prev != 0L) {
                val gap = (now - prev).toFloat()
                val frames = (1 + dropCount).toFloat().coerceAtLeast(1f)
                intervals.addLast(gap / frames)
                while (intervals.size > MAX_FRAMES) {
                    intervals.removeFirst()
                }
            }
            prev = now
            if (now - lastShown >= REFRESH_MS) {
                if (intervals.isNotEmpty()) {
                    var sum = 0f
                    for (v in intervals) sum += v
                    val avgMs = sum / intervals.size
                    val computed = if (avgMs > 0f) (1000f / avgMs).toInt().coerceIn(0, 240) else 0
                    if (computed != fpsState.intValue) fpsState.intValue = computed
                }
                lastShown = now
            }
        }
    }

    DisposableEffect(activity) {
        val a = activity
        if (a != null) {
            a.window.addOnFrameMetricsAvailableListener(onFrame, Handler(Looper.getMainLooper()))
        }
        onDispose {
            a?.window?.removeOnFrameMetricsAvailableListener(onFrame)
        }
    }

    RefreshPageScaffold(
        title = stringResource(R.string.monitor_title),
        outerContentPadding = outerContentPadding,
        largeTitle = stringResource(R.string.monitor_title),
    ) {
        item(key = "rect_fps") {
            RectangleFpsCard(
                fps = fps,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 24.dp),
            )
        }
    }
}

@Composable
private fun RectangleFpsCard(fps: Int, modifier: Modifier = Modifier) {
    Card(
        cornerRadius = 26.dp,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start,
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = if (fps > 0) fps.toString() else "--",
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        color = MiuixTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Hz",
                        fontSize = 14.sp,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        modifier = Modifier.padding(bottom = 7.dp),
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.monitor_current),
                    fontSize = 12.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
            Spacer(Modifier.width(10.dp))
            FpsRing(modifier = Modifier.size(72.dp))
        }
    }
}

@Composable
private fun FpsRing(modifier: Modifier) {
    val accent = MiuixTheme.colorScheme.primary
    val transition = rememberInfiniteTransition(label = "fps_ring")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "rotation",
    )
    Canvas(modifier) {
        val stroke = 5.dp.toPx()
        val inset = stroke / 2f
        val arcSize = Size(size.width - stroke, size.height - stroke)
        drawArc(
            color = accent.copy(alpha = 0.18f),
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = arcSize,
            style = Stroke(stroke, cap = StrokeCap.Round),
        )
        val seg = 12f
        val steps = 10
        val half = steps / 2
        for (i in 0 until steps) {
            val offset = i - half
            val angle = rotation + offset * seg
            val a = (1f - abs(offset) / half.toFloat()) * 0.75f + 0.25f
            drawArc(
                color = accent.copy(alpha = a.coerceIn(0.25f, 1f)),
                startAngle = angle,
                sweepAngle = seg,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(stroke, cap = StrokeCap.Round),
            )
        }
        drawCircle(
            color = accent,
            radius = 5.dp.toPx(),
            center = center,
        )
    }
}