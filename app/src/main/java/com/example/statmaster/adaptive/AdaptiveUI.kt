package com.example.statmaster.adaptive

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.statmaster.AuthManager
import com.example.statmaster.R
import com.example.statmaster.collabfiltering.HybridRecommendationRepository
import com.example.statmaster.terver.QuestionCard
import com.example.statmaster.ui.theme.BackgroundColor
import com.example.statmaster.ui.theme.Blue
import com.example.statmaster.ui.theme.DarkBlue
import com.example.statmaster.ui.theme.Green
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptiveTestScreen(
    navController: NavController,
    topicId: Int? = null,
    topicTitle: String = "Адаптивный тест"
) {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
//    val repository = remember { AdaptiveTestingRepository(authManager, context) }
    val repository = remember { AdaptiveTestingRepository(authManager, context) }
    val hybridRepository = remember { HybridRecommendationRepository(authManager, context) }


    val coroutineScope = rememberCoroutineScope()

    var currentQuestion by remember { mutableStateOf<AdaptiveQuestion?>(null) }
    var selectedAnswerId by remember { mutableStateOf<Int?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var testCompleted by remember { mutableStateOf(false) }
    var recommendations by remember { mutableStateOf<List<Recommendation>>(emptyList()) }
    var sessionStats by remember { mutableStateOf<SessionStats?>(null) }

    val userAbility by repository.userAbility.collectAsState()
    var startTime by remember { mutableStateOf(System.currentTimeMillis()) }

    // Загрузка первого вопроса с использованием гибридной рекомендации
    LaunchedEffect(Unit) {
        val userId = authManager.supabase.auth.currentUserOrNull()?.id
        Log.d("AdaptiveTest", "=== НАЧАЛО ЗАГРУЗКИ ТЕСТА ===")
        Log.d("AdaptiveTest", "User ID: $userId")
        Log.d("AdaptiveTest", "Topic ID: $topicId")

        if (userId != null) {
            // Инициализируем IRT-репозиторий
            repository.initializeUserAbility(userId)
            Log.d("AdaptiveTest", "IRT репозиторий инициализирован")

            // Обновляем вектор пользователя
            hybridRepository.updateUserVector(userId)
            Log.d("AdaptiveTest", "Вектор пользователя обновлён")

            // Получаем гибридную рекомендацию
//            currentQuestion = hybridRepository.getHybridRecommendation(
//                userId = userId,
//                topicId = topicId,
//                excludeQuestionIds = emptySet()
//            )

            currentQuestion = repository.getNextQuestion(topicId)

            Log.d("AdaptiveTest", "Получен вопрос: ${currentQuestion?.question?.questionText ?: "null"}")
            Log.d("AdaptiveTest", "Сложность вопроса: ${currentQuestion?.difficulty}")

            isLoading = false
            startTime = System.currentTimeMillis()

            if (currentQuestion == null) {
                Log.e("AdaptiveTest", "ВОПРОС НЕ ПОЛУЧЕН! Проверьте:")
                Log.e("AdaptiveTest", "1. Есть ли вопросы в БД для test_id = $topicId")
                Log.e("AdaptiveTest", "2. Есть ли у вопросов поле difficulty (easy/medium/hard)")
                Log.e("AdaptiveTest", "3. Работает ли getNextQuestion()")
            }
        } else {
            Log.e("AdaptiveTest", "Пользователь не авторизован")
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(topicTitle)
                        if (userAbility != null) {
                            Text(
                                text = "Уровень: ${formatAbilityLevel(userAbility!!.abilityLevel)}",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                },
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
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                testCompleted -> {
                    TestCompleteScreen(
                        userAbility = userAbility,
                        recommendations = recommendations,
                        sessionStats = sessionStats,
                        onRestart = {
                            coroutineScope.launch {
                                repository.resetSession()
                                testCompleted = false
                                isLoading = true

                                val userId = authManager.supabase.auth.currentUserOrNull()?.id
                                if (userId != null) {
                                    currentQuestion = hybridRepository.getHybridRecommendation(
                                        userId = userId,
                                        topicId = topicId,
                                        excludeQuestionIds = emptySet()
                                    )
                                }

                                isLoading = false
                                startTime = System.currentTimeMillis()
                                selectedAnswerId = null
                            }
                        },
                        onFinish = { navController.popBackStack() }
                    )
                }
                currentQuestion != null -> {
                    QuestionScreen(
                        question = currentQuestion!!,
                        selectedAnswerId = selectedAnswerId,
                        onAnswerSelected = { answerId ->
                            selectedAnswerId = answerId
                        },
                        onAnswerSubmit = {
                            val responseTime =
                                ((System.currentTimeMillis() - startTime) / 1000).toInt()

                            coroutineScope.launch {
                                val isCorrect = currentQuestion!!.question.answers
                                    .firstOrNull { it.id == selectedAnswerId }?.isCorrect == true

                                // Обрабатываем ответ через IRT-репозиторий
                                repository.processAnswer(
                                    questionId = currentQuestion!!.question.id,
                                    difficulty = currentQuestion!!.difficulty,
                                    isCorrect = isCorrect,
                                    responseTime = responseTime
                                )

                                // Получаем следующий вопрос через гибридную рекомендацию
                                val userId = authManager.supabase.auth.currentUserOrNull()?.id
                                val askedQuestionIds = repository.sessionResponses.map { it.questionId }.toSet()

                                val nextQuestion = if (userId != null) {
                                    hybridRepository.getHybridRecommendation(
                                        userId = userId,
                                        topicId = topicId,
                                        excludeQuestionIds = askedQuestionIds
                                    )
                                } else {
                                    repository.getNextQuestion(topicId)
                                }

                                if (nextQuestion != null && repository.getSessionStats().totalQuestions < 10) {
                                    currentQuestion = nextQuestion
                                    selectedAnswerId = null
                                    startTime = System.currentTimeMillis()
                                } else {
                                    recommendations = repository.getRecommendations()
                                    sessionStats = repository.getSessionStats()
                                    testCompleted = true
                                    currentQuestion = null

                                    // Обновляем вектор пользователя после завершения теста
                                    if (userId != null) {
                                        hybridRepository.updateUserVector(userId)
                                    }
                                }
                            }
                        },
                        questionNumber = repository.getSessionStats().totalQuestions + 1,
                        totalQuestions = 10
                    )
                }
            }
        }
    }
}

@Composable
fun QuestionScreen(
    question: AdaptiveQuestion,
    selectedAnswerId: Int?,
    onAnswerSelected: (Int) -> Unit,
    onAnswerSubmit: () -> Unit,
    questionNumber: Int,
    totalQuestions: Int
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Индикатор прогресса
        LinearProgressIndicator(
            progress = questionNumber.toFloat() / totalQuestions.toFloat(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            color = Blue
        )

        Text(
            text = "Вопрос $questionNumber из $totalQuestions",
            modifier = Modifier.padding(bottom = 8.dp),
            color = Color.Gray
        )

        // Индикатор сложности
        DifficultyIndicator(difficulty = question.difficulty)

        Spacer(modifier = Modifier.height(16.dp))

        // Карточка вопроса
        QuestionCard(
            question = question.question,
            selectedAnswerId = selectedAnswerId,
            checked = false,
            testCompleted = false,
            onAnswerSelected = onAnswerSelected
        )

        Spacer(modifier = Modifier.weight(1f))

        // Кнопка отправки ответа
        Button(
            onClick = onAnswerSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = selectedAnswerId != null,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedAnswerId != null) Green else DarkBlue
            ),
            shape = RoundedCornerShape(60.dp)
        ) {
            Text(
                text = "Ответить",
                fontSize = 18.sp,
                fontFamily = FontFamily(Font(R.font.jura))
            )
        }
    }
}

