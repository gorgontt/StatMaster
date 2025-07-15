package com.example.statmaster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.statmaster.auth.MainClass
import com.example.statmaster.ui.theme.StatMasterTheme
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.ktor.client.plugins.websocket.WebSockets

class MainActivity : ComponentActivity() {

    @OptIn(SupabaseInternal::class)
    val supaBaseClient = createSupabaseClient(
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImdqbHh4Y2V0YW5jeGxxdHRxcWRoIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTI0OTMwMzcsImV4cCI6MjA2ODA2OTAzN30.qt-xq9R7thW26-78Ri0chdC3Y0ut-PKOLTjAPZrriNg",
        supabaseUrl = "https://gjlxxcetancxlqttqqdh.supabase.co"
    ){
        install(Realtime)
        install(Postgrest)
        httpConfig { this.install(WebSockets) }
    }


    @OptIn(SupabaseInternal::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StatMasterTheme {

                MainClass()

            }
        }
    }
}

@Composable
fun Navigation() {
    val navController = rememberNavController()
//    val context = LocalContext.current
//    val savedPlayers = loadPlayers(context).toMutableList()
//    val playersList = remember { mutableStateListOf(*savedPlayers.toTypedArray()) }
//
//    val onDismiss: () -> Unit = { /* действие при закрытии */ }
//    val onCheckedChange: (Boolean) -> Unit = { /* действие при закрытии */ }

    NavHost(
        navController = navController,
        startDestination = "main_class"
    ) {
        composable(Routes.MainClass.route) { MainClass() }
        //composable(Routes.SignUp.route) { SignUp() }

//            composable("choose_version") {
//                ChooseVersion(navController)
//            }

//        // 1 навигация
//        composable("players_list/{type}") { backStackEntry ->
//            val type = backStackEntry.arguments?.getString("type")
//            val checkedState = backStackEntry.arguments?.getString("checkedState")?.toBoolean() ?: false
//            // val onCheckedChange = backStackEntry.arguments?.getString("onCheckedChange")
//            type?.let {
//                AddNewPlayers(type = it, navController, playersList, checkedState, onCheckedChange)
//            }
//        }
//
//        composable("pager/{type}/{checkedState}") { backStackEntry ->
//            val type = backStackEntry.arguments?.getString("type")
//            val checkedState = backStackEntry.arguments?.getString("checkedState")?.toBoolean() ?: false
//            type?.let {
//                Pager(type = it, playersList, onDismiss, navController, checkedState)
//            }
//        }


    }
}
