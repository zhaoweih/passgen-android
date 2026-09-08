package dev.passgen.app.data

import java.util.Locale
import java.util.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordGeneratorTest {

  private val generator = PasswordGenerator(Random(42))

  @Test
  fun pool_containsOnlySelectedSets() {
    val pool = generator.pool(setOf(PasswordOption.UPPERCASE, PasswordOption.DIGITS))
    assertEquals(CharacterSets.UPPER + CharacterSets.DIGITS, pool)
  }

  @Test
  fun pool_fallsBackToLowercase_whenNothingSelected() {
    assertEquals(CharacterSets.LOWER, generator.pool(emptySet()))
  }

  @Test
  fun pool_dropsAmbiguousCharacters_whenAvoidLookAlikesIsOn() {
    val pool = generator.pool(PasswordOption.entries.toSet())
    assertTrue(CharacterSets.AMBIGUOUS.none { it in pool })
    assertTrue('A' in pool)
  }

  @Test
  fun generate_hasRequestedLengthAndStaysInsidePool() {
    val options = setOf(PasswordOption.LOWERCASE, PasswordOption.DIGITS)
    val pool = generator.pool(options)
    val password = generator.generate(24, options)
    assertEquals(24, password.length)
    assertTrue(password.all { it in pool })
  }

  @Test
  fun generate_isNotRepeatable_acrossCalls() {
    val options = setOf(PasswordOption.LOWERCASE, PasswordOption.UPPERCASE, PasswordOption.DIGITS)
    val first = generator.generate(32, options)
    val second = generator.generate(32, options)
    assertFalse(first == second)
  }

  @Test
  fun entropyBits_matchesLog2OfKeyspace() {
    // 26 lowercase letters is log2(26) ~= 4.70 bits per character.
    assertEquals(94, entropyBits(20, 26))
    assertEquals(0, entropyBits(0, 26))
    assertEquals(0, entropyBits(20, 1))
  }

  @Test
  fun strength_tiersFollowEntropyThresholds() {
    assertEquals(Strength.WEAK, Strength.of(44))
    assertEquals(Strength.FAIR, Strength.of(45))
    assertEquals(Strength.FAIR, Strength.of(64))
    assertEquals(Strength.STRONG, Strength.of(65))
    assertEquals(Strength.STRONG, Strength.of(89))
    assertEquals(Strength.VERY_STRONG, Strength.of(90))
  }

  @Test
  fun crackTime_describesShortAndLongDurations() {
    assertEquals("an instant", crackTime(20, Locale.US))
    assertEquals("billions of years", crackTime(200, Locale.US))
    // Each value below is 2^(bits-1) guesses at 100 billion guesses per second.
    assertEquals("11 seconds", crackTime(41, Locale.US))
    assertEquals("2.9 minutes", crackTime(45, Locale.US))
    assertEquals("1.6 hours", crackTime(50, Locale.US))
    assertEquals("1.1 month", crackTime(59, Locale.US))
    assertEquals("187 years", crackTime(70, Locale.US))
  }
}
