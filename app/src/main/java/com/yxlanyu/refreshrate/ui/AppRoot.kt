package com.yxlanyu.refreshrate.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.yxlanyu.refreshrate.MainActivity
import com.yxlanyu.refreshrate.R
import com.yxlanyu.refreshrate.service.UpdateWorker
import com.yxlanyu.refreshrate.ui.components.UpdateDialog
import com.yxlanyu.refreshrate.ui.components.rememberUpdateController
import com.yxlanyu.refreshrate.ui.screens.CustomScreen
import com.yxlanyu.refreshrate.ui.screens.HomeScreen
import com.yxlanyu.refreshrate.ui.screens.MonitorScreen
import com.yxlanyu.refreshrate.ui.screens.SettingsScreen
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.GridView
import top.yukonga.miuix.kmp.icon.extended.Refresh
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.icon.extended.Tune

private enum class MainTab(
    val labelRes: Int,
    val icon: ImageVector,
) {
    Home(R.string.nav_home, MiuixIcons.Refresh),
    Custom(R.string.nav_custom_app_refresh, MiuixIcons.Tune),
    Tools(R.string.nav_tools, MiuixIcons.GridView),
    Settings(R.string.nav_settings, MiuixIcons.Settings),
}

@Composable
fun AppRoot() {
    var currentTab by rememberSaveable { mutableIntStateOf(0) }
    var langVersion by rememberSaveable { mutableIntStateOf(0) }
    val tabs = MainTab.entries
    val context = LocalContext.current
    val updateController = rememberUpdateController()

    LaunchedEffect(Unit) {
        updateController.checkSilently()
    }

    val activity = context as? MainActivity
    LaunchedEffect(activity?.intent) {
        val act = activity ?: return@LaunchedEffect
        if (act.intent.getBooleanExtra(UpdateWorker.EXTRA_SHOW_UPDATE, false)) {
            act.intent.removeExtra(UpdateWorker.EXTRA_SHOW_UPDATE)
            if (updateController.downloading) {
                updateController.reshow()
            } else {
                updateController.checkAndShow()
            }
        }
    }

    val forceLangRecompose = langVersion
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = index == currentTab,
                        onClick = { currentTab = index },
                        icon = tab.icon,
                        label = stringResource(tab.labelRes),
                    )
                }
            }
        },
    ) { innerPadding ->
        forceLangRecompose
        when (tabs[currentTab]) {
            MainTab.Home -> HomeScreen(outerContentPadding = innerPadding)
            MainTab.Custom -> CustomScreen(outerContentPadding = innerPadding)
            MainTab.Tools -> MonitorScreen(outerContentPadding = innerPadding)
            MainTab.Settings -> SettingsScreen(
                outerContentPadding = innerPadding,
                updateController = updateController,
                onLanguageChanged = {
                    activity?.refreshAppliedLang()
                    langVersion++
                },
            )
        }
        UpdateDialog(updateController)
    }
}