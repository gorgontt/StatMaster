package com.example.statmaster

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Level(
    val id: Int,
    val title: String,
    val description: String,
    @SerialName("order_number") val orderNumber: Int,
    @SerialName("is_completed") val isCompleted: Boolean
)

@Serializable
data class LevelDocument(

    val id: Int,
    @SerialName("level_id") val levelId: Int,
    val content: String,
    @SerialName("image_url") val imageUrl: String?
)

data class ParsedDocument(
    val title: String,
    val content: List<ContentBlock>
)

sealed class ContentBlock {
    data class Paragraph(val text: String) : ContentBlock()
    data class Quote(val text: String) : ContentBlock()
    data class Image(val url: String) : ContentBlock()
}

