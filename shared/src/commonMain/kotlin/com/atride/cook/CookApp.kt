package com.atride.cook

import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_LARGE_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import com.atride.cook.ui.CookAppState
import com.atride.cook.ui.DevicePreviews
import com.atride.cook.ui.panels.DualPaneLayout
import com.atride.cook.ui.panels.SinglePaneLayout
import com.atride.cook.ui.panels.TriplePaneLayout
import com.atride.cook.ui.rememberCookAppState
import com.atride.cook.ui.sessionPanelWidth
import com.atride.cook.ui.theme.CookTheme

@Composable
fun CookApp(
    appState: CookAppState,
    modifier: Modifier = Modifier,
    windowInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfoV2(),
) {
    var isContextPanelExpanded by remember { mutableStateOf(true) }

    CookApp(
        appState = appState,
        isContextPanelExpanded = isContextPanelExpanded,
        onContextPanelToggle = { isContextPanelExpanded = !isContextPanelExpanded },
        modifier = modifier,
        windowInfo = windowInfo,
    )
}

@Composable
internal fun CookApp(
    appState: CookAppState,
    isContextPanelExpanded: Boolean,
    onContextPanelToggle: () -> Unit,
    modifier: Modifier = Modifier,
    windowInfo: WindowAdaptiveInfo,
) {
    val wsc = windowInfo.windowSizeClass
    val isCompact by remember {
        derivedStateOf { !wsc.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND) }
    }
    val isWide by remember {
        derivedStateOf { wsc.isWidthAtLeastBreakpoint(WIDTH_DP_LARGE_LOWER_BOUND) }
    }

    Scaffold(modifier = modifier) {
        when {
            isCompact && isTouchPlatform -> SinglePaneLayout(appState.navigator)
            !isWide -> DualPaneLayout(
                navigator = appState.navigator,
                panelWidth = sessionPanelWidth(wsc, isTouch = !isTouchPlatform),
            )
            else -> TriplePaneLayout(
                navigator = appState.navigator,
                panelWidth = sessionPanelWidth(wsc, isTouch = false),
                isContextPanelExpanded = isContextPanelExpanded,
                onContextPanelToggle = onContextPanelToggle,
            )
        }
    }
}

@DevicePreviews
@Composable
fun AppPreview() {
    CookTheme {
        CookApp(rememberCookAppState())
    }
}