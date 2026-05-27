package com.example.statmaster.adaptive

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class UserVector(
    @SerialName("user_id") val userId: String,
    @SerialName("topic_vector") val topicVector: List<Float>
)

@Serializable
data class SimilarUser(
    @SerialName("user_id") val userId: String,
    @SerialName("similarity_score") val similarityScore: Float
)

@Serializable
data class UserResponseSimple(
    @SerialName("question_id") val questionId: Int,
    @SerialName("is_correct") val isCorrect: Boolean
)

@Serializable
data class UserAbilitySimple(
    @SerialName("ability_level") val abilityLevel: Float
)


