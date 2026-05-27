package com.example.statmaster.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.statmaster.AuthManager
import com.example.statmaster.R
import com.example.statmaster.Routes
import com.example.statmaster.ui.theme.BackgroundColor
import com.example.statmaster.ui.theme.Black
import com.example.statmaster.ui.theme.Blue
import com.example.statmaster.ui.theme.DarkBlue
import com.example.statmaster.ui.theme.Transparent
import com.example.statmaster.ui.theme.White
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainClass(navController: NavController){

    //val navController = LocalNavController.current

    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val coroutineScope = rememberCoroutineScope()

    var isCheckingSession by remember { mutableStateOf(true) }


    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val session = authManager.supabase.auth.currentSessionOrNull()
            if (session != null) {
                navController.navigate(Routes.MainContent.route) {
                    popUpTo(Routes.MainClass.route) { inclusive = true }
                }
            }
            isCheckingSession = false
        }
    }

    // Пока проверяем сессию — показываем загрузку
    if (isCheckingSession) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .background(Blue), verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {


        Spacer(modifier = Modifier
            .height(30.dp)
            .background(BackgroundColor))

        Text(modifier = Modifier.align(Alignment.CenterHorizontally),
            text = "StatMaster",
            style = TextStyle(
                color = Black, fontSize = 32.sp, fontFamily = FontFamily(
                    Font(R.font.jura)
                )
            )
        )

        Spacer(modifier = Modifier
            .height(20.dp)
            .background(BackgroundColor))

        Image(
            modifier = Modifier.padding(20.dp),
            painter = painterResource(id = R.drawable.main_icon_background),
            contentDescription = "MainIcon"
        )

        Spacer(modifier = Modifier
            .height(20.dp)
            .background(BackgroundColor))

        Column (modifier = Modifier
            .fillMaxWidth()
            .padding(start = 30.dp, end = 30.dp)
            .fillMaxHeight(0.4f)
            .background(Blue)){


            val sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true,

            )
            var isSheetOpen by rememberSaveable { mutableStateOf(false) }


            val sheet2State = rememberModalBottomSheetState(
                skipPartiallyExpanded = true,

            )
            var isSheet2Open by rememberSaveable { mutableStateOf(false) }

//            val sheetState = androidx.compose.material3.rememberModalBottomSheetState()
//            var isSheetOpen by rememberSaveable {
//                mutableStateOf(false)
//            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(alignment = Alignment.CenterHorizontally)
                    .background(Transparent)
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
            ){

                Button(
                    onClick = {isSheetOpen = true},
                    //contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BackgroundColor),
                    colors = ButtonDefaults.buttonColors(containerColor = White),
                ) {


                    Text(
                        modifier = Modifier.padding(top=10.dp, bottom = 10.dp),
                        text = "Создать аккаунт",
                        style = TextStyle(
                            color = Black, fontSize = 20.sp, fontFamily = FontFamily(
                                Font(R.font.jura)
                            )
                        )
                    )
                }

                if (isSheetOpen) {
                    ModalBottomSheet(
                        containerColor = BackgroundColor,
                        sheetState = sheetState,
                        onDismissRequest = { isSheetOpen = false },
                        modifier = Modifier.fillMaxHeight() // Устанавливаем 70% высоты экрана
                    ) {
                        BottomSheetSignUpDialogContent(
                            onDismiss = { isSheetOpen = false },
                            navController = navController
                        )
                    }
                }
            }

            Spacer(modifier = Modifier
                .height(20.dp)
                .background(BackgroundColor))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(alignment = Alignment.CenterHorizontally)
                    .background(Transparent)
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
            ){

                Button(
                    onClick = {isSheet2Open = true},
                    //contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Blue),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBlue),
                ) {


                    Text(
                        modifier = Modifier.padding(top=10.dp, bottom = 10.dp),
                        text = "Войти",
                        style = TextStyle(
                            color = White, fontSize = 20.sp, fontFamily = FontFamily(
                                Font(R.font.jura)
                            )
                        )
                    )
                }

                if (isSheet2Open) {
                    ModalBottomSheet(
                        containerColor = BackgroundColor,
                        sheetState = sheet2State,
                        onDismissRequest = { isSheet2Open = false },
                        modifier = Modifier.fillMaxHeight(0.7f)
                    ) {
                        BottomSheetSignInDialogContent(
                            onDismiss = { isSheet2Open = false },
                            navController = navController
                        )
                    }
                }
            }}



    }
}
