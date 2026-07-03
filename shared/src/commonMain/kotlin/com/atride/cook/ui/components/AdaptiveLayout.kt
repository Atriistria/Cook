package com.atride.cook.ui.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class WindowSizeClass {
    Compact, Medium, Expanded
}

@Composable
fun AdaptiveLayout(
    content: @Composable (WindowSizeClass) -> Unit
) {
    BoxWithConstraints {
        val windowSizeClass = when {
            maxWidth < 600.dp -> WindowSizeClass.Compact
            maxWidth < 840.dp -> WindowSizeClass.Medium
            else -> WindowSizeClass.Expanded
        }
        content(windowSizeClass)
    }
}

const val SESSION_PANEL_WIDTH_COMPACT = 0
const val SESSION_PANEL_WIDTH_MEDIUM = 260
const val SESSION_PANEL_WIDTH_EXPANDED = 300

fun WindowSizeClass.sessionPanelWidth(): Dp = when (this) {
    WindowSizeClass.Compact -> SESSION_PANEL_WIDTH_COMPACT.dp
    WindowSizeClass.Medium -> SESSION_PANEL_WIDTH_MEDIUM.dp
    WindowSizeClass.Expanded -> SESSION_PANEL_WIDTH_EXPANDED.dp
}
