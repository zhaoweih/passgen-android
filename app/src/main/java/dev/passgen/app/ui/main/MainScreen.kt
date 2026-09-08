package dev.passgen.app.ui.main

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.PersistableBundle
import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.passgen.app.R
import dev.passgen.app.data.MAX_LENGTH
import dev.passgen.app.data.MIN_LENGTH
import dev.passgen.app.data.PasswordOption
import dev.passgen.app.data.Strength
import dev.passgen.app.theme.AppTheme
import dev.passgen.app.theme.HankenGrotesk
import dev.passgen.app.theme.IbmPlexMono
import dev.passgen.app.theme.InstrumentSerif
import dev.passgen.app.theme.PasswordGeneratorTheme
import dev.passgen.app.theme.TightLineHeight
import dev.passgen.app.theme.TightPlatformStyle
import kotlin.math.roundToInt

private val CardShape = RoundedCornerShape(18.dp)
private val ButtonShape = RoundedCornerShape(13.dp)
private val SquareButtonShape = RoundedCornerShape(12.dp)
private val FooterShape = RoundedCornerShape(14.dp)
private val PillShape = RoundedCornerShape(99.dp)

@Composable
fun MainScreen(
  isDarkTheme: Boolean,
  onToggleTheme: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: MainScreenViewModel = viewModel { MainScreenViewModel() },
) {
  val state by viewModel.uiState.collectAsStateWithLifecycle()
  val context = LocalContext.current
  val haptics = LocalHapticFeedback.current

  MainScreen(
    state = state,
    isDarkTheme = isDarkTheme,
    onToggleTheme = onToggleTheme,
    onLengthChange = viewModel::setLength,
    onToggleOption = viewModel::toggleOption,
    onRegenerate = { viewModel.regenerate() },
    onCopy = {
      context.copyPassword(state.password)
      haptics.performHapticFeedback(HapticFeedbackType.LongPress)
      viewModel.onPasswordCopied()
    },
    modifier = modifier,
  )
}

@Composable
internal fun MainScreen(
  state: MainScreenUiState,
  isDarkTheme: Boolean,
  onToggleTheme: () -> Unit,
  onLengthChange: (Int) -> Unit,
  onToggleOption: (PasswordOption) -> Unit,
  onRegenerate: () -> Unit,
  onCopy: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val colors = AppTheme.colors
  Column(
    modifier =
      modifier
        .fillMaxSize()
        .background(colors.pageBg)
        .windowInsetsPadding(WindowInsets.safeDrawing)
        .verticalScroll(rememberScrollState())
        .padding(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 20.dp),
    verticalArrangement = Arrangement.spacedBy(18.dp),
  ) {
    Header(isDarkTheme = isDarkTheme, onToggleTheme = onToggleTheme)
    PasswordCard(state = state, onCopy = onCopy, onRegenerate = onRegenerate)
    LengthSection(length = state.length, onLengthChange = onLengthChange)
    OptionsCard(enabled = state.options, onToggleOption = onToggleOption)
    PrivacyNote()
  }
}

@Composable
private fun Header(isDarkTheme: Boolean, onToggleTheme: () -> Unit) {
  val colors = AppTheme.colors
  val titleStyle =
    TextStyle(
      fontFamily = InstrumentSerif,
      fontSize = 32.sp,
      lineHeight = 35.2.sp,
      letterSpacing = (-0.32).sp,
      platformStyle = TightPlatformStyle,
      lineHeightStyle = TightLineHeight,
    )

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalAlignment = Alignment.Top,
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(stringResource(R.string.title_line_one), style = titleStyle, color = colors.ink)
      Text(
        stringResource(R.string.title_line_two),
        style = titleStyle.copy(fontStyle = FontStyle.Italic),
        color = colors.accent,
      )
    }
    SquareButton(
      // U+FE0E keeps the sun as a text glyph instead of a colour emoji.
      glyph = if (isDarkTheme) "\u2600\uFE0E" else "\u263E",
      fontSize = 15.sp,
      contentDescription =
        stringResource(if (isDarkTheme) R.string.switch_to_light_theme else R.string.switch_to_dark_theme),
      onClick = onToggleTheme,
    )
  }
}

