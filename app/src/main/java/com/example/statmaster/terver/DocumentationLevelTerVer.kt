package com.example.statmaster.terver

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.CardColors
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
import com.example.statmaster.ui.theme.Transparent


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

    LaunchedEffect(levelId) {
        if (levelId != null) {
            // Проверяем, является ли уровень тестом
            val test = levelRepository.getTestByLevelId(levelId)
            if (test != null) {
                isTest = true
                testData = test
                questions = levelRepository.getQuestionsWithAnswers(test.id)
                // Инициализируем пустые ответы
                userAnswers = questions.associate { it.id to null }
            } else {
                // Загружаем обычный документ
                val doc = levelRepository.getLevelDocument(levelId)
                levelDocument = doc
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
                TestContent(
                    test = testData!!,
                    questions = questions,
                    userAnswers = userAnswers,
                    checkedAnswers = checkedAnswers,
                    onAnswerSelected = { questionId, answerId ->
                        userAnswers = userAnswers + (questionId to answerId)
                    },
                    onCheckAnswers = {
                        checkedAnswers = userAnswers.filterValues { it != null }.keys
                    }
                )
            } else if (levelDocument != null) {
                LevelDocumentContent(levelDocument!!)
            } else {
                Text("Контент не найден")
            }
        },

        bottomBar = {
            BottomAppBar {
                BottomAppBar(
                    containerColor = BackgroundColor,
                    modifier = Modifier.height(100.dp),
                    contentColor = BackgroundColor
                ) {

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Transparent)
                            .padding(start = 30.dp, end = 30.dp)
                            .border(width = 2.dp, shape = RoundedCornerShape(30.dp), color = Green)
                            .shadow(
                                elevation = 4.dp,
                                ambientColor = Color.Black,
                                spotColor = Color.Black,
                                shape = RoundedCornerShape(30.dp)
                            )

                            .clickable {
                                //navController.navigate("players_list/компания")
                            },
                        shape = RoundedCornerShape(30.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
                    ) {

                        Button(
                            onClick = {},
                            //contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Blue),
                            colors = ButtonDefaults.buttonColors(containerColor = BackgroundColor),
                        ) {


                            Text(
                                modifier = Modifier.padding(top = 10.dp, bottom = 10.dp),
                                text = "Завершить урок",
                                style = TextStyle(
                                    color = Black, fontSize = 20.sp, fontFamily = FontFamily(
                                        Font(R.font.jura)
                                    )
                                )
                            )
                        }
                    }

                }


            }
        }
    )
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
fun TestContent(
    test: Test,
    questions: List<QuestionWithAnswers>,
    userAnswers: Map<Int, Int?>,
    checkedAnswers: Set<Int>,
    onAnswerSelected: (Int, Int) -> Unit,
    onCheckAnswers: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(16.dp)
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

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                    onClick = onCheckAnswers,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Blue)
        ) {
            Text("Проверить ответы")
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
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