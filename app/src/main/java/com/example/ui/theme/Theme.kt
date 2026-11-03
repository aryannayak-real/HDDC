package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val HDDCColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = BackgroundNavy,
    primaryContainer = GoldMuted,
    onPrimaryContainer = TextPrimary,
    secondary = AmberAccent,
    onSecondary = BackgroundNavy,
    tertiary = CyanHUD,
    onTertiary = BackgroundNavy,
    background = BackgroundNavy,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    outlineVariant = BorderActive,
    error = LossRed,
    onError = TextPrimary
)

@Composable
fun HDDCTheme(
    darkTheme: Boolean = true, // HDDC is a dedicated dark gaming intelligence interface
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = BackgroundNavy.toArgb()
                window.navigationBarColor = BackgroundNavy.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = false
                insetsController.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = HDDCColorScheme,
        typography = Typography,
        content = content
    )
}
