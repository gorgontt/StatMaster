package com.example.statmaster.terver

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.statmaster.AuthManager
import com.example.statmaster.Level
import com.example.statmaster.R
import com.example.statmaster.ui.theme.BackgroundColor
import com.example.statmaster.ui.theme.Black
import com.example.statmaster.ui.theme.Blue
import com.example.statmaster.ui.theme.DarkGreen
import com.example.statmaster.ui.theme.Green
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LevelsTerVer(navController: NavController, scrollToChapter: String? = null) {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val levelRepository = remember { LevelRepository(authManager, context) }
    var levels = remember { mutableStateListOf<Level>() }
    var isLoading by remember { mutableStateOf(true) }
    var connectionError by remember { mutableStateOf(false) }

    // Состояние для анимации прогресса (0..1)
    val progress = remember { Animatable(0f) }

    // Флаг для управления бесконечной анимацией
    var shouldAnimate by remember { mutableStateOf(true) }

    //состояние для принудительного обновления
    var refreshTrigger by remember { mutableStateOf(0) }

    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    //var levels by remember { mutableStateOf(emptyList<Level>()) }

    LaunchedEffect(Unit) {
        // Исправление: используем addAll вместо присваивания
        levels.clear()
        levels.addAll(levelRepository.getAllLevels())
        isLoading = false
    }

    // Эффект для прокрутки при изменении параметра или загрузке данных
    LaunchedEffect(scrollToChapter, levels.isNotEmpty()) {
        if (levels.isEmpty() || scrollToChapter != "chapter2") return@LaunchedEffect

        val chapter2Index = levels.indexOfFirst { it.id == 13 || it.title == "Тест 5" }
        if (chapter2Index != -1) {
            delay(100) // Небольшая задержка для инициализации
            coroutineScope.launch {
                lazyListState.animateScrollToItem(chapter2Index)
            }
        }
    }


    LaunchedEffect(Unit) {
        navController.currentBackStackEntry?.savedStateHandle?.getStateFlow<Boolean>(
            "shouldRefresh", false
        )?.collect { shouldRefresh ->
            if (shouldRefresh) {
                // Перезагружаем уровни
                levels.clear()
                levels.addAll(levelRepository.getAllLevels())
                // Сбрасываем флаг
                navController.currentBackStackEntry?.savedStateHandle?.set(
                    "shouldRefresh",
                    false
                )
            }
        }
    }


    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger > 0) {
            levels.clear()
            val loadedLevels = levelRepository.getAllLevels()
            levels.addAll(loadedLevels)
        }
    }

    LaunchedEffect(Unit) {
        // Запускаем бесконечную анимацию в фоне
        launch {
            while (shouldAnimate) {
                progress.animateTo(0.8f, animationSpec = tween(800))
                progress.animateTo(0.2f, animationSpec = tween(800))
            }
        }

        // Параллельно загружаем данные
        launch {
            val isConnected = authManager.testConnection()
            connectionError = !isConnected

            if (isConnected) {
                levels.addAll(levelRepository.getAllLevels())
            }

            // Завершаем анимацию
            shouldAnimate = false
            progress.animateTo(1f, animationSpec = tween(300))
            isLoading = false
        }
    }

    Scaffold(
        modifier = Modifier.background(BackgroundColor),
        topBar = {
            TopAppBar(
                title = { Text("Теория вероятностей") },
                colors = TopAppBarDefaults.topAppBarColors(BackgroundColor),
                navigationIcon = { /* ... */ }
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    //.padding(padding)
                    .fillMaxSize()
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingIndicator(progress.value)
                    }
                } else if (connectionError) {
                    Text("Ошибка подключения")
                } else if (levels.isEmpty()) {
                    Text("Нет данных")
                } else {
                    // Используем LazyColumn вместо Column с вертикальным скроллом
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize()
                            .background(BackgroundColor)
                    ) {
                        items(levels) { level ->
                            LevelCard(level, navController)

                            if (level.id == 13 || level.title == "Тест 5") {
                                ChapterDivider("Глава 2")
                            }
                        }
                    }

                }
            }
        })


}

