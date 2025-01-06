package com.example.salokotlin.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.salokotlin.ui.components.CameraXScreen
import com.example.salokotlin.ui.components.HistoryScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "camera") {
        composable("camera") { CameraXScreen(navController) }
        composable("history") { HistoryScreen() }
    }
}