@Composable
private fun SquareButton(
  glyph: String,
  fontSize: androidx.compose.ui.unit.TextUnit,
  contentDescription: String,
  onClick: () -> Unit,
) {
  val colors = AppTheme.colors
  Box(
    modifier =
      Modifier
        .size(38.dp)
        .clip(SquareButtonShape)
        .background(colors.cardBg)
        .border(1.dp, colors.border, SquareButtonShape)
        .clickable(onClick = onClick)
        .semantics { this.contentDescription = contentDescription },
    contentAlignment = Alignment.Center,
  ) {
    Text(glyph, fontSize = fontSize, color = colors.muted)
  }
}

@Composable
private fun PasswordCard(state: MainScreenUiState, onCopy: () -> Unit, onRegenerate: () -> Unit) {
  val colors = AppTheme.colors
  val passwordDescription = stringResource(R.string.generated_password)

  Column(
    modifier =
      Modifier
        .fillMaxWidth()
        .clip(CardShape)
        .background(colors.cardBg)
        .border(1.dp, colors.border, CardShape)
        .padding(18.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Box(modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp), contentAlignment = Alignment.CenterStart) {
      Text(
        text = state.displayedPassword.toColoredPassword(),
        style =
          TextStyle(
            fontFamily = IbmPlexMono,
            fontWeight = FontWeight.Medium,
            fontSize = 22.sp,
            lineHeight = 31.9.sp,
            letterSpacing = 0.22.sp,
            platformStyle = TightPlatformStyle,
            lineHeightStyle = TightLineHeight,
          ),
        modifier = Modifier.semantics { contentDescription = passwordDescription },
      )
    }

    StrengthMeter(strength = state.strength)

    val crackTime = state.crackTime
    val line = stringResource(R.string.crack_time_line, crackTime, state.entropy)
    val emphasisStart = line.indexOf(crackTime)
    Text(
      text =
        buildAnnotatedString {
          append(line)
          if (emphasisStart >= 0) {
            addStyle(
              SpanStyle(fontWeight = FontWeight.SemiBold, color = colors.ink),
              emphasisStart,
              emphasisStart + crackTime.length,
            )
          }
        },
      fontFamily = HankenGrotesk,
      fontSize = 12.sp,
      lineHeight = 16.8.sp,
      color = colors.muted,
    )

    Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
      Box(
        modifier =
          Modifier
            .weight(1f)
            .height(46.dp)
            .clip(ButtonShape)
            .background(colors.buttonBg)
            .clickable(onClick = onCopy),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = stringResource(if (state.copied) R.string.copied else R.string.copy_password),
          fontFamily = HankenGrotesk,
          fontSize = 14.5.sp,
          fontWeight = FontWeight.SemiBold,
          color = colors.buttonFg,
        )
      }
      val regenerateDescription = stringResource(R.string.regenerate)
      Box(
        modifier =
          Modifier
            .size(46.dp)
            .clip(ButtonShape)
            .background(colors.pageBg)
            .border(1.dp, colors.border, ButtonShape)
            .clickable(onClick = onRegenerate)
            .semantics { contentDescription = regenerateDescription },
        contentAlignment = Alignment.Center,
      ) {
        Text("↻", fontSize = 18.sp, color = colors.ink)
      }
    }
  }
}

/** Tints digits and symbols so the shape of a random string is readable at a glance. */
@Composable
private fun String.toColoredPassword(): AnnotatedString {
  val colors = AppTheme.colors
  return buildAnnotatedString {
    for (ch in this@toColoredPassword) {
      val color =
        when {
          ch.isDigit() -> colors.digit
          ch.isLetter() -> colors.ink
          else -> colors.symbol
        }
      withStyle(SpanStyle(color = color)) { append(ch) }
    }
  }
}

@Composable
private fun StrengthMeter(strength: Strength) {
  val colors = AppTheme.colors
  val tierColor = colors.tiers[strength.ordinal]
  val fraction by animateFloatAsState(strength.fraction, tween(300), label = "strengthFraction")
  val animatedColor by animateColorAsState(tierColor, tween(300), label = "strengthColor")

  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
    Box(modifier = Modifier.weight(1f).height(4.dp).clip(PillShape).background(colors.trackBg)) {
      Box(modifier = Modifier.fillMaxWidth(fraction).height(4.dp).clip(PillShape).background(animatedColor))
    }
    Text(
      text = stringResource(strength.labelRes),
      fontFamily = HankenGrotesk,
      fontSize = 12.5.sp,
      fontWeight = FontWeight.SemiBold,
      color = animatedColor,
    )
  }
}

