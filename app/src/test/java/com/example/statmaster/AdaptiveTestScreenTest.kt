package com.example.statmaster

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import com.example.statmaster.adaptive.AdaptiveTestScreen
import org.junit.Rule
import org.junit.Test

class AdaptiveTestScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `adaptive test screen shows loading state`() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            AdaptiveTestScreen(
                navController = navController,
                topicId = 1,
                topicTitle = "Test Topic"
            )
        }

        composeTestRule
            .onNodeWithText("Загрузка вопросов...")
            .assertIsDisplayed()
    }

    @Test
    fun `adaptive test screen displays title`() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            AdaptiveTestScreen(
                navController = navController,
                topicId = 1,
                topicTitle = "Test Topic"
            )
        }

        composeTestRule
            .onNodeWithText("Test Topic")
            .assertIsDisplayed()
    }
}