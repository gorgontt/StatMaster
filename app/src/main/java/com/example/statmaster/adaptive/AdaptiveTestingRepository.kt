package com.example.statmaster.adaptive

import android.content.Context
import android.util.Log
import com.example.statmaster.AuthManager
import com.example.statmaster.QuestionWithAnswers
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withTimeout
import kotlin.math.exp

class AdaptiveTestingRepository(
    private val authManager: AuthManager,
    private val context: Context
) {

    private val _userAbility = MutableStateFlow<UserAbility?>(null)
    val userAbility: StateFlow<UserAbility?> = _userAbility.asStateFlow()
    val sessionResponses = mutableListOf<UserResponse>()

    companion object {
        const val INITIAL_ABILITY = 0.0f
        const val INITIAL_VARIANCE = 1.0f
        const val LEARNING_RATE = 0.3f
        const val MIN_QUESTIONS_FOR_EVALUATION = 5
        private const val REQUEST_TIMEOUT_MS = 15000L  // 15 секунд
    }

    // Инициализация профиля пользователя (локальная, без БД)
    suspend fun initializeUserAbility(userId: String) {
        val localAbility = UserAbility(
            userId = userId,
            abilityLevel = INITIAL_ABILITY,
            abilityVariance = INITIAL_VARIANCE,
            questionsAnswered = 0
        )
        _userAbility.value = localAbility
        Log.d("AdaptiveTest", "Локальный профиль создан для пользователя: $userId")
    }

    // Получение следующего вопроса с таймаутом и повторными попытками
    suspend fun getNextQuestion(topicId: Int? = null, retryCount: Int = 0): AdaptiveQuestion? {
        val ability = _userAbility.value
        if (ability == null) {
            Log.e("AdaptiveTest", "ability is NULL!")
            return null
        }

        val targetDifficulty = selectDifficultyBasedOnAbility(ability.abilityLevel)
        Log.d("AdaptiveTest", "getNextQuestion: ability=${ability.abilityLevel}, targetDifficulty=$targetDifficulty, попытка=${retryCount + 1}")

        return try {
            withTimeout(REQUEST_TIMEOUT_MS) {
                val query = if (topicId != null) {
                    authManager.supabase.postgrest
                        .from("question")
                        .select(Columns.raw("*, answers:answer(*)")) {
                            filter { eq("test_id", topicId) }
                        }
                } else {
                    authManager.supabase.postgrest
                        .from("question")
                        .select(Columns.raw("*, answers:answer(*)"))
                }

                val allQuestions = query.decodeList<QuestionWithAnswers>()
                Log.d("AdaptiveTest", "Загружено вопросов: ${allQuestions.size}")

                // Фильтруем вопросы по сложности и наличию ответов
                val availableQuestions = allQuestions.filter { question ->
                    question.difficulty == targetDifficulty && question.answers.isNotEmpty()
                }
                Log.d("AdaptiveTest", "Доступно вопросов сложности $targetDifficulty: ${availableQuestions.size}")

                val askedQuestionIds = sessionResponses.map { it.questionId }.toSet()
                val notAskedQuestions = availableQuestions.filter { it.id !in askedQuestionIds }
                Log.d("AdaptiveTest", "Неотвеченных вопросов: ${notAskedQuestions.size}")

                // Выбираем случайный вопрос из неотвеченных, если есть, иначе из всех доступных
                val selectedQuestion = notAskedQuestions.randomOrNull() ?: availableQuestions.randomOrNull()

                selectedQuestion?.let {
                    Log.d("AdaptiveTest", "Выбран вопрос: id=${it.id}, text=${it.questionText.take(50)}")
                    AdaptiveQuestion(question = it, difficulty = targetDifficulty)
                } ?: run {
                    Log.e("AdaptiveTest", "Нет доступных вопросов для сложности $targetDifficulty")
                    null
                }
            }
        } catch (e: TimeoutCancellationException) {
            Log.e("AdaptiveTest", "Timeout при загрузке вопросов, попытка ${retryCount + 1}")
            if (retryCount < 2) {
                // Повторяем попытку через 1 секунду
                kotlinx.coroutines.delay(1000)
                getNextQuestion(topicId, retryCount + 1)
            } else {
                Log.e("AdaptiveTest", "Все попытки загрузки вопросов исчерпаны")
                null
            }
        } catch (e: Exception) {
            Log.e("AdaptiveTest", "Ошибка при загрузке вопросов", e)
            if (retryCount < 2) {
                kotlinx.coroutines.delay(1000)
                getNextQuestion(topicId, retryCount + 1)
            } else {
                null
            }
        }
    }

    // Обработка ответа пользователя
    suspend fun processAnswer(
        questionId: Int,
        difficulty: String,
        isCorrect: Boolean,
        responseTime: Int
    ) {
        val ability = _userAbility.value
        if (ability == null) {
            Log.e("AdaptiveTest", "processAnswer: ability is NULL!")
            return
        }

        val userId = ability.userId
        val abilityBefore = ability.abilityLevel
        val updatedAbility = updateAbilityLevel(
            currentAbility = ability.abilityLevel,
            currentVariance = ability.abilityVariance,
            difficulty = difficulty,
            isCorrect = isCorrect
        )

        Log.d("AdaptiveTest", "processAnswer: вопрос=$questionId, isCorrect=$isCorrect, уровень был=$abilityBefore, стал=${updatedAbility.first}")

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

        val newAbility = ability.copy(
            abilityLevel = updatedAbility.first,
            abilityVariance = updatedAbility.second,
            questionsAnswered = ability.questionsAnswered + 1
        )
        _userAbility.value = newAbility

        // Сохранение в БД временно отключено для стабильности
        // try {
        //     authManager.supabase.postgrest
        //         .from("user_responses")
        //         .insert(response)
        //
        //     authManager.supabase.postgrest
        //         .from("user_ability")
        //         .update(
        //             mapOf(
        //                 "ability_level" to newAbility.abilityLevel,
        //                 "ability_variance" to newAbility.abilityVariance,
        //                 "questions_answered" to newAbility.questionsAnswered
        //             )
        //         ) {
        //             filter { eq("user_id", userId) }
        //         }
        // } catch (e: Exception) {
        //     Log.e("AdaptiveTesting", "Error saving response to DB", e)
        // }

        Log.d("AdaptiveTest", "processAnswer завершён, новый уровень=${newAbility.abilityLevel}, отвечено вопросов=${sessionResponses.size}")
    }

    // Обновление уровня способностей по IRT модели
    private fun updateAbilityLevel(
        currentAbility: Float,
        currentVariance: Float,
        difficulty: String,
        isCorrect: Boolean
    ): Pair<Float, Float> {
        val difficultyValue = when (difficulty) {
            "easy" -> -1.0f
            "medium" -> 0.0f
            "hard" -> 1.0f
            else -> 0.0f
        }

        val discrimination = 1.0f

        // Вероятность правильного ответа по логистической функции
        val probability = 1.0f / (1.0f + exp(-discrimination * (currentAbility - difficultyValue)))

        // Градиентный спуск
        val gradient = if (isCorrect) 1 - probability else -probability
        val newAbility = currentAbility + LEARNING_RATE * gradient * discrimination

        // Уменьшаем дисперсию (уверенность растёт)
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

    // Получение рекомендаций по дальнейшему обучению
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
        Log.d("AdaptiveTest", "Сессия сброшена")
    }

    // Получение статистики по сессии
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