package com.example.statmaster

import com.example.statmaster.adaptive.SimilarUser
import org.junit.Assert.*
import org.junit.Test

class CollaborativeFilteringTest {

    @Test
    fun `cosine similarity returns 1 for identical vectors`() {
        val vector1 = floatArrayOf(0.8f, 0.2f, 0.5f)
        val vector2 = floatArrayOf(0.8f, 0.2f, 0.5f)

        val similarity = cosineSimilarity(vector1, vector2)

        assertEquals("Similarity should be 1.0", 1.0f, similarity, 0.01f)
    }

    @Test
    fun `cosine similarity returns 0 for orthogonal vectors`() {
        val vector1 = floatArrayOf(1.0f, 0.0f, 0.0f)
        val vector2 = floatArrayOf(0.0f, 1.0f, 0.0f)

        val similarity = cosineSimilarity(vector1, vector2)

        assertEquals("Similarity should be 0.0", 0.0f, similarity, 0.01f)
    }

    @Test
    fun `cosine similarity returns correct value for similar vectors`() {
        val vector1 = floatArrayOf(0.9f, 0.1f, 0.2f)
        val vector2 = floatArrayOf(0.8f, 0.2f, 0.3f)

        val similarity = cosineSimilarity(vector1, vector2)

        assertTrue("Similarity should be high", similarity > 0.9f)
        assertTrue("Similarity should be less than 1", similarity < 1.0f)
    }

    @Test
    fun `cosine similarity handles zero vectors`() {
        val vector1 = floatArrayOf(0.0f, 0.0f, 0.0f)
        val vector2 = floatArrayOf(0.8f, 0.2f, 0.5f)

        val similarity = cosineSimilarity(vector1, vector2)

        assertEquals("Similarity should be 0 for zero vector", 0.0f, similarity, 0.01f)
    }

    @Test
    fun `similar users sorted by similarity score`() {
        val users = listOf(
            SimilarUser("user1", 0.5f),
            SimilarUser("user2", 0.9f),
            SimilarUser("user3", 0.3f)
        )

        val sorted = users.sortedByDescending { it.similarityScore }

        assertEquals("Highest similarity should be first", "user2", sorted[0].userId)
        assertEquals("Lowest similarity should be last", "user3", sorted[2].userId)
    }

    private fun cosineSimilarity(vector1: FloatArray, vector2: FloatArray): Float {
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

        return dotProduct / (kotlin.math.sqrt(norm1) * kotlin.math.sqrt(norm2))
    }
}