@Composable
fun DifficultyIndicator(difficulty: String) {
    val (color, text) = when (difficulty) {
        "easy" -> Pair(Color.Green, "Легкий")
        "medium" -> Pair(Color(0xFFFFA500), "Средний")
        "hard" -> Pair(Color.Red, "Сложный")
        else -> Pair(Color.Gray, "Неизвестно")
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.2f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Сложность: $text",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                color = color,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun TestCompleteScreen(
    userAbility: UserAbility?,
    recommendations: List<Recommendation>,
    sessionStats: SessionStats?,
    onRestart: () -> Unit,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
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
                Text(
                    text = "Тест завершен!",
                    fontSize = 28.sp,
                    fontFamily = FontFamily(Font(R.font.jura_semibold)),
                    color = DarkBlue
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Информация об уровне пользователя
                if (userAbility != null) {
                    ResultRow(
                        label = "Ваш уровень:",
                        value = formatAbilityLevel(userAbility.abilityLevel)
                    )
                }

                // Статистика сессии
                sessionStats?.let { stats ->
                    ResultRow(
                        label = "Правильных ответов:",
                        value = "${stats.correctAnswers}/${stats.totalQuestions}"
                    )

                    ResultRow(
                        label = "Точность:",
                        value = "${(stats.accuracy * 100).toInt()}%"
                    )

                    ResultRow(
                        label = "Среднее время:",
                        value = "${stats.averageResponseTime} сек"
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Рекомендации
                if (recommendations.isNotEmpty()) {
                    Text(
                        text = "Рекомендации:",
                        fontSize = 20.sp,
                        fontFamily = FontFamily(Font(R.font.jura_semibold)),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    recommendations.forEach { rec ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = when (rec.difficulty) {
                                    "easy" -> Color.Green.copy(alpha = 0.1f)
                                    "medium" -> Color(0xFFFFA500).copy(alpha = 0.1f)
                                    "hard" -> Color.Red.copy(alpha = 0.1f)
                                    else -> Color.Gray.copy(alpha = 0.1f)
                                }
                            )
                        ) {
                            Text(
                                text = "• ${rec.text}",
                                modifier = Modifier.padding(12.dp),
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Кнопки действий
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = onRestart,
                        colors = ButtonDefaults.buttonColors(containerColor = Blue)
                    ) {
                        Text("Пройти еще раз")
                    }

                    Button(
                        onClick = onFinish,
                        colors = ButtonDefaults.buttonColors(containerColor = DarkBlue)
                    ) {
                        Text("Завершить")
                    }
                }
            }
        }
    }
}

@Composable
fun ResultRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 16.sp)
        Text(value, fontSize = 16.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
    }
}

/**
 * Форматирование уровня способностей в понятную строку
 */
private fun formatAbilityLevel(ability: Float): String {
    return when {
        ability < -0.5f -> "Начальный"
        ability < 0.3f -> "Средний"
        ability < 1.0f -> "Продвинутый"
        else -> "Эксперт"
    }
}