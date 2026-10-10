package dev.PC22200322y22200350.pc1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import dev.PC22200322y22200350.pc1.presentation.navigation.AppNavGraph
import dev.PC22200322y22200350.pc1.ui.theme.PC1Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PC1Theme(darkTheme = false, dynamicColor = false) {
                val view = LocalView.current
                SideEffect {
                    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
                    WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = true
                }
                Box(Modifier.windowInsetsPadding(WindowInsets.safeDrawing)) { AppNavGraph() }
            }
        }
    }
}