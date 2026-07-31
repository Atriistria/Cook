package com.atride.cook

import androidx.compose.runtime.Composable

actual val isTouchPlatform: Boolean
    get() = true

@Composable
actual fun DraggableDivider(
    isDragging: Boolean,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit
) {
}