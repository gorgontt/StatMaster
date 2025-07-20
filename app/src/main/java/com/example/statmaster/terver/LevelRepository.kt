package com.example.statmaster.terver

import android.util.Log
import com.example.statmaster.AuthManager
import com.example.statmaster.Level
import com.example.statmaster.LevelDocument
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order

class LevelRepository(private val authManager: AuthManager) {

    suspend fun getAllLevels(): List<Level> {
        return try {
            authManager.supabase.postgrest["level"]  // Исправлено на "level"
                .select(
                    columns = Columns.list("id", "title", "description", "order_number", "is_completed")
                ) {
                    order("order_number", Order.ASCENDING)
                }
                .decodeList<Level>()
                .also {
                    Log.d("Supabase", "Loaded ${it.size} levels from 'level' table")
                }
        } catch (e: Exception) {
            Log.e("Supabase", "Error loading levels", e)
            emptyList()
        }
    }

    suspend fun getLevelDocument(levelId: Int): LevelDocument? {
        return try {
            authManager.supabase.postgrest
                .from("level_documents?select=*&level_id=eq.$levelId")
                .select()
                .decodeSingleOrNull<LevelDocument>()
        } catch (e: Exception) {
            Log.e("Supabase", "Error loading document", e)
            null
        }
    }
}