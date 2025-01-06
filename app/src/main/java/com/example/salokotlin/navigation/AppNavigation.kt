package com.example.salokotlin.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.salokotlin.ui.components.CameraXScreen
import com.example.salokotlin.ui.components.HistoryScreen
import com.example.salokotlin.ui.components.SettingsScreen

@Composable
fun AppNavigation(context: Context) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "camera") {
        composable("camera") { CameraXScreen(navController) }
        composable("history") { HistoryScreen(navController) }
        composable("settings") { SettingsScreen(context, navController) } // Pass context to the settings screen
    }
}