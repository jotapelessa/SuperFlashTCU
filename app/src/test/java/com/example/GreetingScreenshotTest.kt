package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.model.L1DeckSummary
import com.example.ui.components.DeckCard
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val sampleDeck = L1DeckSummary(
      l1 = "Auditor TCU/TCEs/TCDF - PARTE 1",
      totalCards = 48,
      dueCards = 6,
      masteredCards = 20,
      learningCards = 18,
      newCards = 10,
      l2Count = 4,
      l3Count = 12,
      disciplineColors = listOf("#2563EB", "#D97706", "#7C3AED")
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        DeckCard(
          deck = sampleDeck,
          onClick = {},
          onQuickStudy = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
