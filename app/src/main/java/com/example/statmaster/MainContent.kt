package com.example.statmaster

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.statmaster.ui.theme.BackgroundColor
import com.example.statmaster.ui.theme.Black
import com.example.statmaster.ui.theme.DarkBlue
import com.example.statmaster.ui.theme.Transparent

@Composable
fun MainContent(navController: NavController){

    var expandedTerVer by remember { mutableStateOf(false) }
    var expandedStat by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf("") }
    val options = listOf("Option 1", "Option 2", "Option 3", "Option 4")

    Column (modifier = Modifier.fillMaxSize().background(BackgroundColor)){

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 16.dp),
            text = "Библиотека",
            style = TextStyle(
                color = DarkBlue,
                fontSize = 30.sp,
                fontFamily = FontFamily(Font(R.font.jura_semibold))
            )
        )

        Spacer(modifier = Modifier.height(30.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
                .background(BackgroundColor)
                .padding(start = 30.dp, end = 30.dp)
                .align(alignment = Alignment.CenterHorizontally)
                .shadow(
                    elevation = 4.dp,
                    ambientColor = Color.Black,
                    spotColor = Color.Black,
                    shape = RoundedCornerShape(9.dp)
                )

                .clickable {
                    expandedTerVer = true
                },
            shape = RoundedCornerShape(9.dp),
            colors = CardDefaults.cardColors(BackgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 9.dp)
        ) {


            Text(
                modifier = Modifier.padding(top = 15.dp, bottom = 15.dp, start = 10.dp),
                text = "Теория вероятностей",
                style = TextStyle(
                    color = Black, fontSize = 20.sp, fontFamily = FontFamily(
                        Font(R.font.jura)
                    )
                )
            )

            DropdownMenu(
                modifier = Modifier.background(BackgroundColor).fillMaxWidth(0.9f).align(Alignment.CenterHorizontally).padding(end = 30.dp),
                expanded = expandedTerVer,
                onDismissRequest = { expandedTerVer = false },
                offset = DpOffset(x = 20.dp, y = 10.dp)
            ) {
                DropdownMenuItem(
                    onClick = {navController.navigate(Routes.LevelsTerVer.route)},
                    text = { Text("Основные понятия теории вероятностей") }
                )
                Divider()
                DropdownMenuItem(
                    onClick = {  },
                    text = { Text("Комбинаторика для теории вероятностей") }
                )
                Divider()
                DropdownMenuItem(
                    onClick = { },
                    text = { Text("Условная вероятность и независимость") }
                )
                Divider()
                DropdownMenuItem(
                    onClick = { },
                    text = { Text("Случайные величины") }
                )
                Divider()
                DropdownMenuItem(
                    onClick = { },
                    text = { Text("Основные распределения вероятностей") }
                )
                Divider()
                DropdownMenuItem(
                    onClick = { },
                    text = { Text("Многомерные распределения") }
                )
                Divider()
                DropdownMenuItem(
                    onClick = { },
                    text = { Text("Предельные теоремы") }
                )
                Divider()
                DropdownMenuItem(
                    onClick = { },
                    text = { Text("Случайные процессы") }
                )
                Divider()
                DropdownMenuItem(
                    onClick = { },
                    text = { Text("Прикладные аспекты") }
                )
                Divider()
                DropdownMenuItem(
                    onClick = { },
                    text = { Text("Практикум и задачи") }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
                .background(BackgroundColor)
                .align(alignment = Alignment.CenterHorizontally)
                .padding(start = 30.dp, end = 30.dp)
                .shadow(
                    elevation = 4.dp,
                    ambientColor = Color.Black,
                    spotColor = Color.Black,
                    shape = RoundedCornerShape(9.dp)
                )

                .clickable {
                    //navController.navigate("players_list/компания")
                },
            shape = RoundedCornerShape(9.dp),
            colors = CardDefaults.cardColors(BackgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {


            Text(
                modifier = Modifier.padding(top = 15.dp, bottom = 15.dp, start = 10.dp),
                text = "Статистика",
                style = TextStyle(
                    color = Black, fontSize = 20.sp, fontFamily = FontFamily(
                        Font(R.font.jura)
                    )
                )
            )

            DropdownMenu(
                modifier = Modifier.background(BackgroundColor),
                expanded = expandedStat,
                onDismissRequest = { expandedStat = false },
                offset = DpOffset(x = 20.dp, y = 10.dp)
            ) {
                DropdownMenuItem(
                    onClick = {},
                    text = { Text("Скопировать") }
                )
                DropdownMenuItem(
                    onClick = {  },
                    text = { Text("Вставить") }
                )
                Divider()
                DropdownMenuItem(
                    onClick = { },
                    text = { Text("Настройки") }
                )
            }
        }



    }

}
