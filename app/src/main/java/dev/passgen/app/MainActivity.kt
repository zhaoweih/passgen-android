package dev.passgen.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.material3.Surface
import dev.passgen.app.theme.AppTheme
import dev.passgen.app.theme.PasswordGeneratorTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    enableEdgeToEdge()
    setContent {
      // The design ships its own light/dark switch, so the system setting is only the starting point.
      var themeOverride by rememberSaveable { mutableStateOf<Boolean?>(null) }
      val darkTheme = themeOverride ?: isSystemInDarkTheme()

      DisposableEffect(darkTheme) {
        enableEdgeToEdge(
          statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme },
          navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme },
        )
        onDispose {}
      }

      PasswordGeneratorTheme(darkTheme = darkTheme) {
        Surface(modifier = Modifier.fillMaxSize(), color = AppTheme.colors.pageBg) {
          MainNavigation(isDarkTheme = darkTheme, onToggleTheme = { themeOverride = !darkTheme })
        }
      }
    }
  }
}
