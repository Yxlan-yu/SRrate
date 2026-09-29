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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.TopAppBarState
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurColors
import top.yukonga.miuix.kmp.blur.ProgressiveBlur
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.progressiveTextureBlur
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical

/** Blur radius, in dp, of the top bar frost where it is at full strength. */
internal const val TopBarFrostBlurRadius = 20f

/**
 * How far the list has to slide under the bar before the frost starts appearing, and where it is
 * fully opaque -- both as a fraction of [TopAppBarState.overlappedFraction].
 */
internal const val TopBarFrostFadeStart = 0.02f
internal const val TopBarFrostFadeEnd = 0.4f

/** Translucent page-colour scrim mixed into the frost, so the title stays readable over content. */
internal const val TopBarFrostScrimAlpha = 0.32f

/**
 * 1.4.1-beta5: whether the scrollable top bars show real frosted glass. Fed from AppRoot, which
 * reads it off Settings > Theme > Interaction > "Advanced material" (already ANDed with the
 * RuntimeShader gate). Everything downstream treats it as a plain boolean.
 */
internal val LocalTopBarFrost = staticCompositionLocalOf { false }

/** Maps "how much of the bar the list has slid under" onto the frost's opacity. */
private fun frostAlpha(overlapped: Float): Float =
    ((overlapped - TopBarFrostFadeStart) / (TopBarFrostFadeEnd - TopBarFrostFadeStart))
        .coerceIn(0f, 1f)

/**
 * A scrolling page: a collapsing top bar over a lazy list.
 *
 * 1.4.1-beta5 adds the scroll-driven frost. Two things had to be true for it to work, and beta4
 * had neither:
 *
 *  * **The frost must sample the list.** miuix's [Scaffold] places the top bar *after* the body,
 *    so the bar is already drawn on top of the list and list content does slide underneath it --
 *    the geometry was never the problem. What beta4 sampled was a pager-wide backdrop, which has
 *    nothing behind it while the large title is expanded, so the blur had nothing to blur and
 *    only the white tint survived: a flat haze with a hard edge. Recording the list itself makes
 *    "nothing behind it" mean "nothing to draw".
 *  * **The frost must fade with the overlap.** [TopAppBarState.overlappedFraction] is 0 while the
 *    bar is expanded and 1 once the list covers all of it, which is exactly the MIUI behaviour the
 *    user asked for, and it keeps the expanded state clean instead of tinting an empty backdrop.
 *
 * The frost lives in a [Box] beside the bar, never inside the recorded list, so it can never
 * sample the surface it is itself drawn into.
 */
@Composable
fun RefreshPageScaffold(
    title: String,
    outerContentPadding: PaddingValues,
    largeTitle: String = "",
    subtitle: String = "",
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    content: LazyListScope.() -> Unit,
) {
    val scrollBehavior = MiuixScrollBehavior()
    val pageColor = MiuixTheme.colorScheme.surface
    val listBackdrop = if (LocalTopBarFrost.current) {
        rememberLayerBackdrop { drawContent() }
    } else {
        null
    }
    val frostColors = remember(pageColor) {
        BlurColors(
            blendColors = listOf(BlendColorEntry(pageColor.copy(alpha = TopBarFrostScrimAlpha))),
        )
    }
    // Transparent only when there is something that can actually show through; with the frost off
    // the bar keeps the opaque page colour it has always had.
    val barColor = if (listBackdrop != null) Color.Transparent else pageColor

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Box(Modifier) {
                listBackdrop?.let { backdrop ->
                    Box(
                        Modifier
                            .matchParentSize()
                            // Read in the draw phase on purpose: overlappedFraction is derived
                            // from contentOffset, which moves on every scroll frame, and reading
                            // it during composition would recompose this subtree 60 times a second.
                            .graphicsLayer {
                                alpha = frostAlpha(scrollBehavior.state.overlappedFraction)
                            }
                            .progressiveTextureBlur(
                                backdrop = backdrop,
                                shape = RectangleShape,
                                blurRadius = TopBarFrostBlurRadius,
                                gradient = ProgressiveBlur.Top,
                                colors = frostColors,
                            ),
                    )
                }
                if (largeTitle.isNotEmpty()) {
                    TopAppBar(
                        title = title,
                        largeTitle = largeTitle,
                        subtitle = subtitle,
                        navigationIcon = navigationIcon ?: {},
                        actions = actions ?: {},
                        scrollBehavior = scrollBehavior,
                        color = barColor,
                    )
                } else {
                    SmallTopAppBar(
                        title = title,
                        navigationIcon = navigationIcon ?: {},
                        actions = actions ?: {},
                        scrollBehavior = scrollBehavior,
                        color = barColor,
                    )
                }
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .overScrollVertical()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .then(
                    if (listBackdrop != null) Modifier.layerBackdrop(listBackdrop) else Modifier,
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

/**
 * 1.4.1-beta4: a page whose whole body is guaranteed to fit on screen.
 *
 * [RefreshPageScaffold] cannot be used for these: turning its scroll behavior off still leaves
 * a [LazyColumn] in the tree, so the content stays draggable and the large title still
 * collapses. This variant has no scroll behavior, no overscroll and no large title at all --
 * the body is a plain [Column], so there is physically nothing to scroll and nothing to fold.
 */
@Composable
fun StaticPageScaffold(
    title: String,
    outerContentPadding: PaddingValues,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            SmallTopAppBar(
                title = title,
                navigationIcon = navigationIcon ?: {},
                actions = actions ?: {},
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
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