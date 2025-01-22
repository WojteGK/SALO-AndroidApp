package com.example.salokotlin.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.salokotlin.R // Update this to your actual resource import

@Composable
fun MenuDrawer(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // User Profile Section
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.salo_logo), // Replace with your avatar drawable
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(150.dp)
                    .align(Alignment.CenterVertically)
            )

        }

        // Separator
        Divider(color = Color.Gray, thickness = 1.dp, modifier = Modifier.padding(vertical = 16.dp))

        // History Option
        ClickableText(
            text = AnnotatedString("History"),
            style = TextStyle(fontSize = 24.sp,
                color = MaterialTheme.colors.onSurface),
            onClick = {
                navController.navigate("history")
            }
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Settings Option
        ClickableText(
            text = AnnotatedString("Settings"),
            style = TextStyle(fontSize = 24.sp,
                color = MaterialTheme.colors.onSurface),
            onClick = {
                navController.navigate("settings")
            }
        )
    }
}
