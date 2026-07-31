package com.atride.cook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.atride.cook.ui.rememberCookAppState
import com.atride.cook.ui.theme.CookTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            CookTheme {
                val appState = rememberCookAppState()
                CookApp(appState)
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
}