private val Strength.labelRes: Int
  @StringRes
  get() =
    when (this) {
      Strength.WEAK -> R.string.strength_weak
      Strength.FAIR -> R.string.strength_fair
      Strength.STRONG -> R.string.strength_strong
      Strength.VERY_STRONG -> R.string.strength_very_strong
    }

@Composable
private fun LengthSection(length: Int, onLengthChange: (Int) -> Unit) {
  val colors = AppTheme.colors
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Row(modifier = Modifier.fillMaxWidth()) {
      Text(
        text = stringResource(R.string.length),
        fontFamily = HankenGrotesk,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = colors.label,
        modifier = Modifier.alignByBaseline(),
      )
      Spacer(Modifier.weight(1f))
      Text(
        text = length.toString(),
        fontFamily = IbmPlexMono,
        fontSize = 26.sp,
        lineHeight = 26.sp,
        color = colors.ink,
        modifier = Modifier.alignByBaseline(),
      )
    }

    LengthSlider(length = length, onLengthChange = onLengthChange)

    Row(modifier = Modifier.fillMaxWidth()) {
      val edgeStyle =
        TextStyle(fontFamily = IbmPlexMono, fontSize = 10.5.sp, color = colors.muted)
      Text(MIN_LENGTH.toString(), style = edgeStyle)
      Spacer(Modifier.weight(1f))
      Text(MAX_LENGTH.toString(), style = edgeStyle)
    }
  }
}

@Composable
private fun LengthSlider(length: Int, onLengthChange: (Int) -> Unit) {
  val colors = AppTheme.colors
  val density = LocalDensity.current
  val label = stringResource(R.string.password_length)
  var trackWidth by remember { mutableIntStateOf(0) }
  val fraction = (length - MIN_LENGTH).toFloat() / (MAX_LENGTH - MIN_LENGTH)

  val updateFromX: (Float) -> Unit = { x ->
    if (trackWidth > 0) {
      val ratio = (x / trackWidth).coerceIn(0f, 1f)
      onLengthChange(MIN_LENGTH + (ratio * (MAX_LENGTH - MIN_LENGTH)).roundToInt())
    }
  }

  Box(
    modifier =
      Modifier
        .fillMaxWidth()
        .height(32.dp)
        .onSizeChanged { trackWidth = it.width }
        .pointerInput(trackWidth) { detectTapGestures { updateFromX(it.x) } }
        .pointerInput(trackWidth) {
          detectHorizontalDragGestures(
            onDragStart = { updateFromX(it.x) },
            onHorizontalDrag = { change, _ ->
              updateFromX(change.position.x)
              change.consume()
            },
          )
        }
        .semantics { contentDescription = label },
    contentAlignment = Alignment.CenterStart,
  ) {
    Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(PillShape).background(colors.trackBg))
    Box(
      modifier =
        Modifier
          .width(with(density) { (fraction * trackWidth).toDp() })
          .height(6.dp)
          .clip(PillShape)
          .background(colors.accent)
    )
    Box(
      modifier =
        Modifier
          .offset { IntOffset((fraction * trackWidth - 12.dp.toPx()).roundToInt(), 0) }
          .size(24.dp)
          .shadow(3.dp, CircleShape)
          .background(colors.knobBg, CircleShape)
          .border(1.dp, colors.border, CircleShape)
    )
  }
}

@Composable
private fun OptionsCard(enabled: Set<PasswordOption>, onToggleOption: (PasswordOption) -> Unit) {
  val colors = AppTheme.colors
  Column(
    modifier =
      Modifier
        .fillMaxWidth()
        .clip(CardShape)
        .background(colors.cardBg)
        .border(1.dp, colors.border, CardShape)
  ) {
    PasswordOption.entries.forEach { option ->
      OptionRow(
        option = option,
        checked = option in enabled,
        onToggle = { onToggleOption(option) },
      )
    }
  }
}

