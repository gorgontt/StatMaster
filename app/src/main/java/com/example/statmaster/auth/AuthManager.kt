package com.example.statmaster.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.example.statmaster.config.AppConfig
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.providers.builtin.IDToken
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.ktor.client.plugins.websocket.WebSockets
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.security.MessageDigest
import java.util.UUID

sealed interface AuthResponse{
    data object Succes: AuthResponse
    data class Error(val message: String?): AuthResponse
}

class AuthManager(private val context: Context) {


    fun getContext(): Context = context

    companion object {
        private const val TAG = "AuthManager"
    }

    @OptIn(SupabaseInternal::class)
    val supabase = try {
        val url = AppConfig.SUPABASE_URL
        val key = AppConfig.SUPABASE_KEY

        Log.d(TAG, "🔐 Initializing Supabase client...")
        Log.d(TAG, "📡 URL: $url")
        Log.d(TAG, "🔑 Key length: ${key.length}")

        if (key.isEmpty() || key == "your-development-key") {
            Log.w(TAG, "⚠️ Using DEVELOPMENT key! This will not work in production.")
        }

        createSupabaseClient(
            supabaseKey = key,
            supabaseUrl = url
        ) {
            install(Realtime)
            install(io.github.jan.supabase.gotrue.Auth)
            install(io.github.jan.supabase.postgrest.Postgrest) {
                serializer = KotlinXSerializer(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
            httpConfig {
                install(WebSockets)
            }
            httpConfig {
                install(io.ktor.client.plugins.HttpTimeout) {
                    requestTimeoutMillis = 30000
                    connectTimeoutMillis = 30000
                    socketTimeoutMillis = 30000
                }
            }
        }
    } catch (e: Exception) {
        Log.e(TAG, "❌ Failed to create Supabase client", e)
        throw e
    }

    fun SignUpWithEmail(emailValue: String, passwordValue: String): Flow<AuthResponse> = flow {
        try {
            supabase.auth.signUpWith(Email) {
                email = emailValue
                password = passwordValue
            }

            val session = supabase.auth.currentSessionOrNull()
            if (session != null) {
                emit(AuthResponse.Succes)
            } else {
                emit(AuthResponse.Succes)
            }
        } catch (e: Exception) {
            val message = when {
                e.message?.contains("rate limit", ignoreCase = true) == true ->
                    "Слишком много попыток. Подождите 15 минут."
                e.message?.contains("User already registered", ignoreCase = true) == true ->
                    "Пользователь с таким email уже существует"
                e.message?.contains("Password should be at least 6 characters", ignoreCase = true) == true ->
                    "Пароль должен содержать минимум 6 символов"
                e.message?.contains("Invalid email", ignoreCase = true) == true ->
                    "Некорректный email адрес"
                else -> e.localizedMessage ?: "Ошибка регистрации"
            }
            emit(AuthResponse.Error(message))
        }
    }
    fun SignInWithEmail(emailValue: String, passwordValue: String): Flow<AuthResponse> = flow {
        try {
            val result = supabase.auth.signInWith(Email) {
                email = emailValue
                password = passwordValue
            }

            val session = supabase.auth.currentSessionOrNull()
            if (session != null) {
                emit(AuthResponse.Succes)
            } else {
                emit(AuthResponse.Error("Не удалось создать сессию"))
            }
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

    // AuthManager.kt
    fun loginGoogleUser(): Flow<AuthResponse> = flow {
        try {
            val hashedNonce = createNonce()
            Log.d(TAG, "🚀 Starting Google Sign-In...")

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
                val result = credentialManager.getCredential(request = request, context = context)
                Log.d(TAG, "Credential получен")

                val credential = result.credential
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val googleIdToken = googleIdTokenCredential.idToken
                Log.d(TAG, "Google ID Token получен, длина: ${googleIdToken.length}")

                try {
                    supabase.auth.signInWith(IDToken) {
                        idToken = googleIdToken
                        provider = Google
                    }
                    Log.d(TAG, "Supabase auth запрос отправлен")
                } catch (e: Exception) {
                    Log.e(TAG, "Ошибка при signInWith: ${e.message}", e)
                    emit(AuthResponse.Error("Ошибка авторизации: ${e.message}"))
                    return@flow
                }

                val session = supabase.auth.currentSessionOrNull()
                if (session != null) {
                    val userEmail = session.user?.email ?: "email не найден"
                    Log.d(TAG, "Сессия создана успешно! User: $userEmail")
                    emit(AuthResponse.Succes)
                } else {
                    Log.e(TAG, "Сессия не создана")
                    emit(AuthResponse.Error("Не удалось создать сессию"))
                }

            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при получении credential", e)
                emit(AuthResponse.Error("Ошибка получения данных от Google: ${e.message}"))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Общая ошибка Google Sign-In", e)
            emit(AuthResponse.Error(e.localizedMessage ?: "Ошибка входа через Google"))
        }
    }




    @Serializable
    data class TestLevel(val id: Int? = null)

    suspend fun testConnection(): Boolean {
        return try {
            @Serializable
            data class SimpleResponse(val id: Int)

            supabase.postgrest["level"]
                .select()
                .decodeList<SimpleResponse>()
                .isNotEmpty()
        } catch (e: Exception) {
            Log.e("Supabase", "Connection test failed", e)
            false
        }
    }


}