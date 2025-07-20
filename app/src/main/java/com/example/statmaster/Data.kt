package com.example.statmaster

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Level(
    val id: Int,
    val title: String,
    val description: String,
    @SerialName("order_number") val orderNumber: Int,
    @SerialName("is_completed") val isCompleted: Boolean // Используем camelCase в коде
)

@Serializable
data class LevelDocument(
    val id: Int,
    @SerialName("level_id") val levelId: Int,
    val content: String,
    @SerialName("image_url") val imageUrl: String?
)