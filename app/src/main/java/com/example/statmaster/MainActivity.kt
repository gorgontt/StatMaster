package com.example.statmaster

import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
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
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.ktor.client.plugins.websocket.WebSockets
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.security.MessageDigest
import java.util.UUID

class MainActivity : ComponentActivity() {
    @OptIn(SupabaseInternal::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StatMasterTheme {
                Navigation()
            }
        }
    }
}

sealed interface AuthResponse{
    data object Succes: AuthResponse
    data class Error(val message: String?): AuthResponse
}

class AuthManager(
    val context: Context
){
    @OptIn(SupabaseInternal::class)
    val supabase = createSupabaseClient(
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImdqbHh4Y2V0YW5jeGxxdHRxcWRoIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTI0OTMwMzcsImV4cCI6MjA2ODA2OTAzN30.qt-xq9R7thW26-78Ri0chdC3Y0ut-PKOLTjAPZrriNg",
        supabaseUrl = "https://gjlxxcetancxlqttqqdh.supabase.co"
    ){
        install(Realtime)
        install(Auth)
        install(Postgrest) {
            serializer = KotlinXSerializer(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        httpConfig { this.install(WebSockets) }
    }

    fun SignUpWithEmail(emailValue: String, passwordValue: String): Flow<AuthResponse> = flow {
        try {
            supabase.auth.signUpWith(Email) {
                email = emailValue
                password = passwordValue
            }
            emit(AuthResponse.Succes)
        } catch (e: Exception) {
            emit(AuthResponse.Error(e.localizedMessage))
        }
    }

//    init {
//        supabase.postgrest.setSerializer(KotlinXSerializer(
//            Json {
//                ignoreUnknownKeys = true
//                isLenient = true
//            }
//        ))
//    }

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

    @Serializable
    data class TestLevel(val id: Int? = null)

    suspend fun testConnection(): Boolean {
        return try {
            // Вариант 1: Простой запрос без limit
            @Serializable
            data class SimpleResponse(val id: Int)

            supabase.postgrest["level"]
                .select()  // Просто выбираем все поля (можно указать columns)
                .decodeList<SimpleResponse>()
                .isNotEmpty()
        } catch (e: Exception) {
            Log.e("Supabase", "Connection test failed", e)
            false
        }
    }


}
