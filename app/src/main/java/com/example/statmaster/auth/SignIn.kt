package com.example.statmaster.auth

import android.content.ContentValues.TAG
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.example.statmaster.R
import com.example.statmaster.ui.theme.BackgroundColor
import com.example.statmaster.ui.theme.Black
import com.example.statmaster.ui.theme.Blue
import com.example.statmaster.ui.theme.DarkBlue
import com.example.statmaster.ui.theme.Transparent
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetSignInDialogContent(onDismiss: () -> Unit) {

    //Google SignIn
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val onClick: () -> Unit = {
        val credentialManager = CredentialManager.create(context)

        // Generate a nonce and hash it with sha-256
        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

        // Замените на ваш реальный client ID из Google Cloud Console
        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId("850534604231-elobf2jjkap2pqqs6bcupguulid02crc.apps.googleusercontent.com")
            .setNonce(hashedNonce)
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        coroutineScope.launch {
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

                // Здесь можно добавить логику входа через Supabase
                /*
                supabase.auth.signInWith(IDToken) {
                    idToken = googleIdToken
                    provider = Google
                    nonce = rawNonce
                }
                */

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

    var email by remember { mutableStateOf("Эл.почта") }
    var password by remember { mutableStateOf("Пароль") }

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
            text = "Вход в аккаунт",
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
            value = email,
            onValueChange = { email = it },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            textStyle = TextStyle(
                color = Black,
                fontSize = 20.sp,
                fontFamily = FontFamily(Font(R.font.jura))
            ),
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
            shape = RoundedCornerShape(37.dp),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Transparent,
                unfocusedTextColor = Color.Gray,
                focusedContainerColor = Blue,
                focusedTextColor = Black
            )
        )

        Spacer(modifier = Modifier.height(30.dp))


        val buttonVisible = email.isNotBlank()
        AnimatedVisibility(visible = buttonVisible) {

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
                        if (email.isNotBlank()) {
                            email = ""
                        }
                        onDismiss()
                    },
                    //contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth().background(BackgroundColor),
                    colors = ButtonDefaults.buttonColors(containerColor = Blue),
                ) {


                    Text(
                        modifier = Modifier.padding(top = 10.dp, bottom = 10.dp),
                        text = "Войти",
                        style = TextStyle(
                            color = Black, fontSize = 20.sp, fontFamily = FontFamily(
                                Font(R.font.jura)
                            )
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            modifier = Modifier.padding(top = 10.dp, bottom = 10.dp).align(Alignment.CenterHorizontally),
            text = "Войти с помощью:",
            style = TextStyle(
                color = Black, fontSize = 16.sp, fontFamily = FontFamily(
                    Font(R.font.jura)
                )
            )
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically ){

            Button(onClick = onClick) {

                Image(
                    modifier = Modifier.padding(20.dp),
                    painter = painterResource(id = R.drawable.google_icon),
                    contentDescription = "GoogleIcon"
                )
            }


            Image(
                modifier = Modifier.padding(20.dp),
                painter = painterResource(id = R.drawable.facebook_icon),
                contentDescription = "FaceBookIcon"
            )

        }
    }
}
