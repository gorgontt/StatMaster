package com.example.statmaster

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import com.example.statmaster.auth.MainClass
import org.junit.Rule
import org.junit.Test

class AuthScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `auth screen displays title correctly`() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            MainClass(navController = navController)
        }

        composeTestRule
            .onNodeWithText("StatMaster")
            .assertIsDisplayed()
    }

    @Test
    fun `auth screen displays sign up button`() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            MainClass(navController = navController)
        }

        composeTestRule
            .onNodeWithText("Создать аккаунт")
            .assertIsDisplayed()
    }

    @Test
    fun `auth screen displays sign in button`() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            MainClass(navController = navController)
        }

        composeTestRule
            .onNodeWithText("Войти")
            .assertIsDisplayed()
    }
}