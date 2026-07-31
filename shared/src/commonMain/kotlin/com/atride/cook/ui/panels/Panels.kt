package com.atride.cook.ui.panels

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.atride.cook.goBack
import com.atride.cook.navigation.Route
import com.atride.cook.ui.CookAppState
import com.atride.cook.ui.screens.SettingsScreen
import kotlinx.coroutines.launch


@Composable
fun SinglePaneLayout(
    backStack: SnapshotStateList<Route>,
    appState: CookAppState,
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val settingsRoute = backStack.lastOrNull()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                SessionListPanel(
                    selectedSessionId = appState.selectedSessionId ?: "",
                    onSessionClick = { id ->
                        appState.selectedSessionId = id
                        scope.launch { drawerState.close() }
                    },
                    onNewSession = {
                        appState.selectedSessionId = "new"
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.fillMaxHeight(),
                )
            }
        },
    ) {
        if (settingsRoute is Route.Settings) {
            SettingsScreen(onNavigateBack = { backStack.goBack() })
        } else {
            ChatArea(
                selectedSessionId = appState.selectedSessionId,
                onMenuClick = { scope.launch { drawerState.open() } },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
fun DualPaneLayout(
    backStack: SnapshotStateList<Route>,
    appState: CookAppState,
    panelWidth: Dp,
) {
    val settingsRoute = backStack.lastOrNull()
    Row(Modifier.fillMaxSize()) {
        SessionListPanel(
            selectedSessionId = appState.selectedSessionId ?: "",
            onSessionClick = { id -> appState.selectedSessionId = id },
            onNewSession = { appState.selectedSessionId = "new" },
            modifier = Modifier.width(panelWidth).fillMaxHeight(),
        )
        Box(Modifier.weight(1f).fillMaxHeight()) {
            if (settingsRoute is Route.Settings) {
                SettingsScreen(onNavigateBack = { backStack.goBack() })
            } else {
                ChatArea(
                    selectedSessionId = appState.selectedSessionId,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
fun TriplePaneLayout(
    backStack: SnapshotStateList<Route>,
    appState: CookAppState,
    panelWidth: Dp,
    isContextPanelExpanded: Boolean,
    onContextPanelToggle: () -> Unit,
) {
    val settingsRoute = backStack.lastOrNull()
    Row(Modifier.fillMaxSize()) {
        SessionListPanel(
            selectedSessionId = appState.selectedSessionId ?: "",
            onSessionClick = { id -> appState.selectedSessionId = id },
            onNewSession = { appState.selectedSessionId = "new" },
            modifier = Modifier.width(panelWidth).fillMaxHeight(),
        )
        if (settingsRoute is Route.Settings) {
            SettingsScreen(onNavigateBack = { backStack.goBack() })
        } else {
            ChatArea(
                selectedSessionId = appState.selectedSessionId,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
        }
        if (isContextPanelExpanded) {
            Box(
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxHeight()
            )
        }
    }
}