package com.example.salokotlin.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.salokotlin.MainActivity
import com.example.salokotlin.utils.UniqueIdManager
import java.io.File

@Composable
fun SettingsScreen(context: Context, navController: NavController) {
    val mainActivity = context as MainActivity

    var maxPhotosToSave by remember { mutableStateOf(mainActivity.getMaxPhotoLimit()) }

    // Fetch unique ID using UniqueIdManager
    val uniqueId = remember { UniqueIdManager.getOrCreateUniqueId(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            // Display Unique ID
            Text("Device Unique ID:", fontSize = 18.sp, modifier = Modifier.padding(bottom = 8.dp))
            Text(uniqueId, fontSize = 16.sp, modifier = Modifier.padding(bottom = 16.dp))

            // Option to set how many photos to save
            Text("Number of Photos to Keep in History:", fontSize = 18.sp, modifier = Modifier.padding(bottom = 8.dp))

            // TextField to input the number of photos
            TextField(
                value = maxPhotosToSave.toString(),
                onValueChange = {
                    maxPhotosToSave = it.toIntOrNull() ?: mainActivity.getMaxPhotoLimit()
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    mainActivity.saveMaxPhotoLimit(maxPhotosToSave)
                    Toast.makeText(context, "Max photos to save updated to $maxPhotosToSave", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .padding(top = 40.dp)
                    .align(Alignment.CenterHorizontally)
                    .width(200.dp)
                    .clip(RoundedCornerShape(50.dp))
            ) {
                Text("Save Setting")
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Button to clear history
            Button(
                onClick = {
                    clearHistory(context)
                    Toast.makeText(context, "History cleared!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(200.dp)
                    .clip(RoundedCornerShape(50.dp))
            ) {
                Text("Clear History")
            }
        }
    }
}


/**
 * Clears all saved photos in the history.
 */
fun clearHistory(context: Context) {
    val folder = File(context.getExternalFilesDir("ProcessedPhotos"), "")
    if (folder.exists()) {
        folder.listFiles()?.forEach { it.delete() }
    }
}
