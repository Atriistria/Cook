package com.atride.cook.navigation

class Navigator(val state: NavigationState) {

    fun navigate(key: Route) {
        when (key) {
            state.currentTopLevelKey -> clearSubStack()
            in state.topLevelKeys -> goToTopLevel(key)
            else -> state.currentSubStack.apply {
                removeAll { it::class == key::class }
                add(key)
            }
        }
    }

    fun goBack() {
        when (state.currentKey) {
            state.startKey -> error("")
            state.currentTopLevelKey -> state.topLevelStack.removeLastOrNull()
            else -> state.currentSubStack.removeLastOrNull()
        }
    }

    private fun goToTopLevel(key: Route) {
        state.topLevelStack.apply {
            if (key == state.startKey) clear()
            else {
                removeAll { it == key }
                add(key)
            }
        }
    }

    private fun clearSubStack() {
        val stack = state.currentSubStack
        while (stack.size > 1) stack.removeLastOrNull()
    }
}