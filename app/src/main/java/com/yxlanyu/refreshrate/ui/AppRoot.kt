package com.yxlanyu.refreshrate.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.yxlanyu.refreshrate.ui.components.liquid.LiquidGlassNavigationBar
import com.yxlanyu.refreshrate.ui.components.rememberUpdateController
import com.yxlanyu.refreshrate.ui.screens.CustomScreen
import com.yxlanyu.refreshrate.ui.screens.HomeScreen
import com.yxlanyu.refreshrate.ui.screens.MonitorScreen
import com.yxlanyu.refreshrate.ui.screens.SettingsScreen
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.NavigationItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.GridView
import top.yukonga.miuix.kmp.icon.extended.Refresh
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.icon.extended.Tune
import top.yukonga.miuix.kmp.theme.MiuixTheme

private enum class MainTab(
    val labelRes: Int,
    val icon: ImageVector,
) {
    Home(R.string.nav_home, MiuixIcons.GridView),
    Custom(R.string.nav_custom_app_refresh, MiuixIcons.Tune),
    Tools(R.string.nav_tools, MiuixIcons.Refresh),
    Settings(R.string.nav_settings, MiuixIcons.Settings),
}

@Composable
fun AppRoot() {
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

    val prefs = remember {
        context.getSharedPreferences("s", android.content.Context.MODE_PRIVATE)
    }
    var advancedMaterial by remember { mutableStateOf(prefs.getBoolean("advanced_material", false)) }
    val isBlurActive = advancedMaterial && isRuntimeShaderSupported()

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { tabs.size },
    )
    val scope = rememberCoroutineScope()

    val forceLangRecompose = langVersion
    if (isBlurActive) {
        val backgroundColor = MiuixTheme.colorScheme.surface
        val backdrop = rememberLayerBackdrop {
            drawRect(backgroundColor)
            drawContent()
        }
        val items = tabs.map { tab ->
            NavigationItem(
                label = stringResource(tab.labelRes),
                icon = tab.icon,
            )
        }
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                val current = pagerState.currentPage
                LiquidGlassNavigationBar(
                    items = items,
                    selectedIndex = current,
                    onItemClick = { index ->
                        scope.launch { pagerState.animateScrollToPage(index) }
                    },
                    backdrop = backdrop,
                    isBlurActive = true,
                )
            },
        ) { innerPadding ->
            forceLangRecompose
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .layerBackdrop(backdrop),
            ) { page ->
                when (tabs[page]) {
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
                        onAdvancedMaterialChanged = { checked ->
                            advancedMaterial = checked
                        },
                    )
                }
            }
            UpdateDialog(updateController)
        }
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                val current = pagerState.currentPage
                NavigationBar {
                    tabs.forEachIndexed { index, tab ->
                        NavigationBarItem(
                            selected = index == current,
                            onClick = {
                                scope.launch { pagerState.animateScrollToPage(index) }
                            },
                            icon = tab.icon,
                            label = stringResource(tab.labelRes),
                        )
                    }
                }
            },
        ) { innerPadding ->
            forceLangRecompose
            HorizontalPager(state = pagerState) { page ->
                when (tabs[page]) {
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
                        onAdvancedMaterialChanged = { checked ->
                            advancedMaterial = checked
                        },
                    )
                }
            }
            UpdateDialog(updateController)
        }
    }
}