package com.atride.cook

import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_LARGE_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import com.atride.cook.navigation.Route
import com.atride.cook.ui.CookAppState
import com.atride.cook.ui.DevicePreviews
import com.atride.cook.ui.panels.ChatArea
import com.atride.cook.ui.panels.DualPaneLayout
import com.atride.cook.ui.panels.SessionListPanel
import com.atride.cook.ui.panels.SinglePaneLayout
import com.atride.cook.ui.panels.TriplePaneLayout
import com.atride.cook.ui.rememberCookAppState
import com.atride.cook.ui.screens.SettingsScreen
import com.atride.cook.ui.sessionPanelWidth
import com.atride.cook.ui.theme.CookTheme

@Composable
fun CookApp(
    appState: CookAppState,
    modifier: Modifier = Modifier,
    windowInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfoV2()
) {
    val backStack = remember { mutableStateListOf<Route>(Route.SessionList) }

    var isContextPanelExpanded by remember { mutableStateOf(true) }

    CookApp(
        appState = appState,
        backStack = backStack,
        isContextPanelExpanded = isContextPanelExpanded,
        onContextPanelToggle = { isContextPanelExpanded = !isContextPanelExpanded },
        modifier = modifier,
        windowInfo = windowInfo
    )

}

@Composable
internal fun CookApp(
    appState: CookAppState,
    backStack: SnapshotStateList<Route>,
    isContextPanelExpanded: Boolean,
    onContextPanelToggle: () -> Unit,
    modifier: Modifier = Modifier,
    windowInfo: WindowAdaptiveInfo
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
            isCompact && isTouchPlatform -> SinglePaneLayout(backStack, appState)

            !isWide -> DualPaneLayout(backStack, appState,
                panelWidth = sessionPanelWidth(wsc, isTouch = !isTouchPlatform))

            else -> TriplePaneLayout(
                backStack = backStack,
                appState = appState,
                panelWidth = sessionPanelWidth(wsc, isTouch = false),
                isContextPanelExpanded = isContextPanelExpanded,
                onContextPanelToggle = onContextPanelToggle,
            )
        }
    }

}

@Composable
fun RouteContent(
    route: Route,
    appState: CookAppState,
    backStack: SnapshotStateList<Route>,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    when (route) {
        is Route.SessionList -> {
            SessionListPanel(
                selectedSessionId = appState.selectedSessionId ?: "",
                onSessionClick = { id -> appState.selectedSessionId = id },
                onNewSession = { appState.selectedSessionId = "new" },
                modifier = modifier,
            )
        }
        is Route.SessionDetail -> {
            ChatArea(appState.selectedSessionId, modifier)
        }
        is Route.Settings -> {
            SettingsScreen(onNavigateBack = onBack ?: {})
        }
    }
}


fun MutableList<Route>.navigateToSettings() {
    removeAll { it is Route.Settings }
    add(Route.Settings)
}

fun MutableList<Route>.goBack() {
    if (size > 1) removeLastOrNull()
}

@DevicePreviews
@Composable
fun AppPreview() {
    CookTheme {
        CookApp(rememberCookAppState())
    }
}