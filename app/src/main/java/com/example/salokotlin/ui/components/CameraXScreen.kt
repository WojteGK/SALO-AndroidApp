package com.example.salokotlin.ui.components

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
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import coil.compose.rememberAsyncImagePainter
import com.example.salokotlin.network.uploadPhoto
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.navigation.NavController
import com.example.salokotlin.utils.drawBoundingBoxesOnPhoto
import com.example.salokotlin.utils.parseBoundingBoxesFromResponse
import com.example.salokotlin.utils.saveBitmapToFile

@Composable
fun CameraXScreen(navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalContext.current as LifecycleOwner

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val imageCapture = remember { mutableStateOf<ImageCapture?>(null) }

    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    var capturedPhotoPath by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val scaffoldState = rememberScaffoldState()

    Scaffold(
        scaffoldState = scaffoldState,
        drawerContent = {
            MenuDrawer(navController = navController) // Include the MenuDrawer here
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
                Box(modifier = Modifier
                    .weight(1f)
                    .aspectRatio(4f / 3f)) {
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
                // Display Captured Photo
                Box(modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)) {
                    Image(
                        painter = rememberAsyncImagePainter(model = capturedPhotoPath),
                        contentDescription = "Captured Photo",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = {
                        coroutineScope.launch {
                            val photoUri = Uri.fromFile(File(capturedPhotoPath!!))
                            val serverResponse = uploadPhoto(context, photoUri)
                            val boundingBoxes = parseBoundingBoxesFromResponse(serverResponse)

                            val bitmapWithBoundingBoxes = drawBoundingBoxesOnPhoto(capturedPhotoPath!!, boundingBoxes)
                            val updatedPhotoPath = saveBitmapToFile(context, bitmapWithBoundingBoxes)

                            capturedPhotoPath = updatedPhotoPath
                        }
                    }) {
                        Text("Send Photo")
                    }

                    Button(onClick = {
                        capturedPhotoPath = null
                    }) {
                        Text("Retake Photo")
                    }
                }
            }
        }
    }
}