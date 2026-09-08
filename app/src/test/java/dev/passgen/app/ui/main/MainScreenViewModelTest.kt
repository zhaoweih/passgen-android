package dev.passgen.app.ui.main

import dev.passgen.app.data.MAX_LENGTH
import dev.passgen.app.data.MIN_LENGTH
import dev.passgen.app.data.PasswordGenerator
import dev.passgen.app.data.PasswordOption
import dev.passgen.app.data.Strength
import java.util.Random
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainScreenViewModelTest {

  private val dispatcher = StandardTestDispatcher()

  @Before fun setUp() = Dispatchers.setMain(dispatcher)

  @After fun tearDown() = Dispatchers.resetMain()

  private fun viewModel() = MainScreenViewModel(PasswordGenerator(Random(7)))

  @Test
  fun uiState_startsWithAGeneratedPassword() {
    val state = viewModel().uiState.value
    assertEquals(MainScreenUiState.DEFAULT_LENGTH, state.password.length)
    assertNull(state.scramble)
    assertTrue(state.poolSize > 0)
  }

  @Test
  fun setLength_regeneratesAtTheNewLength() {
    val viewModel = viewModel()
    viewModel.setLength(32)
    assertEquals(32, viewModel.uiState.value.length)
    assertEquals(32, viewModel.uiState.value.password.length)
  }

  @Test
  fun setLength_isClampedToTheSupportedRange() {
    val viewModel = viewModel()
    viewModel.setLength(1000)
    assertEquals(MAX_LENGTH, viewModel.uiState.value.length)
    viewModel.setLength(0)
    assertEquals(MIN_LENGTH, viewModel.uiState.value.length)
  }

  @Test
  fun toggleOption_shrinksThePoolAndTheEntropy() {
    val viewModel = viewModel()
    val before = viewModel.uiState.value
    viewModel.toggleOption(PasswordOption.SYMBOLS)
    val after = viewModel.uiState.value
    assertFalse(PasswordOption.SYMBOLS in after.options)
    assertTrue(after.poolSize < before.poolSize)
    assertTrue(after.entropy < before.entropy)
  }

  @Test
  fun uiState_reportsStrengthForTheCurrentSettings() {
    val viewModel = viewModel()
    viewModel.setLength(MIN_LENGTH)
    assertEquals(Strength.WEAK, viewModel.uiState.value.strength)
    viewModel.setLength(MAX_LENGTH)
    assertEquals(Strength.VERY_STRONG, viewModel.uiState.value.strength)
  }

  @Test
  fun regenerate_animates_thenSettlesOnTheFinalPassword() = runTest(dispatcher) {
    val viewModel = viewModel()
    viewModel.regenerate(animate = true)
    val target = viewModel.uiState.value.password

    advanceUntilIdle()
    assertNull(viewModel.uiState.value.scramble)
    assertEquals(target, viewModel.uiState.value.displayedPassword)
  }

  @Test
  fun onPasswordCopied_showsConfirmation_thenClearsIt() = runTest(dispatcher) {
    val viewModel = viewModel()
    viewModel.onPasswordCopied()
    assertTrue(viewModel.uiState.value.copied)

    advanceUntilIdle()
    assertFalse(viewModel.uiState.value.copied)
  }
}
