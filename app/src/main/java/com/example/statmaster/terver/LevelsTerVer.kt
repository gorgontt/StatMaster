package com.example.statmaster.terver

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
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
import com.example.statmaster.ui.theme.DarkBlue
import com.example.statmaster.ui.theme.DarkBlue2
import com.example.statmaster.ui.theme.DarkGreen
import com.example.statmaster.ui.theme.Green
import com.example.statmaster.ui.theme.Pink
import com.example.statmaster.ui.theme.White
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

    LaunchedEffect(Unit) {
        try {
            // Запускаем анимацию загрузки
            launch {
                while (shouldAnimate) {
                    progress.animateTo(0.8f, animationSpec = tween(800))
                    progress.animateTo(0.2f, animationSpec = tween(800))
                }
            }

            // Проверяем соединение
            val isConnected = authManager.testConnection()
            connectionError = !isConnected

            if (isConnected) {
                // Загружаем данные только один раз
                val loadedLevels = levelRepository.getAllLevels()
                levels.clear()
                levels.addAll(loadedLevels)
            }

        } finally {
            // Останавливаем анимацию
            shouldAnimate = false
            progress.animateTo(1f, animationSpec = tween(300))
            isLoading = false
        }
    }

    // Прокрутка к нужной главе
    LaunchedEffect(scrollToChapter, levels.isNotEmpty()) {
        if (scrollToChapter == "chapter2" && levels.isNotEmpty()) {
            val chapter2Index = levels.indexOfFirst { it.id == 13 || it.title == "Итоговый тест 1" }
            if (chapter2Index >= 0) {
                delay(300) // Увеличили задержку для гарантии рендеринга
                coroutineScope.launch {
                    lazyListState.animateScrollToItem(chapter2Index)
                }
            }
        }

        if (scrollToChapter == "chapter3" && levels.isNotEmpty()) {
            val chapter3Index = levels.indexOfFirst { it.id == 22 || it.title == "Итоговый тест 2" }
            if (chapter3Index >= 0) {
                delay(300)
                coroutineScope.launch {
                    lazyListState.animateScrollToItem(chapter3Index)
                }
            }
        }
    }


    LaunchedEffect(Unit) {
        navController.currentBackStackEntry?.savedStateHandle?.getStateFlow<Boolean>(
            "shouldRefresh", false
        )?.collect { shouldRefresh ->
            if (shouldRefresh) {
                levels.clear()
                levels.addAll(levelRepository.getAllLevels())
                navController.currentBackStackEntry?.savedStateHandle?.set(
                    "shouldRefresh", false
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


    Scaffold(
        modifier = Modifier.background(BackgroundColor),
        topBar = {
            TopAppBar(
                title = { Text("Теория вероятностей") },
                colors = TopAppBarDefaults.topAppBarColors(BackgroundColor),
                navigationIcon = {
                    IconButton({
                        navController.currentBackStackEntry?.savedStateHandle?.set("shouldRefresh", true)
                        navController.popBackStack()
                    }) {
                        Image(
                            painter = painterResource(id = R.drawable.arrow_icon_back),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
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
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize()
                            .background(BackgroundColor)
                    ) {
                        items(levels) { level ->
                            LevelCard(level, navController)

                            if (level.id == 13 || level.title == "Итоговый тест 1") {
                                ChapterDivider("Глава 2")
                            }

                            if (level.id == 22 || level.title == "Итоговый тест 2") {
                                ChapterDivider("Глава 3")
                            }

                            if (level.id == 35 || level.title == "Итоговый тест 3") {
                                ChapterDivider("Глава 4")
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
            startAngle = -90f,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

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
    var isTestCompleted by remember { mutableStateOf(false) }

    // Загружаем данные теста, если это тест
    LaunchedEffect(level.id) {
        if (level.title.startsWith("Тест") || level.title.startsWith("Итоговый тест")) {
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
//                        val (correct, total) = calculateScore(questions, userAnswers)
//                        correctAnswers = correct
//                        isTestCompleted = correct > 0
                    }
                }
            }
        }
    }

    // Определяем цвет карточки
    val cardColor = when {
        (level.title.startsWith("Тест") && (level.isCompleted || isCompletedLocally)) -> Green
        (level.title.startsWith("Итоговый") && (level.isCompleted || isCompletedLocally)) -> DarkBlue

        (level.isCompleted || isCompletedLocally) -> Green

        (level.title.startsWith("Тест") || level.title.startsWith("Итоговый")) -> White

        else -> White
    }

    val textColor = when {
        (level.title.startsWith("Тест") && (level.isCompleted || isCompletedLocally)) -> Black
        (level.title.startsWith("Итоговый") && (level.isCompleted || isCompletedLocally)) -> Color.Yellow

        else -> Black
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
                shape = RoundedCornerShape(60.dp)
            )
            .clickable {
                navController.navigate("documentation_level/${level.id}")
            },
        shape = RoundedCornerShape(60.dp),
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
                    color = textColor,
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
                        color = textColor,
                        fontSize = 14.sp,
                        fontFamily = FontFamily(Font(R.font.jura)))
                )

                if (level.title.startsWith("Тест") || level.title.startsWith("Итоговый")) {
                    val starIcon = if (level.isCompleted || isCompletedLocally || isTestCompleted) {
                        R.drawable.star_icon_yellow // Тест выполнен
                    } else {
                        R.drawable.star_icon_gray // Тест не выполнен
                    }

                    Image(
                        modifier = Modifier.padding(start = 5.dp, end = 10.dp),
                        painter = painterResource(id = starIcon),
                        contentDescription = if (starIcon == R.drawable.star_icon_yellow) "Completed" else "NotCompleted"
                    )
                }
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

            if (level.title.startsWith("Итоговый тест") && correctAnswers > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row (modifier = Modifier.fillMaxWidth()
                    .padding(start = 10.dp, end = 10.dp)){
                    Text(
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        text = "Правильных ответов: $correctAnswers/$totalQuestions",
                        style = TextStyle(
                            color = White,
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
    Card (
        modifier = modifier
            .padding(vertical = 20.dp),
        shape = RoundedCornerShape(0.dp, 30.dp, 30.dp, 0.dp),
        colors = CardDefaults.cardColors(containerColor = DarkBlue2)
       // contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = title,
            style = TextStyle(
                color = White,
                fontSize = 24.sp,
                fontFamily = FontFamily(Font(R.font.jura_semibold))
            ),
            modifier = Modifier
                .padding(top = 10.dp, bottom = 10.dp, start = 20.dp, end = 20.dp)
        )
    }
}
