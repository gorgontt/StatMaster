package com.example.statmaster.adaptive

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.statmaster.AuthManager
import com.example.statmaster.R
import com.example.statmaster.ui.theme.BackgroundColor
import com.example.statmaster.ui.theme.Blue
import com.example.statmaster.ui.theme.DarkBlue
import com.example.statmaster.ui.theme.White
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

// ============================================================
// DATA КЛАССЫ
// ============================================================

@Serializable
data class Topic(
    val id: Int,
    val title: String,
    val questionCount: Int
)

@Serializable
data class TestSimple(
    val id: Int,
    val title: String
)

// ============================================================
// ЭКРАН ВЫБОРА ТЕМЫ
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptiveTopicSelectionScreen(navController: NavController) {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }

    var topics by remember { mutableStateOf<List<Topic>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTopicId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        topics = getAvailableTopics(authManager)
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Выберите тему") },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundColor)
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                topics.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Нет доступных тем",
                            fontSize = 18.sp,
                            fontFamily = FontFamily(Font(R.font.jura))
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { navController.popBackStack() },
                            colors = ButtonDefaults.buttonColors(containerColor = Blue)
                        ) {
                            Text("Назад")
                        }
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Выберите тему для адаптивного тестирования:",
                            fontSize = 18.sp,
                            fontFamily = FontFamily(Font(R.font.jura)),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(topics) { topic ->
                                TopicCard(
                                    topic = topic,
                                    isSelected = selectedTopicId == topic.id,
                                    onSelect = { selectedTopicId = topic.id }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = {
                                if (selectedTopicId != null) {
                                    navController.navigate("adaptive_test/${selectedTopicId}")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = selectedTopicId != null,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedTopicId != null) Blue else DarkBlue
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Начать тест",
                                fontSize = 18.sp,
                                fontFamily = FontFamily(Font(R.font.jura))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TopicCard(
    topic: Topic,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Blue.copy(alpha = 0.2f) else White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = topic.title,
                    fontSize = 18.sp,
                    fontFamily = FontFamily(Font(R.font.jura_semibold))
                )
                Text(
                    text = "${topic.questionCount} вопросов",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            if (isSelected) {
                Text(
                    text = "✓",
                    fontSize = 24.sp,
                    color = Blue
                )
            }
        }
    }
}

// ============================================================
// ФУНКЦИЯ ПОЛУЧЕНИЯ ТЕМ
// ============================================================

suspend fun getAvailableTopics(authManager: AuthManager): List<Topic> {
    return try {
        // Получаем все тесты
        val tests = authManager.supabase.postgrest
            .from("test")
            .select(Columns.raw("id, title"))
            .decodeList<TestSimple>()

        // Для каждого теста считаем количество вопросов
        val topics = mutableListOf<Topic>()
        for (test in tests) {
            val count = authManager.supabase.postgrest
                .from("question")
                .select(Columns.raw("id")) {
                    filter { eq("test_id", test.id) }
                }
                .decodeList<Map<String, Int>>()
                .size

            topics.add(Topic(test.id, test.title, count))
        }

        topics
    } catch (e: Exception) {
        Log.e("AdaptiveTesting", "Error loading topics", e)

        // Возвращаем тестовые данные для разработки
        listOf(
            Topic(1, "Случайные события", 10),
            Topic(2, "Теоремы вероятностей", 8),
            Topic(3, "Одномерные случайные величины", 12),
            Topic(4, "Многомерные случайные величины", 8),
            Topic(5, "Закон больших чисел", 6),
            Topic(6, "Математическая статистика", 10)
        )
    }
}