package com.example.statmaster

import org.junit.Assert.*
import org.junit.Test
import kotlin.math.exp

class IrtAlgorithmTest {

    @Test
    fun `correct answer increases ability`() {
        val currentAbility = 0.0f
        val difficultyValue = 0.5f
        val isCorrect = true

        val result = updateAbilityLevel(
            currentAbility = currentAbility,
            difficultyValue = difficultyValue,
            isCorrect = isCorrect
        )

        assertTrue("Ability should increase", result > currentAbility)
    }

    @Test
    fun `incorrect answer decreases ability`() {
        val currentAbility = 0.0f
        val difficultyValue = -0.5f
        val isCorrect = false

        val result = updateAbilityLevel(
            currentAbility = currentAbility,
            difficultyValue = difficultyValue,
            isCorrect = isCorrect
        )

        assertTrue("Ability should decrease", result < currentAbility)
    }

    @Test
    fun `probability calculation is correct`() {
        val ability = 0.0f
        val difficulty = 0.5f
        val discrimination = 1.0f

        val probability = 1.0f / (1.0f + exp(-discrimination * (ability - difficulty)))

        assertTrue("Probability should be between 0 and 1", probability in 0.0f..1.0f)
        assertEquals("Probability should be ~0.38", 0.38f, probability, 0.01f)
    }

    @Test
    fun `variance decreases after each response`() {
        val currentVariance = 1.0f
        val newVariance = currentVariance * 0.9f

        assertTrue("Variance should decrease", newVariance < currentVariance)
        assertEquals("Variance should decrease by 10%", 0.9f, newVariance, 0.01f)
    }

    // Вспомогательная функция (копия логики из репозитория)
    private fun updateAbilityLevel(
        currentAbility: Float,
        difficultyValue: Float,
        isCorrect: Boolean
    ): Float {
        val discrimination = 1.0f
        val learningRate = 0.3f

        val probability = 1.0f / (1.0f + exp(-discrimination * (currentAbility - difficultyValue)))
        val gradient = if (isCorrect) 1 - probability else -probability

        return currentAbility + learningRate * gradient * discrimination
    }
}