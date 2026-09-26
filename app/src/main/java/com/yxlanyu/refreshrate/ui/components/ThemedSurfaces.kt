package com.yxlanyu.refreshrate.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.math.sqrt

/**
 * 上檐渐进遮罩（CZeroX ProgressiveBlur 曲线的纯绘制实现）。
 *
 * 复刻 CZeroX v1.2.7 ProgressiveBlur AGSL 的固定参数解：band=(0,1)、curve=1、level=1、slope=1，
 * 取 S(x)=x^2(3-2x)，权重 w=S(1-S(raw))，raw 沿上檐高度自上而下 0..1。
 * 下方按 0.05 步长列出该曲线采样值（顶部 1.0，最底部 0.0）。
 *
 * 注意：本文件刻意不使用任何 RenderEffect / RuntimeShader / backdrop 模糊。
 * MIUI（HyperOS）的 libhwui 在处理带模糊的 RenderNode 时会在
 * `skiapipeline::MiBackgroundBlurBlend::preUpdateInfo` 内发生 SIGSEGV（RenderThread 原生崩溃，
 * Java 层无法捕获）。因此上檐改为「不透明顶栏 + CZeroX 曲线渐隐」的渐变遮罩：
 * 曲线形状与原 AGSL 完全一致，滚动内容依旧自上而下渐进溶入顶栏，只是少了像素模糊本身。
 */
private val CzeroXRawStops = floatArrayOf(
    0.00f, 0.05f, 0.10f, 0.15f, 0.20f, 0.25f, 0.30f, 0.35f, 0.40f, 0.45f, 0.50f,
    0.55f, 0.60f, 0.65f, 0.70f, 0.75f, 0.80f, 0.85f, 0.90f, 0.95f, 1.00f,
)

private val CzeroXWeights = floatArrayOf(
    1.000f, 1.000f, 0.998f, 0.989f, 0.970f, 0.934f, 0.880f, 0.807f, 0.716f, 0.611f, 0.500f,
    0.389f, 0.285f, 0.193f, 0.120f, 0.066f, 0.030f, 0.011f, 0.002f, 0.000f, 0.000f,
)

/** 顶栏顶端的最大遮罩强度，略低于 1 以保留一丝通透感。 */
private const val EaveTopAlpha = 0.95f

/**
 * 上檐：顶栏背景需为 [androidx.compose.ui.graphics.Color.Transparent]，
 * 本 modifier 自上而下刷出 CZeroX 曲线遮罩（顶端近乎不透明，底端完全透明）。
 */
@Composable
fun Modifier.themedEave(): Modifier {
    val surface = MiuixTheme.colorScheme.surface
    val stops = remember(surface) {
        CzeroXRawStops.indices.map { i ->
            CzeroXRawStops[i] to surface.copy(alpha = CzeroXWeights[i] * EaveTopAlpha)
        }
    }
    val brush = remember(stops) { Brush.verticalGradient(*stops.toTypedArray()) }
    return this.drawBehind { drawRect(brush = brush) }
}

private const val AcrylicBaseAlpha = 0.85f
private const val ShadowRadiusDp = 16f
private const val HighlightHeightDp = 2f

private val LightBloomAlphas = listOf(
    0.00f to 0.48f, 0.30f to 0.34f, 0.55f to 0.22f, 0.78f to 0.14f, 1.00f to 0.09f,
)

private val DarkBloomAlphas = listOf(
    0.00f to 0.62f, 0.30f to 0.46f, 0.55f to 0.30f, 0.78f to 0.19f, 1.00f to 0.12f,
)

/**
 * 亚克力卡片表面：中性底 + 主题色中心径向晕染 + 顶部高光边 + 中心柔影。
 *
 * 径向渐变使用 farthest-side 语义（半径取半对角线）且所有 stop 均保留非零 alpha，
 * 保证主题色铺满整卡、中心浓边缘淡但不回白；整体裁剪在圆角内，不会溢出卡外。
 * 卡片自身的 CardColors 需设为透明，由本 modifier 负责底色绘制。
 *
 * 底色不透明度由 0.62 提升到 0.85：原值依赖背后的模糊层提供实体感，去掉模糊后
 * 需要更实的底色才能维持同样的观感。
 */
@Composable
fun Modifier.themedAcrylicCard(cornerRadius: Dp): Modifier {
    val dark = isSystemInDarkTheme()
    val container = MiuixTheme.colorScheme.surfaceContainer
    val primary = MiuixTheme.colorScheme.primary
    val shape = remember(cornerRadius) { RoundedCornerShape(cornerRadius) }
    val baseTint = remember(container) { container.copy(alpha = AcrylicBaseAlpha) }
    val bloomStops = remember(primary, dark) {
        val alphas = if (dark) DarkBloomAlphas else LightBloomAlphas
        alphas.map { (pos, alpha) -> pos to primary.copy(alpha = alpha) }
    }
    val edgeAlpha = if (dark) 0.10f else 0.55f
    val shadowAlpha = if (dark) 0.38f else 0.10f
    return this
        .dropShadow(
            shape = shape,
            shadow = Shadow(radius = ShadowRadiusDp.dp, color = Color.Black, alpha = shadowAlpha),
        )
        .clip(shape)
        .drawBehind {
            val highlightHeight = HighlightHeightDp.dp.toPx()
            drawRect(color = baseTint)
            drawRect(
                brush = Brush.radialGradient(
                    *bloomStops.toTypedArray(),
                    center = Offset(size.width / 2f, size.height / 2f),
                    radius = sqrt(size.width * size.width + size.height * size.height) / 2f,
                ),
            )
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.White.copy(alpha = edgeAlpha), Color.Transparent),
                    endY = highlightHeight,
                ),
                size = Size(size.width, highlightHeight),
            )
        }
}
