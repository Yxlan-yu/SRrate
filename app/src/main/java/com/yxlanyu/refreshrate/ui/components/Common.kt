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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical

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
    val blurAvailable = isRuntimeShaderSupported()
    val surfaceColor = MiuixTheme.colorScheme.surface
    val backdrop = rememberLayerBackdrop {
        drawRect(surfaceColor)
        drawContent()
    }
    val barColor = if (blurAvailable) Color.Transparent else surfaceColor
    val barModifier = if (blurAvailable) Modifier.themedEave(backdrop) else Modifier
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (largeTitle.isNotEmpty()) {
                TopAppBar(
                    title = title,
                    largeTitle = largeTitle,
                    subtitle = subtitle,
                    modifier = barModifier,
                    color = barColor,
                    navigationIcon = navigationIcon ?: {},
                    actions = actions ?: {},
                    scrollBehavior = scrollBehavior,
                )
            } else {
                SmallTopAppBar(
                    title = title,
                    modifier = barModifier,
                    color = barColor,
                    navigationIcon = navigationIcon ?: {},
                    actions = actions ?: {},
                    scrollBehavior = scrollBehavior,
                )
            }
        },
    ) { innerPadding ->
        ProvideThemedBackdrop(backdrop) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (blurAvailable) Modifier.layerBackdrop(backdrop) else Modifier)
                    .overScrollVertical()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding(),
                    bottom = outerContentPadding.calculateBottomPadding(),
                ),
            ) {
                content()
            }
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