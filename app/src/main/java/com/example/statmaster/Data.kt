package com.example.statmaster

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Level(
    val id: Int,
    val title: String,
    val description: String,
    @SerialName("order_number") val orderNumber: Int,
    @SerialName("is_completed") val isCompleted: Boolean
)

@Serializable
data class LevelDocument(

    val id: Int,
    @SerialName("level_id") val levelId: Int,
    val content: String,
    @SerialName("image_url") val imageUrl: String?
)

data class ParsedDocument(
    val title: String,
    val content: List<ContentBlock>
)

sealed class ContentBlock {
    data class Paragraph(val text: String) : ContentBlock()
    data class Quote(val text: String) : ContentBlock()
    data class Image(val url: String) : ContentBlock()
}

@Serializable
data class Test(
    val id: Int,
    @SerialName("level_id") val levelId: Int,
    val title: String,
    val description: String
)

@Serializable
data class QuestionWithAnswers(
    val id: Int,
    @SerialName("test_id") val testId: Int,
    @SerialName("question_text") val questionText: String,
    @SerialName("order_number") val orderNumber: Int,
    val answers: List<Answer> = emptyList()
)

@Serializable
data class Answer(
    val id: Int,
    @SerialName("question_id") val questionId: Int,
    @SerialName("answer_text") val answerText: String,
    @SerialName("is_correct") val isCorrect: Boolean,
    @SerialName("order_number") val orderNumber: Int
)



