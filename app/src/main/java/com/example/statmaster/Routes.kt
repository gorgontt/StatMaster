package com.example.statmaster

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.statmaster.adaptive.AdaptiveTestResultScreen
import com.example.statmaster.adaptive.AdaptiveTestScreen
import com.example.statmaster.adaptive.AdaptiveTopicSelectionScreen
import com.example.statmaster.auth.MainClass
import com.example.statmaster.stat.LevelsStat
import com.example.statmaster.terver.DocumentationLevelTerVer
import com.example.statmaster.terver.LevelsTerVer

sealed class Routes(val route: String) {
    object MainClass : Routes("main_class")
    object MainContent : Routes("main_content")

    object LevelsTerVer : Routes("levels_terver/{scrollTo}") {
        fun createRoute(scrollTo: String? = null) =
            "levels_terver/${scrollTo ?: "default"}"
    }

    object DocumentationLevel : Routes("documentation_level/{levelId}") {
        fun createRoute(levelId: Int) = "documentation_level/$levelId"
    }

    object LevelsStat : Routes("levels_stat/{scrollTo}") {
        fun createRoute(scrollTo: String? = null) =
            "levels_stat/${scrollTo ?: "default"}"
    }

    object AdaptiveTopicSelection : Routes("adaptive_topic_selection")
    object AdaptiveTest : Routes("adaptive_test/{topicId}") {
        fun createRoute(topicId: Int) = "adaptive_test/$topicId"
    }
}

@Composable
fun Navigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }

    NavHost(
        navController = navController,
        startDestination = Routes.MainClass.route
    ) {
        composable(Routes.MainClass.route) { MainClass(navController) }
        composable(Routes.MainContent.route) { MainContent(navController) }

        composable("demo_recommendations") {
            AdaptiveTestResultScreen(navController = navController)
        }

        //Terver
        composable(
            route = Routes.LevelsTerVer.route,
            arguments = listOf(
                navArgument("scrollTo") {
                    type = NavType.StringType
                    defaultValue = "default"
                }
            )
        ) { backStackEntry ->
            val scrollTo = backStackEntry.arguments?.getString("scrollTo")
            LevelsTerVer(
                navController = navController,
                scrollToChapter = scrollTo.takeIf { it != "default" }
            )
        }

        composable(
            route = Routes.DocumentationLevel.route,
            arguments = listOf(navArgument("levelId") { type = NavType.IntType })
        ) { backStackEntry ->
            val levelId = backStackEntry.arguments?.getInt("levelId")
            DocumentationLevelTerVer(navController, levelId)
        }

        //Stat

        composable(
            route = Routes.LevelsStat.route,
            arguments = listOf(
                navArgument("scrollTo") {
                    type = NavType.StringType
                    defaultValue = "default"
                }
            )
        ) { backStackEntry ->
            val scrollTo = backStackEntry.arguments?.getString("scrollTo")
            LevelsStat(
                navController = navController,
                scrollToChapter = scrollTo.takeIf { it != "default" }
            )
        }

        composable(Routes.AdaptiveTopicSelection.route) {
            AdaptiveTopicSelectionScreen(navController = navController)
        }

        composable(
            route = Routes.AdaptiveTest.route,
            arguments = listOf(navArgument("topicId") { type = NavType.IntType })
        ) { backStackEntry ->
            val topicId = backStackEntry.arguments?.getInt("topicId") ?: 0
            AdaptiveTestScreen(
                navController = navController,
                topicId = topicId,
                topicTitle = "Адаптивный тест"
            )
        }

    }
}