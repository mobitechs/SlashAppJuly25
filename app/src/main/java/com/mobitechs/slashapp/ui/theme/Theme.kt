package com.mobitechs.slashapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext


private val LightColorScheme = lightColorScheme(
    primary = SlashColors.Primary,
    onPrimary = SlashColors.White,
    secondary = SlashColors.ButtonBackground,  // Fixed: Use ButtonBackground instead of Blue
    onSecondary = SlashColors.White,
    tertiary = SlashColors.TextSecondary,  // Fixed: Use TextSecondary instead of Gray
    background = SlashColors.Background,
    surface = SlashColors.White,
    onBackground = SlashColors.TextPrimary,  // Fixed: Use TextPrimary instead of Surface
    onSurface = SlashColors.TextPrimary,  // Fixed: Use TextPrimary instead of Surface
    error = SlashColors.TextError,  // Fixed: Use TextError instead of Error
    onError = SlashColors.White
)

private val DarkColorScheme = darkColorScheme(
    primary = SlashColors.Primary,
    onPrimary = SlashColors.White,
    secondary = SlashColors.ButtonBackground,
    onSecondary = SlashColors.White,
    tertiary = SlashColors.TextSecondary,
    background = SlashColors.TextPrimary,  // Dark background
    surface = SlashColors.TextPrimary,
    onBackground = SlashColors.White,
    onSurface = SlashColors.White,
    error = SlashColors.TextError,
    onError = SlashColors.White
)

@Composable
fun SlashTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}