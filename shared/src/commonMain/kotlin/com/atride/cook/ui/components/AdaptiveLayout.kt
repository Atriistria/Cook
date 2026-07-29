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
    Compact,     // 0 - 600dp (手机竖屏)
    Medium,      // 600 - 840dp (折叠屏或平板竖屏)
    Expanded,    // 840 - 1200dp (平板横屏)
    Large,       // 1200 - 1600dp (普通电脑窗口)
    ExtraLarge   // 1600dp+ (宽屏显示器)
}

enum class LayoutMode { SINGLE_PANE, DUAL_PANE, TRIPLE_PANE }

fun WindowSizeClass.layoutMode(): LayoutMode = when (this) {
    WindowSizeClass.Compact -> LayoutMode.SINGLE_PANE
    WindowSizeClass.Medium,
    WindowSizeClass.Expanded -> LayoutMode.DUAL_PANE

    WindowSizeClass.Large,
    WindowSizeClass.ExtraLarge -> LayoutMode.TRIPLE_PANE
}

enum class ThemeMode { LIGHT, DARK, SYSTEM }

fun getWindowSizeClass(width: Dp): WindowSizeClass {
    return when {
        width < 600.dp -> WindowSizeClass.Compact
        width < 840.dp -> WindowSizeClass.Medium
        width < 1200.dp -> WindowSizeClass.Expanded
        width < 1600.dp -> WindowSizeClass.Large
        else -> WindowSizeClass.ExtraLarge
    }
}

@Composable
fun AdaptiveLayout(
    content: @Composable (WindowSizeClass) -> Unit
) {
    BoxWithConstraints {
        val windowSizeClass = getWindowSizeClass(maxWidth)
        content(windowSizeClass)
    }
}

const val SESSION_PANEL_WIDTH_COMPACT = 0
const val SESSION_PANEL_WIDTH_MEDIUM = 260
const val SESSION_PANEL_WIDTH_EXPANDED = 300
const val SESSION_PANEL_WIDTH_LARGE = 360
const val SESSION_PANEL_WIDTH_ExtraLarge = 400

fun WindowSizeClass.sessionPanelWidth(): Dp = when (this) {
    WindowSizeClass.Compact -> SESSION_PANEL_WIDTH_COMPACT.dp           // 手机窄屏，不显示侧栏
    WindowSizeClass.Medium -> SESSION_PANEL_WIDTH_MEDIUM.dp          // 平板竖屏，窄侧栏
    WindowSizeClass.Expanded -> SESSION_PANEL_WIDTH_EXPANDED.dp        // 平板横屏，中等侧栏
    WindowSizeClass.Large -> SESSION_PANEL_WIDTH_LARGE.dp           // 桌面窗口，宽侧栏
    WindowSizeClass.ExtraLarge -> SESSION_PANEL_WIDTH_ExtraLarge.dp      // 宽屏，更宽
}