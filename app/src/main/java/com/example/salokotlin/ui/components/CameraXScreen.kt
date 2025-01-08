package com.example.salokotlin.ui.components

import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.salokotlin.network.uploadPhoto
import com.example.salokotlin.utils.correctBitmapOrientation
import com.example.salokotlin.utils.drawBoundingBoxesOnPhoto
import com.example.salokotlin.utils.parseGroupedBoundingBoxesFromResponse
import com.example.salokotlin.utils.saveBitmapToFile
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun CameraXScreen(navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalContext.current as LifecycleOwner

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val imageCapture = remember { mutableStateOf<ImageCapture?>(null) }

    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    var capturedPhotoPath by remember { mutableStateOf<String?>(null) }
    var correctedPhotoPath by remember { mutableStateOf<String?>(null) }
    var serverResponse by remember { mutableStateOf<Map<String, List<Rect>>?>(null) }
    val coroutineScope = rememberCoroutineScope()
    var selectedGroup by remember { mutableStateOf<String?>(null) }

    var currentImagePath by remember { mutableStateOf<String?>(null) } // For tracking the current image path
    var expanded by remember { mutableStateOf(false) } // For DropdownMenu state

    val scaffoldState = rememberScaffoldState()

    Scaffold(
        scaffoldState = scaffoldState,
        drawerContent = {
            MenuDrawer(navController = navController)
        },
        topBar = {
            TopAppBar(
                title = { Text("Camera App") },
                navigationIcon = {
                    IconButton(onClick = {
                        coroutineScope.launch {
                            scaffoldState.drawerState.open()
                        }
                    }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            if (capturedPhotoPath == null) {
                // Camera Preview
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(4f / 3f)
                ) {
                    AndroidView(factory = { ctx ->
                        val previewView = PreviewView(ctx)

                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()

                            val preview = androidx.camera.core.Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                            val imageCaptureBuilder = ImageCapture.Builder()
                            imageCapture.value = imageCaptureBuilder.build()

                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    imageCapture.value
                                )
                            } catch (e: Exception) {
                                Log.e("CameraXScreen", "Use case binding failed", e)
                            }
                        }, ContextCompat.getMainExecutor(context))

                        previewView
                    }, modifier = Modifier.fillMaxSize())
                }

                // Capture Photo Button
                Button(
                    onClick = {
                        val photoFile = File(
                            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                            "photo_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())}.jpg"
                        )

                        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                        imageCapture.value?.takePicture(
                            outputOptions,
                            cameraExecutor,
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onError(exception: ImageCaptureException) {
                                    Log.e("CameraXScreen", "Photo capture failed: ${exception.message}", exception)
                                }

                                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                    capturedPhotoPath = photoFile.absolutePath
                                    coroutineScope.launch {
                                        // Correct the photo's orientation and scale it
                                        val correctedBitmap = correctBitmapOrientation(capturedPhotoPath!!)
                                        correctedPhotoPath = saveBitmapToFile(context, correctedBitmap) // Save the corrected photo
                                        currentImagePath = correctedPhotoPath // Set the current image path
                                    }
                                    Log.d("CameraXScreen", "Photo saved: $capturedPhotoPath")
                                }
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Text("Capture Photo")
                }
            } else {
                // Display captured photo
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    currentImagePath?.let {
                        Image(
                            painter = rememberAsyncImagePainter(it),
                            contentDescription = "Captured Photo",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Display object count for selected group
                selectedGroup?.let { group ->
                    val objectCount = serverResponse?.get(group)?.size ?: 0
                    Text(
                        text = "Group $group: $objectCount objects",
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        color = MaterialTheme.colors.primary
                    )
                }

                // Dropdown for selecting group
                if (serverResponse != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("Select Group: ", modifier = Modifier.padding(end = 8.dp))
                        Box {
                            Button(onClick = { expanded = true }) {
                                Text(selectedGroup ?: "Select Group")
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                serverResponse!!.keys.forEach { group ->
                                    DropdownMenuItem(onClick = {
                                        selectedGroup = group
                                        expanded = false
                                        coroutineScope.launch {
                                            try {
                                                // Always start from the corrected photo
                                                val baseImagePath = correctedPhotoPath!!
                                                val boundingBoxes = serverResponse!![group] ?: emptyList()

                                                // Draw bounding boxes for the selected group only
                                                val bitmapWithBoundingBoxes = drawBoundingBoxesOnPhoto(
                                                    baseImagePath, // Reset to clean, corrected photo
                                                    mapOf(group to boundingBoxes) // Draw only the selected group's bounding boxes
                                                )

                                                // Save the updated image with bounding boxes
                                                val updatedPhotoPath = saveBitmapToFile(context, bitmapWithBoundingBoxes)
                                                currentImagePath = updatedPhotoPath // Update the image path for display
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                                Log.e("CameraXScreen", "Error updating bounding boxes", e)
                                            }
                                        }
                                    }) {
                                        Text(group)
                                    }
                                }
                            }
                        }
                    }
                }

                // Buttons for sending or retaking the photo
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = {
                        coroutineScope.launch {
                            try {
                                val photoUri = Uri.fromFile(File(correctedPhotoPath!!))
                                val responseString = uploadPhoto(context, photoUri)

                                // Parse and store server response for bounding boxes
                                serverResponse = parseGroupedBoundingBoxesFromResponse(responseString)

                                // Do not draw any bounding boxes yet
                                currentImagePath = correctedPhotoPath
                                selectedGroup = null
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                Log.e("CameraXScreen", "Error during photo upload or processing", e)
                            }
                        }
                    }) {
                        Text("Send Photo")
                    }

                    Button(onClick = {
                        capturedPhotoPath = null
                        correctedPhotoPath = null
                        currentImagePath = null
                        serverResponse = null
                        selectedGroup = null
                    }) {
                        Text("Retake Photo")
                    }
                }
            }
        }
    }
}
