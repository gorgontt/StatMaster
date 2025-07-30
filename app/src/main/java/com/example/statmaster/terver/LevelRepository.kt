package com.example.statmaster.terver

import android.content.Context
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import com.example.statmaster.Answer
import com.example.statmaster.AuthManager
import com.example.statmaster.Level
import com.example.statmaster.LevelDocument
import com.example.statmaster.QuestionWithAnswers
import com.example.statmaster.Test
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order

class LevelRepository(private val authManager: AuthManager,  private val context: Context ) {

    suspend fun getAllLevels(): List<Level> {
        return try {
            authManager.supabase.postgrest["level"]
                .select {
                    order("order_number", Order.ASCENDING)
                }
                .decodeList<Level>()
        } catch (e: Exception) {
            Log.e("Supabase", "Error loading levels", e)
            emptyList()
        }
    }

    suspend fun getLevelDocument(levelId: Int): LevelDocument? {
        return try {
            authManager.supabase.postgrest
                .from("levels_documents?select=*&level_id=eq.$levelId")
                .select()
                .decodeSingleOrNull<LevelDocument>()
        } catch (e: Exception) {
            Log.e("Supabase", "Error loading document", e)
            null
        }
    }


    suspend fun getTestByLevelId(levelId: Int): Test? {
        return try {
            // Проверим существование уровня
            val levelExists = authManager.supabase.postgrest
                .from("level?id=eq.$levelId")
                .select()
                .decodeSingleOrNull<Level>() != null

            if (!levelExists) {
                Log.w("Supabase", "Level $levelId doesn't exist")
                return null
            }

            // Получаем тест
            authManager.supabase.postgrest
                .from("test?select=id,level_id,title,description&level_id=eq.$levelId")
                .select()
                .decodeSingleOrNull<Test>()
                .also { test ->
                    if (test == null) {
                        Log.w("Supabase", "No test found for level $levelId")
                    } else {
                        Log.d("Supabase", "Successfully loaded test: ${test.title}")
                    }
                }
        } catch (e: Exception) {
            Log.e("Supabase", "Failed to load test for level $levelId", e)
            null
        }
    }

    suspend fun debugAllTests(): List<Test> {
        return try {
            authManager.supabase.postgrest
                .from("test?select=id,level_id,title,description")
                .select()
                .decodeList<Test>()
                .also { tests ->
                    Log.d("Supabase", "All tests in DB: ${tests.joinToString()}")
                }
        } catch (e: Exception) {
            Log.e("Supabase", "Error loading all tests", e)
            emptyList()
        }
    }

    suspend fun getQuestionsWithAnswers(testId: Int): List<QuestionWithAnswers> {
        return try {
            // Получаем вопросы для теста
            val questions = authManager.supabase.postgrest
                .from("question?select=*&test_id=eq.$testId&order=order_number.asc")
                .select()
                .decodeList<QuestionWithAnswers>()
                .also {
                    Log.d("Supabase", "Loaded ${it.size} questions for test $testId")
                }

            // Для каждого вопроса получаем ответы
            questions.map { question ->
                val answers = authManager.supabase.postgrest
                    .from("answer?select=*&question_id=eq.${question.id}&order=order_number.asc")
                    .select()
                    .decodeList<Answer>()
                    .also {
                        Log.d("Supabase", "Loaded ${it.size} answers for question ${question.id}")
                    }

                question.copy(answers = answers)
            }
        } catch (e: Exception) {
            Log.e("Supabase", "Error loading questions with answers for test $testId", e)
            emptyList()
        }
    }

    fun markLevelAsCompleted(levelId: Int) {
        val sharedPref =
            authManager.context.getSharedPreferences("LevelProgress", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("level_$levelId", true)
            apply()
        }
    }


    suspend fun updateLevelCompletion(levelId: Int, isCompleted: Boolean): Boolean {
        return try {
            Log.d("LevelRepository", "Updating level $levelId to completed=$isCompleted")

            // 1. Обновляем в Supabase
            authManager.supabase.postgrest
                .from("level")
                .update(
                    mapOf("is_completed" to isCompleted)
                ) {
                    filter {
                        eq("id", levelId)
                    }
                }

            // 2. Обновляем локальное хранилище
            markLevelAsCompleted(levelId)

            // 3. Возвращаем успех без проверки (временно)
            Log.d("LevelRepository", "Level $levelId update request completed")
            true
        } catch (e: Exception) {
            Log.e("LevelRepository", "Error updating level", e)
            false
        }
    }

    // Добавим метод для получения одного уровня
    suspend fun getLevel(levelId: Int): Level? {
        return try {
            authManager.supabase.postgrest
                .from("level")
                .select {
                    filter {
                        eq("id", levelId)
                    }
                }
                .decodeSingle<Level>()
        } catch (e: Exception) {
            Log.e("LevelRepository", "Error getting level", e)
            null
        }
    }


    suspend fun getLevelById(levelId: Int): Level? {
        return try {
            // Проверяем сначала локальное хранилище
            val sharedPref = context.getSharedPreferences("LevelProgress", Context.MODE_PRIVATE)
            val isCompletedLocally = sharedPref.getBoolean("level_$levelId", false)

            // Получаем данные из Supabase
            val level = authManager.supabase.postgrest
                .from("level?id=eq.$levelId")
                .select()
                .decodeSingleOrNull<Level>()

            // Если есть локальное завершение, но в БД нет - обновляем БД
            if (isCompletedLocally && (level?.isCompleted != true)) {
                updateLevelCompletion(levelId, true)
            }

            level?.copy(isCompleted = level.isCompleted || isCompletedLocally)
        } catch (e: Exception) {
            Log.e("Supabase", "Error loading level", e)
            null
        }
    }


}

