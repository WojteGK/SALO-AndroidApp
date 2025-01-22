package com.example.salokotlin.ui.components

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import java.io.File

@Composable
fun HistoryScreen(navController: NavController) {
    val context = LocalContext.current
    val photos = remember { getLastPhotos(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(photos) { photo ->
                Image(
                    painter = rememberAsyncImagePainter(photo),
                    contentDescription = "Photo",
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
        }
    }
}

/**
 * Fetches the last photos from the ProcessedPhotos folder, limited by the user-specified number.
 */
fun getLastPhotos(context: Context): List<File> {
    val sharedPreferences = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
    val maxPhotosToRetain = sharedPreferences.getInt("maxPhotosToRetain", 30) // Default to 30

    val folder = File(context.getExternalFilesDir("ProcessedPhotos"), "")
    if (!folder.exists()) return emptyList()

    return folder.listFiles()
        ?.sortedByDescending { it.lastModified() }
        ?.take(maxPhotosToRetain)
        ?: emptyList()
}