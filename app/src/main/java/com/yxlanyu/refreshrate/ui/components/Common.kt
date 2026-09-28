package com.yxlanyu.refreshrate.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.blur
import top.yukonga.miuix.kmp.blur.drawBackdrop
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical

/**
 * 1.4.1-beta4 backdrop plumbing. "Advanced material" turns this on from [AppRoot]; only
 * then does a page record its own list, and the top bar and the bottom bar consume that
 * one recording from outside it. A page must never be recorded by its parent as well: a
 * [drawBackdrop] node nested inside a recorded node is promoted to a background-blur layer
 * and blows the RenderThread stack (SIGSEGV in libhwui prepareTreeImpl), which is why the
 * pager-wide recording of 1.4.1 was removed.
 */
internal val LocalBackdropEnabled = staticCompositionLocalOf { false }

/**
 * Sink for that recording. [owner] is the identity of the publishing page instance, so a
 * page that is disposed mid-transition can only ever clear its own entry: a page transition
 * keeps both the outgoing and the incoming [AnimatedContent] instance alive for ~300ms, and
 * an unconditional clear would let the loser wipe the winner's live backdrop.
 */
internal val LocalBackdropSink =
    staticCompositionLocalOf<(owner: Any, backdrop: LayerBackdrop?) -> Unit> { { _, _ -> } }

// CZeroX's top bar measures a very low horizontal-frequency with a real colour range, i.e. a
// heavy blur over a wide sample footprint, lifting the page background 247 -> 250..255. A flat
// semi-transparent white scrim is what gets us there; 6dp is the radius that reads as "frosted"
// without smearing the (deliberately crisp) title drawn on top of it.
private const val FROST_TINT_LIGHT = 0.16f
private const val FROST_TINT_DARK = 0.08f
private val FROST_BLUR_RADIUS = 6.dp
private val FROST_SAMPLE_PADDING = 24.dp

@Composable
fun RefreshPageScaffold(
    title: String,
    outerContentPadding: PaddingValues,
    largeTitle: String = "",
    subtitle: String = "",
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    scrollEnabled: Boolean = true,
    content: LazyListScope.() -> Unit,
) {
    val scrollBehavior = MiuixScrollBehavior()
    // Only allocated when the effect can actually run: the blur library needs a RuntimeShader
    // (Android 13+), and instantiating the layer on 8.0-12.x is what 1.4.1-beta3 avoided.
    val backdrop = if (LocalBackdropEnabled.current) rememberLayerBackdrop() else null
    val reportBackdrop = LocalBackdropSink.current
    val frostOwner = remember { Any() }
    SideEffect { reportBackdrop(frostOwner, backdrop) }
    DisposableEffect(frostOwner) {
        onDispose { reportBackdrop(frostOwner, null) }
    }
    val barColor = if (backdrop != null) Color.Transparent else MiuixTheme.colorScheme.surface
    val frostTint = if (isSystemInDarkTheme()) FROST_TINT_DARK else FROST_TINT_LIGHT
    val barScrollBehavior = if (scrollEnabled) scrollBehavior else null
    val barSlot: @Composable () -> Unit = {
        if (largeTitle.isNotEmpty()) {
            TopAppBar(
                title = title,
                largeTitle = largeTitle,
                subtitle = subtitle,
                navigationIcon = navigationIcon ?: {},
                actions = actions ?: {},
                scrollBehavior = barScrollBehavior,
                color = barColor,
            )
        } else {
            SmallTopAppBar(
                title = title,
                navigationIcon = navigationIcon ?: {},
                actions = actions ?: {},
                scrollBehavior = barScrollBehavior,
                color = barColor,
            )
        }
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            // The frost is a sibling of the list (Miuix's Scaffold places both at 0,0), never
            // a child of it, so this drawBackdrop stays outside the recorded node.
            if (backdrop != null) {
                Box(
                    modifier = Modifier.drawBackdrop(
                        backdrop = backdrop,
                        shape = { RectangleShape },
                        effects = {
                            padding = maxOf(padding, FROST_SAMPLE_PADDING.toPx())
                            blur(FROST_BLUR_RADIUS.toPx(), FROST_BLUR_RADIUS.toPx())
                        },
                        onDrawSurface = { drawRect(Color.White.copy(alpha = frostTint)) },
                    ),
                ) {
                    barSlot()
                }
            } else {
                barSlot()
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .then(if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier)
                .then(if (scrollEnabled) Modifier.overScrollVertical() else Modifier)
                .then(
                    if (scrollEnabled) {
                        Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
                    } else {
                        Modifier
                    },
                ),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding(),
                bottom = outerContentPadding.calculateBottomPadding(),
            ),
        ) {
            content()
        }
    }
}

@Composable
fun PlaceholderItem(text: String) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        text = text,
        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
    )
}

@Composable
fun FicIcon(
    @DrawableRes resId: Int,
    accent: Boolean = false,
    contentDescription: String? = null,
) {
    val bgColor = if (accent) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.surfaceVariant
    Box(
        modifier = Modifier
            .size(40.dp)
            .then(
                if (accent) {
                    Modifier.background(bgColor, RoundedCornerShape(12.dp))
                } else {
                    Modifier
                        .background(bgColor, RoundedCornerShape(12.dp))
                        .border(1.dp, MiuixTheme.colorScheme.dividerLine, RoundedCornerShape(12.dp))
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(resId),
            contentDescription = contentDescription,
            modifier = Modifier.size(22.dp),
            tint = if (accent) Color.White else MiuixTheme.colorScheme.primary,
        )
    }
}

@Composable
fun <T> PageTransitionContent(
    targetState: T,
    depth: (T) -> Int,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit,
) {
    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        transitionSpec = {
            val forward = depth(targetState) > depth(initialState)
            val enter =
                slideInHorizontally(animationSpec = tween(300)) { fullWidth ->
                    if (forward) fullWidth else -fullWidth / 4
                } + fadeIn(animationSpec = tween(300))
            val exit =
                slideOutHorizontally(animationSpec = tween(300)) { fullWidth ->
                    if (forward) -fullWidth / 4 else fullWidth
                } + fadeOut(animationSpec = tween(300))
            enter togetherWith exit
        },
        label = "page_transition",
    ) { p ->
        content(p)
    }
}