package com.example.statmaster

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavController


//val LocalNavController = staticCompositionLocalOf<NavController> {
//    error("No NavController provided")
//}


sealed class Routes(val route: String) {

    object MainClass : Routes("main_class")
    object MainContent : Routes("main_content")
    object LevelsTerVer : Routes("levels_ter_ver")



}
