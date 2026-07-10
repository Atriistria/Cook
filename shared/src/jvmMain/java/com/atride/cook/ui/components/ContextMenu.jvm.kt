package com.atride.cook.ui.components

import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.runtime.Composable
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

@Composable
actual fun ContextMenu(
    items: List<String>,
    onItemClick: (String) -> Unit,
    content: @Composable (() -> Unit),
    selectedText: String?,
) {
    ContextMenuArea(
        items = {
            items.map { label ->
                ContextMenuItem(label) {
                    if (label == "复制" && selectedText != null) {
                        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
                        clipboard.setContents(StringSelection(selectedText), null)
                    }
                    onItemClick(label)
                }
            }
        }
    ) {
        content()
    }
}

fun copyToClipboard(text: String) {
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    clipboard.setContents(StringSelection(text), null)
}

