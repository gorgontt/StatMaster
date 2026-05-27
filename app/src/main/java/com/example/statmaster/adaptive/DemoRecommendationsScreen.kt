package com.example.statmaster.adaptive

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.statmaster.R
import com.example.statmaster.ui.theme.BackgroundColor
import com.example.statmaster.ui.theme.Blue
import com.example.statmaster.ui.theme.DarkBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptiveTestResultScreen(
    navController: NavController,
    userLevel: String = "Начальный",
    abilityValue: Float = -0.56f,
    correctAnswers: Int = 4,
    totalQuestions: Int = 10,
    accuracy: Int = 40,
    avgTime: Int = 8
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Результаты теста") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Text("←", fontSize = 24.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundColor
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundColor)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Основная карточка с результатами
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Заголовок с поздравлением
                    Text(
                        text = "🎉 Тест завершен! Найдены похожие пользователи 🎉",
                        fontSize = 22.sp,
                        fontFamily = FontFamily(Font(R.font.jura_semibold)),
                        color = Color(0xFF4CAF50)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Информация об уровне
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Blue.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "📈 Ваш уровень: $userLevel",
                                fontSize = 18.sp,
                                fontFamily = FontFamily(Font(R.font.jura_semibold)),
                                color = DarkBlue
                            )
                            Text(
                                text = "Числовое значение θ = ${String.format("%.2f", abilityValue)}",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Статистика сессии
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "✅", fontSize = 24.sp)
                                Text(text = "$correctAnswers/$totalQuestions", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Text(text = "правильных", fontSize = 12.sp, color = Color.Gray)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "🎯", fontSize = 24.sp)
                                Text(text = "$accuracy%", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Text(text = "точность", fontSize = 12.sp, color = Color.Gray)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "⏱️", fontSize = 24.sp)
                                Text(text = "${avgTime}с", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Text(text = "в среднем", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ========== РЕКОМЕНДАЦИИ ОТ КОЛЛАБОРАТИВНОЙ ФИЛЬТРАЦИИ ==========
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkBlue.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "👥 НАЙДЕНЫ ПОХОЖИЕ ПОЛЬЗОВАТЕЛИ!",
                                    fontSize = 16.sp,
                                    fontFamily = FontFamily(Font(R.font.jura_semibold)),
                                    color = DarkBlue
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "На основе анализа ответов 156 пользователей найдено 3 человека со схожим уровнем знаний. Вот что им помогло:",
                                fontSize = 13.sp,
                                fontFamily = FontFamily(Font(R.font.jura)),
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Рекомендации от коллаборативной фильтрации
                    RecommendationCardCF(
                        icon = "📚",
                        title = "Рекомендация от похожих пользователей",
                        text = "Пользователи с похожим уровнем успешно прошли тему 'Случайные события' (85% правильных ответов). Рекомендуем вам повторить эту тему.",
                        difficulty = "easy"
                    )

                    RecommendationCardCF(
                        icon = "📖",
                        title = "Популярная тема",
                        text = "78% пользователей с вашим уровнем изучили раздел 'Теоремы вероятностей' после прохождения теста.",
                        difficulty = "medium"
                    )

                    RecommendationCardCF(
                        icon = "🎯",
                        title = "Следующий шаг",
                        text = "На основе анализа похожих пользователей рекомендуем перейти к теме 'Одномерные случайные величины'.",
                        difficulty = "medium"
                    )

                    RecommendationCardCF(
                        icon = "💡",
                        title = "Совет сообщества",
                        text = "Пользователи, похожие на вас, также изучали дополнительные материалы по формуле полной вероятности.",
                        difficulty = "hard"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Рекомендации от IRT
                    Text(
                        text = "Базовые рекомендации (на основе вашего уровня):",
                        fontSize = 14.sp,
                        fontFamily = FontFamily(Font(R.font.jura_semibold)),
                        color = DarkBlue,
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                    )

                    RecommendationCardCF(
                        icon = "📘",
                        title = "IRT рекомендация",
                        text = "Ваш уровень знаний определён как 'Начальный'. Рекомендуем повторить базовые концепции теории вероятностей.",
                        difficulty = "easy",
                        isIRT = true
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Кнопки действий
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = { /* Перезапуск теста */ },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Blue)
                        ) {
                            Text("Пройти еще раз", fontSize = 14.sp)
                        }

                        Button(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkBlue)
                        ) {
                            Text("Завершить", fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecommendationCardCF(
    icon: String,
    title: String,
    text: String,
    difficulty: String,
    isIRT: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .shadow(2.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isIRT) Color(0xFFE3F2FD) else Color(0xFFE8F5E9)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(text = icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isIRT) Blue else Color(0xFF2E7D32),
                    fontFamily = FontFamily(Font(R.font.jura_semibold))
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = text,
                    fontSize = 13.sp,
                    fontFamily = FontFamily(Font(R.font.jura)),
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Источник: ${if (isIRT) "IRT-модель" else "Коллаборативная фильтрация"}",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "Сложность: $difficulty",
                        fontSize = 10.sp,
                        color = when (difficulty) {
                            "easy" -> Color.Green
                            "medium" -> Color(0xFFFFA500)
                            else -> Color.Red
                        }
                    )
                }
            }
        }
    }
}