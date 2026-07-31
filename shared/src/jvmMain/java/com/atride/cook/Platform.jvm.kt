package com.atride.cook

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import java.awt.Cursor

actual val isTouchPlatform: Boolean
    get() = false

@Composable
actual fun DraggableDivider(
    isDragging: Boolean,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit
) {
    var dragging by remember { mutableStateOf(false) }

    LaunchedEffect(dragging) {
        val window = java.awt.Window.getWindows().firstOrNull { it.isActive }
        if (dragging) {
            window?.cursor = Cursor(Cursor.E_RESIZE_CURSOR)
        } else {
            window?.cursor = Cursor.getDefaultCursor()
        }
    }

    Box(
        modifier = Modifier
            .width(12.dp)
            .fillMaxHeight()
            .pointerHoverIcon(PointerIcon(Cursor(Cursor.E_RESIZE_CURSOR)))
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { dragging = true },
                    onDragEnd = {
                        dragging = false
                        onDragEnd()
                    },
                    onHorizontalDrag = { _, dragAmount -> onDrag(dragAmount) },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .width(2.dp)
                .fillMaxHeight()
                .background(Color.LightGray),
        )
    }
}