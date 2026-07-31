package com.atride.cook

import androidx.compose.ui.window.ComposeUIViewController
import com.atride.cook.ui.rememberCookAppState
import com.atride.cook.ui.theme.CookTheme

fun MainViewController() = ComposeUIViewController {
    CookTheme {
        val appState = rememberCookAppState()
        CookApp(appState)
    }
}