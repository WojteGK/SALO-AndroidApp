package com.example.salokotlin.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import java.io.File

@Composable
fun HistoryScreen() {
    val context = LocalContext.current
    val photos = remember { getLast30Photos(context) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(photos) { photo ->
            Column(modifier = Modifier.padding(8.dp)) {
                Image(
                    painter = rememberAsyncImagePainter(photo),
                    contentDescription = "Photo",
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                )
                Text(photo.name, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

fun getLast30Photos(context: android.content.Context): List<File> {
    val folder = File(context.getExternalFilesDir("ProcessedPhotos"), "")
    if (!folder.exists()) return emptyList()

    return folder.listFiles()
        ?.sortedByDescending { it.lastModified() }
        ?.take(30)
        ?: emptyList()
}