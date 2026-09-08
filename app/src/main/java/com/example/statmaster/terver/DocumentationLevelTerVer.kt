package com.example.statmaster.terver

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.example.statmaster.Level
import com.example.statmaster.LevelDocument
import com.example.statmaster.R
import com.example.statmaster.QuestionWithAnswers
import com.example.statmaster.Test
import com.example.statmaster.auth.AuthManager
import com.example.statmaster.ui.theme.BackgroundColor
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun DocumentationLevelTerVer(navController: NavController, levelId: Int?) {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val levelRepository = remember { LevelRepository(authManager, context) }

    var showResetDialog by remember { mutableStateOf(false) }

    var levelDocument by remember { mutableStateOf<LevelDocument?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isTest by remember { mutableStateOf(false) }
    var testData by remember { mutableStateOf<Test?>(null) }
    var questions by remember { mutableStateOf<List<QuestionWithAnswers>>(emptyList()) }
    var userAnswers by remember { mutableStateOf<Map<Int, Int?>>(emptyMap()) }
    var checkedAnswers by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var showAnswerAllQuestionsWarning by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var currentLevel by remember { mutableStateOf<Level?>(null) }
    var answersChecked by remember { mutableStateOf(false) }
    var testCompleted by remember { mutableStateOf(false) }
    var isLevelCompleted by remember { mutableStateOf(false) }
    var totalQuestions by remember { mutableStateOf(0) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(levelId) {
        if (levelId != null) {
            try {
                isLoading = true
                showError = false

                val loadedLevel = levelRepository.getLevelById(levelId)
                currentLevel = loadedLevel

                val sharedPref = context.getSharedPreferences("LevelProgress", Context.MODE_PRIVATE)
                isLevelCompleted = loadedLevel?.isCompleted ?: sharedPref.getBoolean("level_$levelId", false)

                if ((loadedLevel?.title?.startsWith("Тест") == true) || (loadedLevel?.title?.startsWith("Итоговый") == true)) {
                    val test = levelRepository.getTestByLevelId(levelId)
                    test?.let {
                        isTest = true
                        testData = test
                        val loadedQuestions = levelRepository.getQuestionsWithAnswers(test.id)
                        questions = loadedQuestions
                        totalQuestions = loadedQuestions.size

                        val answersPref = context.getSharedPreferences("TestAnswers", Context.MODE_PRIVATE)
                        userAnswers = loadedQuestions.associate { question ->
                            val key = "answer_${levelId}_${question.id}"
                            question.id to if (answersPref.contains(key)) answersPref.getInt(key, -1) else null
                        }

                        if (userAnswers.values.all { it != null }) {
                            checkedAnswers = loadedQuestions.map { it.id }.toSet()
                            answersChecked = true
                            testCompleted = true
                        }
                    }
                } else {
                    levelDocument = levelRepository.getLevelDocument(levelId)
                }
            } catch (e: Exception) {
                Log.e("DocumentationLevel", "Error loading level data", e)
                showError = true
            } finally {
                isLoading = false
            }
        }
    }

    val completeLevel: () -> Unit = {
        coroutineScope.launch {
            if (levelId == null) return@launch
            isLoading = true
            try {
                levelRepository.updateLevelCompletion(levelId, true)
                context.getSharedPreferences("LevelProgress", Context.MODE_PRIVATE)
                    .edit().putBoolean("level_$levelId", true).apply()
                isLevelCompleted = true
                Toast.makeText(context, "Урок успешно завершен", Toast.LENGTH_SHORT).show()
                navController.currentBackStackEntry?.savedStateHandle?.set("shouldRefresh", true)
                navController.popBackStack()
            } catch (e: Exception) {
                showError = true
            } finally {
                isLoading = false
            }
        }
    }

    val completeTest = {
        coroutineScope.launch {
            if (levelId == null) return@launch
            isLoading = true
            try {
                val sharedPref = context.getSharedPreferences("TestAnswers", Context.MODE_PRIVATE)
                with(sharedPref.edit()) {
                    userAnswers.forEach { (questionId, answerId) ->
                        putInt("answer_${levelId}_$questionId", answerId ?: -1)
                    }
                    apply()
                }

                val (correct, total) = calculateScore(questions, userAnswers)
                val resultsPref = context.getSharedPreferences("TestResults", Context.MODE_PRIVATE)
                resultsPref.edit().putInt("correct_$levelId", correct).apply()
                resultsPref.edit().putInt("total_$levelId", total).apply()

                levelRepository.updateLevelCompletion(levelId, true)
                context.getSharedPreferences("LevelProgress", Context.MODE_PRIVATE)
                    .edit().putBoolean("level_$levelId", true).apply()

                isLevelCompleted = true
                testCompleted = true
                answersChecked = true

                Toast.makeText(context, "Тест успешно завершен", Toast.LENGTH_SHORT).show()
                navController.currentBackStackEntry?.savedStateHandle?.set("shouldRefresh", true)
                navController.popBackStack()
            } catch (e: Exception) {
                showError = true
            } finally {
                isLoading = false
            }
        }
    }

    val resetTestData: () -> Unit = {
        coroutineScope.launch {
            userAnswers = emptyMap()
            checkedAnswers = emptySet()
            answersChecked = false
            testCompleted = false
            showAnswerAllQuestionsWarning = false

            if (levelId != null) {
                val answersPref = context.getSharedPreferences("TestAnswers", Context.MODE_PRIVATE)
                questions.forEach { question ->
                    answersPref.edit().remove("answer_${levelId}_${question.id}").apply()
                }
                val resultsPref = context.getSharedPreferences("TestResults", Context.MODE_PRIVATE)
                resultsPref.edit().remove("correct_$levelId").apply()
                resultsPref.edit().remove("total_$levelId").apply()

                val progressPref = context.getSharedPreferences("LevelProgress", Context.MODE_PRIVATE)
                progressPref.edit().putBoolean("level_$levelId", false).apply()
                levelRepository.updateLevelCompletion(levelId, false)
            }
            Toast.makeText(context, "Тест сброшен", Toast.LENGTH_SHORT).show()
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Сбросить тест") },
            text = { Text("Вы уверены, что хотите пройти тест заново?") },
            confirmButton = {
                TextButton(onClick = { showResetDialog = false; resetTestData() }) {
                    Text("Да, сбросить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.background(BackgroundColor),
                colors = TopAppBarDefaults.topAppBarColors(BackgroundColor),
                title = { Text(currentLevel?.title ?: testData?.title ?: "Документация") },
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
        content = {
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        LoadingIndicator()
                    }
                }
                isTest -> {
                    TestScreenContent(
                        test = testData!!,
                        questions = questions,
                        userAnswers = userAnswers,
                        checkedAnswers = checkedAnswers,
                        showWarning = showAnswerAllQuestionsWarning,
                        onAnswerSelected = { questionId, answerId ->
                            userAnswers = userAnswers + (questionId to answerId)
                            showAnswerAllQuestionsWarning = false
                        },
                        context = context,
                        levelId = levelId ?: 0
                    )
                }
                levelDocument != null -> {
                    DocumentContent(levelDocument!!)
                }
                else -> {
                    Text("Контент не найден")
                }
            }
        },
        bottomBar = {
            when {
                isTest -> {
                    TestBottomBar(
                        questions = questions,
                        userAnswers = userAnswers,
                        checkedAnswers = checkedAnswers,
                        answersChecked = answersChecked,
                        testCompleted = testCompleted,
                        totalQuestions = totalQuestions,
                        onCheckAnswers = {
                            if (userAnswers.size == questions.size && userAnswers.values.all { it != null }) {
                                val sharedPref = context.getSharedPreferences("TestAnswers", Context.MODE_PRIVATE)
                                with(sharedPref.edit()) {
                                    userAnswers.forEach { (questionId, answerId) ->
                                        putInt("answer_${levelId}_$questionId", answerId ?: -1)
                                    }
                                    apply()
                                }
                                checkedAnswers = questions.map { it.id }.toSet()
                                answersChecked = true
                                showAnswerAllQuestionsWarning = false
                                val (correct, total) = calculateScore(questions, userAnswers)
                                Toast.makeText(context, "Правильных: $correct из $total", Toast.LENGTH_SHORT).show()
                            } else {
                                showAnswerAllQuestionsWarning = true
                                Toast.makeText(context, "Ответьте на все вопросы", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onCompleteTest = { completeTest() },
                        onResetTest = { showResetDialog = true }
                    )
                }
                levelDocument != null -> {
                    LessonBottomBar(
                        isLevelCompleted = isLevelCompleted,
                        isLoading = isLoading,
                        onCompleteLevel = completeLevel,
                        showError = showError
                    )
                }
            }
        }
    )
}