package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = PremiereBlue,
    secondary = NeonCyan,
    tertiary = PremiereOrange,
    background = DarkSlateBackground,
    surface = SurfaceSlate,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
  )

private val LightColorScheme = DarkColorScheme // Force dark workspace always for professional look

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // default to true for professional dark workspace
  dynamicColor: Boolean = false, // false to prevent system override of the editor theme
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme // force professional dark color palette

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
