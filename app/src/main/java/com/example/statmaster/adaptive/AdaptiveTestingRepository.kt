package com.example.statmaster.adaptive

import android.content.Context
import android.util.Log
import com.example.statmaster.AuthManager
import com.example.statmaster.QuestionWithAnswers
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.exp

class AdaptiveTestingRepository(
    private val authManager: AuthManager,
    private val context: Context
) {

    // Текущее состояние пользователя
    private val _userAbility = MutableStateFlow<UserAbility?>(null)
    val userAbility: StateFlow<UserAbility?> = _userAbility.asStateFlow()

    // История ответов в текущей сессии
    private val sessionResponses = mutableListOf<UserResponse>()

    // Константы для IRT модели
    companion object {
        const val INITIAL_ABILITY = 0.0f
        const val INITIAL_VARIANCE = 1.0f
        const val LEARNING_RATE = 0.3f
        const val MIN_QUESTIONS_FOR_EVALUATION = 5
    }

    // Загружаем или создаем профиль способностей пользователя
    suspend fun initializeUserAbility(userId: String) {
        try {
            // Пытаемся получить существующий профиль
            val existingAbility = authManager.supabase.postgrest
                .from("user_ability")
                .select(Columns.raw("""*""")) {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeSingleOrNull<UserAbility>()

            if (existingAbility != null) {
                _userAbility.value = existingAbility
            } else {
                // Создаем новый профиль
                val newAbility = UserAbility(
                    userId = userId,
                    abilityLevel = INITIAL_ABILITY,
                    abilityVariance = INITIAL_VARIANCE,
                    questionsAnswered = 0
                )

                authManager.supabase.postgrest
                    .from("user_ability")
                    .insert(newAbility)

                _userAbility.value = newAbility
            }
        } catch (e: Exception) {
            Log.e("AdaptiveTesting", "Error initializing user ability", e)
        }
    }

    // Получаем следующий вопрос на основе текущего уровня
    suspend fun getNextQuestion(topicId: Int? = null): AdaptiveQuestion? {
        val ability = _userAbility.value ?: return null

        return try {
            // Определяем целевой уровень сложности
            val targetDifficulty = selectDifficultyBasedOnAbility(ability.abilityLevel)

            // Используем raw запрос
            val query = if (topicId != null) {
                authManager.supabase.postgrest
                    .from("question")
                    .select(Columns.raw("*, answers:answer(*)")) {
                        filter {
                            eq("test_id", topicId)
                        }
                    }
            } else {
                authManager.supabase.postgrest
                    .from("question")
                    .select(Columns.raw("*, answers:answer(*)"))
            }

            val allQuestions = query.decodeList<QuestionWithAnswers>()

            // Фильтруем по сложности в коде
            val questionsByDifficulty = allQuestions.filter { question ->
                question.difficulty == targetDifficulty
            }

            // Выбираем вопрос, который еще не задавали в этой сессии
            val askedQuestionIds = sessionResponses.map { it.questionId }.toSet()
            val availableQuestions = questionsByDifficulty.filter { it.id !in askedQuestionIds }

            if (availableQuestions.isNotEmpty()) {
                val selectedQuestion = availableQuestions.random()
                AdaptiveQuestion(
                    question = selectedQuestion,
                    difficulty = targetDifficulty
                )
            } else {
                // Если нет вопросов нужной сложности, берем любой из неотвеченных
                val anyAvailable = allQuestions.filter { it.id !in askedQuestionIds }
                anyAvailable.randomOrNull()?.let {
                    AdaptiveQuestion(
                        question = it,
                        difficulty = it.difficulty ?: "medium"
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("AdaptiveTesting", "Error getting next question", e)
            null
        }
    }

    // Обрабатываем ответ пользователя и обновляем уровень способностей
    suspend fun processAnswer(
        questionId: Int,
        difficulty: String,
        isCorrect: Boolean,
        responseTime: Int
    ) {
        val ability = _userAbility.value ?: return
        val userId = ability.userId

        // Сохраняем уровень до ответа
        val abilityBefore = ability.abilityLevel

        // Обновляем уровень по IRT модели
        val updatedAbility = updateAbilityLevel(
            currentAbility = ability.abilityLevel,
            currentVariance = ability.abilityVariance,
            difficulty = difficulty,
            isCorrect = isCorrect
        )

        // Создаем запись об ответе
        val response = UserResponse(
            userId = userId,
            questionId = questionId,
            difficulty = difficulty,
            isCorrect = isCorrect,
            responseTime = responseTime,
            abilityBefore = abilityBefore,
            abilityAfter = updatedAbility.first
        )

        sessionResponses.add(response)

        // Обновляем состояние
        val newAbility = ability.copy(
            abilityLevel = updatedAbility.first,
            abilityVariance = updatedAbility.second,
            questionsAnswered = ability.questionsAnswered + 1
        )

        _userAbility.value = newAbility

        // Сохраняем в базу данных
        try {
            // Сохраняем ответ
            authManager.supabase.postgrest
                .from("user_responses")
                .insert(response)

            // Обновляем профиль способностей
            authManager.supabase.postgrest
                .from("user_ability")
                .update(
                    mapOf(
                        "ability_level" to newAbility.abilityLevel,
                        "ability_variance" to newAbility.abilityVariance,
                        "questions_answered" to newAbility.questionsAnswered
                    )
                ) {
                    filter {
                        eq("user_id", userId)
                    }
                }
        } catch (e: Exception) {
            Log.e("AdaptiveTesting", "Error saving response", e)
        }
    }

    // Обновление уровня способностей по IRT модели
    private fun updateAbilityLevel(
        currentAbility: Float,
        currentVariance: Float,
        difficulty: String,
        isCorrect: Boolean
    ): Pair<Float, Float> {
        // Преобразуем сложность в числовое значение
        val difficultyValue = when (difficulty) {
            "easy" -> -1.0f
            "medium" -> 0.0f
            "hard" -> 1.0f
            else -> 0.0f
        }

        // Дискриминативность
        val discrimination = 1.0f

        // Вычисляем вероятность правильного ответа
        val probability = 1.0f / (1.0f + exp(-discrimination * (currentAbility - difficultyValue)))

        // Обновляем уровень способностей
        val gradient = if (isCorrect) 1 - probability else -probability
        val newAbility = currentAbility + LEARNING_RATE * gradient * discrimination

        // Уменьшаем дисперсию
        val newVariance = currentVariance * 0.9f

        return Pair(newAbility, newVariance)
    }

    // Выбор сложности на основе текущего уровня
    private fun selectDifficultyBasedOnAbility(ability: Float): String {
        return when {
            ability < -0.5f -> "easy"
            ability > 0.5f -> "hard"
            else -> "medium"
        }
    }

    // Оценка уровня владения темой
    fun evaluateMasteryLevel(): MasteryLevel {
        val ability = _userAbility.value ?: return MasteryLevel.BEGINNER

        val recentResponses = sessionResponses.takeLast(5)
        if (recentResponses.isEmpty()) return MasteryLevel.BEGINNER

        val correctCount = recentResponses.count { it.isCorrect }
        val consistency = correctCount.toFloat() / recentResponses.size

        return when {
            ability.abilityLevel > 1.0f && consistency > 0.8f -> MasteryLevel.EXPERT
            ability.abilityLevel > 0.3f && consistency > 0.7f -> MasteryLevel.INTERMEDIATE
            else -> MasteryLevel.BEGINNER
        }
    }

    // Получаем рекомендации по дальнейшему обучению
    fun getRecommendations(): List<Recommendation> {
        val masteryLevel = evaluateMasteryLevel()

        return when (masteryLevel) {
            MasteryLevel.BEGINNER -> listOf(
                Recommendation("Повторите базовые концепции", "easy"),
                Recommendation("Попробуйте больше простых задач", "easy"),
                Recommendation("Посмотрите видео-уроки по основам", "easy")
            )
            MasteryLevel.INTERMEDIATE -> listOf(
                Recommendation("Переходите к задачам средней сложности", "medium"),
                Recommendation("Изучите дополнительные материалы", "medium"),
                Recommendation("Попробуйте решать задачи с ограничением по времени", "medium")
            )
            MasteryLevel.EXPERT -> listOf(
                Recommendation("Попробуйте сложные задачи", "hard"),
                Recommendation("Проверьте себя итоговым тестом", "hard"),
                Recommendation("Поделитесь знаниями с другими учениками", "hard")
            )
        }
    }

    // Сброс сессии
    fun resetSession() {
        sessionResponses.clear()
    }

    // Получить статистику по сессии
    fun getSessionStats(): SessionStats {
        val totalQuestions = sessionResponses.size
        val correctAnswers = sessionResponses.count { it.isCorrect }
        val accuracy = if (totalQuestions > 0) correctAnswers.toFloat() / totalQuestions else 0f

        val averageTime = if (sessionResponses.isNotEmpty()) {
            sessionResponses.map { it.responseTime }.average().toInt()
        } else 0

        return SessionStats(
            totalQuestions = totalQuestions,
            correctAnswers = correctAnswers,
            accuracy = accuracy,
            averageResponseTime = averageTime
        )
    }
}