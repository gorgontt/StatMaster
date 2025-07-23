package com.example.statmaster

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.statmaster.auth.MainClass
import com.example.statmaster.terver.DocumentationLevelTerVer
import com.example.statmaster.terver.LevelsTerVer

sealed class Routes(val route: String) {
    object MainClass : Routes("main_class")
    object MainContent : Routes("main_content")
    object LevelsTerVer : Routes("levels_terver")
    object DocumentationLevel : Routes("documentation_level/{levelId}") {
        fun createRoute(levelId: Int) = "documentation_level/$levelId"
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
        composable(Routes.LevelsTerVer.route) { LevelsTerVer(navController) }
        composable(
            route = Routes.DocumentationLevel.route,
            arguments = listOf(navArgument("levelId") { type = NavType.IntType })
        ) { backStackEntry ->
            val levelId = backStackEntry.arguments?.getInt("levelId")
            DocumentationLevelTerVer(navController, levelId)
        }
    }
}
