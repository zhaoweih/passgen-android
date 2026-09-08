package dev.passgen.app.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import dev.passgen.app.R

val HankenGrotesk =
  FontFamily(
    Font(R.font.hanken_grotesk_regular, FontWeight.Normal),
    Font(R.font.hanken_grotesk_medium, FontWeight.Medium),
    Font(R.font.hanken_grotesk_semibold, FontWeight.SemiBold),
    Font(R.font.hanken_grotesk_bold, FontWeight.Bold),
  )

val InstrumentSerif =
  FontFamily(
    Font(R.font.instrument_serif_regular, FontWeight.Normal),
    Font(R.font.instrument_serif_italic, FontWeight.Normal, FontStyle.Italic),
  )

val IbmPlexMono =
  FontFamily(
    Font(R.font.ibm_plex_mono_regular, FontWeight.Normal),
    Font(R.font.ibm_plex_mono_medium, FontWeight.Medium),
  )

/**
 * The design lays text out with CSS line boxes, so drop the font's built-in padding and center the
 * glyphs in the line height to keep the vertical rhythm identical.
 */
@Suppress("DEPRECATION")
val TightPlatformStyle = PlatformTextStyle(includeFontPadding = false)

val TightLineHeight =
  LineHeightStyle(alignment = LineHeightStyle.Alignment.Center, trim = LineHeightStyle.Trim.None)

private val Default = Typography()

val Typography =
  Typography(
    displayLarge = Default.displayLarge.copy(fontFamily = HankenGrotesk),
    displayMedium = Default.displayMedium.copy(fontFamily = HankenGrotesk),
    displaySmall = Default.displaySmall.copy(fontFamily = HankenGrotesk),
    headlineLarge = Default.headlineLarge.copy(fontFamily = HankenGrotesk),
    headlineMedium = Default.headlineMedium.copy(fontFamily = HankenGrotesk),
    headlineSmall = Default.headlineSmall.copy(fontFamily = HankenGrotesk),
    titleLarge = Default.titleLarge.copy(fontFamily = HankenGrotesk),
    titleMedium = Default.titleMedium.copy(fontFamily = HankenGrotesk),
    titleSmall = Default.titleSmall.copy(fontFamily = HankenGrotesk),
    bodyLarge = Default.bodyLarge.copy(fontFamily = HankenGrotesk),
    bodyMedium = Default.bodyMedium.copy(fontFamily = HankenGrotesk),
    bodySmall = Default.bodySmall.copy(fontFamily = HankenGrotesk),
    labelLarge = Default.labelLarge.copy(fontFamily = HankenGrotesk),
    labelMedium = Default.labelMedium.copy(fontFamily = HankenGrotesk),
    labelSmall = Default.labelSmall.copy(fontFamily = HankenGrotesk),
  )
