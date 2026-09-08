package com.example.statmaster

import com.example.statmaster.terver.calculateScore
import org.junit.Assert.*
import org.junit.Test

class ScoreCalculatorTest {

    @Test
    fun `calculateScore returns correct for all correct answers`() {
        val questions = createQuestionsWithAnswers()
        val userAnswers = mapOf(
            1 to 101,
            2 to 202,
            3 to 303
        )

        val (correct, total) = calculateScore(questions, userAnswers)

        assertEquals("All answers should be correct", 3, correct)
        assertEquals("Total questions should be 3", 3, total)
    }

    @Test
    fun `calculateScore returns correct for mix of correct and incorrect`() {
        val questions = createQuestionsWithAnswers()
        val userAnswers = mapOf(
            1 to 102,  // incorrect
            2 to 202,  // correct
            3 to 304   // incorrect
        )

        val (correct, total) = calculateScore(questions, userAnswers)

        assertEquals("Only one answer should be correct", 1, correct)
        assertEquals("Total questions should be 3", 3, total)
    }

    @Test
    fun `calculateScore returns zero for all incorrect answers`() {
        val questions = createQuestionsWithAnswers()
        val userAnswers = mapOf(
            1 to 102,
            2 to 201,
            3 to 304
        )

        val (correct, total) = calculateScore(questions, userAnswers)

        assertEquals("No answers should be correct", 0, correct)
        assertEquals("Total questions should be 3", 3, total)
    }

    @Test
    fun `calculateScore handles unanswered questions`() {
        val questions = createQuestionsWithAnswers()
        val userAnswers = mapOf(
            1 to 101,
            2 to 202
        )

        val (correct, total) = calculateScore(questions, userAnswers)

        assertEquals("Only 2 questions answered", 2, correct)
        assertEquals("Total questions should be 3", 3, total)
    }

    private fun createQuestionsWithAnswers(): List<QuestionWithAnswers> {
        return listOf(
            QuestionWithAnswers(
                id = 1,
                testId = 1,
                questionText = "Question 1",
                orderNumber = 1,
                answers = listOf(
                    Answer(101, 1, "A", true, 1),
                    Answer(102, 1, "B", false, 2)
                )
            ),
            QuestionWithAnswers(
                id = 2,
                testId = 1,
                questionText = "Question 2",
                orderNumber = 2,
                answers = listOf(
                    Answer(201, 2, "A", false, 1),
                    Answer(202, 2, "B", true, 2)
                )
            ),
            QuestionWithAnswers(
                id = 3,
                testId = 1,
                questionText = "Question 3",
                orderNumber = 3,
                answers = listOf(
                    Answer(301, 3, "A", false, 1),
                    Answer(303, 3, "C", true, 3),
                    Answer(304, 3, "D", false, 4)
                )
            )
        )
    }
}