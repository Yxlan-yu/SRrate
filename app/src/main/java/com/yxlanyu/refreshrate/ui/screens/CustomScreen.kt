package com.yxlanyu.refreshrate.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.yxlanyu.refreshrate.R
import com.yxlanyu.refreshrate.ui.components.PlaceholderItem
import com.yxlanyu.refreshrate.ui.components.RefreshPageScaffold

@Composable
fun CustomScreen(outerContentPadding: PaddingValues) {
    RefreshPageScaffold(
        title = stringResource(R.string.nav_custom_app_refresh),
        outerContentPadding = outerContentPadding,
    ) {
        item(key = "placeholder") {
            PlaceholderItem(text = stringResource(R.string.nav_custom_app_refresh))
        }
    }
}