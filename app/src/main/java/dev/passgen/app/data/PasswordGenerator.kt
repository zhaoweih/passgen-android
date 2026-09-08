package dev.passgen.app.data

import java.security.SecureRandom
import java.text.NumberFormat
import java.util.Locale
import java.util.Random
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/** Character pools the generator draws from. */
object CharacterSets {
  const val UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
  const val LOWER = "abcdefghijklmnopqrstuvwxyz"
  const val DIGITS = "0123456789"
  const val SYMBOLS = "!@#\$%^&*()-_=+[]{};:,.?/"

  /** Glyphs that are easy to confuse when a password is read aloud or typed from a screen. */
  const val AMBIGUOUS = "l1IO0oB8S5Z2"
}

const val MIN_LENGTH = 6
const val MAX_LENGTH = 48

/** A character class the user can switch on or off. Labels and samples live in the UI layer. */
enum class PasswordOption {
  UPPERCASE,
  LOWERCASE,
  DIGITS,
  SYMBOLS,
  AVOID_LOOK_ALIKES,
}

/** Generates passwords from a cryptographically secure source. */
class PasswordGenerator(private val random: Random = SecureRandom()) {

  /** The characters available for [options]; falls back to lowercase if every set is switched off. */
  fun pool(options: Set<PasswordOption>): String {
    val pool = buildString {
      if (PasswordOption.UPPERCASE in options) append(CharacterSets.UPPER)
      if (PasswordOption.LOWERCASE in options) append(CharacterSets.LOWER)
      if (PasswordOption.DIGITS in options) append(CharacterSets.DIGITS)
      if (PasswordOption.SYMBOLS in options) append(CharacterSets.SYMBOLS)
    }
    val base = pool.ifEmpty { CharacterSets.LOWER }
    return if (PasswordOption.AVOID_LOOK_ALIKES in options) {
      base.filterNot { it in CharacterSets.AMBIGUOUS }
    } else {
      base
    }
  }

  fun generate(length: Int, options: Set<PasswordOption>): String = generateFrom(pool(options), length)

  fun generateFrom(pool: String, length: Int): String = buildString {
    repeat(length) { append(pick(pool)) }
  }

  fun pick(pool: String): Char = pool[random.nextInt(pool.length)]
}

enum class Strength(val fraction: Float) {
  WEAK(0.25f),
  FAIR(0.50f),
  STRONG(0.78f),
  VERY_STRONG(1f);

  companion object {
    fun of(bits: Int): Strength =
      when {
        bits < 45 -> WEAK
        bits < 65 -> FAIR
        bits < 90 -> STRONG
        else -> VERY_STRONG
      }
  }
}

/** Shannon entropy of a uniformly random string of [length] characters drawn from [poolSize]. */
fun entropyBits(length: Int, poolSize: Int): Int {
  if (length <= 0 || poolSize <= 1) return 0
  return (length * (ln(poolSize.toDouble()) / ln(2.0))).roundToInt()
}

private val TIME_UNITS =
  listOf(
    1.0 to "second",
    60.0 to "minute",
    3600.0 to "hour",
    86_400.0 to "day",
    2_592_000.0 to "month",
    31_536_000.0 to "year",
  )

private const val SECONDS_PER_YEAR = 31_536_000.0

/** Rough time to exhaust half the keyspace at 100 billion guesses per second. */
fun crackTime(bits: Int, locale: Locale = Locale.getDefault()): String {
  val seconds = 2.0.pow(bits - 1) / 1e11
  if (seconds < 1) return "an instant"
  if (seconds > SECONDS_PER_YEAR * 1e9) return "billions of years"

  var result = "1 second"
  for ((factor, unit) in TIME_UNITS) {
    if (seconds < factor) continue
    val value = seconds / factor
    val rounded = value.roundToLong()
    val text =
      when {
        value >= 1000 -> NumberFormat.getIntegerInstance(locale).format(rounded)
        value < 10 -> String.format(locale, "%.1f", value)
        else -> rounded.toString()
      }
    result = text + " " + unit + if (rounded == 1L) "" else "s"
  }
  return result
}
