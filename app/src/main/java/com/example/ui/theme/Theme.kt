package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
  primary = ActionGreenLight,
  onPrimary = DarkSurface,
  primaryContainer = ForestMedium,
  onPrimaryContainer = DarkText,
  secondary = ColorTankLight,
  onSecondary = DarkSurface,
  tertiary = ColorSoilLight,
  background = DarkBg,
  surface = DarkSurface,
  surfaceVariant = DarkSurfaceVariant,
  onBackground = DarkText,
  onSurface = DarkText,
  onSurfaceVariant = DarkTextMuted,
  outline = DarkBorder,
  outlineVariant = DarkBorderSubtle,
  error = StatusCritLight,
  onError = DarkSurface
)

private val LightColorScheme = lightColorScheme(
  primary = ForestDark,
  onPrimary = LightSurface,
  primaryContainer = ActionGreen,
  onPrimaryContainer = LightSurface,
  secondary = ColorTank,
  onSecondary = LightSurface,
  tertiary = ColorSoil,
  background = LightBg,
  surface = LightSurface,
  surfaceVariant = LightSurfaceVariant,
  onBackground = LightText,
  onSurface = LightText,
  onSurfaceVariant = LightTextMuted,
  outline = LightBorder,
  outlineVariant = LightBorderSubtle,
  error = StatusCrit,
  onError = LightSurface
)

@Composable
fun SmartAgriTheme(
  themeMode: String = "auto", // "light", "dark", "auto"
  content: @Composable () -> Unit
) {
  val systemDark = isSystemInDarkTheme()
  val darkTheme = when (themeMode) {
    "dark" -> true
    "light" -> false
    else -> systemDark
  }

  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window
      if (window != null) {
        window.statusBarColor = (if (darkTheme) DarkSurface else ForestDark).toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme && false
      }
    }
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
