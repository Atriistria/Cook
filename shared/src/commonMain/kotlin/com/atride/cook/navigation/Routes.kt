package com.atride.cook.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {
    @Serializable
    data object SessionList : Route

    @Serializable
    data class SessionDetail(val sessionId: String) : Route

    @Serializable
    data object Settings : Route
}