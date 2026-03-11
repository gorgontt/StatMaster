package com.example.statmaster.adaptive

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import kotlinx.coroutines.launch
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import com.example.statmaster.Test
import com.example.statmaster.ui.theme.LightBlue
import com.example.statmaster.ui.theme.RedColor
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.rpc

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptiveTopicSelectionScreen(navController: NavController) {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val repository = remember { AdaptiveTestingRepository(authManager, context) }

    var topics by remember { mutableStateOf<List<Topic>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTopicId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        // Загружаем доступные темы (главы)
        topics = getAvailableTopics(authManager)
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Выберите тему", style = TextStyle(
                    color = DarkBlue,
                    fontSize = 30.sp,
                    fontFamily = FontFamily(Font(R.font.jura_semibold))
                )) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Image(
                            painter = painterResource(id = R.drawable.arrow_icon_back),
                            contentDescription = "Back"
                        )
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
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 50.dp)
                ) {
                    Text(
                        text = "Выберите тему для адаптивного тестирования:",
                        fontSize = 18.sp,
                        fontFamily = FontFamily(Font(R.font.jura)),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    LazyColumn(
                        //verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(topics) { topic ->
                            TopicCard(
                                topic = topic,
                                isSelected = selectedTopicId == topic.id,
                                onSelect = { selectedTopicId = topic.id }
                            )
                        }
                    }

                   //Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = {
                            if (selectedTopicId != null) {
                                navController.navigate("adaptive_test/${selectedTopicId}")
                            }
                        },
                        modifier = Modifier
                            .background(DarkBlue),
                        enabled = selectedTopicId != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTopicId != null) White else DarkBlue
                        ),
                        shape = RoundedCornerShape(60.dp)
                    ) {
                        Text(
                            text = "Начать тест",
                            fontSize = 18.sp,
                            color = White,
                            fontFamily = FontFamily(Font(R.font.jura))
                        )
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
            .background(BackgroundColor)
            .padding(top = 10.dp, bottom = 5.dp)
            .shadow(
                elevation = 4.dp,
                ambientColor = Color.Black,
                spotColor = Color.Black,
                shape = RoundedCornerShape(60.dp)
            )
            .clickable { onSelect() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) LightBlue else White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
            .padding(start = 10.dp, end = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .padding(top = 15.dp, bottom = 15.dp, start = 10.dp, end = 10.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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


        }
    }
}

data class Topic(
    val id: Int,
    val title: String,
    val questionCount: Int
)

suspend fun getAvailableTopics(authManager: AuthManager): List<Topic> {
    return try {
        // Получаем все тесты с количеством вопросов
        val result = authManager.supabase.postgrest
            .rpc("get_topics_with_question_count")
            .decodeList<Topic>()

        if (result.isNotEmpty()) {
            result
        } else {
            // Если RPC не работает, получаем тесты и считаем вопросы отдельно
            val tests = authManager.supabase.postgrest
                .from("test")
                .select(Columns.raw("id, title"))
                .decodeList<Test>()

            tests.map { test ->
                val count = authManager.supabase.postgrest
                    .from("question")
                    .select(Columns.raw("count")) {
                        filter {
                            eq("test_id", test.id)
                        }
                    }
                    .decodeSingle<Int>()

                Topic(test.id, test.title, count)
            }
        }
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

// Добавьте RPC функцию в Supabase
// CREATE OR REPLACE FUNCTION get_topics_with_question_count()
// RETURNS TABLE(id INT, title TEXT, questionCount BIGINT) AS $$
// BEGIN
//     RETURN QUERY
//     SELECT t.id, t.title, COUNT(q.id)::BIGINT as questionCount
//     FROM test t
//     LEFT JOIN question q ON q.test_id = t.id
//     GROUP BY t.id, t.title;
// END;
// $$ LANGUAGE plpgsql;