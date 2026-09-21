package com.yxlanyu.refreshrate.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController

private val LightBg = Color(0xFFF2F4F7)
private val LightCard = Color(0xFFFFFFFF)
private val LightScard = Color(0xFFF8F9FB)
private val LightText = Color(0xFF1B1D23)
private val LightSub = Color(0xFF5E6470)
private val LightIcon = Color(0xFFA8ADBA)
private val DarkBg = Color(0xFF15171B)
private val DarkCard = Color(0xFF1E2024)
private val DarkScard = Color(0xFF26282D)
private val DarkText = Color(0xFFE4E5E9)
private val DarkSub = Color(0xFF9BA0A8)
private val DarkIcon = Color(0xFF6A6F78)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val themeController = remember {
        ThemeController(colorSchemeMode = ColorSchemeMode.MonetSystem)
    }
    val base = themeController.currentColors()
    val colors = remember(base, dark) {
        base.copy(
            background = if (dark) DarkBg else LightBg,
            surface = if (dark) DarkBg else LightBg,
            surfaceVariant = if (dark) DarkScard else LightScard,
            surfaceContainer = if (dark) DarkCard else LightCard,
            surfaceContainerHigh = if (dark) DarkScard else LightScard,
            surfaceContainerHighest = if (dark) DarkScard else LightScard,
            onSurface = if (dark) DarkText else LightText,
            onBackground = if (dark) DarkText else LightText,
            onSurfaceContainer = if (dark) DarkText else LightText,
            onSurfaceContainerHigh = if (dark) DarkText else LightText,
            onSurfaceContainerHighest = if (dark) DarkText else LightText,
            onSurfaceVariantSummary = if (dark) DarkSub else LightSub,
            onSurfaceContainerVariant = if (dark) DarkIcon else LightIcon,
            onBackgroundVariant = if (dark) DarkSub else LightSub,
            secondaryContainer = if (dark) DarkScard else LightScard,
            onSecondaryContainer = if (dark) DarkText else LightText,
            dividerLine = if (dark) Color(0x12FFFFFF) else Color(0x0F000000),
        )
    }
    MiuixTheme(
        colors = colors,
        content = content,
    )
}