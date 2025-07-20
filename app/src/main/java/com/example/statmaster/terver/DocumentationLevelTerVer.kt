package com.example.statmaster.terver

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.statmaster.AuthManager
import com.example.statmaster.LevelDocument
import com.example.statmaster.R


@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun DocumentationLevelTerVer(navController: NavController, levelId: Int?) {

    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }


    val levelRepository = remember { LevelRepository(authManager) }
    var levelDocument by remember { mutableStateOf<LevelDocument?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(levelId) {
        if (levelId != null) {
            val doc = levelRepository.getLevelDocument(levelId)
            println("Loaded document for level $levelId: $doc") // Добавьте эту строку
            levelDocument = doc
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Документация уровня") },
                navigationIcon = {
                    IconButton({ navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.back_icon),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        content = {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (levelDocument != null) {
                LevelDocumentContent(levelDocument!!)
            } else {
                Text("Документация не найдена")
            }
        }
    )
}

@Composable
fun LevelDocumentContent(document: LevelDocument) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = document.content,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        document.imageUrl?.let { url ->
            AsyncImage(
                model = url,
                contentDescription = "Documentation image",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
        }
    }
}