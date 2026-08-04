package com.atride.cook.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic


@OptIn(ExperimentalSerializationApi::class)
private val config: SavedStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclassesOfSealed<Route>()
        }
    }
}

@Composable
fun rememberCookNavigator(
    backStack: MutableList<NavKey> = rememberNavBackStack(config, Route.SessionList)
): CookNavigator {
    return remember(backStack) {
        CookNavigator(backStack)
    }
}

class CookNavigator(val backStack: MutableList<NavKey>) {

    val currentRoute: Route?
        get() = backStack.lastOrNull() as? Route

    val selectedSessionId: String?
        get() = (backStack.lastOrNull() as? Route.SessionDetail)?.sessionId

    fun openSession(id: String) {
        backStack.removeAll { it is Route.SessionDetail }
        backStack.add(Route.SessionDetail(id))
    }

    fun pushSettings() {
        backStack.removeAll { it is Route.Settings }
        backStack.add(Route.Settings)
    }

    fun goBack() {
        if (backStack.size > 1) {
            backStack.removeLast()
        }
    }
}