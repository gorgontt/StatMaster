package com.example.statmaster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.statmaster.ui.theme.StatMasterTheme
import io.github.jan.supabase.annotations.SupabaseInternal

class MainActivity : ComponentActivity() {
    @OptIn(SupabaseInternal::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StatMasterTheme {
                Navigation()
            }
        }
    }
}


