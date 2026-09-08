package com.example.statmaster.terver

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.statmaster.ContentBlock
import com.example.statmaster.LevelDocument
import com.example.statmaster.QuestionWithAnswers
import com.example.statmaster.R
import com.example.statmaster.Test
import com.example.statmaster.ui.theme.*

@Composable
fun TestScreenContent(
    test: Test,
    questions: List<QuestionWithAnswers>,
    userAnswers: Map<Int, Int?>,
    checkedAnswers: Set<Int>,
    showWarning: Boolean,
    onAnswerSelected: (Int, Int) -> Unit,
    context: Context,
    levelId: Int
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(top = 100.dp, bottom = 100.dp, start = 16.dp, end = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            modifier = Modifier.padding(bottom = 20.dp),
            text = test.title,
            style = TextStyle(fontSize = 24.sp, fontFamily = FontFamily(Font(R.font.jura_semibold)))
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
fun QuestionCard(
    question: QuestionWithAnswers,
    selectedAnswerId: Int?,
    checked: Boolean,
    onAnswerSelected: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .shadow(elevation = 4.dp, ambientColor = Color.Black, spotColor = Color.Black, shape = RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = BackgroundColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = question.questionText,
                style = TextStyle(fontSize = 16.sp, fontFamily = FontFamily(Font(R.font.jura_semibold))),
                modifier = Modifier.padding(bottom = 15.dp)
            )

            question.answers.forEach { answer ->
                val isSelected = selectedAnswerId == answer.id
                val isCorrect = answer.isCorrect
                val showCorrectness = checked && (isSelected || isCorrect)

                val backgroundColor = when {
                    !showCorrectness -> White
                    isCorrect -> Green
                    isSelected && !isCorrect -> RedColor
                    else -> White
                }

                val borderColor = if (isSelected) DarkBlue else White

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .border(1.dp, borderColor, RoundedCornerShape(50.dp))
                        .background(backgroundColor, RoundedCornerShape(50.dp))
                        .clickable(enabled = !checked) { onAnswerSelected(answer.id) }
                        .padding(12.dp)
                ) {
                    Text(
                        modifier = Modifier.padding(vertical = 10.dp),
                        text = answer.answerText,
                        style = TextStyle(fontSize = 16.sp, fontFamily = FontFamily(Font(R.font.jura)))
                    )
                }
            }
        }
    }
}
@Composable
fun DocumentContent(document: LevelDocument) {
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
        Text(
            text = parsedDocument.title,
            fontSize = 24.sp,
            style = TextStyle(color = Black, fontSize = 24.sp, fontFamily = FontFamily(Font(R.font.jura_semibold))),
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 16.dp)
        )

        parsedDocument.content.forEach { block ->
            when (block) {
                is ContentBlock.Paragraph -> {
                    val isSubtitle = document.content.lines().any {
                        it.trim().startsWith("--") && it.trim().substring(2).trim() == block.text
                    }
                    Text(
                        text = block.text,
                        fontSize = if (isSubtitle) 20.sp else 18.sp,
                        style = TextStyle(
                            color = Black,
                            fontFamily = FontFamily(Font(R.font.jura)),
                            fontWeight = if (isSubtitle) FontWeight.Bold else FontWeight.Normal
                        ),
                        modifier = Modifier
                            .padding(if (isSubtitle) 16.dp else 8.dp, bottom = 8.dp)
                            .align(Alignment.Start)
                    )
                }
                is ContentBlock.Quote -> {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            modifier = Modifier.padding(12.dp),
                            text = block.text,
                            style = TextStyle(
                                color = Black,
                                fontSize = 18.sp,
                                fontFamily = FontFamily(Font(R.font.jura)),
                                fontStyle = FontStyle.Italic
                            )
                        )
                    }
                }
                is ContentBlock.Image -> {
                    AsyncImage(
                        model = block.url,
                        contentDescription = "Documentation image",
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun TestBottomBar(
    questions: List<QuestionWithAnswers>,
    userAnswers: Map<Int, Int?>,
    checkedAnswers: Set<Int>,
    answersChecked: Boolean,
    testCompleted: Boolean,
    totalQuestions: Int,
    onCheckAnswers: () -> Unit,
    onCompleteTest: () -> Unit,
    onResetTest: () -> Unit
) {
    BottomAppBar(containerColor = BackgroundColor) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                modifier = Modifier
                    .padding(start = 30.dp)
                    .shadow(4.dp, RoundedCornerShape(30.dp)),
                shape = RoundedCornerShape(30.dp)
            ) {
                Row(
                    modifier = Modifier.background(when {
                        testCompleted || answersChecked -> Green
                        else -> DarkBlue
                    }),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.padding(start = 10.dp, top = 15.dp, bottom = 15.dp),
                        text = if (checkedAnswers.isNotEmpty()) calculateScore(questions, userAnswers).first.toString() else "0",
                        style = TextStyle(
                            color = if (testCompleted || answersChecked) Black else White,
                            fontSize = 20.sp,
                            fontFamily = FontFamily(Font(R.font.jura))
                        )
                    )
                    Text(
                        modifier = Modifier.padding(end = 10.dp, top = 15.dp, bottom = 15.dp),
                        text = "/$totalQuestions",
                        style = TextStyle(
                            color = if (testCompleted || answersChecked) Black else White,
                            fontSize = 20.sp,
                            fontFamily = FontFamily(Font(R.font.jura))
                        )
                    )
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 30.dp)
                    .shadow(4.dp, RoundedCornerShape(30.dp)),
                shape = RoundedCornerShape(30.dp)
            ) {
                Button(
                    onClick = {
                        when {
                            testCompleted -> onResetTest()
                            !answersChecked -> onCheckAnswers()
                            else -> onCompleteTest()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = if (!answersChecked) {
                        userAnswers.size == questions.size && userAnswers.values.all { it != null }
                    } else true,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when {
                            testCompleted -> Color(0xFFFF9800)
                            answersChecked -> Green
                            else -> DarkBlue
                        }
                    )
                ) {
                    Text(
                        modifier = Modifier.padding(vertical = 10.dp),
                        text = when {
                            testCompleted -> "Пройти заново"
                            answersChecked -> "Завершить тест"
                            else -> "Проверить"
                        },
                        style = TextStyle(
                            color = if (testCompleted || answersChecked) Black else White,
                            fontSize = 20.sp,
                            fontFamily = FontFamily(Font(R.font.jura))
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun LessonBottomBar(
    isLevelCompleted: Boolean,
    isLoading: Boolean,
    onCompleteLevel: () -> Unit,
    showError: Boolean
) {
    BottomAppBar(
        containerColor = BackgroundColor,
        modifier = Modifier.height(130.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 30.dp)
                .shadow(4.dp, RoundedCornerShape(30.dp)),
            shape = RoundedCornerShape(30.dp)
        ) {
            Button(
                onClick = onCompleteLevel,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && !isLevelCompleted,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLevelCompleted) Green else DarkBlue
                )
            ) {
                Text(
                    modifier = Modifier.padding(vertical = 10.dp),
                    text = if (isLevelCompleted) "Урок пройден" else "Завершить урок",
                    style = TextStyle(
                        color = if (isLevelCompleted) Black else White,
                        fontSize = 20.sp,
                        fontFamily = FontFamily(Font(R.font.jura))
                    )
                )
            }
        }
        if (showError) {
            Text(
                text = "Ошибка при завершении",
                color = Color.Red,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
fun LoadingIndicator() {
    val rotation = remember { Animatable(0f) }
    val infiniteAnimation = remember {
        infiniteRepeatable<Float>(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    }

    LaunchedEffect(Unit) {
        rotation.animateTo(360f, infiniteAnimation)
    }

    Canvas(modifier = Modifier.size(70.dp)) {
        drawCircle(
            color = BackgroundColor,
            radius = size.minDimension / 2 - 4.dp.toPx()
        )
        drawArc(
            color = Blue,
            startAngle = rotation.value - 90f,
            sweepAngle = 90f,
            useCenter = false,
            style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}