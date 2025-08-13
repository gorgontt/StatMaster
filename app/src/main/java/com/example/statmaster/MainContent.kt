package com.example.statmaster

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.navigation.NavController
import com.example.statmaster.ui.theme.BackgroundColor
import com.example.statmaster.ui.theme.Black
import com.example.statmaster.ui.theme.DarkBlue
import com.example.statmaster.ui.theme.Transparent

@Composable
fun MainContent(navController: NavController) {

    val terVerOptions = listOf(
        "Случайные события",
        "Теоремы вероятностей",
        "Одномерные случайные величины",
        "Многомерные случайные величины",
        "Закон больших чисел и центральная предметная теорема",
        "Математическая статистика"
    )

    val statOptions = listOf("Скопировать", "Вставить", "Настройки")

    Column(modifier = Modifier.fillMaxSize().background(BackgroundColor)) {

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

        CustomDropdownCard(
            title = "Теория вероятностей",
            options = terVerOptions,
            onItemSelected = { selectedOption ->
                when (selectedOption) {
                    "Случайные события" ->
                        navController.navigate(Routes.LevelsTerVer.route)
                    "Теоремы вероятностей" ->
                        navController.navigate("${Routes.LevelsTerVer.route}?startFrom=chapter2")
                }
            }
        )



        CustomDropdownCard(
            title = "Статистика",
            options = statOptions,
            onItemSelected = { selectedOption ->
                // Обработка выбора для статистики
            }
        )



    }

}

@Composable
fun CustomDropdownCard(
    title: String,
    options: List<String>,
    onItemSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf("") }
    val cardHorizontalPadding = 30.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = cardHorizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Карточка
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 4.dp,
                    ambientColor = Color.Black,
                    spotColor = Color.Black,
                    shape = RoundedCornerShape(9.dp)
                )
                .clickable { expanded = true },
            shape = RoundedCornerShape(9.dp),
            colors = CardDefaults.cardColors(BackgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 9.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = title,
                    style = TextStyle(
                        color = Black,
                        fontSize = 20.sp,
                        fontFamily = FontFamily(Font(R.font.jura))
                    ),
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Dropdown",
                    tint = Black
                )
            }
        }

        Box(
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(BackgroundColor)
                    .padding(start = 0.dp)
                    .fillMaxWidth(0.85f)
                    .align(Alignment.Center),

            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        onClick = {
                            selectedOption = option
                            onItemSelected(option)
                            expanded = false
                        },
                        text = {
                            Text(
                                text = option,
                                style = TextStyle(
                                    fontFamily = FontFamily(Font(R.font.jura))
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (option != options.last()) {
                        //Divider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }
}

