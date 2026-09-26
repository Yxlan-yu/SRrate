package com.yxlanyu.refreshrate.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.RoundedCornerShape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurDefaults
import top.yukonga.miuix.kmp.blur.drawBackdrop
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.textureBlurEffect
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.math.sqrt

/**
 * 当前页面记录的背景层，由 [RefreshPageScaffold] 提供。
 * 不支持运行时着色器时为 null，此时上檐与卡片自动退回纯色方案。
 */
val LocalThemedBackdrop = staticCompositionLocalOf<Backdrop?> { null }

@Composable
fun ProvideThemedBackdrop(backdrop: Backdrop?, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalThemedBackdrop provides backdrop, content = content)
}

/**
 * CZeroX 上檐渐进遮罩。
 *
 * 复刻 CZeroX v1.2.7 ProgressiveBlur AGSL 的固定参数解：band=(0,1)、curve=1、level=1、slope=1，
 * 取 S(x)=x^2(3-2x)，权重 w=S(1-S(raw))，raw 沿上檐高度自上而下 0..1。
 * 下方按 0.05 步长列出该曲线采样值（顶部 1.0，最底部 0.0），仅 alpha 参与 DstIn 遮罩。
 */
private val CzeroXEaveMaskStops: Array<Pair<Float, Color>> = arrayOf(
    0.00f to 1.000f, 0.05f to 1.000f, 0.10f to 0.998f, 0.15f to 0.989f, 0.20f to 0.970f,
    0.25f to 0.934f, 0.30f to 0.880f, 0.35f to 0.807f, 0.40f to 0.716f, 0.45f to 0.611f,
    0.50f to 0.500f, 0.55f to 0.389f, 0.60f to 0.285f, 0.65f to 0.193f, 0.70f to 0.120f,
    0.75f to 0.066f, 0.80f to 0.030f, 0.85f to 0.011f, 0.90f to 0.002f, 0.95f to 0.000f,
    1.00f to 0.000f,
).map { (pos, weight) -> pos to Color.Black.copy(alpha = weight) }.toTypedArray()

private val CzeroXEaveMaskBrush: Brush = Brush.verticalGradient(*CzeroXEaveMaskStops)

/**
 * 上檐渐进模糊：20dp 模糊 + surface 45% 薄染，再按 CZeroX 曲线把模糊层向上渐隐。
 * 调用方需在不支持运行时着色器时改用不透明顶栏色。
 */
@Composable
fun Modifier.themedEave(backdrop: Backdrop): Modifier {
    val tint = MiuixTheme.colorScheme.surface.copy(alpha = 0.45f)
    val colors = BlurDefaults.blurColors(blendColors = listOf(BlendColorEntry(color = tint)))
    return this.drawBackdrop(
        backdrop = backdrop,
        shape = { RectangleShape },
        effects = { textureBlurEffect(blurRadiusX = 20f, colors = colors) },
        onDrawBackdrop = { drawLayer ->
            drawLayer()
            drawRect(brush = CzeroXEaveMaskBrush, blendMode = BlendMode.DstIn)
        },
    )
}

private const val AcrylicBaseAlpha = 0.62f
private const val FrostBlurDp = 16f
private const val FrostSaturation = 1.35f
private const val ShadowRadiusDp = 16f
private const val HighlightHeightDp = 2f

private val LightBloomAlphas = listOf(
    0.00f to 0.48f, 0.30f to 0.34f, 0.55f to 0.22f, 0.78f to 0.14f, 1.00f to 0.09f,
)

private val DarkBloomAlphas = listOf(
    0.00f to 0.62f, 0.30f to 0.46f, 0.55f to 0.30f, 0.78f to 0.19f, 1.00f to 0.12f,
)

/**
 * 亚克力卡片表面：磨砂背景 + 62% 中性底 + 主题色中心径向晕染 + 顶部高光边 + 中心柔影。
 *
 * 径向渐变使用 farthest-side 语义（半径取半对角线）且所有 stop 均保留非零 alpha，
 * 保证主题色铺满整卡、中心浓边缘淡但不回白；整体裁剪在圆角内，不会溢出卡外。
 * 卡片自身的 CardColors 需设为透明，由本 modifier 负责底色绘制。
 */
@Composable
fun Modifier.themedAcrylicCard(cornerRadius: Dp): Modifier {
    val dark = isSystemInDarkTheme()
    val backdrop = LocalThemedBackdrop.current
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
    val frost = if (backdrop != null && isRuntimeShaderSupported()) {
        val colors = BlurDefaults.blurColors(saturation = FrostSaturation)
        Modifier.drawBackdrop(
            backdrop = backdrop,
            shape = { shape },
            effects = { textureBlurEffect(blurRadiusX = FrostBlurDp, colors = colors) },
        )
    } else {
        Modifier
    }
    return this
        .dropShadow(shape, Shadow(radius = ShadowRadiusDp.dp, color = Color.Black, alpha = shadowAlpha))
        .clip(shape)
        .then(frost)
        .drawBehind {
            val highlightHeight = HighlightHeightDp.dp.toPx()
            drawRect(color = baseTint)
            drawRect(
                brush = Brush.radialGradient(
                    colorStops = bloomStops,
                    center = center,
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
