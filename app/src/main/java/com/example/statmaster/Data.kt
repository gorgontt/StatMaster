package com.example.statmaster

import android.content.Context
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class Level(
    val id: Int,
    val level_number: Int,
    val title: String,
    val description: String,
    val is_completed: Boolean,
    val icon_url: String?,
    val category: String
)

class LevelsRepository(private val context: Context) {
    private val authManager = AuthManager(context)

    suspend fun getLevels(): List<Level> = withContext(Dispatchers.IO) {
        authManager.supabase.from("levels")
            .select()
            //.order("order_index", Order.ASCENDING) // Исправлено: убедитесь, что 'order_index' указан правильно
            .decodeList<Level>()
    }

    suspend fun markLevelCompleted(levelId: Int) = withContext(Dispatchers.IO) {
        authManager.supabase.from("levels")
            .update {
                //it.set("is_completed", true) // Исправлено: добавлен 'it' перед 'set'
            }
            //.eq("id", levelId) // Исправлено: 'eq' корректно используется здесь
            //.execute()
    }
}