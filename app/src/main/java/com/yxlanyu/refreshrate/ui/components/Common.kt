package com.yxlanyu.refreshrate.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
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
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (largeTitle.isNotEmpty()) {
                TopAppBar(
                    title = title,
                    largeTitle = largeTitle,
                    subtitle = subtitle,
                    navigationIcon = navigationIcon ?: {},
                    actions = actions ?: {},
                    scrollBehavior = scrollBehavior,
                )
            } else {
                SmallTopAppBar(
                    title = title,
                    navigationIcon = navigationIcon ?: {},
                    actions = actions ?: {},
                    scrollBehavior = scrollBehavior,
                )
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
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