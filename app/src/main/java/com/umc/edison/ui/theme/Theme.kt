package com.umc.edison.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorPalette = darkColorScheme(
    primary = Main,
    secondary = Aqua300,
    background = Black000,
    surface = Gray800,
    error = Red300,
    onPrimary = White000,
    onSecondary = Black000,
    onBackground = White000,
    onSurface = White000,
    onError = Black000
)


private val LightColorPalette = lightColorScheme(
    primary = Main,
    secondary = Aqua500,
    background = Gray10,
    surface = White000,
    error = Red500,
    onPrimary = Black000,
    onSecondary = White000,
    onBackground = Gray900,
    onSurface = Gray900,
    onError = White000
)

@Composable
fun EdisonTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorPalette
        else -> LightColorPalette
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
