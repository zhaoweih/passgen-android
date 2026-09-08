package dev.passgen.app.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * The palette from the Password Generator design. It sits alongside the Material color scheme
 * because every surface in this app is drawn from the design's own tokens rather than from
 * Material roles.
 */
@Immutable
data class AppColors(
  val pageBg: Color,
  val cardBg: Color,
  val border: Color,
  val trackBg: Color,
  val ink: Color,
  val label: Color,
  val muted: Color,
  val accent: Color,
  val knobBg: Color,
  val toggleOff: Color,
  val toggleKnob: Color,
  val buttonBg: Color,
  val buttonFg: Color,
  val rowBorder: Color,
  val noteBg: Color,
  val noteBorder: Color,
  val digit: Color,
  val symbol: Color,
  val tiers: List<Color>,
)

val LightAppColors =
  AppColors(
    pageBg = Color(0xFFFAF9F5),
    cardBg = Color(0xFFFFFFFF),
    border = Color(0xFFE3DFD3),
    trackBg = Color(0xFFE6E2D6),
    ink = Color(0xFF1F1E1D),
    label = Color(0xFF57544D),
    muted = Color(0xFF6B675F),
    accent = Color(0xFFC1603F),
    knobBg = Color(0xFFFFFFFF),
    toggleOff = Color(0xFFDDD8CA),
    toggleKnob = Color(0xFFFFFFFF),
    buttonBg = Color(0xFFA94E30),
    buttonFg = Color(0xFFFFFFFF),
    rowBorder = Color(0xFFEFECE3),
    noteBg = Color(0xFFF2EFE4),
    noteBorder = Color(0xFFE6E2D6),
    digit = Color(0xFFC1603F),
    symbol = Color(0xFF8A7A52),
    tiers = listOf(Color(0xFFB8453A), Color(0xFFC58A2E), Color(0xFF5F7D52), Color(0xFF4C6B41)),
  )

val DarkAppColors =
  AppColors(
    pageBg = Color(0xFF262624),
    cardBg = Color(0xFF30302E),
    border = Color(0xFF403F3B),
    trackBg = Color(0xFF454440),
    ink = Color(0xFFF5F4EF),
    label = Color(0xFFC5C2B9),
    muted = Color(0xFFA8A49B),
    accent = Color(0xFFD97757),
    knobBg = Color(0xFFF5F4EF),
    toggleOff = Color(0xFF4D4C47),
    toggleKnob = Color(0xFFF5F4EF),
    buttonBg = Color(0xFFD97757),
    buttonFg = Color(0xFF26211F),
    rowBorder = Color(0xFF3A3936),
    noteBg = Color(0xFF2C2B28),
    noteBorder = Color(0xFF3F3E3A),
    digit = Color(0xFFE89A7F),
    symbol = Color(0xFFD3BD86),
    tiers = listOf(Color(0xFFE0705F), Color(0xFFDFAB52), Color(0xFF8FB37C), Color(0xFFA3C48E)),
  )

val LocalAppColors = staticCompositionLocalOf { LightAppColors }
