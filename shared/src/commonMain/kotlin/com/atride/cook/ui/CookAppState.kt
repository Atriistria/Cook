package com.atride.cook.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXTRA_LARGE_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_LARGE_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import kotlinx.coroutines.CoroutineScope

@Composable
fun rememberCookAppState(
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
): CookAppState {
    return remember { CookAppState(coroutineScope) }
}

@Stable
class CookAppState(
    val coroutineScope: CoroutineScope,
) {
    var selectedSessionId by mutableStateOf<String?>(null)
}

fun sessionPanelWidth(wsc: WindowSizeClass, isTouch: Boolean): Dp = when {
    isTouch && !wsc.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND) -> 0.dp
    wsc.isWidthAtLeastBreakpoint(WIDTH_DP_EXTRA_LARGE_LOWER_BOUND) -> 400.dp
    wsc.isWidthAtLeastBreakpoint(WIDTH_DP_LARGE_LOWER_BOUND) -> 360.dp
    wsc.isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND) -> 300.dp
    wsc.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND) -> 260.dp
    else -> 200.dp
}