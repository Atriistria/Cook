package com.atride.cook.ui.components

import androidx.compose.runtime.Composable


@Composable
expect fun ContextMenu(
    items: List<String>,
    onItemClick: (String) -> Unit,
    content: @Composable () -> Unit,
    selectedText: String? = null,
)