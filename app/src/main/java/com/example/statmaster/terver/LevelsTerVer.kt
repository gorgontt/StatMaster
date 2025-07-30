package com.example.statmaster.terver

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.statmaster.AuthManager
import com.example.statmaster.Level
import com.example.statmaster.R
import com.example.statmaster.ui.theme.BackgroundColor
import com.example.statmaster.ui.theme.Black
import com.example.statmaster.ui.theme.Blue
import com.example.statmaster.ui.theme.Green
import com.example.statmaster.ui.theme.White
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LevelsTerVer(navController: NavController) {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val levelRepository = remember { LevelRepository(authManager) }
    var levels = remember { mutableStateListOf<Level>() }
    var isLoading by remember { mutableStateOf(true) }
    var connectionError by remember { mutableStateOf(false) }

    // Состояние для анимации прогресса (0..1)
    val progress = remember { Animatable(0f) }

    // Флаг для управления бесконечной анимацией
    var shouldAnimate by remember { mutableStateOf(true) }

    //состояние для принудительного обновления
    var refreshTrigger by remember { mutableStateOf(0) }


    var levelsState by remember { mutableStateOf(emptyList<Level>()) }

    val coroutineScope = rememberCoroutineScope()

//    LaunchedEffect(navController) {
//        snapshotFlow { navController.currentBackStackEntry }.collect {
//            if (it?.destination?.route == "LevelsTerVer") {
//                // Перезагружаем уровни при возврате
//                levels.clear()
//                levels.addAll(levelRepository.getAllLevels())
//            }
//        }
//    }

    LaunchedEffect(Unit) {
        // Первоначальная загрузка
        levels = withContext(Dispatchers.IO) {
            levelRepository.getAllLevels().toMutableStateList()
        }

        // Подписка на обновления
        navController.currentBackStackEntry?.savedStateHandle?.getStateFlow<Int>(
            "updated_level",
            -1
        )?.collect { updatedId ->
            if (updatedId > 0) {
                Log.d("LevelsTerVer", "Received update for level $updatedId")
                val updatedLevels = withContext(Dispatchers.IO) {
                    levelRepository.getAllLevels()
                }
                levels.clear()
                levels.addAll(updatedLevels)
                navController.currentBackStackEntry?.savedStateHandle?.remove<Int>("updated_level")
            }
        }
    }

//    LaunchedEffect(navController) {
//        navController.currentBackStackEntry?.savedStateHandle?.getStateFlow<Boolean>("shouldRefresh", false)
//            ?.collect { shouldRefresh ->
//                if (shouldRefresh) {
//                    refreshTrigger++
//                    navController.currentBackStackEntry?.savedStateHandle?.remove<Boolean>("shouldRefresh")
//                }
//            }
//    }

    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger > 0) {
            levels.clear()
            val loadedLevels = levelRepository.getAllLevels()
            levels.addAll(loadedLevels)
        }
    }

//    LaunchedEffect(Unit) {
//        navController.currentBackStackEntry?.savedStateHandle?.getStateFlow<Boolean>("shouldRefresh", false)
//            ?.collect { shouldRefresh ->
//                if (shouldRefresh) {
//                    levels.clear()
//                    levels.addAll(levelRepository.getAllLevels())
//                    navController.currentBackStackEntry?.savedStateHandle?.remove<Boolean>("shouldRefresh")
//                }
//            }
//    }

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
                    .padding(padding)
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
                    ContentTerVerLevels(levels, navController)
                }
            }
        },

                bottomBar = {
                    BottomAppBar(
                        containerColor = BackgroundColor,
                        modifier = Modifier.height(130.dp)
                    ) {

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val testLevels = levelRepository.getAllLevels()
                                    Log.d(
                                        "LevelCheck",
                                        "Current levels: ${testLevels.joinToString { it.id.toString() + "=" + it.isCompleted }}"
                                    )
                                }
                            },
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text("Проверить статусы уровней")
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
fun ContentTerVerLevels(levels: List<Level>, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .background(BackgroundColor)
            .verticalScroll(rememberScrollState())
            .padding()
    ) {
        levels.forEach { level ->
            LevelCard(level, navController)

        }
    }
}

@Composable
fun LevelCard(level: Level, navController: NavController) {

    val cardColor = when {
        level.isCompleted -> Green.copy(alpha = 0.3f)
        level.title.startsWith("Тест") -> Blue
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
        Row(
            modifier = Modifier
                .background(cardColor)
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier
                    .padding(top = 15.dp, bottom = 15.dp, start = 10.dp, end = 10.dp)
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = level.title,
                    style = TextStyle(
                        color = Black,
                        fontSize = 20.sp,
                        fontFamily = FontFamily(Font(R.font.jura_semibold))
                    ))
                            Text(
                            modifier = Modifier.padding(top = 5.dp),
                    text = level.description,
                    style = TextStyle(
                        color = Black,
                        fontSize = 14.sp,
                        fontFamily = FontFamily(Font(R.font.jura))
                    ))
            }
            if (level.isCompleted) {
                Image(
                    modifier = Modifier.padding(end = 20.dp),
                    painter = painterResource(id = R.drawable.tick_icon),
                    contentDescription = "Completed"
                )
            }
        }
    }
}