@Composable
fun LoadingIndicator(progress: Float) {
    val sweepAngle = progress * 360f

    Canvas(modifier = Modifier.size(70.dp)) {
        // Фоновый круг
        drawCircle(
            color = BackgroundColor,
            radius = size.minDimension / 2 - 4.dp.toPx()
        )

        // Прогресс
        drawArc(
            color = Blue,
            startAngle = -90f, // Начинаем сверху
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

private const val DURATION = 1000



@Composable
fun LevelCard(level: Level, navController: NavController) {
    val context = LocalContext.current
    val levelRepository = remember { LevelRepository(AuthManager(context), context) }
    val coroutineScope = rememberCoroutineScope()

    val isCompletedLocally = remember(level.id) {
        context.getSharedPreferences("LevelProgress", Context.MODE_PRIVATE)
            .getBoolean("level_${level.id}", false)
    }

    // состояние для хранения количества правильных ответов
    var correctAnswers by remember { mutableStateOf(0) }
    var totalQuestions by remember { mutableStateOf(0) }

    // Загружаем данные теста, если это тест
    LaunchedEffect(level.id) {
        if (level.title.startsWith("Тест")) {
            coroutineScope.launch {
                val test = levelRepository.getTestByLevelId(level.id)
                test?.let {
                    val questions = levelRepository.getQuestionsWithAnswers(it.id)
                    totalQuestions = questions.size

                    // Проверяем сохраненные ответы
                    val sharedPref = context.getSharedPreferences("TestAnswers", Context.MODE_PRIVATE)
                    val userAnswers = questions.associate { q ->
                        q.id to sharedPref.getInt("answer_${level.id}_${q.id}", -1)
                    }

                    if (userAnswers.values.all { it != -1 }) {
                        correctAnswers = calculateScore(questions, userAnswers).first
                    }
                }
            }
        }
    }

    // Определяем цвет карточки
    val cardColor = when {
        // Если это тест и он пройден - красный
        (level.title.startsWith("Тест") && (level.isCompleted || isCompletedLocally)) -> DarkGreen
        // Если это уроыень и пройден - зеленый
        (level.isCompleted || isCompletedLocally) -> Green
        // Если это тест (но не пройден) - синий
        level.title.startsWith("Тест") -> Blue
        // Во всех остальных случаях - цвет фона
        else -> BackgroundColor
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundColor)
            .padding(top = 10.dp, bottom = 5.dp, start = 30.dp, end = 30.dp)
            .shadow(
                elevation = 4.dp,
                ambientColor = Color.Black,
                spotColor = Color.Black,
                shape = RoundedCornerShape(40.dp)
            )
            .clickable {
                navController.navigate("documentation_level/${level.id}")
            },
        shape = RoundedCornerShape(40.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(cardColor)
                .padding(top = 15.dp, bottom = 15.dp, start = 10.dp, end = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = level.title,
                style = TextStyle(
                    color = Black,
                    fontSize = 20.sp,
                    fontFamily = FontFamily(Font(R.font.jura_semibold)))
            )

            Row(
                modifier = Modifier
                    .background(cardColor)
                    .fillMaxSize()
                    .padding(top = 5.dp, start = 10.dp, end = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    text = level.description,
                    style = TextStyle(
                        color = Black,
                        fontSize = 14.sp,
                        fontFamily = FontFamily(Font(R.font.jura)))
                )

//                if (level.isCompleted || isCompletedLocally) {
//                    Image(
//                        modifier = Modifier.padding(start = 5.dp),
//                        painter = painterResource(id = R.drawable.tick_icon),
//                        contentDescription = "Completed"
//                    )
//                }
            }

            if (level.title.startsWith("Тест") && correctAnswers > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row (modifier = Modifier.fillMaxWidth()
                    .padding(start = 10.dp, end = 10.dp)){
                    Text(
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        text = "Правильных ответов: $correctAnswers/$totalQuestions",
                        style = TextStyle(
                            color = Black,
                            fontSize = 14.sp,
                            fontFamily = FontFamily(Font(R.font.jura_semibold)))
                    )
                }
            }
        }
    }
}

@Composable
fun ChapterDivider(
    title: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(vertical = 20.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = title,
            style = TextStyle(
                color = Color.Black,
                fontSize = 24.sp,
                fontFamily = FontFamily(Font(R.font.jura_semibold))
            ),
            modifier = Modifier
                .background(BackgroundColor)
                .padding(horizontal = 16.dp)
        )
    }
}
