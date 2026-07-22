package org.dlof.tm.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = DlofGreen,
    onPrimary = DlofWhite,
    secondary = DlofGreenLight,
    onSecondary = DlofBlack,
    background = DlofWhite,
    surface = DlofWhite,
    onBackground = DlofBlack,
    onSurface = DlofBlack,
    error = DlofErrorRed
)

private val DarkColors = darkColorScheme(
    primary = DlofGreenLight,
    onPrimary = DlofBlack,
    secondary = DlofGreen,
    onSecondary = DlofWhite,
    background = DlofSurfaceDark,
    surface = DlofSurfaceDark,
    onBackground = DlofWhite,
    onSurface = DlofWhite,
    error = DlofErrorRed
)

@Composable
fun DlofTmTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        val activity = view.context as? Activity
        activity?.let {
            it.window.statusBarColor = DlofGreenDark.toArgb()
            WindowCompat.getInsetsController(it.window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = DlofTypography,
        content = content
    )
}
