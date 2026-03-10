package com.example.statmaster.adaptive

import com.example.statmaster.QuestionWithAnswers
import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant
import kotlinx.datetime.Clock
import kotlinx.serialization.SerialName

// Модели данных для адаптивного тестирования
@Serializable
data class UserAbility(
    @SerialName("user_id") val userId: String,  // Маппинг на колонку user_id
    @SerialName("ability_level") var abilityLevel: Float = 0.0f,
    @SerialName("ability_variance") var abilityVariance: Float = 1.0f,
    @SerialName("questions_answered") var questionsAnswered: Int = 0,
    @SerialName("last_updated") val lastUpdated: Long = System.currentTimeMillis()
)

@Serializable
data class UserResponse(
    val id: Int = 0,
    @SerialName("user_id") val userId: String,
    @SerialName("question_id") val questionId: Int,
    val difficulty: String,
    @SerialName("is_correct") val isCorrect: Boolean,
    @SerialName("response_time") val responseTime: Int,
    @SerialName("ability_before") val abilityBefore: Float,
    @SerialName("ability_after") val abilityAfter: Float,
    @SerialName("created_at") val createdAt: Long = System.currentTimeMillis()
)

data class AdaptiveQuestion(
    val question: QuestionWithAnswers,
    val difficulty: String,
    val discrimination: Float = 1.0f,
    val guessing: Float = 0.25f
)

enum class MasteryLevel {
    BEGINNER,
    INTERMEDIATE,
    EXPERT
}

data class Recommendation(
    val text: String,
    val difficulty: String
)

data class SessionStats(
    val totalQuestions: Int,
    val correctAnswers: Int,
    val accuracy: Float,
    val averageResponseTime: Int
)