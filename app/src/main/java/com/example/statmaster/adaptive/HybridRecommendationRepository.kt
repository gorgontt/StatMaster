package com.example.statmaster.adaptive

import android.content.Context
import android.util.Log
import com.example.statmaster.auth.AuthManager
import com.example.statmaster.QuestionWithAnswers

class HybridRecommendationRepository(
    private val authManager: AuthManager,
    private val context: Context
) {

    val irtRepository = AdaptiveTestingRepository(authManager, context)
    private val collaborativeFiltering = CollaborativeFiltering(authManager, context)

    companion object {
        const val IRT_WEIGHT = 0.7f
        const val CF_WEIGHT = 0.3f
    }

    suspend fun getHybridRecommendation(
        userId: String,
        topicId: Int? = null,
        excludeQuestionIds: Set<Int> = emptySet()
    ): AdaptiveQuestion? {
        val irtQuestion = irtRepository.getNextQuestion(topicId)
        val cfQuestions = collaborativeFiltering.getCollaborativeRecommendations(
            userId = userId,
            currentTopicId = topicId,
            excludeQuestionIds = excludeQuestionIds
        )
        val finalQuestion = if (irtQuestion != null && cfQuestions.isNotEmpty()) {
            combineRecommendations(irtQuestion, cfQuestions)
        } else {
            irtQuestion ?: cfQuestions.firstOrNull()?.let {
                AdaptiveQuestion(question = it, difficulty = it.difficulty ?: "medium")
            }
        }

        return finalQuestion
    }
    private fun combineRecommendations(
        irtQuestion: AdaptiveQuestion,
        cfQuestions: List<QuestionWithAnswers>
    ): AdaptiveQuestion {

        val isInCF = cfQuestions.any { it.id == irtQuestion.question.id }

        if (isInCF) {
            Log.d("HybridRecommendation", "Вопрос ${irtQuestion.question.id} рекомендован обеими моделями")
            return irtQuestion
        }

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

    // персонализированные рекомендаци для пользователя

    suspend fun getPersonalizedRecommendations(userId: String): List<Recommendation> {
        val similarUsers = collaborativeFiltering.findSimilarUsers(userId)
        val recommendations = mutableListOf<Recommendation>()

        recommendations.addAll(irtRepository.getRecommendations())

        if (similarUsers.isNotEmpty()) {
            recommendations.add(
                Recommendation(
                    text = "Пользователь с похожим уровнем успешно прошёл дополнительные темы. Рекомендуем вам тоже.",
                    difficulty = "medium"
                )
            )
        }

        return recommendations
    }

    suspend fun updateUserVector(userId: String) {
        collaborativeFiltering.buildUserVector(userId)
    }
}