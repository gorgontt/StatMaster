package com.example.statmaster.adaptive


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

    /**
     * Получение персонализированных рекомендаций для пользователя
     */
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


//    suspend fun getPersonalizedRecommendations(userId: String): List<Recommendation> {
//        // ВРЕМЕННО: принудительно показываем, что нашли похожих пользователей
//        val similarUsers = collaborativeFiltering.findSimilarUsers(userId)
//
//        val recommendations = mutableListOf<Recommendation>()
//
//        // Базовые рекомендации от IRT
//        recommendations.addAll(irtRepository.getRecommendations())
//
//        // ПРИНУДИТЕЛЬНО ДОБАВЛЯЕМ РЕКОМЕНДАЦИИ ОТ CF
//        // Даже если реальных похожих пользователей нет, показываем пример
//        recommendations.add(
//            Recommendation(
//                text = "👥 Найдены похожие пользователи! На основе анализа их успехов рекомендуем тему 'Случайные события'.",
//                difficulty = "easy"
//            )
//        )
//        recommendations.add(
//            Recommendation(
//                text = "📊 85% пользователей с вашим уровнем успешно прошли тест по 'Теоремам вероятностей'.",
//                difficulty = "medium"
//            )
//        )
//        recommendations.add(
//            Recommendation(
//                text = "🎯 Следующая тема для изучения: 'Одномерные случайные величины' (рекомендация от похожих пользователей).",
//                difficulty = "medium"
//            )
//        )
//
//        return recommendations
//    }

    /**
     * Обновление вектора пользователя
     */
    suspend fun updateUserVector(userId: String) {
        collaborativeFiltering.buildUserVector(userId)
    }
}