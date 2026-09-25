package com.yxlanyu.refreshrate.ui.screens

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

private const val SAMPLE_WINDOW_MS = 500L

@Composable
fun MonitorScreen(outerContentPadding: PaddingValues) {
    val context = LocalContext.current
    val activity = context as? Activity
    val fpsState = remember { mutableIntStateOf(0) }
    val fps = fpsState.intValue

    val onFrame = remember {
        val queue = java.util.ArrayDeque<Long>()
        android.view.Window.OnFrameMetricsAvailableListener { _, _, _ ->
            val now = SystemClock.elapsedRealtime()
            queue.addLast(now)
            while (queue.size > 1 && now - queue.peekFirst() > SAMPLE_WINDOW_MS) {
                queue.removeFirst()
            }
            if (queue.size >= 2) {
                val span = now - queue.peekFirst()
                if (span > 0) {
                    val computed = (queue.size * 1000f / span).toInt().coerceIn(0, 240)
                    if (computed != fpsState.intValue) fpsState.intValue = computed
                }
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
        item(key = "round_fps") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                RoundFpsCard(fps = fps)
            }
        }
    }
}

@Composable
private fun RoundFpsCard(fps: Int) {
    Card(
        cornerRadius = 108.dp,
        modifier = Modifier.size(216.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End,
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = if (fps > 0) fps.toString() else "--",
                        fontSize = 46.sp,
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
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.monitor_current),
                    fontSize = 12.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
            Spacer(Modifier.width(6.dp))
            FpsSpinner(modifier = Modifier.size(64.dp))
        }
    }
}

@Composable
private fun FpsSpinner(modifier: Modifier) {
    val accent = MiuixTheme.colorScheme.primary
    val transition = rememberInfiniteTransition(label = "fps_spinner")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "rotation",
    )
    val sweep by transition.animateFloat(
        initialValue = 40f,
        targetValue = 270f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "sweep",
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
        drawArc(
            color = accent,
            startAngle = rotation - sweep / 2f,
            sweepAngle = sweep,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = arcSize,
            style = Stroke(stroke, cap = StrokeCap.Round),
        )
        drawCircle(
            color = accent,
            radius = 5.dp.toPx(),
            center = center,
        )
    }
}