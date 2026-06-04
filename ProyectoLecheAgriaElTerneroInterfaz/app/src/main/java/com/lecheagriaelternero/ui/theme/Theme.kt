package com.lecheagriaelternero.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = TerneroGreen,
    onPrimary = Color.White,
    secondary = SurfaceDark,
    onSecondary = Color.White,
    tertiary = TerneroGreenLight,
    background = BackgroundLight,
    surface = CardBackground,
    onSurface = SurfaceDark
)

@Composable
fun LecheAgriaElTerneroTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {

    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}