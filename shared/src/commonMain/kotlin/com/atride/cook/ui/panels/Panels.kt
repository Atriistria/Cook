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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.atride.cook.navigation.CookNavigator
import com.atride.cook.navigation.Route
import com.atride.cook.ui.screens.SettingsScreen
import kotlinx.coroutines.launch
import com.atride.cook.ui.ChatViewModel
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun SinglePaneLayout(
    navigator: CookNavigator,
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val chatViewModel = koinViewModel<ChatViewModel>()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                SessionListPanel(
                    selectedSessionId = navigator.selectedSessionId ?: "",
                    onSessionClick = { id ->
                        navigator.openSession(id)
                        scope.launch { drawerState.close() }
                    },
                    onNewSession = {
                        navigator.deselectSession()
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.fillMaxHeight(),
                )
            }
        },
    ) {
        when (navigator.currentRoute) {
            is Route.Settings -> SettingsScreen(
                onNavigateBack = { navigator.goBack() },
            )

            else -> ChatArea(
                selectedSessionId = navigator.selectedSessionId,
                onMenuClick = { scope.launch { drawerState.open() } },
                onSessionCreated = { id -> navigator.openSession(id) },
                modifier = Modifier.fillMaxSize(),
                viewModel = chatViewModel,
            )
        }
    }
}

@Composable
fun DualPaneLayout(
    navigator: CookNavigator,
    panelWidth: Dp,
) {
    val scope = rememberCoroutineScope()
    val chatViewModel = koinViewModel<ChatViewModel>()

    Row(Modifier.fillMaxSize()) {
        SessionListPanel(
            selectedSessionId = navigator.selectedSessionId ?: "",
            onSessionClick = { id -> navigator.openSession(id) },
            onNewSession = { navigator.deselectSession() },
            modifier = Modifier.width(panelWidth).fillMaxHeight(),
        )
        Box(Modifier.weight(1f).fillMaxHeight()) {
            when (navigator.currentRoute) {
                is Route.Settings -> SettingsScreen(
                    onNavigateBack = { navigator.goBack() },
                )

                else -> ChatArea(
                    selectedSessionId = navigator.selectedSessionId,
                    modifier = Modifier.fillMaxSize(),
                    onSessionCreated = { id -> navigator.openSession(id) },
                    viewModel = chatViewModel,
                )
            }
        }
    }
}

@Composable
fun TriplePaneLayout(
    navigator: CookNavigator,
    panelWidth: Dp,
    isContextPanelExpanded: Boolean,
    onContextPanelToggle: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val chatViewModel = koinViewModel<ChatViewModel>()

    Row(Modifier.fillMaxSize()) {
        SessionListPanel(
            selectedSessionId = navigator.selectedSessionId ?: "",
            onSessionClick = { id -> navigator.openSession(id) },
            onNewSession = { navigator.deselectSession() },
            modifier = Modifier.width(panelWidth).fillMaxHeight(),
        )
        Box(Modifier.weight(1f).fillMaxHeight()) {
            when (navigator.currentRoute) {
                is Route.Settings -> SettingsScreen(
                    onNavigateBack = { navigator.goBack() },
                )

                else -> ChatArea(
                    selectedSessionId = navigator.selectedSessionId,
                    modifier = Modifier.fillMaxSize(),
                    onSessionCreated = { id -> navigator.openSession(id) },
                    viewModel = chatViewModel,
                )
            }
        }
        if (isContextPanelExpanded) {
            Box(Modifier.width(280.dp).fillMaxHeight())
        }
    }
}