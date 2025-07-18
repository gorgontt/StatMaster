package com.example.statmaster

import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.statmaster.auth.MainClass
import com.example.statmaster.auth.MainContent
import com.example.statmaster.ui.theme.StatMasterTheme
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.providers.builtin.IDToken
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.ktor.client.plugins.websocket.WebSockets
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.security.MessageDigest
import java.util.UUID

class MainActivity : ComponentActivity() {
    @OptIn(SupabaseInternal::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StatMasterTheme {
                MainClass()
//                // Создаем NavController и оборачиваем в CompositionLocalProvider
//                val navController = rememberNavController()
//                CompositionLocalProvider(LocalNavController provides navController) {
//                    // Убедимся, что Navigation - корневой компонент
//                    AppNavigation()

            }
        }
    }
}

//@Composable
//fun AppNavigation() {
//    val navController = LocalNavController.current
//    NavHost(
//        navController = navController,
//        startDestination = Routes.MainClass.route
//    ) {
//        composable(Routes.MainClass.route) { MainClass() }
//        composable(Routes.MainContent.route) { MainContent() }
//    }
//}

//@Composable
//fun Navigation() {
//    val navController = NavController.
//    NavHost(
//        navController = navController,
//        startDestination = Routes.MainClass.route
//    ) {
//        composable(Routes.MainClass.route) { MainClass() }
//        composable(Routes.MainContent.route) { MainContent() }


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

//
//    }
//}

sealed interface AuthResponse{
    data object Succes: AuthResponse
    data class Error(val message: String?): AuthResponse
}

class AuthManager(
    private val context: Context
){
    @OptIn(SupabaseInternal::class)
    val supabase = createSupabaseClient(
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImdqbHh4Y2V0YW5jeGxxdHRxcWRoIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTI0OTMwMzcsImV4cCI6MjA2ODA2OTAzN30.qt-xq9R7thW26-78Ri0chdC3Y0ut-PKOLTjAPZrriNg",
        supabaseUrl = "https://gjlxxcetancxlqttqqdh.supabase.co"
    ){
        install(Realtime)
        install(Auth)
        install(Postgrest)
        httpConfig { this.install(WebSockets) }
    }

    fun SignUpWithEmail(emailValue: String, passwordValue: String): Flow<AuthResponse> = flow {

        try{
            supabase.auth.signUpWith(Email){
                email = emailValue
                password = passwordValue
            }
            emit(AuthResponse.Succes)

        }catch (e: Exception){
            emit(AuthResponse.Error(e.localizedMessage))
        }
    }

    fun SignInWithEmail(emailValue: String, passwordValue: String): Flow<AuthResponse> = flow {
        try {
            supabase.auth.signInWith(Email) {
                email = emailValue
                password = passwordValue
            }
            emit(AuthResponse.Succes)
        } catch (e: Exception) {
            emit(AuthResponse.Error(e.localizedMessage ?: "Неверный email или пароль"))
        }
    }

    fun createNonce(): String{
        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    fun loginGoogleUser(): Flow<AuthResponse> = flow {
        val hashedNonce = createNonce()

        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId("850534604231-elobf2jjkap2pqqs6bcupguulid02crc.apps.googleusercontent.com")
            .setNonce(hashedNonce)
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val credentialManager = CredentialManager.create(context)

        try {
            val result = credentialManager.getCredential(
                request = request,
                context = context
            )
            val credential = result.credential
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val googleIdToken = googleIdTokenCredential.idToken

            Log.i(TAG, "Google ID Token: $googleIdToken")
            Toast.makeText(context, "Вы успешно вошли!", Toast.LENGTH_LONG).show()

            supabase.auth.signInWith(IDToken) {
                idToken = googleIdToken
                provider = Google
                //nonce = rawNonce
            }

            supabase.auth.signInWith(IDToken) {
                idToken = googleIdToken
                provider = Google
            }
            emit(AuthResponse.Succes)
        } catch (e: Exception) {
            emit(AuthResponse.Error(e.localizedMessage ?: "Ошибка входа через Google"))
        } catch (e: GetCredentialException) {
            Log.e(TAG, "GetCredentialException", e)
            Toast.makeText(context, "Ошибка входа: ${e.message}", Toast.LENGTH_LONG).show()
        } catch (e: GoogleIdTokenParsingException) {
            Log.e(TAG, "GoogleIdTokenParsingException", e)
            Toast.makeText(context, "Ошибка обработки токена: ${e.message}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error", e)
            Toast.makeText(context, "Неизвестная ошибка: ${e.message}", Toast.LENGTH_LONG).show()
        }



    }
}
