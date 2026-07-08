package com.atride.cook

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.atride.cook.navigation.MainRoute
import com.atride.cook.navigation.SettingsRoute
import com.atride.cook.ui.screens.MainScreen
import com.atride.cook.ui.screens.SettingsScreen
import com.atride.cook.ui.screens.chat.ChatViewModel
import com.atride.cook.ui.theme.CookTheme


@Composable
fun App() {
    CookTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val navController = rememberNavController()
            NavHost(
                navController = navController,
                startDestination = MainRoute
            ) {
                composable<MainRoute> {
                    MainScreen(
                        onNavigateToSettings = {
                            navController.navigate(SettingsRoute)
                        },

                    )
                }
                composable<SettingsRoute> {
                    SettingsScreen(
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}
