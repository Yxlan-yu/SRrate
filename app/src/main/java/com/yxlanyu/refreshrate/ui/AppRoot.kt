package com.yxlanyu.refreshrate.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yxlanyu.refreshrate.MainActivity
import com.yxlanyu.refreshrate.R
import com.yxlanyu.refreshrate.service.UpdateWorker
import com.yxlanyu.refreshrate.ui.components.LocalBackdropEnabled
import com.yxlanyu.refreshrate.ui.components.LocalBackdropSink
import com.yxlanyu.refreshrate.ui.components.UpdateDialog
import com.yxlanyu.refreshrate.ui.components.liquid.LiquidGlassNavigationBar
import com.yxlanyu.refreshrate.ui.components.liquid.NavCapsuleBottomPadding
import com.yxlanyu.refreshrate.ui.components.liquid.NavCapsuleHeightDefault
import com.yxlanyu.refreshrate.ui.components.liquid.NavCapsuleWidthDefault
import com.yxlanyu.refreshrate.ui.components.liquid.NavGlassStrengthDefault
import com.yxlanyu.refreshrate.ui.components.rememberUpdateController
import com.yxlanyu.refreshrate.ui.screens.CustomScreen
import com.yxlanyu.refreshrate.ui.screens.HomeScreen
import com.yxlanyu.refreshrate.ui.screens.MonitorScreen
import com.yxlanyu.refreshrate.ui.screens.SettingsScreen
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.NavigationItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.GridView
import top.yukonga.miuix.kmp.icon.extended.Refresh
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.icon.extended.Tune

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
    // 1.4.1-beta3 bottom bar geometry, all adjustable from Settings > Theme > Interaction.
    var capsuleWidth by remember { mutableFloatStateOf(prefs.getFloat("nav_capsule_width", NavCapsuleWidthDefault.value)) }
    var capsuleHeight by remember { mutableFloatStateOf(prefs.getFloat("nav_capsule_height", NavCapsuleHeightDefault.value)) }
    var navBarBottomOffset by remember { mutableFloatStateOf(prefs.getFloat("nav_bar_bottom_offset", NavCapsuleBottomPadding.value)) }
    var glassStrength by remember { mutableFloatStateOf(prefs.getFloat("nav_glass_strength", NavGlassStrengthDefault)) }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { tabs.size },
    )
    val scope = rememberCoroutineScope()

    val forceLangRecompose = langVersion

    // 1.4.1-beta4: the recording moved down into RefreshPageScaffold, so each page hands up
    // the backdrop of *its own* list. A page-level recording is the only safe one here: a
    // pager-wide recording would contain the neighbouring pages' top bar frost, i.e. a
    // drawBackdrop nested inside the recorded node, which promotes itself to a
    // background-blur layer and blows the RenderThread stack (SIGSEGV in libhwui
    // prepareTreeImpl). The value is (owner identity, backdrop) so that a page disposed
    // mid-transition can only clear its own entry. Cards still never sample a backdrop.
    val pageBackdrops = remember { mutableStateMapOf<Int, Pair<Any, LayerBackdrop>>() }
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
                backdrop = if (isBlurActive) pageBackdrops[current]?.second else null,
                isBlurActive = isBlurActive,
                capsuleWidth = capsuleWidth.dp,
                capsuleHeight = capsuleHeight.dp,
                bottomPadding = navBarBottomOffset.dp,
                glassStrength = glassStrength,
            )
        },
    ) { innerPadding ->
        forceLangRecompose
        CompositionLocalProvider(LocalBackdropEnabled provides isBlurActive) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            // Provided per pager page so the sink captures a stable `page`, never
            // pagerState.currentPage, which two pages mid-swipe would race over.
            val reportForPage: (Any, LayerBackdrop?) -> Unit = remember(page) {
                { owner: Any, backdrop: LayerBackdrop? ->
                    if (backdrop == null) {
                        if (pageBackdrops[page]?.first === owner) pageBackdrops.remove(page)
                    } else {
                        pageBackdrops[page] = owner to backdrop
                    }
                }
            }
            CompositionLocalProvider(LocalBackdropSink provides reportForPage) {
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
                    onNavGeometryChanged = { widthDp, heightDp, offsetDp, strength ->
                        capsuleWidth = widthDp
                        capsuleHeight = heightDp
                        navBarBottomOffset = offsetDp
                        glassStrength = strength
                    },
                )
            }
            }
        }
        }
        UpdateDialog(updateController)
    }
}