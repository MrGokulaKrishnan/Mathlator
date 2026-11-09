package com.mathlator.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class CalculatorUiTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testCalculatorExpressionAndResult() {
        // Type 2 5 9 − 4 4 9
        composeTestRule.onNodeWithContentDescription("2").performClick()
        composeTestRule.onNodeWithContentDescription("5").performClick()
        composeTestRule.onNodeWithContentDescription("9").performClick()
        composeTestRule.onNodeWithContentDescription("minus").performClick()
        composeTestRule.onNodeWithContentDescription("4").performClick()
        composeTestRule.onNodeWithContentDescription("4").performClick()
        composeTestRule.onNodeWithContentDescription("9").performClick()

        // Live preview or equals should display -190
        composeTestRule.onNodeWithContentDescription("equals").performClick()
        composeTestRule.onNodeWithText("= -190").assertIsDisplayed()
    }

    @Test
    fun testScientificPanelToggle() {
        // Toggle scientific mode via mode key
        composeTestRule.onNodeWithContentDescription("toggle scientific mode").performClick()
        composeTestRule.onNodeWithContentDescription("sine").assertIsDisplayed()
    }

    @Test
    fun testTabSwitching() {
        // Switch to Converters tab
        composeTestRule.onNodeWithContentDescription("Converters").performClick()
        composeTestRule.onNodeWithText("Length").assertIsDisplayed()

        // Switch to Finance tab
        composeTestRule.onNodeWithContentDescription("Finance").performClick()
        composeTestRule.onNodeWithText("Loan").assertIsDisplayed()
    }
}
