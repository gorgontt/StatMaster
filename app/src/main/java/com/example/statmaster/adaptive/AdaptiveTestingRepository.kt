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
import kotlin.math.abs
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
        private const val REQUEST_TIMEOUT_MS = 15000L
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

    /**
     * Получение следующего вопроса с непрерывным подбором сложности
     * Вопрос выбирается так, чтобы его сложность (difficulty_value) была
     * максимально близка к текущему уровню пользователя
     */
    suspend fun getNextQuestion(topicId: Int? = null, retryCount: Int = 0): AdaptiveQuestion? {
        val ability = _userAbility.value
        if (ability == null) {
            Log.e("AdaptiveTest", "ability is NULL!")
            return null
        }

        Log.d("AdaptiveTest", "getNextQuestion: ability=${ability.abilityLevel}, попытка=${retryCount + 1}")

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

                // Фильтруем вопросы, у которых есть ответы
                val questionsWithAnswers = allQuestions.filter { it.answers.isNotEmpty() }

                val askedQuestionIds = sessionResponses.map { it.questionId }.toSet()
                val notAskedQuestions = questionsWithAnswers.filter { it.id !in askedQuestionIds }

                // Если все вопросы уже отвечены, берём любые
                val candidateQuestions = if (notAskedQuestions.isNotEmpty()) notAskedQuestions else questionsWithAnswers

                // НЕПРЕРЫВНЫЙ ВЫБОР: ищем вопрос с difficulty_value, наиболее близким к текущему уровню
                val selectedQuestion = candidateQuestions.minByOrNull { question ->
                    val questionDifficulty = question.difficultyValue ?: 0.0
                    abs(questionDifficulty - ability.abilityLevel)
                }

                selectedQuestion?.let {
                    val difficultyCategory = categorizeDifficulty(it.difficultyValue ?: 0.0)
                    Log.d("AdaptiveTest", "Выбран вопрос: id=${it.id}, text=${it.questionText.take(50)}, difficulty_value=${it.difficultyValue}, категория=$difficultyCategory")
                    AdaptiveQuestion(question = it, difficulty = difficultyCategory)
                } ?: run {
                    Log.e("AdaptiveTest", "Нет доступных вопросов")
                    null
                }
            }
        } catch (e: TimeoutCancellationException) {
            Log.e("AdaptiveTest", "Timeout при загрузке вопросов, попытка ${retryCount + 1}")
            if (retryCount < 2) {
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

    /**
     * Преобразование числовой сложности в категорию для отображения
     */
    private fun categorizeDifficulty(difficultyValue: Double): String {
        return when {
            difficultyValue <= -0.7 -> "easy"
            difficultyValue >= 0.7 -> "hard"
            else -> "medium"
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

        // Для обновления уровня используем числовое значение сложности
        val difficultyValue = getDifficultyValueFromCategory(difficulty)
        val updatedAbility = updateAbilityLevelWithValue(
            currentAbility = ability.abilityLevel,
            currentVariance = ability.abilityVariance,
            difficultyValue = difficultyValue,
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
        Log.d("AdaptiveTest", "processAnswer завершён, новый уровень=${newAbility.abilityLevel}, отвечено вопросов=${sessionResponses.size}")
    }

    /**
     * Обновление уровня способностей по IRT модели с числовым значением сложности
     */
    private fun updateAbilityLevelWithValue(
        currentAbility: Float,
        currentVariance: Float,
        difficultyValue: Float,
        isCorrect: Boolean
    ): Pair<Float, Float> {
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

    /**
     * Преобразование категории сложности в числовое значение
     */
    private fun getDifficultyValueFromCategory(difficulty: String): Float {
        return when (difficulty) {
            "easy" -> -1.0f
            "medium" -> 0.0f
            "hard" -> 1.0f
            else -> 0.0f
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