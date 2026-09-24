package com.yxlanyu.refreshrate.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yxlanyu.refreshrate.R
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * HSV 选色板（色相条 + SV 二维面板 + hex 预览 + 预置色推荐）。
 * 用于替代旧的纯 8 色网格，支持任意颜色。
 */
@Composable
fun HsvColorPicker(
    initialColor: Int,
    onConfirm: (Int) -> Unit,
    onCancel: () -> Unit,
) {
    // 用初始颜色初始化 HSV 状态
    val initHue = Color(initialColor).toHsv().first
    val initSat = Color(initialColor).toHsv().second
    val initVal = Color(initialColor).toHsv().third
    var hue by remember { mutableStateOf(initHue) }
    var sat by remember { mutableStateOf(initSat) }
    var value by remember { mutableStateOf(initVal) }

    // 当前选中色（HSV → RGB）
    val currentColor = Color.hsv(hue, sat, value)
    val currentArgb = currentColor.toArgb()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        // SV 二维面板
        SvPanel(
            hue = hue,
            sat = sat,
            value = value,
            onSatValueChange = { s, v ->
                sat = s
                value = v
            },
        )
        Spacer(Modifier.height(16.dp))
        // 色相条
        HueBar(
            hue = hue,
            onHueChange = { hue = it },
        )
        Spacer(Modifier.height(16.dp))

        // hex 显示 + 预置推荐
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(currentColor),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = hexString(currentArgb),
                fontSize = 13.sp,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
        Spacer(Modifier.height(10.dp))
        // 预置 8 色
        THEME_COLORS.chunked(4).forEach { rowColors ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
            ) {
                rowColors.forEach { c ->
                    val selected = currentArgb == c
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(c))
                            .border(
                                2.dp,
                                if (selected) MiuixTheme.colorScheme.surface else Color.Transparent,
                                CircleShape,
                            )
                            .clickable {
                                hue = Color(c).toHsv().first
                                sat = Color(c).toHsv().second
                                value = Color(c).toHsv().third
                            },
                    )
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(top = 4.dp),
            thickness = 1.dp,
            color = MiuixTheme.colorScheme.dividerLine,
        )

        // 底部按钮
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End,
        ) {
            TextButton(
                text = stringResource(R.string.settings_color_cancel),
                onClick = onCancel,
            )
            Spacer(Modifier.width(8.dp))
            TextButton(
                text = stringResource(R.string.settings_color_confirm),
                onClick = { onConfirm(currentArgb) },
            )
        }
    }
}

/**
 * SV 二维面板（Saturation 横轴，Value 纵轴）。
 */
@Composable
private fun SvPanel(
    hue: Float,
    sat: Float,
    value: Float,
    onSatValueChange: (Float, Float) -> Unit,
) {
    val hueColor = Color.hsv(hue, 1f, 1f)
    val thumbRadius = 9.dp
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color.White, hueColor),
                ),
            )
            .drawBehind {
                // 覆盖一层黑色渐变（从上到下透明 → 到黑），实现 Value 轴
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black),
                    ),
                )
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val x = change.position.x / size.width
                    val y = change.position.y / size.height
                    onSatValueChange(
                        x.coerceIn(0f, 1f),
                        (1f - y).coerceIn(0f, 1f),
                    )
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { pos ->
                    onSatValueChange(
                        (pos.x / size.width).coerceIn(0f, 1f),
                        (1f - pos.y / size.height).coerceIn(0f, 1f),
                    )
                }
            },
    ) {
        // 游标
        val thumbX = sat * size.width
        val thumbY = (1f - value) * size.height
        if (size.width > 0 && size.height > 0) {
            androidx.compose.foundation.Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {},
            ) {
                drawCircle(
                    color = Color.White,
                    radius = thumbRadius.toPx(),
                    center = Offset(thumbX, thumbY),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()),
                )
                drawCircle(
                    color = Color.Black,
                    radius = 2.dp.toPx(),
                    center = Offset(thumbX, thumbY),
                )
            }
        }
    }
}

/**
 * 色相条（横向渐变，从 0° 到 360°）。
 */
@Composable
private fun HueBar(
    hue: Float,
    onHueChange: (Float) -> Unit,
) {
    val thumbX = hue / 360f
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(22.dp)
            .clip(CircleShape)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color.hsv(0f, 1f, 1f),
                        Color.hsv(60f, 1f, 1f),
                        Color.hsv(120f, 1f, 1f),
                        Color.hsv(180f, 1f, 1f),
                        Color.hsv(240f, 1f, 1f),
                        Color.hsv(300f, 1f, 1f),
                        Color.hsv(360f, 1f, 1f),
                    ),
                ),
            )
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    onHueChange((change.position.x / size.width).coerceIn(0f, 1f) * 360f)
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { pos ->
                    onHueChange((pos.x / size.width).coerceIn(0f, 1f) * 360f)
                }
            },
    ) {
        // 游标（按实际宽度定位）
        Canvas(modifier = Modifier.fillMaxSize()) {
            val x = thumbX * size.width
            val cy = size.height / 2
            drawCircle(
                color = Color.White,
                radius = 11.dp.toPx(),
                center = Offset(x, cy),
            )
            drawCircle(
                color = Color.hsv(hue, 1f, 1f),
                radius = 7.dp.toPx(),
                center = Offset(x, cy),
            )
        }
    }
}

/** 颜色转 hex 字符串，如 #3B76FD */
private fun hexString(color: Int): String {
    val rgb = color and 0xFFFFFF
    return String.format("#%06X", rgb)
}

/** Color 转 HSV（FloatArray 三元组） */
private fun Color.toHsv(): FloatArray {
    val out = floatArrayOf(0f, 0f, 0f)
    android.graphics.Color.colorToHSV(toArgb(), out)
    return out
}
