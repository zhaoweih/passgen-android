package dev.passgen.app.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.passgen.app.data.CharacterSets
import dev.passgen.app.data.MAX_LENGTH
import dev.passgen.app.data.MIN_LENGTH
import dev.passgen.app.data.PasswordGenerator
import dev.passgen.app.data.PasswordOption
import dev.passgen.app.data.Strength
import dev.passgen.app.data.crackTime
import dev.passgen.app.data.entropyBits
import kotlin.math.roundToInt
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainScreenUiState(
  val length: Int = DEFAULT_LENGTH,
  val options: Set<PasswordOption> =
    setOf(
      PasswordOption.UPPERCASE,
      PasswordOption.LOWERCASE,
      PasswordOption.DIGITS,
      PasswordOption.SYMBOLS,
    ),
  val password: String = "",
  /** Intermediate frame of the shuffle animation; null once the password has settled. */
  val scramble: String? = null,
  val poolSize: Int = 0,
  val copied: Boolean = false,
) {
  val displayedPassword: String
    get() = scramble ?: password

  val entropy: Int
    get() = entropyBits(length, poolSize)

  val strength: Strength
    get() = Strength.of(entropy)

  val crackTime: String
    get() = crackTime(entropy)

  companion object {
    const val DEFAULT_LENGTH = 20
  }
}

class MainScreenViewModel(private val generator: PasswordGenerator = PasswordGenerator()) : ViewModel() {

  private val _uiState = MutableStateFlow(MainScreenUiState())
  val uiState: StateFlow<MainScreenUiState> = _uiState.asStateFlow()

  private var scrambleJob: Job? = null
  private var copyJob: Job? = null

  init {
    regenerate(animate = false)
  }

  fun setLength(length: Int) {
    val clamped = length.coerceIn(MIN_LENGTH, MAX_LENGTH)
    if (clamped == _uiState.value.length) return
    _uiState.update { it.copy(length = clamped) }
    regenerate(animate = false)
  }

  fun toggleOption(option: PasswordOption) {
    _uiState.update {
      val options = if (option in it.options) it.options - option else it.options + option
      it.copy(options = options)
    }
    regenerate(animate = false)
  }

  fun regenerate(animate: Boolean = true) {
    scrambleJob?.cancel()
    val state = _uiState.value
    val pool = generator.pool(state.options)
    val target = generator.generateFrom(pool, state.length)
    _uiState.update {
      it.copy(password = target, poolSize = pool.length, scramble = null, copied = false)
    }
    if (!animate) return

    // Settle the password left-to-right over a handful of frames, as the design does.
    val scramblePool = pool + CharacterSets.SYMBOLS
    scrambleJob =
      viewModelScope.launch {
        for (frame in 1..SCRAMBLE_FRAMES) {
          delay(SCRAMBLE_FRAME_MILLIS)
          if (frame == SCRAMBLE_FRAMES) {
            _uiState.update { it.copy(scramble = null) }
            break
          }
          val settled = (target.length * frame.toDouble() / SCRAMBLE_FRAMES).roundToInt()
          val frameText =
            target.mapIndexed { i, ch -> if (i < settled) ch else generator.pick(scramblePool) }.joinToString("")
          _uiState.update { it.copy(scramble = frameText) }
        }
      }
  }

  fun onPasswordCopied() {
    copyJob?.cancel()
    _uiState.update { it.copy(copied = true) }
    copyJob =
      viewModelScope.launch {
        delay(CONFIRMATION_MILLIS)
        _uiState.update { it.copy(copied = false) }
      }
  }

  private companion object {
    const val SCRAMBLE_FRAMES = 9
    const val SCRAMBLE_FRAME_MILLIS = 34L
    const val CONFIRMATION_MILLIS = 1600L
  }
}
