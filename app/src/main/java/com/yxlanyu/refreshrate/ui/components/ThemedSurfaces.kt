package com.yxlanyu.refreshrate.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.theme.MiuixTheme

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

private const val GlassBaseAlpha = 0.78f
private const val ShadowRadiusDp = 10f
private const val EdgeLineDp = 1f
private const val InnerShadowDepthDp = 10f

/**
 * 玻璃卡片表面：与底栏「高级材质」同构的纯绘制实现。
 *
 * 保留底栏的容器底色 / 高光描边 / 外投影 / 内阴影四项，去掉真模糊与透镜
 * （MIUI libhwui 的 MiBackgroundBlurBlend 会让 RenderThread 原生崩溃，详见文件头说明）。
 *
 * 边缘工艺全部用渐变手绘，不依赖 Modifier.innerShadow / RenderEffect：
 *   * 底色   surfaceContainer @ 78%（叠在纯色页面上）
 *   * 内阴影 上下左右四条渐变，模拟玻璃板的厚度
 *   * 高光   顶部 1dp 亮线 + 底部 1dp 弱线 + 1dp 内缩描边
 *   * 外投影 10dp / 黑 10%（深色 20%），与底栏一致
 *
 * 卡片自身的 CardColors 需设为透明，由本 modifier 负责底色绘制。
 */
@Composable
fun Modifier.themedAcrylicCard(cornerRadius: Dp): Modifier {
    val dark = isSystemInDarkTheme()
    val container = MiuixTheme.colorScheme.surfaceContainer
    val shape = remember(cornerRadius) { RoundedCornerShape(cornerRadius) }
    val baseTint = remember(container) { container.copy(alpha = GlassBaseAlpha) }
    val shadowAlpha = if (dark) 0.20f else 0.10f
    val innerAlpha = if (dark) 0.30f else 0.14f
    val rimAlpha = if (dark) 0.13f else 0.22f
    val topLineAlpha = if (dark) 0.24f else 0.62f
    val bottomLineAlpha = if (dark) 0.07f else 0.18f
    return this
        .dropShadow(
            shape = shape,
            shadow = Shadow(radius = ShadowRadiusDp.dp, color = Color.Black, alpha = shadowAlpha),
        )
        .clip(shape)
        .drawBehind {
            val line = EdgeLineDp.dp.toPx()
            val depth = InnerShadowDepthDp.dp.toPx()
            val radius = cornerRadius.toPx()

            drawRect(color = baseTint)

            val inner = Color.Black.copy(alpha = innerAlpha)
            drawRect(
                brush = Brush.verticalGradient(listOf(inner, Color.Transparent), endY = depth),
                size = Size(size.width, depth),
            )
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, inner),
                    startY = size.height - depth,
                    endY = size.height,
                ),
                size = Size(size.width, depth),
                topLeft = Offset(0f, size.height - depth),
            )
            drawRect(
                brush = Brush.horizontalGradient(listOf(inner, Color.Transparent), endX = depth),
                size = Size(depth, size.height),
            )
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, inner),
                    startX = size.width - depth,
                    endX = size.width,
                ),
                size = Size(depth, size.height),
                topLeft = Offset(size.width - depth, 0f),
            )

            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.White.copy(alpha = topLineAlpha), Color.Transparent),
                    endY = line,
                ),
                size = Size(size.width, line),
            )
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.White.copy(alpha = bottomLineAlpha)),
                    startY = size.height - line,
                    endY = size.height,
                ),
                size = Size(size.width, line),
                topLeft = Offset(0f, size.height - line),
            )

            val rimRadius = (radius - line / 2f).coerceAtLeast(0f)
            drawRoundRect(
                color = Color.White.copy(alpha = rimAlpha),
                topLeft = Offset(line / 2f, line / 2f),
                size = Size(size.width - line, size.height - line),
                cornerRadius = CornerRadius(rimRadius, rimRadius),
                style = Stroke(width = line),
            )
        }
}
