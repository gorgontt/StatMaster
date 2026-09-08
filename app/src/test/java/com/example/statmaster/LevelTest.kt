package com.example.statmaster

import org.junit.Assert.*
import org.junit.Test

class LevelTest {

    @Test
    fun `level has correct properties`() {
        val level = Level(
            id = 1,
            title = "Test Level",
            description = "Test Description",
            orderNumber = 1,
            isCompleted = false
        )

        assertEquals(1, level.id)
        assertEquals("Test Level", level.title)
        assertEquals("Test Description", level.description)
        assertEquals(1, level.orderNumber)
        assertFalse(level.isCompleted)
    }

    @Test
    fun `level can be completed`() {
        val level = Level(
            id = 1,
            title = "Test Level",
            description = "Test Description",
            orderNumber = 1,
            isCompleted = true
        )

        assertTrue(level.isCompleted)
    }
}