package com.atride.cook

import androidx.compose.runtime.Composable

expect val isTouchPlatform: Boolean

@Composable
expect fun DraggableDivider(
    isDragging: Boolean,
    onDrag: (Float) -> Unit,     // delta px
    onDragEnd: () -> Unit,
)