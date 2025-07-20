package com.example.statmaster.terver

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.statmaster.Level
import com.example.statmaster.LevelsRepository
import com.example.statmaster.R
import com.example.statmaster.ui.theme.BackgroundColor
import com.example.statmaster.ui.theme.Black
import com.example.statmaster.ui.theme.DarkBlue
import com.example.statmaster.ui.theme.Transparent
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LevelsTerVer(navController: NavController){

    Scaffold(
        modifier = Modifier.background(BackgroundColor),
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Теория вероятностей", fontSize = 22.sp) },
                navigationIcon = {
                    IconButton({ }) {
                        Icon(
                            painter =  painterResource(id = R.drawable.back_icon),
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // IconButton({ }) { Icon(Icons.Filled.Info, contentDescription = "О приложении")}
                    //IconButton({ }) {Icon(Icons.Filled.Search, contentDescription = "Поиск")}
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundColor,
                    titleContentColor = DarkBlue,
                    navigationIconContentColor = DarkBlue,
                    actionIconContentColor = DarkBlue
                )
            )
        },

        content = {
           ContentTerVerLevels(navController)
        }

    )

}

@Composable
fun ContentTerVerLevels(navController: NavController) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var levels by remember { mutableStateOf<List<Level>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val repository = LevelsRepository(context)
                levels = repository.getLevels()
                isLoading = false
            } catch (e: Exception) {
                // Обработка ошибок
                isLoading = false
                // Можно показать Snackbar с ошибкой
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .background(BackgroundColor)
            .verticalScroll(rememberScrollState())
            .offset(0.dp, 100.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            levels.forEach { level ->
                LevelCard(
                    level = level,
                    onClick = {
                        navController.navigate("level_detail/${level.id}")
                    }
                )
            }
        }
    }
}

@Composable
fun LevelCard(level: Level, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp, horizontal = 30.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(40.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .background(BackgroundColor)
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Уровень ${level.level_number}", // Используем level_number вместо levelNumber
                    style = TextStyle(
                        color = Black,
                        fontSize = 20.sp,
                        fontFamily = FontFamily(Font(R.font.jura_semibold))
                    )
                )
                Text(
                    text = level.description,
                    style = TextStyle(
                        color = Black,
                        fontSize = 14.sp,
                        fontFamily = FontFamily(Font(R.font.jura))
                    )
                )
            }

            if (level.is_completed) { // Используем is_completed вместо isCompleted
                Image(
                    painter = painterResource(id = R.drawable.tick_icon),
                    contentDescription = "Completed"
                )
            }
        }
    }
}