package dev.passgen.app.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

/** Access to the design palette from anywhere inside [PasswordGeneratorTheme]. */
object AppTheme {
  val colors: AppColors
    @Composable @ReadOnlyComposable get() = LocalAppColors.current
}

@Composable
fun PasswordGeneratorTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
  val appColors = if (darkTheme) DarkAppColors else LightAppColors

  // Material is only a fallback here: the screen paints itself from `appColors`. Mapping the design
  // tokens onto the scheme keeps any stock component (ripples, text selection) on-brand.
  val colorScheme =
    if (darkTheme) {
      darkColorScheme(
        primary = appColors.accent,
        onPrimary = appColors.buttonFg,
        background = appColors.pageBg,
        onBackground = appColors.ink,
        surface = appColors.cardBg,
        onSurface = appColors.ink,
        outline = appColors.border,
      )
    } else {
      lightColorScheme(
        primary = appColors.accent,
        onPrimary = appColors.buttonFg,
        background = appColors.pageBg,
        onBackground = appColors.ink,
        surface = appColors.cardBg,
        onSurface = appColors.ink,
        outline = appColors.border,
      )
    }

  CompositionLocalProvider(LocalAppColors provides appColors) {
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
  }
}
