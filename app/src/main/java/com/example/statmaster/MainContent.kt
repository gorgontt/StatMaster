package com.example.statmaster

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.statmaster.ui.theme.BackgroundColor
import com.example.statmaster.ui.theme.Black
import com.example.statmaster.ui.theme.DarkBlue
import com.example.statmaster.ui.theme.Transparent

@Composable
fun MainContent(navController: NavController){

    Column (modifier = Modifier.fillMaxSize().background(BackgroundColor)){

        Text(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 16.dp),
            text = "Библиотека",
            style = TextStyle(
                color = DarkBlue,
                fontSize = 30.sp,
                fontFamily = FontFamily(Font(R.font.jura_semibold))
            )
        )

        Card(
            modifier = Modifier.fillMaxWidth()
                .align(alignment = Alignment.CenterHorizontally)
                .background(Transparent)
                .shadow(
                    elevation = 4.dp,
                    ambientColor = Color.Black,
                    spotColor = Color.Black,
                    shape = RoundedCornerShape(9.dp)
                )

                .clickable {
                    //navController.navigate("players_list/компания")
                },
            shape = RoundedCornerShape(9.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 9.dp)
        ) {


            Text(
                modifier = Modifier.padding(top = 10.dp, bottom = 10.dp),
                text = "Теория вероятностей",
                style = TextStyle(
                    color = Black, fontSize = 20.sp, fontFamily = FontFamily(
                        Font(R.font.jura)
                    )
                )
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth()
                .align(alignment = Alignment.CenterHorizontally)
                .background(Transparent)
                .shadow(
                    elevation = 4.dp,
                    ambientColor = Color.Black,
                    spotColor = Color.Black,
                    shape = RoundedCornerShape(9.dp)
                )

                .clickable {
                    //navController.navigate("players_list/компания")
                },
            shape = RoundedCornerShape(9.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {


            Text(
                modifier = Modifier.padding(top = 10.dp, bottom = 10.dp),
                text = "Статистика",
                style = TextStyle(
                    color = Black, fontSize = 20.sp, fontFamily = FontFamily(
                        Font(R.font.jura)
                    )
                )
            )
        }



    }

}
