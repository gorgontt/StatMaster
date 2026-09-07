// app/src/main/java/com/example/statmaster/auth/BottomSheetSignInDialogContent.kt
package com.example.statmaster.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.statmaster.AuthManager
import com.example.statmaster.AuthResponse
import com.example.statmaster.R
import com.example.statmaster.Routes
import com.example.statmaster.ui.theme.BackgroundColor
import com.example.statmaster.ui.theme.Black
import com.example.statmaster.ui.theme.Blue
import com.example.statmaster.ui.theme.DarkBlue
import com.example.statmaster.ui.theme.Transparent
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch

@Composable
fun BottomSheetSignInDialogContent(
    onDismiss: () -> Unit,
    navController: NavController
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }

    // Проверяем, авторизован ли пользователь
    LaunchedEffect(Unit) {
        val session = authManager.supabase.auth.currentSessionOrNull()
        if (session != null) {
            Toast.makeText(context, "Уже авторизованы", Toast.LENGTH_LONG).show()
            navController.navigate(Routes.MainContent.route) {
                popUpTo(Routes.MainClass.route) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(
                start = 24.dp,
                end = 24.dp,
                top = 16.dp,
                bottom = 40.dp
            )
            .background(BackgroundColor)
    ) {
        // Индикатор свайпа вниз
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .height(4.dp)
                    .fillMaxWidth(0.3f)
                    .background(Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
            )
        }

        Text(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 8.dp, bottom = 16.dp),
            text = "Вход в аккаунт",
            style = TextStyle(
                color = DarkBlue,
                fontSize = 28.sp,
                fontFamily = FontFamily(Font(R.font.jura_semibold))
            )
        )

        // Отображение ошибки
        errorMessage?.let { message ->
            Text(
                text = message,
                color = Color.Red,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // ✅ Исправленный OutlinedTextField с правильными цветами
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .border(
                    width = 2.dp,
                    color = Blue,
                    shape = RoundedCornerShape(37.dp)
                ),
            value = email,
            onValueChange = {
                email = it
                errorMessage = null
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            textStyle = TextStyle(
                color = Black,
                fontSize = 18.sp,
                fontFamily = FontFamily(Font(R.font.jura))
            ),
            placeholder = {
                Text(
                    text = "Адрес электронной почты",
                    style = TextStyle(
                        color = DarkBlue,
                        fontSize = 18.sp,
                        fontFamily = FontFamily(Font(R.font.jura))
                    )
                )
            },
            shape = RoundedCornerShape(37.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedTextColor = Black,
                unfocusedTextColor = Black,
                focusedPlaceholderColor = DarkBlue,
                unfocusedPlaceholderColor = DarkBlue,
                cursorColor = Blue,
                errorCursorColor = Color.Red,
                focusedSupportingTextColor = DarkBlue,
                unfocusedSupportingTextColor = DarkBlue
            ),
            singleLine = true,
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ✅ Исправленный OutlinedTextField для пароля
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .border(
                    width = 2.dp,
                    color = Blue,
                    shape = RoundedCornerShape(37.dp)
                ),
            value = password,
            onValueChange = {
                password = it
                errorMessage = null
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            textStyle = TextStyle(
                color = Black,
                fontSize = 18.sp,
                fontFamily = FontFamily(Font(R.font.jura))
            ),
            visualTransformation = PasswordVisualTransformation(),
            placeholder = {
                Text(
                    text = "Введите пароль",
                    style = TextStyle(
                        color = DarkBlue,
                        fontSize = 18.sp,
                        fontFamily = FontFamily(Font(R.font.jura))
                    )
                )
            },
            shape = RoundedCornerShape(37.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedTextColor = Black,
                unfocusedTextColor = Black,
                focusedPlaceholderColor = DarkBlue,
                unfocusedPlaceholderColor = DarkBlue,
                cursorColor = Blue,
                errorCursorColor = Color.Red
            ),
            singleLine = true,
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .shadow(
                    elevation = 4.dp,
                    ambientColor = Color.Black.copy(alpha = 0.2f),
                    spotColor = Color.Black.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(30.dp)
                ),
            shape = RoundedCornerShape(30.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Заполните все поля"
                        return@Button
                    }

                    isLoading = true
                    coroutineScope.launch {
                        authManager.SignInWithEmail(email, password)
                            .collect { response ->
                                isLoading = false
                                when (response) {
                                    is AuthResponse.Succes -> {
                                        Toast.makeText(context, "Успешный вход!", Toast.LENGTH_LONG).show()
                                        onDismiss()
                                        navController.navigate(Routes.MainContent.route) {
                                            popUpTo(Routes.MainClass.route) { inclusive = true }
                                        }
                                    }
                                    is AuthResponse.Error -> {
                                        errorMessage = response.message ?: "Ошибка входа"
                                    }
                                }
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLoading) Blue.copy(alpha = 0.7f) else Blue
                ),
                enabled = !isLoading
            ) {
                Text(
                    modifier = Modifier.padding(vertical = 12.dp),
                    text = if (isLoading) "Вход..." else "Войти",
                    style = TextStyle(
                        color = Black,
                        fontSize = 20.sp,
                        fontFamily = FontFamily(Font(R.font.jura))
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = { /* Переход к регистрации */ }
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Light,
                            color = Color.Gray
                        )
                    ) {
                        append("Нет аккаунта? ")
                    }
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = DarkBlue
                        )
                    ) {
                        append("Регистрация")
                    }
                },
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(Color.Gray.copy(alpha = 0.3f))
            )
            Text(
                modifier = Modifier.padding(horizontal = 12.dp),
                text = "Войти с помощью:",
                style = TextStyle(
                    color = Color.Gray,
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(R.font.jura))
                )
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(Color.Gray.copy(alpha = 0.3f))
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GoogleSignInButton(
                onClick = {
                    if (isLoading) return@GoogleSignInButton
                    isLoading = true
                    coroutineScope.launch {
                        authManager.loginGoogleUser()
                            .collect { response ->
                                isLoading = false
                                when (response) {
                                    is AuthResponse.Succes -> {
                                        Toast.makeText(context, "Вход через Google успешен!", Toast.LENGTH_LONG).show()
                                        onDismiss()
                                        navController.navigate(Routes.MainContent.route) {
                                            popUpTo(Routes.MainClass.route) { inclusive = true }
                                        }
                                    }
                                    is AuthResponse.Error -> {
                                        errorMessage = response.message ?: "Ошибка входа через Google"
                                    }
                                }
                            }
                    }
                },
                enabled = !isLoading
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun GoogleSignInButton(
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .background(BackgroundColor)
            .fillMaxWidth(0.6f)
            .padding(horizontal = 8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Black
        ),
        shape = RoundedCornerShape(30.dp),
        enabled = enabled
    ) {
        Image(
            modifier = Modifier.padding(end = 8.dp),
            painter = painterResource(id = R.drawable.google_icon),
            contentDescription = "Google Icon"
        )
        Text(
            text = "Google",
            style = TextStyle(
                color = Black,
                fontSize = 18.sp,
                fontFamily = FontFamily(Font(R.font.jura))
            )
        )
    }
}