package com.atride.cook

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.atride.cook.di.initKoin
import com.atride.cook.ui.rememberCookAppState
import com.atride.cook.ui.theme.CookTheme
import java.io.PrintStream

fun main() {
    System.setOut(PrintStream(System.out, true, "UTF-8"))
    application {
        initKoin()
        Window(
            onCloseRequest = ::exitApplication,
            title = "Cook",
        ) {
            CookTheme {
                CookApp(
                    rememberCookAppState()
                )
            }
        }
    }
}