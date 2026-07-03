package com.atride.cook.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atride.cook.model.MockData
import com.atride.cook.ui.components.AdaptiveLayout
import com.atride.cook.ui.components.ChatBubble
import com.atride.cook.ui.components.ChatInputBar
import com.atride.cook.ui.components.SessionListItem
import com.atride.cook.ui.components.WindowSizeClass
import com.atride.cook.ui.components.sessionPanelWidth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToSettings: () -> Unit
) {
    var selectedSessionId by remember { mutableStateOf<String?>(null) }
    var selectedModel by remember { mutableStateOf("gpt-4o") }
    var sidebarVisible by remember { mutableStateOf(true) }

    AdaptiveLayout { windowSizeClass ->
        when (windowSizeClass) {
            WindowSizeClass.Compact -> {
                CompactLayout(
                    selectedSessionId = selectedSessionId,
                    onSessionSelected = { selectedSessionId = it },
                    selectedModel = selectedModel,
                    onModelChange = { selectedModel = it },
                    onNavigateToSettings = onNavigateToSettings
                )
            }
            else -> {
                ExpandedLayout(
                    windowSizeClass = windowSizeClass,
                    selectedSessionId = selectedSessionId,
                    onSessionSelected = { selectedSessionId = it },
                    sidebarVisible = sidebarVisible,
                    onToggleSidebar = { sidebarVisible = !sidebarVisible },
                    selectedModel = selectedModel,
                    onModelChange = { selectedModel = it },
                    onNavigateToSettings = onNavigateToSettings
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompactLayout(
    selectedSessionId: String?,
    onSessionSelected: (String) -> Unit,
    selectedModel: String,
    onModelChange: (String) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                SessionsPanel(
                    selectedSessionId = selectedSessionId,
                    onSessionSelected = { sessionId ->
                        onSessionSelected(sessionId)
                        scope.launch { drawerState.close() }
                    },
                    onNewSession = {
                        onSessionSelected("s1")
                        scope.launch { drawerState.close() }
                    },
                    showNewSessionButton = true
                )
            }
        }
    ) {
        ChatArea(
            selectedSessionId = selectedSessionId,
            selectedModel = selectedModel,
            onModelChange = onModelChange,
            onNavigateToSettings = onNavigateToSettings,
            navigationIcon = {
                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                    Text("\u2630", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            },
            showModelSelector = true
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpandedLayout(
    windowSizeClass: WindowSizeClass,
    selectedSessionId: String?,
    onSessionSelected: (String) -> Unit,
    sidebarVisible: Boolean,
    onToggleSidebar: () -> Unit,
    selectedModel: String,
    onModelChange: (String) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        if (windowSizeClass == WindowSizeClass.Expanded) {
            SessionsPanel(
                selectedSessionId = selectedSessionId,
                onSessionSelected = onSessionSelected,
                onNewSession = { onSessionSelected("s1") },
                modifier = Modifier
                    .width(windowSizeClass.sessionPanelWidth())
                    .fillMaxHeight(),
                showNewSessionButton = true
            )
            HorizontalDivider(
                modifier = Modifier.fillMaxHeight().width(1.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
        } else {
            AnimatedVisibility(
                visible = sidebarVisible,
                enter = slideInHorizontally { -it },
                exit = slideOutHorizontally { -it }
            ) {
                Row {
                    SessionsPanel(
                        selectedSessionId = selectedSessionId,
                        onSessionSelected = { onSessionSelected(it); onToggleSidebar() },
                        onNewSession = { onSessionSelected("s1"); onToggleSidebar() },
                        modifier = Modifier
                            .width(260.dp)
                            .fillMaxHeight(),
                        showNewSessionButton = true
                    )
                    HorizontalDivider(
                        modifier = Modifier.fillMaxHeight().width(1.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }

        ChatArea(
            selectedSessionId = selectedSessionId,
            selectedModel = selectedModel,
            onModelChange = onModelChange,
            onNavigateToSettings = onNavigateToSettings,
            navigationIcon = if (windowSizeClass == WindowSizeClass.Medium) {
                {
                    IconButton(onClick = onToggleSidebar) {
                    Text("\u2630", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            } else null,
            showModelSelector = true
        )
    }
}

@Composable
private fun SessionsPanel(
    selectedSessionId: String?,
    onSessionSelected: (String) -> Unit,
    onNewSession: () -> Unit,
    modifier: Modifier = Modifier,
    showNewSessionButton: Boolean = false
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 8.dp)
    ) {
        if (showNewSessionButton) {
            IconButton(
                onClick = onNewSession,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("+", fontSize = 20.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(12.dp))
            }
        }
        Text(
            text = "Koog",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(
                items = MockData.sessions,
                key = { it.id }
            ) { session ->
                SessionListItem(
                    session = session,
                    isSelected = session.id == selectedSessionId,
                    onClick = { onSessionSelected(session.id) },
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatArea(
    selectedSessionId: String?,
    selectedModel: String,
    onModelChange: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    navigationIcon: @Composable (() -> Unit)? = null,
    showModelSelector: Boolean = false
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val title = MockData.sessions.find { it.id == selectedSessionId }?.title ?: "Cook"
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    if (navigationIcon != null) {
                        navigationIcon()
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Text("\u2699", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            ChatInputBar(
                selectedModel = selectedModel,
                onModelChange = onModelChange,
                onSend = { /* noop for prototype */ },
                showModelSelector = showModelSelector
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (selectedSessionId == null) {
            EmptyState(modifier = Modifier.padding(padding))
        } else {
            val messages = MockData.currentMessages
            val listState = rememberLazyListState()

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                items(
                    items = messages,
                    key = { it.id }
                ) { message ->
                    ChatBubble(message = message)
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Start a Conversation",
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Select a session or start a new one",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

