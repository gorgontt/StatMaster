package com.example.statmaster.adaptive

import android.content.Context
import android.util.Log
import com.example.statmaster.AuthManager
import com.example.statmaster.QuestionWithAnswers
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.sqrt

class CollaborativeFiltering(
    private val authManager: AuthManager,
    private val context: Context
) {

    companion object {
        private const val SIMILARITY_THRESHOLD = 0.1f
        private const val MAX_RECOMMENDATIONS = 5
        private const val VECTOR_SIZE = 20
    }

    private val userVectors = mutableMapOf<String, FloatArray>()

    suspend fun buildUserVector(userId: String): FloatArray {
        try {
            Log.d("CollaborativeFiltering", "Building user vector for: $userId")
            val responses = authManager.supabase.postgrest
                .from("user_responses")
                .select(Columns.raw("question_id, is_correct")) {
                    filter { eq("user_id", userId) }
                }
                .decodeList<UserResponseSimple>()
            Log.d("CollaborativeFiltering", "Found ${responses.size} responses")
            val ability = try {
                authManager.supabase.postgrest
                    .from("user_ability")
                    .select(Columns.raw("ability_level")) {
                        filter { eq("user_id", userId) }
                    }
                    .decodeSingleOrNull<UserAbilitySimple>()
            } catch (e: Exception) {
                Log.e("CollaborativeFiltering", "Error getting ability", e)
                null
            }
            val questionStats = responses.groupBy { it.questionId }
            val vector = FloatArray(VECTOR_SIZE) { 0f }
            var index = 0
            for ((_, questionResponses) in questionStats) {
                if (index < VECTOR_SIZE - 1) {
                    val correctCount = questionResponses.count { it.isCorrect }
                    val percentCorrect = if (questionResponses.isNotEmpty()) {
                        correctCount.toFloat() / questionResponses.size
                    } else 0f
                    vector[index] = percentCorrect
                    index++
                } else break
            }
            vector[VECTOR_SIZE - 1] = ability?.abilityLevel ?: 0f
            userVectors[userId] = vector
            saveUserVector(userId, vector)
            Log.d("CollaborativeFiltering", "User vector built successfully")
            return vector

        } catch (e: Exception) {
            Log.e("CollaborativeFiltering", "Error building user vector", e)
            return FloatArray(VECTOR_SIZE) { 0f }
        }
    }

    private suspend fun saveUserVector(userId: String, vector: FloatArray) {
        try {
            val vectorList = vector.toList()
            val existing = authManager.supabase.postgrest
                .from("user_vectors")
                .select(Columns.raw("user_id")) {
                    filter { eq("user_id", userId) }
                }
                .decodeList<UserVector>()

            if (existing.isEmpty()) {
                authManager.supabase.postgrest
                    .from("user_vectors")
                    .insert(UserVector(userId, vectorList))
            } else {
                authManager.supabase.postgrest
                    .from("user_vectors")
                    .update(UserVector(userId, vectorList)) {
                        filter { eq("user_id", userId) }
                    }
            }

            Log.d("CollaborativeFiltering", "User vector saved for: $userId")
        } catch (e: Exception) {
            Log.e("CollaborativeFiltering", "Error saving user vector", e)
            e.printStackTrace()
        }
    }
    private suspend fun loadUserVector(userId: String): FloatArray? {
        return try {
            val userVector = authManager.supabase.postgrest
                .from("user_vectors")
                .select(Columns.raw("*")) {
                    filter { eq("user_id", userId) }
                }
                .decodeSingleOrNull<UserVector>()

            userVector?.topicVector?.toFloatArray()?.also {
                userVectors[userId] = it
                Log.d("CollaborativeFiltering", "User vector loaded for: $userId, size: ${it.size}")
            }
        } catch (e: Exception) {
            Log.e("CollaborativeFiltering", "Error loading user vector", e)
            null
        }
    }
    fun cosineSimilarity(vector1: FloatArray, vector2: FloatArray): Float {
        if (vector1.size != vector2.size) return 0f

        var dotProduct = 0f
        var norm1 = 0f
        var norm2 = 0f

        for (i in vector1.indices) {
            dotProduct += vector1[i] * vector2[i]
            norm1 += vector1[i] * vector1[i]
            norm2 += vector2[i] * vector2[i]
        }

        if (norm1 == 0f || norm2 == 0f) return 0f

        return dotProduct / (sqrt(norm1) * sqrt(norm2))
    }
    suspend fun findSimilarUsers(userId: String): List<SimilarUser> {
        try {
            val currentVector = userVectors[userId] ?: loadUserVector(userId) ?: buildUserVector(userId)

            val allUsers = authManager.supabase.postgrest
                .from("user_vectors")
                .select(Columns.raw("*"))
                .decodeList<UserVector>()
                .filter { it.userId != userId }

            Log.d("CollaborativeFiltering", "Found ${allUsers.size} other users")

            val similarUsers = mutableListOf<SimilarUser>()

            for (other in allUsers) {
                val otherVector = other.topicVector.toFloatArray()
                val similarity = cosineSimilarity(currentVector, otherVector)

                Log.d("CollaborativeFiltering", "Сходство с ${other.userId}: $similarity")

                if (similarity > SIMILARITY_THRESHOLD) {
                    similarUsers.add(SimilarUser(other.userId, similarity))
                }
            }

            Log.d("CollaborativeFiltering", "Found ${similarUsers.size} similar users")
            return similarUsers.sortedByDescending { it.similarityScore }

        } catch (e: Exception) {
            Log.e("CollaborativeFiltering", "Error finding similar users", e)
            return emptyList()
        }
    }

    suspend fun getCollaborativeRecommendations(
        userId: String,
        currentTopicId: Int? = null,
        excludeQuestionIds: Set<Int> = emptySet()
    ): List<QuestionWithAnswers> {
        try {
            val similarUsers = findSimilarUsers(userId)
            if (similarUsers.isEmpty()) {
                Log.d("CollaborativeFiltering", "No similar users found")
                return emptyList()
            }

            val recommendedQuestions = mutableMapOf<Int, Float>()

            for (similar in similarUsers.take(5)) {
                val responses = authManager.supabase.postgrest
                    .from("user_responses")
                    .select(Columns.raw("question_id, is_correct")) {
                        filter { eq("user_id", similar.userId) }
                    }
                    .decodeList<UserResponseSimple>()

                for (response in responses) {
                    if (response.isCorrect && response.questionId !in excludeQuestionIds) {
                        val currentScore = recommendedQuestions[response.questionId] ?: 0f
                        recommendedQuestions[response.questionId] = currentScore + similar.similarityScore
                    }
                }
            }

            val topQuestionIds = recommendedQuestions
                .entries
                .sortedByDescending { it.value }
                .take(MAX_RECOMMENDATIONS)
                .map { it.key }

            if (topQuestionIds.isEmpty()) {
                Log.d("CollaborativeFiltering", "No recommended questions found")
                return emptyList()
            }

            Log.d("CollaborativeFiltering", "Top question IDs: $topQuestionIds")
            val questions = mutableListOf<QuestionWithAnswers>()
            for (qId in topQuestionIds) {
                try {
                    val question = authManager.supabase.postgrest
                        .from("question")
                        .select(Columns.raw("*, answers:answer(*)")) {
                            filter { eq("id", qId) }
                        }
                        .decodeSingleOrNull<QuestionWithAnswers>()
                    question?.let { questions.add(it) }
                } catch (e: Exception) {
                    Log.e("CollaborativeFiltering", "Error loading question $qId", e)
                }
            }

            return questions

        } catch (e: Exception) {
            Log.e("CollaborativeFiltering", "Error getting recommendations", e)
            return emptyList()
        }
    }
}