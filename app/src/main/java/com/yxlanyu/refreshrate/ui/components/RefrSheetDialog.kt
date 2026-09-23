package com.yxlanyu.refreshrate.ui.components

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.captionBarPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.layout.DialogDefaults
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.MiuixPopupUtils.Companion.DialogLayout
import top.yukonga.miuix.kmp.utils.getRoundedCorner

private val SheetSpring = spring(dampingRatio = 0.88f, stiffness = 450f, visibilityThreshold = 0.0001f)

private val SheetEnter: EnterTransition = slideInVertically(
    initialOffsetY = { it },
    animationSpec = SheetSpring,
)

private val SheetExit: ExitTransition = slideOutVertically(
    targetOffsetY = { it },
    animationSpec = SheetSpring,
)

private val SheetEnterLarge: EnterTransition = fadeIn(
    animationSpec = spring(dampingRatio = 0.9f, stiffness = 438.6f, visibilityThreshold = 0.0001f),
) + scaleIn(
    initialScale = 0.8f,
    animationSpec = spring(dampingRatio = 0.9f, stiffness = 438.6f, visibilityThreshold = 0.0001f),
)

private val SheetExitLarge: ExitTransition = fadeOut(
    animationSpec = spring(dampingRatio = 0.9f, stiffness = 438.6f, visibilityThreshold = 0.0001f),
) + scaleOut(
    targetScale = 0.8f,
    animationSpec = spring(dampingRatio = 0.9f, stiffness = 438.6f, visibilityThreshold = 0.0001f),
)

@Composable
private fun isRefrLargeScreen(): Boolean {
    val windowInfo = LocalWindowInfo.current
    return windowInfo.containerDpSize.height >= 480.dp && windowInfo.containerDpSize.width >= 840.dp
}

/**
 * A dialog with a title and other contents, rendered as a bottom sheet on phones and centered
 * on large screens. Exit animation matches the enter animation (symmetric [SheetSpring]).
 *
 * @param show Whether the dialog is currently shown.
 * @param title The title of the dialog.
 * @param titleColor The color of the title.
 * @param backgroundColor The background color of the dialog.
 * @param enableWindowDim Whether to enable window dimming when the dialog is shown.
 * @param onDismissRequest Called when the user tries to dismiss the dialog by clicking
 *   outside or pressing the back button.
 * @param content The content of the dialog.
 */
@Composable
fun RefrSheetDialog(
    show: Boolean,
    title: String? = null,
    titleColor: Color = DialogDefaults.titleColor(),
    backgroundColor: Color = DialogDefaults.backgroundColor(),
    enableWindowDim: Boolean = true,
    onDismissRequest: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val visible = remember { mutableStateOf(false) }
    LaunchedEffect(show) { visible.value = show }

    BackHandler(enabled = show) { onDismissRequest?.invoke() }

    val largeScreen = isRefrLargeScreen()
    val enter = if (largeScreen) SheetEnterLarge else SheetEnter
    val exit = if (largeScreen) SheetExitLarge else SheetExit

    DialogLayout(
        visible = visible,
        enterTransition = enter,
        exitTransition = exit,
        enableWindowDim = enableWindowDim,
        enableAutoLargeScreen = false,
        renderInRootScaffold = true,
    ) {
        RefrSheetContent(
            title = title,
            titleColor = titleColor,
            backgroundColor = backgroundColor,
            onDismissRequest = onDismissRequest,
            content = content,
        )
    }
}

@Composable
private fun RefrSheetContent(
    title: String?,
    titleColor: Color,
    backgroundColor: Color,
    onDismissRequest: (() -> Unit)?,
    content: @Composable () -> Unit,
) {
    val windowInfo = LocalWindowInfo.current
    val windowHeight = windowInfo.containerDpSize.height
    val largeScreen = isRefrLargeScreen()
    val contentAlignment = if (largeScreen) Alignment.Center else Alignment.BottomCenter

    val roundedCorner = getRoundedCorner()
    val outsideMargin = DialogDefaults.outsideMargin
    val insideMargin = DialogDefaults.insideMargin
    val bottomCornerRadius = remember(roundedCorner, outsideMargin.width, largeScreen) {
        val offset = if (largeScreen) 0.dp else outsideMargin.width
        (roundedCorner - offset).coerceAtLeast(32.dp)
    }
    val currentOnDismiss by rememberUpdatedState(onDismissRequest)

    val topInset = maxOf(
        WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
        WindowInsets.captionBar.asPaddingValues().calculateTopPadding(),
        WindowInsets.displayCutout.asPaddingValues().calculateTopPadding(),
    )

    Box(
        modifier = Modifier
            .imePadding()
            .navigationBarsPadding()
            .captionBarPadding()
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { currentOnDismiss?.invoke() },
                )
            }
            .semantics {
                onClick(label = "Dismiss") {
                    currentOnDismiss?.invoke()
                    true
                }
            }
            .padding(horizontal = outsideMargin.width)
            .padding(top = topInset, bottom = outsideMargin.height),
    ) {
        Column(
            modifier = Modifier
                .align(contentAlignment)
                .widthIn(max = DialogDefaults.MaxWidth)
                .heightIn(max = if (largeScreen) windowHeight * (2f / 3f) else Dp.Unspecified)
                .pointerInput(Unit) { detectTapGestures { /* Consume click */ } }
                .squircleSurface(color = backgroundColor, cornerRadius = bottomCornerRadius)
                .padding(horizontal = insideMargin.width, vertical = insideMargin.height),
        ) {
            title?.let {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    text = it,
                    fontSize = MiuixTheme.textStyles.title4.fontSize,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = titleColor,
                )
            }
            content()
        }
    }
}