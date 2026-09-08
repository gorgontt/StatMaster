package com.example.statmaster

import com.example.statmaster.adaptive.MasteryLevel
import com.example.statmaster.adaptive.Recommendation
import com.example.statmaster.adaptive.SessionStats
import com.example.statmaster.adaptive.UserAbility
import org.junit.Assert.*
import org.junit.Test

class AdaptiveModelsTest {

    @Test
    fun `user ability initializes correctly`() {
        val ability = UserAbility(
            userId = "test_user",
            abilityLevel = 0.0f,
            abilityVariance = 1.0f,
            questionsAnswered = 0
        )

        assertEquals("test_user", ability.userId)
        assertEquals(0.0f, ability.abilityLevel, 0.01f)
        assertEquals(1.0f, ability.abilityVariance, 0.01f)
        assertEquals(0, ability.questionsAnswered)
    }

    @Test
    fun `session stats calculate correctly`() {
        val stats = SessionStats(
            totalQuestions = 10,
            correctAnswers = 7,
            accuracy = 0.7f,
            averageResponseTime = 5
        )

        assertEquals(10, stats.totalQuestions)
        assertEquals(7, stats.correctAnswers)
        assertEquals(0.7f, stats.accuracy, 0.01f)
        assertEquals(5, stats.averageResponseTime)
    }

    @Test
    fun `recommendation has difficulty level`() {
        val rec = Recommendation(
            text = "Test recommendation",
            difficulty = "medium"
        )

        assertEquals("Test recommendation", rec.text)
        assertEquals("medium", rec.difficulty)
    }

    @Test
    fun `mastery level enum values`() {
        val expectedValues = listOf(
            MasteryLevel.BEGINNER,
            MasteryLevel.INTERMEDIATE,
            MasteryLevel.EXPERT
        )

        val actualValues = MasteryLevel.values().toList()

        assertTrue(
            "MasteryLevel should contain all expected values",
            actualValues.containsAll(expectedValues)
        )
        assertEquals("MasteryLevel should have exactly 3 values", 3, actualValues.size)
    }
}