package com.atride.cook.model

enum class InteractionMode {
    TOUCH_PRIMARY,
    MOUSE_PRIMARY
}

expect val currentInteractionMode: InteractionMode