@Composable
private fun OptionRow(option: PasswordOption, checked: Boolean, onToggle: () -> Unit) {
  val colors = AppTheme.colors
  val sampleColor =
    when (option) {
      PasswordOption.UPPERCASE, PasswordOption.LOWERCASE -> colors.ink
      PasswordOption.DIGITS -> colors.digit
      PasswordOption.SYMBOLS -> colors.symbol
      PasswordOption.AVOID_LOOK_ALIKES -> colors.muted
    }

  Row(
    modifier =
      Modifier
        .fillMaxWidth()
        .clickable(onClick = onToggle)
        .drawBehind {
          val stroke = 1.dp.toPx()
          drawLine(
            color = colors.rowBorder,
            start = Offset(0f, stroke / 2),
            end = Offset(size.width, stroke / 2),
            strokeWidth = stroke,
          )
        }
        .padding(horizontal = 16.dp, vertical = 13.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Row(
      modifier = Modifier.weight(1f),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
      Text(
        text = option.sample,
        fontFamily = IbmPlexMono,
        fontSize = 12.5.sp,
        color = sampleColor,
        modifier = Modifier.width(42.dp),
      )
      Text(
        text = stringResource(option.labelRes),
        fontFamily = HankenGrotesk,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = colors.ink,
        maxLines = 1,
      )
    }
    DesignSwitch(checked = checked)
  }
}

private val PasswordOption.sample: String
  get() =
    when (this) {
      PasswordOption.UPPERCASE -> "A-Z"
      PasswordOption.LOWERCASE -> "a-z"
      PasswordOption.DIGITS -> "0-9"
      PasswordOption.SYMBOLS -> "!@#"
      PasswordOption.AVOID_LOOK_ALIKES -> "l1O0"
    }

private val PasswordOption.labelRes: Int
  @StringRes
  get() =
    when (this) {
      PasswordOption.UPPERCASE -> R.string.option_uppercase
      PasswordOption.LOWERCASE -> R.string.option_lowercase
      PasswordOption.DIGITS -> R.string.option_numbers
      PasswordOption.SYMBOLS -> R.string.option_symbols
      PasswordOption.AVOID_LOOK_ALIKES -> R.string.option_avoid_look_alikes
    }

@Composable
private fun DesignSwitch(checked: Boolean) {
  val colors = AppTheme.colors
  val trackColor by animateColorAsState(
    if (checked) colors.accent else colors.toggleOff,
    tween(200),
    label = "switchTrack",
  )
  val knobOffset by animateDpAsState(if (checked) 18.dp else 0.dp, tween(200), label = "switchKnob")

  Box(
    modifier = Modifier.size(width = 44.dp, height = 26.dp).clip(PillShape).background(trackColor).padding(3.dp),
    contentAlignment = Alignment.CenterStart,
  ) {
    Box(
      modifier =
        Modifier
          .offset(x = knobOffset)
          .size(20.dp)
          .shadow(2.dp, CircleShape)
          .background(colors.toggleKnob, CircleShape)
    )
  }
}

@Composable
private fun PrivacyNote() {
  val colors = AppTheme.colors
  Box(modifier = Modifier.padding(top = 4.dp)) {
    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .clip(FooterShape)
          .background(colors.noteBg)
          .border(1.dp, colors.noteBorder, FooterShape)
          .padding(horizontal = 15.dp, vertical = 14.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Text("🔒", fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 1.dp))
      Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
          text = stringResource(R.string.no_internet_permission),
          fontFamily = HankenGrotesk,
          fontSize = 12.5.sp,
          fontWeight = FontWeight.Bold,
          color = colors.ink,
        )
        Text(
          text = stringResource(R.string.no_internet_permission_detail),
          fontFamily = HankenGrotesk,
          fontSize = 11.5.sp,
          lineHeight = 16.7.sp,
          color = colors.muted,
        )
      }
    }
  }
}

private fun Context.copyPassword(password: String) {
  if (password.isEmpty()) return
  val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
  val clip = ClipData.newPlainText(getString(R.string.generated_password), password)
  // Keep the password out of clipboard previews and history.
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
    clip.description.extras = PersistableBundle().apply {
      putBoolean("android.content.extra.IS_SENSITIVE", true)
    }
  }
  clipboard.setPrimaryClip(clip)
}

@Preview(showBackground = true, widthDp = 412, heightDp = 892)
@Composable
private fun MainScreenLightPreview() {
  PasswordGeneratorTheme(darkTheme = false) { PreviewScreen(isDarkTheme = false) }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 892)
@Composable
private fun MainScreenDarkPreview() {
  PasswordGeneratorTheme(darkTheme = true) { PreviewScreen(isDarkTheme = true) }
}

@Composable
private fun PreviewScreen(isDarkTheme: Boolean) {
  MainScreen(
    state =
      MainScreenUiState(
        length = 20,
        password = "wR7#qLm2\$vKz9!TbXe4A",
        poolSize = 74,
      ),
    isDarkTheme = isDarkTheme,
    onToggleTheme = {},
    onLengthChange = {},
    onToggleOption = {},
    onRegenerate = {},
    onCopy = {},
  )
}
