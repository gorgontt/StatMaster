package com.example.statmaster.terver

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.statmaster.R
import com.example.statmaster.ui.theme.BackgroundColor
import com.example.statmaster.ui.theme.Black
import com.example.statmaster.ui.theme.DarkBlue
import com.example.statmaster.ui.theme.Transparent

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LevelsTerVer(navController: NavController){

    Scaffold(
        modifier = Modifier.background(BackgroundColor),
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Теория вероятностей", fontSize = 22.sp) },
                navigationIcon = {
                    IconButton({ }) {
                        Icon(
                            painter =  painterResource(id = R.drawable.back_icon),
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // IconButton({ }) { Icon(Icons.Filled.Info, contentDescription = "О приложении")}
                    //IconButton({ }) {Icon(Icons.Filled.Search, contentDescription = "Поиск")}
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundColor,
                    titleContentColor = DarkBlue,
                    navigationIconContentColor = DarkBlue,
                    actionIconContentColor = DarkBlue
                )
            )
        },

        content = {
           ContentTerVerLevels()
        }

    )

}

@Composable
fun ContentTerVerLevels(){
    Column(
        modifier = Modifier
        .fillMaxHeight()
        .fillMaxWidth()
        .background(BackgroundColor)
        .verticalScroll(rememberScrollState())
        .offset(0.dp, 100.dp)) {

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(alignment = Alignment.CenterHorizontally)
                .background(BackgroundColor)
                .padding(top = 10.dp, bottom = 5.dp, start = 30.dp, end = 30.dp)
                .shadow(
                    elevation = 4.dp,
                    ambientColor = Color.Black,
                    spotColor = Color.Black,
                    shape = RoundedCornerShape(40.dp)
                )

                .clickable {
                    //navController.navigate("players_list/компания")
                },
            shape = RoundedCornerShape(40.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ){

            Row(modifier = Modifier.background(BackgroundColor).fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {

                Column (modifier = Modifier.padding(start = 40.dp, top = 10.dp, bottom = 10.dp).weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {

                    Text(
                        modifier = Modifier,
                        text = "Уровень 1",
                        style = TextStyle(
                            color = Black, fontSize = 20.sp, fontFamily = FontFamily(
                                Font(R.font.jura_semibold)
                            )
                        )
                    )

                    Text(
                        modifier = Modifier.padding(top = 5.dp),
                        text = "Основы статистики",
                        style = TextStyle(
                            color = Black, fontSize = 14.sp, fontFamily = FontFamily(
                                Font(R.font.jura)
                            )
                        )
                    )
                }

                Image(
                    modifier = Modifier.padding(end = 20.dp),
                    painter = painterResource(id = R.drawable.tick_icon),
                    contentDescription = "GoogleIcon"
                )

            }


        }


        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(alignment = Alignment.CenterHorizontally)
                .background(BackgroundColor)
                .padding(top = 10.dp, bottom = 5.dp, start = 30.dp, end = 30.dp)
                .shadow(
                    elevation = 4.dp,
                    ambientColor = Color.Black,
                    spotColor = Color.Black,
                    shape = RoundedCornerShape(40.dp)
                )

                .clickable {
                    //navController.navigate("players_list/компания")
                },
            shape = RoundedCornerShape(40.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ){

            Row(modifier = Modifier.background(BackgroundColor).fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {

                Column (modifier = Modifier.padding(start = 40.dp, top = 10.dp, bottom = 10.dp).weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {

                    Text(
                        modifier = Modifier,
                        text = "Уровень 1",
                        style = TextStyle(
                            color = Black, fontSize = 20.sp, fontFamily = FontFamily(
                                Font(R.font.jura_semibold)
                            )
                        )
                    )

                    Text(
                        modifier = Modifier.padding(top = 5.dp),
                        text = "Основы статистики",
                        style = TextStyle(
                            color = Black, fontSize = 14.sp, fontFamily = FontFamily(
                                Font(R.font.jura)
                            )
                        )
                    )
                }

                Image(
                    modifier = Modifier.padding(end = 20.dp),
                    painter = painterResource(id = R.drawable.tick_icon),
                    contentDescription = "GoogleIcon"
                )

            }


        }



        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(alignment = Alignment.CenterHorizontally)
                .background(BackgroundColor)
                .padding(top = 10.dp, bottom = 5.dp, start = 30.dp, end = 30.dp)
                .shadow(
                    elevation = 4.dp,
                    ambientColor = Color.Black,
                    spotColor = Color.Black,
                    shape = RoundedCornerShape(40.dp)
                )

                .clickable {
                    //navController.navigate("players_list/компания")
                },
            shape = RoundedCornerShape(40.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ){

            Row(modifier = Modifier.background(BackgroundColor).fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {

                Column (modifier = Modifier.padding(start = 40.dp, top = 10.dp, bottom = 10.dp).weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {

                    Text(
                        modifier = Modifier,
                        text = "Уровень 1",
                        style = TextStyle(
                            color = Black, fontSize = 20.sp, fontFamily = FontFamily(
                                Font(R.font.jura_semibold)
                            )
                        )
                    )

                    Text(
                        modifier = Modifier.padding(top = 5.dp),
                        text = "Основы статистики",
                        style = TextStyle(
                            color = Black, fontSize = 14.sp, fontFamily = FontFamily(
                                Font(R.font.jura)
                            )
                        )
                    )
                }

                Image(
                    modifier = Modifier.padding(end = 20.dp),
                    painter = painterResource(id = R.drawable.tick_icon),
                    contentDescription = "GoogleIcon"
                )

            }


        }




        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(alignment = Alignment.CenterHorizontally)
                .background(BackgroundColor)
                .padding(top = 10.dp, bottom = 5.dp, start = 30.dp, end = 30.dp)
                .shadow(
                    elevation = 4.dp,
                    ambientColor = Color.Black,
                    spotColor = Color.Black,
                    shape = RoundedCornerShape(40.dp)
                )

                .clickable {
                    //navController.navigate("players_list/компания")
                },
            shape = RoundedCornerShape(40.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ){

            Row(modifier = Modifier.background(BackgroundColor).fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {

                Column (modifier = Modifier.padding(start = 40.dp, top = 10.dp, bottom = 10.dp).weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {

                    Text(
                        modifier = Modifier,
                        text = "Уровень 1",
                        style = TextStyle(
                            color = Black, fontSize = 20.sp, fontFamily = FontFamily(
                                Font(R.font.jura_semibold)
                            )
                        )
                    )

                    Text(
                        modifier = Modifier.padding(top = 5.dp),
                        text = "Основы статистики",
                        style = TextStyle(
                            color = Black, fontSize = 14.sp, fontFamily = FontFamily(
                                Font(R.font.jura)
                            )
                        )
                    )
                }

                Image(
                    modifier = Modifier.padding(end = 20.dp),
                    painter = painterResource(id = R.drawable.tick_icon),
                    contentDescription = "GoogleIcon"
                )

            }


        }



        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(alignment = Alignment.CenterHorizontally)
                .background(BackgroundColor)
                .padding(top = 10.dp, bottom = 5.dp, start = 30.dp, end = 30.dp)
                .shadow(
                    elevation = 4.dp,
                    ambientColor = Color.Black,
                    spotColor = Color.Black,
                    shape = RoundedCornerShape(40.dp)
                )

                .clickable {
                    //navController.navigate("players_list/компания")
                },
            shape = RoundedCornerShape(40.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ){

            Row(modifier = Modifier.background(BackgroundColor).fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {

                Column (modifier = Modifier.padding(start = 40.dp, top = 10.dp, bottom = 10.dp).weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {

                    Text(
                        modifier = Modifier,
                        text = "Уровень 1",
                        style = TextStyle(
                            color = Black, fontSize = 20.sp, fontFamily = FontFamily(
                                Font(R.font.jura_semibold)
                            )
                        )
                    )

                    Text(
                        modifier = Modifier.padding(top = 5.dp),
                        text = "Основы статистики",
                        style = TextStyle(
                            color = Black, fontSize = 14.sp, fontFamily = FontFamily(
                                Font(R.font.jura)
                            )
                        )
                    )
                }

                Image(
                    modifier = Modifier.padding(end = 20.dp),
                    painter = painterResource(id = R.drawable.tick_icon),
                    contentDescription = "GoogleIcon"
                )

            }


        }



        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(alignment = Alignment.CenterHorizontally)
                .background(BackgroundColor)
                .padding(top = 10.dp, bottom = 5.dp, start = 30.dp, end = 30.dp)
                .shadow(
                    elevation = 4.dp,
                    ambientColor = Color.Black,
                    spotColor = Color.Black,
                    shape = RoundedCornerShape(40.dp)
                )

                .clickable {
                    //navController.navigate("players_list/компания")
                },
            shape = RoundedCornerShape(40.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ){

            Row(modifier = Modifier.background(BackgroundColor).fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {

                Column (modifier = Modifier.padding(start = 40.dp, top = 10.dp, bottom = 10.dp).weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {

                    Text(
                        modifier = Modifier,
                        text = "Уровень 1",
                        style = TextStyle(
                            color = Black, fontSize = 20.sp, fontFamily = FontFamily(
                                Font(R.font.jura_semibold)
                            )
                        )
                    )

                    Text(
                        modifier = Modifier.padding(top = 5.dp),
                        text = "Основы статистики",
                        style = TextStyle(
                            color = Black, fontSize = 14.sp, fontFamily = FontFamily(
                                Font(R.font.jura)
                            )
                        )
                    )
                }

                Image(
                    modifier = Modifier.padding(end = 20.dp),
                    painter = painterResource(id = R.drawable.tick_icon),
                    contentDescription = "GoogleIcon"
                )

            }


        }


        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(alignment = Alignment.CenterHorizontally)
                .background(BackgroundColor)
                .padding(top = 10.dp, bottom = 5.dp, start = 30.dp, end = 30.dp)
                .shadow(
                    elevation = 4.dp,
                    ambientColor = Color.Black,
                    spotColor = Color.Black,
                    shape = RoundedCornerShape(40.dp)
                )

                .clickable {
                    //navController.navigate("players_list/компания")
                },
            shape = RoundedCornerShape(40.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ){

            Row(modifier = Modifier.background(BackgroundColor).fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {

                Column (modifier = Modifier.padding(start = 40.dp, top = 10.dp, bottom = 10.dp).weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {

                    Text(
                        modifier = Modifier,
                        text = "Уровень 1",
                        style = TextStyle(
                            color = Black, fontSize = 20.sp, fontFamily = FontFamily(
                                Font(R.font.jura_semibold)
                            )
                        )
                    )

                    Text(
                        modifier = Modifier.padding(top = 5.dp),
                        text = "Основы статистики",
                        style = TextStyle(
                            color = Black, fontSize = 14.sp, fontFamily = FontFamily(
                                Font(R.font.jura)
                            )
                        )
                    )
                }

                Image(
                    modifier = Modifier.padding(end = 20.dp),
                    painter = painterResource(id = R.drawable.tick_icon),
                    contentDescription = "GoogleIcon"
                )

            }


        }


        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(alignment = Alignment.CenterHorizontally)
                .background(BackgroundColor)
                .padding(top = 10.dp, bottom = 5.dp, start = 30.dp, end = 30.dp)
                .shadow(
                    elevation = 4.dp,
                    ambientColor = Color.Black,
                    spotColor = Color.Black,
                    shape = RoundedCornerShape(40.dp)
                )

                .clickable {
                    //navController.navigate("players_list/компания")
                },
            shape = RoundedCornerShape(40.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ){

            Row(modifier = Modifier.background(BackgroundColor).fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {

                Column (modifier = Modifier.padding(start = 40.dp, top = 10.dp, bottom = 10.dp).weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {

                    Text(
                        modifier = Modifier,
                        text = "Уровень 1",
                        style = TextStyle(
                            color = Black, fontSize = 20.sp, fontFamily = FontFamily(
                                Font(R.font.jura_semibold)
                            )
                        )
                    )

                    Text(
                        modifier = Modifier.padding(top = 5.dp),
                        text = "Основы статистики",
                        style = TextStyle(
                            color = Black, fontSize = 14.sp, fontFamily = FontFamily(
                                Font(R.font.jura)
                            )
                        )
                    )
                }

                Image(
                    modifier = Modifier.padding(end = 20.dp),
                    painter = painterResource(id = R.drawable.tick_icon),
                    contentDescription = "GoogleIcon"
                )

            }


        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(alignment = Alignment.CenterHorizontally)
                .background(BackgroundColor)
                .padding(top = 10.dp, bottom = 5.dp, start = 30.dp, end = 30.dp)
                .shadow(
                    elevation = 4.dp,
                    ambientColor = Color.Black,
                    spotColor = Color.Black,
                    shape = RoundedCornerShape(40.dp)
                )

                .clickable {
                    //navController.navigate("players_list/компания")
                },
            shape = RoundedCornerShape(40.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ){

            Row(modifier = Modifier.background(BackgroundColor).fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {

                Column (modifier = Modifier.padding(start = 40.dp, top = 10.dp, bottom = 10.dp).weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {

                    Text(
                        modifier = Modifier,
                        text = "Уровень 1",
                        style = TextStyle(
                            color = Black, fontSize = 20.sp, fontFamily = FontFamily(
                                Font(R.font.jura_semibold)
                            )
                        )
                    )

                    Text(
                        modifier = Modifier.padding(top = 5.dp),
                        text = "Основы статистики",
                        style = TextStyle(
                            color = Black, fontSize = 14.sp, fontFamily = FontFamily(
                                Font(R.font.jura)
                            )
                        )
                    )
                }

                Image(
                    modifier = Modifier.padding(end = 20.dp),
                    painter = painterResource(id = R.drawable.tick_icon),
                    contentDescription = "GoogleIcon"
                )

            }


        }


        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(alignment = Alignment.CenterHorizontally)
                .background(BackgroundColor)
                .padding(top = 10.dp, bottom = 5.dp, start = 30.dp, end = 30.dp)
                .shadow(
                    elevation = 4.dp,
                    ambientColor = Color.Black,
                    spotColor = Color.Black,
                    shape = RoundedCornerShape(40.dp)
                )

                .clickable {
                    //navController.navigate("players_list/компания")
                },
            shape = RoundedCornerShape(40.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ){

            Row(modifier = Modifier.background(BackgroundColor).fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {

                Column (modifier = Modifier.padding(start = 40.dp, top = 10.dp, bottom = 10.dp).weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {

                    Text(
                        modifier = Modifier,
                        text = "Уровень 1",
                        style = TextStyle(
                            color = Black, fontSize = 20.sp, fontFamily = FontFamily(
                                Font(R.font.jura_semibold)
                            )
                        )
                    )

                    Text(
                        modifier = Modifier.padding(top = 5.dp),
                        text = "Основы статистики",
                        style = TextStyle(
                            color = Black, fontSize = 14.sp, fontFamily = FontFamily(
                                Font(R.font.jura)
                            )
                        )
                    )
                }

                Image(
                    modifier = Modifier.padding(end = 20.dp),
                    painter = painterResource(id = R.drawable.tick_icon),
                    contentDescription = "GoogleIcon"
                )

            }


        }


        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(alignment = Alignment.CenterHorizontally)
                .background(BackgroundColor)
                .padding(top = 10.dp, bottom = 5.dp, start = 30.dp, end = 30.dp)
                .shadow(
                    elevation = 4.dp,
                    ambientColor = Color.Black,
                    spotColor = Color.Black,
                    shape = RoundedCornerShape(40.dp)
                )

                .clickable {
                    //navController.navigate("players_list/компания")
                },
            shape = RoundedCornerShape(40.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ){

            Row(modifier = Modifier.background(BackgroundColor).fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {

                Column (modifier = Modifier.padding(start = 40.dp, top = 10.dp, bottom = 10.dp).weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {

                    Text(
                        modifier = Modifier,
                        text = "Уровень 1",
                        style = TextStyle(
                            color = Black, fontSize = 20.sp, fontFamily = FontFamily(
                                Font(R.font.jura_semibold)
                            )
                        )
                    )

                    Text(
                        modifier = Modifier.padding(top = 5.dp),
                        text = "Основы статистики",
                        style = TextStyle(
                            color = Black, fontSize = 14.sp, fontFamily = FontFamily(
                                Font(R.font.jura)
                            )
                        )
                    )
                }

                Image(
                    modifier = Modifier.padding(end = 20.dp),
                    painter = painterResource(id = R.drawable.tick_icon),
                    contentDescription = "GoogleIcon"
                )

            }


        }










    }

}