package com.yxlanyu.refreshrate.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.yxlanyu.refreshrate.ui.components.liquid.InnerShadow
import com.yxlanyu.refreshrate.ui.components.liquid.innerShadow
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** Standard card corner radius for 1.4.1. */
val GlassCornerRadius: Dp = 24.dp

private const val TINT_SUB = 0.28f
private const val TINT_NORMAL = 0.40f
private const val TINT_HERO = 0.48f
private const val TINT_DARK_BONUS = 0.06f

/** Tint weight: secondary cards sit below the standard one, the current-rate hero above it. */
enum class GlassEmphasis { Sub, Normal, Hero }

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = GlassCornerRadius,
    emphasis: GlassEmphasis = GlassEmphasis.Normal,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val isDark = isSystemInDarkTheme()
    val shape = RoundedCornerShape(cornerRadius)
    val surfaceContainer = MiuixTheme.colorScheme.surfaceContainer
    val base = when (emphasis) {
        GlassEmphasis.Sub -> TINT_SUB
        GlassEmphasis.Normal -> TINT_NORMAL
        GlassEmphasis.Hero -> TINT_HERO
    }
    val tint = (base + if (isDark) TINT_DARK_BONUS else 0f).coerceIn(0f, 1f)
    val tintColor = surfaceContainer.copy(alpha = tint)

    Box(
        modifier = modifier
            .dropShadow(
                shape = shape,
                shadow = Shadow(
                    radius = 10.dp,
                    color = Color.Black,
                    alpha = if (isDark) 0.34f else 0.10f,
                ),
            )
            .clip(shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                },
            )
            .background(tintColor, shape)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.55f),
                        Color.White.copy(alpha = 0.16f),
                    ),
                ),
                shape = shape,
            )
            .innerShadow(shape = shape) {
                InnerShadow(
                    radius = 8.dp,
                    offset = DpOffset(0.dp, 5.dp),
                    color = Color.Black.copy(alpha = 0.15f),
                )
            },
    ) {
        content()
    }
}
