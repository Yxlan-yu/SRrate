package com.yxlanyu.refreshrate.ui

import android.app.WallpaperManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController
import top.yukonga.miuix.kmp.theme.darkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme

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

const val PREFS_NAME = "s"
const val KEY_MONET = "monet"
const val KEY_THEME_COLOR = "theme_color"
internal const val DefaultAccent = 0xFF3B76FD.toInt()

private fun extractWallpaperAccent(context: Context): Int? {
    return try {
        val wm = WallpaperManager.getInstance(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            val colors = wm.getWallpaperColors(WallpaperManager.FLAG_SYSTEM)
            colors?.primaryColor?.toArgb()?.let { return it or (0xFF shl 24) }
        }
        val drawable = wm.drawable ?: return null
        val w = drawable.intrinsicWidth.coerceAtLeast(1)
        val h = drawable.intrinsicHeight.coerceAtLeast(1)
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bmp)
        drawable.setBounds(0, 0, w, h)
        drawable.draw(canvas)
        dominantSaturatedColor(scaleBitmapForColor(bmp, 48))
    } catch (e: Exception) {
        null
    }
}

private fun scaleBitmapForColor(bitmap: Bitmap, maxEdge: Int): Bitmap {
    val w = bitmap.width
    val h = bitmap.height
    if (w <= maxEdge && h <= maxEdge) return bitmap
    val scale = maxEdge.toFloat() / maxOf(w, h)
    return Bitmap.createScaledBitmap(bitmap, (w * scale).toInt().coerceAtLeast(1), (h * scale).toInt().coerceAtLeast(1), true)
}

private fun dominantSaturatedColor(bitmap: Bitmap): Int? {
    val width = bitmap.width
    val height = bitmap.height
    if (width == 0 || height == 0) return null
    val pixels = IntArray(width * height)
    bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
    val buckets = mutableMapOf<Long, LongArray>()
    var maxWeight = 0
    var maxKey = 0L
    for (p in pixels) {
        val r = (p shr 16) and 0xFF
        val g = (p shr 8) and 0xFF
        val b = p and 0xFF
        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)
        if (max == 0) continue
        val sat = (max - min) * 255 / max
        if (sat < 70) continue
        if (max < 40 || max > 245) continue
        val key = (((r shr 4) shl 8) or ((g shr 4) shl 4) or (b shr 4)).toLong()
        val arr = buckets.getOrPut(key) { LongArray(4) } // [rSum,gSum,bSum,count]
        arr[0] += r; arr[1] += g; arr[2] += b; arr[3] += 1
        if (arr[3].toInt() > maxWeight) {
            maxWeight = arr[3].toInt()
            maxKey = key
        }
    }
    if (maxKey == 0L) return null
    val arr = buckets[maxKey] ?: return null
    val count = arr[3].toInt()
    return (0xFF shl 24) or (arr[0].toInt() / count shl 16) or (arr[1].toInt() / count shl 8) or (arr[2].toInt() / count)
}

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val context = LocalContext.current
    var wallpaperAccent by remember { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()

    fun refreshWallpaperAccent() {
        scope.launch {
            val accent = withContext(Dispatchers.IO) { extractWallpaperAccent(context) }
            wallpaperAccent = accent
        }
    }

    LaunchedEffect(context) {
        refreshWallpaperAccent()
    }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_WALLPAPER_CHANGED) {
                    refreshWallpaperAccent()
                }
            }
        }
        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_WALLPAPER_CHANGED))
        onDispose { runCatching { context.unregisterReceiver(receiver) } }
    }

    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    val monet = remember { prefs.getBoolean(KEY_MONET, true) }
    val base = if (monet) {
        remember(wallpaperAccent) {
            ThemeController(
                colorSchemeMode = ColorSchemeMode.MonetSystem,
                keyColor = Color(wallpaperAccent ?: DefaultAccent),
            )
        }.currentColors()
    } else {
        val picked = Color(prefs.getInt(KEY_THEME_COLOR, DefaultAccent))
        val onPicked = if (picked.luminance() > 0.5f) Color(0xFF000000) else Color(0xFFFFFFFF)
        remember(picked, dark) {
            if (dark) {
                darkColorScheme(
                    primary = picked,
                    onPrimary = onPicked,
                    primaryVariant = picked,
                    onPrimaryVariant = onPicked,
                )
            } else {
                lightColorScheme(
                    primary = picked,
                    onPrimary = onPicked,
                    primaryVariant = picked,
                    onPrimaryVariant = onPicked,
                )
            }
        }
    }
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