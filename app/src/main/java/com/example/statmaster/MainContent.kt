package com.example.statmaster

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.statmaster.ui.theme.BackgroundColor
import com.example.statmaster.ui.theme.Black
import com.example.statmaster.ui.theme.DarkBlue
import com.example.statmaster.ui.theme.White

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

    val statOptions = listOf("Опция 1", "Опция 2", "Опция 3")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(horizontal = 30.dp)
    ) {

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

        ExpandableCard(
            title = "Теория вероятностей",
            options = terVerOptions,
            onItemSelected = { selectedOption ->
                when (selectedOption) {
                    "Случайные события" ->
                        navController.navigate(Routes.LevelsTerVer.createRoute())
                    "Теоремы вероятностей" ->
                        navController.navigate(Routes.LevelsTerVer.createRoute("chapter2"))
                    "Одномерные случайные величины" ->
                        navController.navigate(Routes.LevelsTerVer.createRoute("chapter3"))
                    // Добавьте обработку для остальных опций по необходимости
                }
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        ExpandableCard(
            title = "Статистика",
            options = statOptions,
            onItemSelected = { selectedOption ->
                // Обработка выбора для статистики
            }
        )
    }
}

@Composable
fun ExpandableCard(
    title: String,
    options: List<String>,
    onItemSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by remember(expanded) {
        mutableStateOf(if (expanded) 90f else 0f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = DarkBlue),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = TextStyle(
                        color = White,
                        fontSize = 20.sp,
                        fontFamily = FontFamily(Font(R.font.jura))
                    )
                )

                Image(
                    painter = painterResource(id = R.drawable.arrow_icon_white),
                    contentDescription = "Expand/Collapse",
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(rotation)
                )
            }

            // Анимированное раскрытие списка опций
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(animationSpec = tween(300)),
                exit = shrinkVertically(animationSpec = tween(300))
            ) {
                Column {
                    Divider(color = White.copy(alpha = 0.3f), thickness = 1.dp)

                    options.forEachIndexed { index, option ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onItemSelected(option)
                                    expanded = false
                                }
                                .padding(vertical = 12.dp, horizontal = 16.dp)
                        ) {


                                Text(
                                    text = option,
                                    style = TextStyle(
                                        color = White,
                                        fontSize = 16.sp,
                                        fontFamily = FontFamily(Font(R.font.jura))
                                    ),
                                    //modifier = Modifier.align(Alignment.CenterStart)
                                )


                        }

                        // Разделитель между элементами (кроме последнего)
                        if (index < options.size - 1) {
                            Divider(
                                color = White.copy(alpha = 0.2f),
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}