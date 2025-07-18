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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
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
fun BottomSheetSignInDialogContent(onDismiss: () -> Unit, navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    //val navController = LocalNavController.current

    // Проверяем, авторизован ли пользователь
    LaunchedEffect(Unit) {
        val session = authManager.supabase.auth.currentSessionOrNull()
        if (session != null) {
            Toast.makeText(context, "Good", Toast.LENGTH_LONG).show()
//            onDismiss()
//            navController.navigate(Routes.MainContent.route) {
//                popUpTo(Routes.MainClass.route) { inclusive = true }
//            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
            .background(BackgroundColor)
    ) {
        Text(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 16.dp),
            text = "Вход в аккаунт",
            style = TextStyle(
                color = DarkBlue,
                fontSize = 30.sp,
                fontFamily = FontFamily(Font(R.font.jura_semibold))
            )
        )

        Spacer(modifier = Modifier.height(30.dp))

        // Отображение ошибки, если есть
        errorMessage?.let { message ->
            Text(
                text = message,
                color = Color.Red,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        OutlinedTextField(
            modifier = Modifier
                .background(Transparent)
                .fillMaxWidth()
                .padding(horizontal = 30.dp)
                .border(
                    width = 2.dp,
                    color = Blue,
                    shape = RoundedCornerShape(37.dp)
                ),
            value = email,
            onValueChange = { email = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            textStyle = TextStyle(
                color = Black,
                fontSize = 20.sp,
                fontFamily = FontFamily(Font(R.font.jura))
            ),
            placeholder = {
                Text(text = "Адрес электронной почты",
                    style = TextStyle(
                        color = DarkBlue,
                        fontSize = 20.sp,
                        fontFamily = FontFamily(Font(R.font.jura))
                    )
                )
            },
            shape = RoundedCornerShape(37.dp),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Transparent,
                unfocusedTextColor = Color.Gray,
                focusedContainerColor = Blue,
                focusedTextColor = Black
            )
        )

        Spacer(modifier = Modifier.height(15.dp))

        OutlinedTextField(
            modifier = Modifier
                .background(Transparent)
                .fillMaxWidth()
                .padding(horizontal = 30.dp)
                .border(
                    width = 2.dp,
                    color = Blue,
                    shape = RoundedCornerShape(37.dp)
                ),
            value = password,
            onValueChange = { password = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            textStyle = TextStyle(
                color = Black,
                fontSize = 20.sp,
                fontFamily = FontFamily(Font(R.font.jura))
            ),
            visualTransformation = PasswordVisualTransformation(),
            placeholder = {
                Text(text = "Введите пароль",
                    style = TextStyle(
                        color = DarkBlue,
                        fontSize = 20.sp,
                        fontFamily = FontFamily(Font(R.font.jura))
                    )
                )
            },
            shape = RoundedCornerShape(37.dp),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Transparent,
                unfocusedTextColor = Color.Gray,
                focusedContainerColor = Blue,
                focusedTextColor = Black
            )
        )

        Spacer(modifier = Modifier.height(30.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
                .align(alignment = Alignment.CenterHorizontally)
                .background(Transparent)
                .padding(start = 30.dp, end = 30.dp)
                .shadow(
                    elevation = 4.dp,
                    ambientColor = Color.Black,
                    spotColor = Color.Black,
                    shape = RoundedCornerShape(30.dp)
                ),
            shape = RoundedCornerShape(30.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Заполните все поля"
                        return@Button
                    }

                    coroutineScope.launch {
                        authManager.SignInWithEmail(email, password)
                            .collect { response ->
                                when (response) {
                                    is AuthResponse.Succes -> {
                                        Toast.makeText(context, "Успешный вход", Toast.LENGTH_LONG).show()
                                        onDismiss()
//                                        navController.navigate(Routes.MainContent.route) {
//                                            popUpTo(Routes.MainClass.route) { inclusive = true }
//                                        }
                                    }
                                    is AuthResponse.Error -> {
                                        errorMessage = response.message ?: "Ошибка входа"
                                    }
                                }
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth().background(BackgroundColor),
                colors = ButtonDefaults.buttonColors(containerColor = Blue),
            ) {
                Text(
                    modifier = Modifier.padding(top = 10.dp, bottom = 10.dp),
                    text = "Войти",
                    style = TextStyle(
                        color = Black,
                        fontSize = 20.sp,
                        fontFamily = FontFamily(Font(R.font.jura))
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(5.dp))

        TextButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = { /* Переход к регистрации */ }
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Light,
                            color = Color.Gray
                        )
                    ) {
                        append("Нет аккаунта?")
                    }
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                    ) {
                        append("Регистрация")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(Color.Gray.copy(alpha = 0.2f))
            )
            Text(
                modifier = Modifier.padding(horizontal = 10.dp),
                text = "Войти с помощью:",
                style = TextStyle(
                    color = Black,
                    fontSize = 16.sp,
                    fontFamily = FontFamily(Font(R.font.jura))
                )
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(Color.Gray.copy(alpha = 0.2f))
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GoogleSignInButton(onClick = {
                coroutineScope.launch {
                    authManager.loginGoogleUser()
                        .collect { response ->
                            when (response) {
                                is AuthResponse.Succes -> {
                                    Toast.makeText(context, "Good", Toast.LENGTH_LONG).show()
                                    navController.navigate(Routes.MainContent.route)
                                    onDismiss()
//                                    navController.navigate(Routes.MainContent.route) {
//                                        popUpTo(Routes.MainClass.route) { inclusive = true }
//                                    }
                                }
                                is AuthResponse.Error -> {
                                    errorMessage = response.message ?: "Ошибка входа через Google"
                                }
                            }
                        }
                }
            })
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
private fun GoogleSignInButton(onClick: () -> Unit){
    OutlinedButton (onClick = onClick,
        modifier = Modifier.background(BackgroundColor).fillMaxWidth().padding(start = 30.dp, end = 30.dp),
        colors = ButtonDefaults.buttonColors(containerColor = BackgroundColor),
        shape = RoundedCornerShape(30.dp)
    ) {
        Image(
            modifier = Modifier,
            painter = painterResource(id = R.drawable.google_icon),
            contentDescription = "GoogleIcon"
        )

        Text(
            modifier = Modifier.padding(horizontal = 10.dp),
            text = "Google",
            style = TextStyle(
                color = Black, fontSize = 20.sp, fontFamily = FontFamily(
                    Font(R.font.jura)
                )
            )
        )
    }
}


