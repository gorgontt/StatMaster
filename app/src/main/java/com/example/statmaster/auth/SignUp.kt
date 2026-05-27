package com.example.statmaster.auth

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.KeyboardCapitalization
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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetSignUpDialogContent(onDismiss: () -> Unit, navController: NavController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var repeatPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()) // Добавляем возможность прокрутки если контент не помещается
            .padding(bottom = 24.dp) // Добавляем отступ снизу
            .background(BackgroundColor))
    {

        Text(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 16.dp),
            text = "Регистрация",
            style = TextStyle(
                color = DarkBlue,
                fontSize = 30.sp,
                fontFamily = FontFamily(Font(R.font.jura_semibold))
            )
        )

        Spacer(modifier = Modifier.height(30.dp))

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
            value = name,
            onValueChange = { name = it },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            textStyle = TextStyle(
                color = Black,
                fontSize = 20.sp,
                fontFamily = FontFamily(Font(R.font.jura))
            ),
            placeholder = { Text(text = "Введите Ваше имя", style = TextStyle(
                color = DarkBlue,
                fontSize = 20.sp,
                fontFamily = FontFamily(Font(R.font.jura))
            )) },
            shape = RoundedCornerShape(37.dp),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Transparent,
                unfocusedTextColor = Color.Gray,
                focusedContainerColor = Blue,
                focusedTextColor = Black,
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
            value = email,
            onValueChange = { email = it },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            textStyle = TextStyle(
                color = Black,
                fontSize = 20.sp,
                fontFamily = FontFamily(Font(R.font.jura))
            ),
            placeholder = { Text(text = "Адрес электронной почты", style = TextStyle(
                color = DarkBlue,
                fontSize = 20.sp,
                fontFamily = FontFamily(Font(R.font.jura))
            ))},
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
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            textStyle = TextStyle(
                color = Black,
                fontSize = 20.sp,
                fontFamily = FontFamily(Font(R.font.jura))
            ),
            visualTransformation = PasswordVisualTransformation(),
            placeholder = { Text(text = "Придумайте пароль", style = TextStyle(
                color = DarkBlue,
                fontSize = 20.sp,
                fontFamily = FontFamily(Font(R.font.jura))
            )) },
            shape = RoundedCornerShape(37.dp),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Transparent,
                unfocusedTextColor = Color.Gray,
                focusedContainerColor = Blue,
                focusedTextColor = Black
            )
        )
        errorMessage?.let { message ->
            Text(
                text = message,
                color = Color.Red,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp)
            )}



        Spacer(modifier = Modifier.height(30.dp))


        //val buttonVisible = name.isNotBlank()
        //AnimatedVisibility(visible = buttonVisible) {

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
                    )

                    .clickable {
                        //navController.navigate("players_list/компания")
                    },
                shape = RoundedCornerShape(30.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {

                Button(
                    onClick = {
                        authManager.SignUpWithEmail(email, password)
                            .onEach { result ->
                                when (result) {
                                    is AuthResponse.Succes -> {
                                        Log.d("auth", "Email success")
                                        onDismiss()
                                        navController.navigate(Routes.MainContent.route) {
                                            popUpTo(Routes.MainClass.route) { inclusive = true }
                                        }
                                    }
                                    is AuthResponse.Error -> {
                                        Log.d("auth", "Email failed: ${result.message}")
                                        Toast.makeText(context, result.message ?: "Ошибка регистрации", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }.launchIn(coroutineScope)
                    },
                    //contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth().background(BackgroundColor),
                    colors = ButtonDefaults.buttonColors(containerColor = Blue),
                ) {


                    Text(
                        modifier = Modifier.padding(top = 10.dp, bottom = 10.dp),
                        text = "Создать аккаунт",
                        style = TextStyle(
                            color = Black, fontSize = 20.sp, fontFamily = FontFamily(
                                Font(R.font.jura)
                            )
                        )
                    )
                }
            }


        }

        Spacer(modifier = Modifier.height(5.dp))

    TextButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = {}) {

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.Light,
                        color = Color.Gray
                    )
                ) {
                    append("Уже есть аккаунт?")
                }

                withStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                ) {
                    append("Войти")
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
                    color = Black, fontSize = 16.sp, fontFamily = FontFamily(
                        Font(R.font.jura)
                    )
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

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically ){

            
           GoogleSignInButton(onClick = {
               authManager.loginGoogleUser()
                   .onEach { result ->
                       if (result is AuthResponse.Succes){
//                           coroutineScope.launch {
//                               authManager.supabase.from("posts").insert(mapOf("content" to "Hello new user!"))
//                           }
                           Log.d("auth", "Google success")
                       }else{
                           Log.d("auth", "Google failed")
                       }
                   }.launchIn(coroutineScope)


           })


        }

        Spacer(modifier = Modifier.height(30.dp))
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


