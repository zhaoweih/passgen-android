package dev.passgen.app.ui.main

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import dev.passgen.app.data.PasswordOption
import dev.passgen.app.theme.PasswordGeneratorTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** UI tests for [dev.passgen.app.ui.main.MainScreen]. */
class MainScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  private val state =
    MainScreenUiState(length = 20, password = "wR7#qLm2vKz9TbXe4Ayp", poolSize = 74)

  private fun setContent(
    onToggleTheme: () -> Unit = {},
    onLengthChange: (Int) -> Unit = {},
    onToggleOption: (PasswordOption) -> Unit = {},
    onRegenerate: () -> Unit = {},
    onCopy: () -> Unit = {},
  ) {
    composeTestRule.setContent {
      PasswordGeneratorTheme(darkTheme = false) {
        MainScreen(
          state = state,
          isDarkTheme = false,
          onToggleTheme = onToggleTheme,
          onLengthChange = onLengthChange,
          onToggleOption = onToggleOption,
          onRegenerate = onRegenerate,
          onCopy = onCopy,
        )
      }
    }
  }

  @Test
  fun screen_showsPasswordLengthAndOptions() {
    setContent()
    composeTestRule.onNodeWithContentDescription("Generated password").assertIsDisplayed()
    composeTestRule.onNodeWithText("20").assertIsDisplayed()
    composeTestRule.onNodeWithText("Very strong").assertIsDisplayed()
    composeTestRule.onNodeWithText("Uppercase").assertIsDisplayed()
    composeTestRule.onNodeWithText("Avoid look-alikes").assertIsDisplayed()
    // The privacy note sits below the fold on a phone-sized screen.
    composeTestRule.onNodeWithText("No internet permission").performScrollTo().assertIsDisplayed()
  }

  @Test
  fun copyButton_reportsClicks() {
    var copies = 0
    setContent(onCopy = { copies++ })
    composeTestRule.onNodeWithText("Copy password").performClick()
    assertEquals(1, copies)
  }

  @Test
  fun regenerateButton_reportsClicks() {
    var regenerations = 0
    setContent(onRegenerate = { regenerations++ })
    composeTestRule.onNodeWithContentDescription("Generate a new password").performClick()
    assertEquals(1, regenerations)
  }

  @Test
  fun optionRow_reportsTheToggledOption() {
    var toggled: PasswordOption? = null
    setContent(onToggleOption = { toggled = it })
    composeTestRule.onNodeWithText("Symbols").performClick()
    assertEquals(PasswordOption.SYMBOLS, toggled)
  }

  @Test
  fun themeButton_reportsClicks() {
    var toggles = 0
    setContent(onToggleTheme = { toggles++ })
    composeTestRule.onNodeWithContentDescription("Switch to dark theme").performClick()
    assertEquals(1, toggles)
  }
}
