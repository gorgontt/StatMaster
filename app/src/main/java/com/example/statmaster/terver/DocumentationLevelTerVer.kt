package com.example.statmaster.terver

import android.annotation.SuppressLint
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.statmaster.AuthManager
import com.example.statmaster.ContentBlock
import com.example.statmaster.LevelDocument
import com.example.statmaster.ParsedDocument
import com.example.statmaster.QuestionWithAnswers
import com.example.statmaster.R
import com.example.statmaster.Test
import com.example.statmaster.ui.theme.BackgroundColor
import com.example.statmaster.ui.theme.Black
import com.example.statmaster.ui.theme.Blue
import com.example.statmaster.ui.theme.Green


@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun DocumentationLevelTerVer(navController: NavController, levelId: Int?) {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val levelRepository = remember { LevelRepository(authManager) }

    var levelDocument by remember { mutableStateOf<LevelDocument?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isTest by remember { mutableStateOf(false) }
    var testData by remember { mutableStateOf<Test?>(null) }
    var questions by remember { mutableStateOf<List<QuestionWithAnswers>>(emptyList()) }
    var userAnswers by remember { mutableStateOf<Map<Int, Int?>>(emptyMap()) }
    var checkedAnswers by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var showAnswerAllQuestionsWarning by remember { mutableStateOf(false) }

    // Проверяем, ответил ли пользователь на все вопросы
    val allQuestionsAnswered = remember(userAnswers, questions) {
        userAnswers.size == questions.size && userAnswers.values.all { it != null }
    }

    // Функция для показа Toast
    val showToast = remember {
        { message: String ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(levelId) {
        if (levelId != null) {
            val test = levelRepository.getTestByLevelId(levelId)
            if (test != null) {
                isTest = true
                testData = test
                questions = levelRepository.getQuestionsWithAnswers(test.id)
                userAnswers = questions.associate { it.id to null }
            } else {
                levelDocument = levelRepository.getLevelDocument(levelId)
            }
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.background(BackgroundColor),
                title = { Text("Документация уровня") },
                navigationIcon = {
                    IconButton({ navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.back_icon),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        content = {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (isTest) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    if (showAnswerAllQuestionsWarning) {
                        Text(
                            text = "Ответьте на все вопросы",
                            color = Color.Red,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                    }

                    TestContent(
                        test = testData!!,
                        questions = questions,
                        userAnswers = userAnswers,
                        checkedAnswers = checkedAnswers,
                        onAnswerSelected = { questionId, answerId ->
                            userAnswers = userAnswers + (questionId to answerId)
                            showAnswerAllQuestionsWarning = false
                        }
                    )
                }
            } else if (levelDocument != null) {
                LevelDocumentContent(levelDocument!!)
            } else {
                Text("Контент не найден")
            }
        },
        bottomBar = {
            if (isTest) {
                BottomAppBar(
                    containerColor = BackgroundColor,
                    modifier = Modifier.height(130.dp)
                ) {
                    Row (
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
                    ) {
                        Card(
                            modifier = Modifier
                                .padding(start = 30.dp)
                                .border(2.dp, Green, RoundedCornerShape(30.dp))
                                .shadow(4.dp, RoundedCornerShape(30.dp))
                                .background(BackgroundColor),
                            shape = RoundedCornerShape(30.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
                        ) {
                            Row (modifier = Modifier.background(BackgroundColor),
                                horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically){
                                Text(
                                    modifier = Modifier.padding(
                                        top = 15.dp,
                                        bottom = 15.dp,
                                        start = 10.dp
                                    ).background(BackgroundColor),
                                    text = if (checkedAnswers.isNotEmpty()) {
                                        val (correct, total) = calculateScore(questions, userAnswers)
                                        correct.toString()
                                    } else {
                                        "0"
                                    },
                                    style = TextStyle(
                                        color = Black,
                                        fontSize = 20.sp,
                                        fontFamily = FontFamily(Font(R.font.jura))
                                    )
                                )

                                Text(
                                    modifier = Modifier.padding(top = 15.dp, bottom = 15.dp, end = 10.dp),
                                    text = "/5",
                                    style = TextStyle(
                                        color = Black,
                                        fontSize = 20.sp,
                                        fontFamily = FontFamily(Font(R.font.jura))
                                    )
                                )

                            }

                        }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BackgroundColor)
                                .padding(start = 10.dp, end = 30.dp)
                                .border(2.dp, Green, RoundedCornerShape(30.dp))
                                .shadow(4.dp, RoundedCornerShape(30.dp)),
                            shape = RoundedCornerShape(30.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (allQuestionsAnswered) {
                                        checkedAnswers = userAnswers.keys
                                        showAnswerAllQuestionsWarning = false
                                    } else {
                                        showAnswerAllQuestionsWarning = true
                                        showToast("Пожалуйста, ответьте на все вопросы")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = allQuestionsAnswered,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (allQuestionsAnswered) Blue else BackgroundColor,
                                    disabledContainerColor = BackgroundColor
                                )
                            ) {
                                Text(
                                    modifier = Modifier.padding(top = 10.dp, bottom = 10.dp),
                                    text = "Проверить ответы",
                                    style = TextStyle(
                                        color = Black,
                                        fontSize = 20.sp,
                                        fontFamily = FontFamily(Font(R.font.jura))
                                    ))
                            }
                        }
                    }
                }
            } else {
                BottomAppBar(
                    containerColor = BackgroundColor,
                    modifier = Modifier.height(130.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 30.dp)
                            .border(2.dp, Green, RoundedCornerShape(30.dp))
                            .shadow(4.dp, RoundedCornerShape(30.dp)),
                        shape = RoundedCornerShape(30.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
                    ) {
                        Button(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Blue)
                        ) {
                            Text(
                                modifier = Modifier.padding(top = 10.dp, bottom = 10.dp),
                                text = "Завершить урок",
                                style = TextStyle(
                                    color = Black,
                                    fontSize = 20.sp,
                                    fontFamily = FontFamily(Font(R.font.jura)))
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun TestContent(
    test: Test,
    questions: List<QuestionWithAnswers>,
    userAnswers: Map<Int, Int?>,
    checkedAnswers: Set<Int>,
    onAnswerSelected: (Int, Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(top = 100.dp, bottom = 100.dp, start = 16.dp, end = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = test.title,
            style = TextStyle(
                fontSize = 24.sp,
                fontFamily = FontFamily(Font(R.font.jura_semibold))),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        questions.forEach { question ->
            QuestionCard(
                question = question,
                selectedAnswerId = userAnswers[question.id],
                checked = checkedAnswers.contains(question.id),
                onAnswerSelected = { answerId ->
                    onAnswerSelected(question.id, answerId)
                }
            )
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun LevelDocumentContent(document: LevelDocument) {
    val parsedDocument = remember(document) {
        parseDocumentContent(document.content, document.imageUrl)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(top = 100.dp, bottom = 50.dp, start = 16.dp, end = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Заголовок
        Text(
            text = parsedDocument.title,
            fontSize = 24.sp,
            style = TextStyle(
                color = Black,
                fontSize = 24.sp,
                fontFamily = FontFamily(Font(R.font.jura_semibold))),
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 16.dp),
        )

        // Контент
        parsedDocument.content.forEachIndexed { index, block ->
            when (block) {
                is ContentBlock.Paragraph -> {
                    // Проверяем, является ли блок подзаголовком (начинается с -- в оригинальном тексте)
                    val isSubtitle = document.content.lines().any {
                        it.trim().startsWith("--") && it.trim().substring(2).trim() == block.text
                    }

                    if (isSubtitle) {
                        Text(
                            text = block.text,
                            fontSize = 20.sp,
                            style = TextStyle(
                                color = Black,
                                fontSize = 20.sp,
                                fontFamily = FontFamily(Font(R.font.jura)),
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier
                                .padding(top = 16.dp, bottom = 8.dp)
                                .align(Alignment.Start)
                        )
                    } else {
                        Text(
                            text = block.text,
                            style = TextStyle(
                                color = Black,
                                fontSize = 18.sp,
                                fontFamily = FontFamily(Font(R.font.jura))
                            ),
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .align(Alignment.Start)
                        )
                    }
                }
                is ContentBlock.Quote -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BackgroundColor)
                            .shadow(
                                elevation = 4.dp,
                                ambientColor = Color.Black,
                                spotColor = Color.Black,
                                shape = RoundedCornerShape(10.dp)
                            ),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(BackgroundColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
                    ) {
                        Text(
                            modifier = Modifier.padding(10.dp),
                            text = block.text,
                            style = TextStyle(
                                color = Black,
                                fontSize = 18.sp,
                                fontFamily = FontFamily(Font(R.font.jura)),
                                fontStyle = FontStyle.Italic
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
                is ContentBlock.Image -> {
                    AsyncImage(
                        model = block.url,
                        contentDescription = "Documentation image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(100.dp))
    }
}



fun parseDocumentContent(content: String, imageUrl: String?): ParsedDocument {
    val normalizedContent = content
        .replace("", "⊂")
        .replace("", "Ω")
        .replace("", "∅")

    val lines = normalizedContent.lines()
    if (lines.isEmpty()) return ParsedDocument("", listOf())

    val title = lines.first().trim()
    val contentBlocks = mutableListOf<ContentBlock>()

    var currentQuote: StringBuilder? = null

    for (line in lines.drop(1)) {
        val trimmedLine = line.trim()
        if (trimmedLine.isEmpty()) continue

        when {
            // Обработка подзаголовков (начинаются с --)
            trimmedLine.startsWith("--") -> {
                // Если есть текущее определение, добавляем его
                currentQuote?.let {
                    contentBlocks.add(ContentBlock.Quote(it.toString()))
                    currentQuote = null
                }
                // Добавляем подзаголовок
                contentBlocks.add(ContentBlock.Paragraph(trimmedLine.substring(2).trim()))
            }
            // Обработка определений (начинаются с >)
            trimmedLine.startsWith(">") -> {
                if (currentQuote == null) {
                    currentQuote = StringBuilder(trimmedLine.substring(1).trim())
                } else {
                    currentQuote!!.append("\n").append(trimmedLine.substring(1).trim())
                }
            }
            else -> {
                // Если есть текущее определение, добавляем его
                currentQuote?.let {
                    contentBlocks.add(ContentBlock.Quote(it.toString()))
                    currentQuote = null
                }
                contentBlocks.add(ContentBlock.Paragraph(trimmedLine))
            }
        }
    }

    // Добавляем последнее определение, если оно есть
    currentQuote?.let {
        contentBlocks.add(ContentBlock.Quote(it.toString()))
    }

    // Добавляем изображение, если оно есть
    imageUrl?.let {
        contentBlocks.add(ContentBlock.Image(it))
    }

    return ParsedDocument(title, contentBlocks)
}


@Composable
fun QuestionCard(
    question: QuestionWithAnswers,
    selectedAnswerId: Int?,
    checked: Boolean,
    onAnswerSelected: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = BackgroundColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = question.questionText,
                style = TextStyle(
                    fontSize = 18.sp,
                    fontFamily = FontFamily(Font(R.font.jura_semibold))),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            question.answers.forEach { answer ->
                val isSelected = selectedAnswerId == answer.id
                val isCorrect = answer.isCorrect
                val showCorrectness = checked && (isSelected || isCorrect)

                val backgroundColor = when {
                    !showCorrectness -> BackgroundColor
                    isCorrect -> Color.Green.copy(alpha = 0.2f)
                    isSelected && !isCorrect -> Color.Red.copy(alpha = 0.2f)
                    else -> BackgroundColor
                }

                val borderColor = when {
                    isSelected -> Blue
                    else -> Color.LightGray
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .border(1.dp, borderColor, RoundedCornerShape(4.dp))
                        .background(backgroundColor, RoundedCornerShape(4.dp))
                        .clickable(
                            enabled = !checked,
                            onClick = { onAnswerSelected(answer.id) }
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = answer.answerText,
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontFamily = FontFamily(Font(R.font.jura))
                        )
                    )
                }
            }
        }
    }
}

fun calculateScore(questions: List<QuestionWithAnswers>, userAnswers: Map<Int, Int?>): Pair<Int, Int> {
    var correct = 0
    questions.forEach { question ->
        val selectedAnswerId = userAnswers[question.id]
        if (selectedAnswerId != null) {
            val selectedAnswer = question.answers.find { it.id == selectedAnswerId }
            if (selectedAnswer?.isCorrect == true) {
                correct++
            }
        }
    }
    return Pair(correct, questions.size)
}