package com.example.statmaster.collabfiltering

import android.content.Context
import android.util.Log
import com.example.statmaster.AuthManager
import com.example.statmaster.QuestionWithAnswers
import com.example.statmaster.adaptive.AdaptiveQuestion
import com.example.statmaster.adaptive.AdaptiveTestingRepository
import com.example.statmaster.adaptive.Recommendation

class HybridRecommendationRepository(
    private val authManager: AuthManager,
    private val context: Context
) {

    private val irtRepository = AdaptiveTestingRepository(authManager, context)
    private val collaborativeFiltering = CollaborativeFiltering(authManager, context)

    companion object {
        // Вес IRT-модели (от 0 до 1)
        const val IRT_WEIGHT = 0.7f
        // Вес коллаборативной фильтрации
        const val CF_WEIGHT = 0.3f
    }

    /**
     * Гибридная рекомендация следующего вопроса
     */
    suspend fun getHybridRecommendation(
        userId: String,
        topicId: Int? = null,
        excludeQuestionIds: Set<Int> = emptySet()
    ): AdaptiveQuestion? {

        // 1. Получаем рекомендацию от IRT (на основе уровня пользователя)
        val irtQuestion = irtRepository.getNextQuestion(topicId)

        // 2. Получаем рекомендацию от коллаборативной фильтрации
        val cfQuestions = collaborativeFiltering.getCollaborativeRecommendations(
            userId = userId,
            currentTopicId = topicId,
            excludeQuestionIds = excludeQuestionIds
        )

        // 3. Гибридное взвешивание
        val finalQuestion = if (irtQuestion != null && cfQuestions.isNotEmpty()) {
            // Комбинируем два подхода
            combineRecommendations(irtQuestion, cfQuestions)
        } else {
            // Если один из подходов не дал результата, используем другой
            irtQuestion ?: cfQuestions.firstOrNull()?.let {
                AdaptiveQuestion(question = it, difficulty = it.difficulty ?: "medium")
            }
        }

        return finalQuestion
    }

    /**
     * Комбинирование рекомендаций с весовыми коэффициентами
     */
    private fun combineRecommendations(
        irtQuestion: AdaptiveQuestion,
        cfQuestions: List<QuestionWithAnswers>
    ): AdaptiveQuestion {

        // Если вопрос из IRT есть среди CF-рекомендаций, усиливаем его приоритет
        val isInCF = cfQuestions.any { it.id == irtQuestion.question.id }

        if (isInCF) {
            // Вопрос рекомендован обеими моделями — отличный выбор
            Log.d("HybridRecommendation", "Вопрос ${irtQuestion.question.id} рекомендован обеими моделями")
            return irtQuestion
        }

        // С вероятностью IRT_WEIGHT берём вопрос от IRT
        // С вероятностью CF_WEIGHT берём лучший вопрос от коллаборативной фильтрации
        val useIRT = Math.random() < IRT_WEIGHT

        return if (useIRT) {
            irtQuestion
        } else {
            val bestCFQuestion = cfQuestions.firstOrNull()
            AdaptiveQuestion(
                question = bestCFQuestion ?: irtQuestion.question,
                difficulty = bestCFQuestion?.difficulty ?: irtQuestion.difficulty
            )
        }
    }

    /**
     * Получение персонализированных рекомендаций для пользователя
     */
    suspend fun getPersonalizedRecommendations(userId: String): List<Recommendation> {
        val similarUsers = collaborativeFiltering.findSimilarUsers(userId)
        val masteryLevel = irtRepository.evaluateMasteryLevel()

        val recommendations = mutableListOf<Recommendation>()

        // Базовые рекомендации от IRT
        recommendations.addAll(irtRepository.getRecommendations())

        // Дополнительные рекомендации от коллаборативной фильтрации
        if (similarUsers.isNotEmpty()) {
            val topSimilar = similarUsers.first()
            recommendations.add(
                Recommendation(
                    text = "Пользователь с похожим уровнем успешно прошёл тему. Рекомендуем вам тоже.",
                    difficulty = "medium"
                )
            )
        }

        return recommendations
    }

    /**
     * Обновление вектора пользователя после прохождения теста
     */
    suspend fun updateUserVector(userId: String) {
        collaborativeFiltering.buildUserVector(userId)
    }
}