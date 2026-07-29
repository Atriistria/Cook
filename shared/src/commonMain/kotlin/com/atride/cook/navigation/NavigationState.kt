package com.atride.cook.navigation

class NavigationState(
    val startKey: Route,
    val topLevelStack: MutableList<Route>,
    val subStacks: Map<Route, MutableList<Route>>
){
    val currentTopLevelKey: Route
        get() = topLevelStack.last()

    val topLevelKeys: Set<Route>
        get() = subStacks.keys

    val currentSubStack: MutableList<Route>
        get() = subStacks[currentTopLevelKey]
            ?: error("")

    val currentKey: Route
        get() = currentSubStack.last()
}