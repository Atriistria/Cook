package com.atride.cook.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.atride.cook.navigation.NavigationState
import com.atride.cook.navigation.Route
import com.atride.cook.ui.components.ThemeMode
import com.atride.cook.ui.components.WindowSizeClass
import com.atride.cook.ui.components.getWindowSizeClass
import kotlinx.coroutines.CoroutineScope

@Composable
fun rememberWindowSizeClass(): State<WindowSizeClass> {
    val state = remember { mutableStateOf(WindowSizeClass.Compact) }

    BoxWithConstraints {
        val widthClass = when {
            maxWidth < 600.dp -> WindowSizeClass.Compact
            maxWidth < 840.dp -> WindowSizeClass.Medium
            else -> WindowSizeClass.Expanded
        }
        SideEffect { state.value = widthClass }
    }
    return state
}

@Composable
fun rememberCookAppState(
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
): CookAppState {
    val windowSizeClassState = remember { mutableStateOf(WindowSizeClass.Compact) }

    BoxWithConstraints {
        val current = getWindowSizeClass(maxWidth)
        SideEffect { windowSizeClassState.value = current }
    }

    return remember { CookAppState(windowSizeClassState, coroutineScope) }
}
@Stable
class CookAppState(
    val windowSizeClass: State<WindowSizeClass>,
    val coroutineScope: CoroutineScope,
) {
    var selectedModelId: String by mutableStateOf("DeepSeek-V4-flash")
    var themeMode: ThemeMode by mutableStateOf(ThemeMode.SYSTEM)
}