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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.salokotlin.network.sendGroupAssignments
import com.example.salokotlin.network.uploadPhoto
import com.example.salokotlin.utils.correctBitmapOrientation
import com.example.salokotlin.utils.drawBoundingBoxesOnPhoto
import com.example.salokotlin.utils.parseGroupedBoundingBoxesFromResponse
import com.example.salokotlin.utils.saveBitmapToFile
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import com.example.salokotlin.R

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
    var namedGroups by remember { mutableStateOf<List<String>?>(null) }
    val coroutineScope = rememberCoroutineScope()
    var selectedGroup by remember { mutableStateOf<String?>(null) }
    var assignments by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var currentImagePath by remember { mutableStateOf<String?>(null) }
    var responseReceived by remember { mutableStateOf(false) }
    var expandedGroupDropdown by remember { mutableStateOf(false) }
    var expandedNamedGroupDropdown by remember { mutableStateOf(false) }

    val scaffoldState = rememberScaffoldState()

    Scaffold(
        scaffoldState = scaffoldState,
        drawerContent = {
            MenuDrawer(navController = navController)
        },
        topBar = {
            TopAppBar(
                title = { Text("SALO App") },
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

                Box(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center // Wyśrodkowanie poziome
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.salo_logo), // Replace with your avatar drawable
                        contentDescription = "App Logo",
                        modifier = Modifier
                            .size(120.dp)
                    )
                }

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
                    }, modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 25.dp))
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
                                        val correctedBitmap = correctBitmapOrientation(capturedPhotoPath!!)
                                        correctedPhotoPath = saveBitmapToFile(context, correctedBitmap)
                                        currentImagePath = correctedPhotoPath
                                    }
                                    Log.d("CameraXScreen", "Photo saved: $capturedPhotoPath")
                                }
                            }
                        )
                    },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(200.dp) // Węższa szerokość
                        .padding(bottom = 40.dp)
                        .clip(RoundedCornerShape(50.dp))
                ) {
                    Text("Capture Photo", fontWeight = FontWeight.Bold)
                }
            } else {
                // Display Image
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

                // Dropdowns and Assignments
                serverResponse?.let { response ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Group Selection Dropdown
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Select Group")
                            Box {
                                Button(onClick = { expandedGroupDropdown = true }) {
                                    Text(selectedGroup ?: "Select Group")
                                }
                                DropdownMenu(
                                    expanded = expandedGroupDropdown,
                                    onDismissRequest = { expandedGroupDropdown = false }
                                ) {
                                    response.keys.forEach { group ->
                                        DropdownMenuItem(onClick = {
                                            selectedGroup = group
                                            expandedGroupDropdown = false
                                            coroutineScope.launch {
                                                val boundingBoxes = response[group] ?: emptyList()
                                                val bitmapWithBoundingBoxes = drawBoundingBoxesOnPhoto(
                                                    correctedPhotoPath!!,
                                                    mapOf(group to boundingBoxes)
                                                )
                                                currentImagePath = saveBitmapToFile(context, bitmapWithBoundingBoxes)
                                            }
                                        }) {
                                            Text(group)
                                        }
                                    }
                                }
                            }
                        }

                        // Named Group Assignment Dropdown
                        selectedGroup?.let { group ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Assign to Named Group")
                                Box {
                                    Button(onClick = { expandedNamedGroupDropdown = true }) {
                                        Text(assignments[group] ?: "Select Named Group")
                                    }
                                    DropdownMenu(
                                        expanded = expandedNamedGroupDropdown,
                                        onDismissRequest = { expandedNamedGroupDropdown = false }
                                    ) {
                                        namedGroups?.forEach { namedGroup ->
                                            DropdownMenuItem(onClick = {
                                                assignments = assignments.toMutableMap().apply {
                                                    this[group] = namedGroup
                                                }
                                                expandedNamedGroupDropdown = false
                                            }) {
                                                Text(namedGroup)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Buttons for Sending Assignments or Retaking the Photo
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 80.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    if (!responseReceived) {
                        Button(onClick = {
                            coroutineScope.launch {
                                try {
                                    val photoUri = Uri.fromFile(File(correctedPhotoPath!!))
                                    val responseString = uploadPhoto(context, photoUri)
                                    val (detections, groups) = parseGroupedBoundingBoxesFromResponse(responseString)
                                    serverResponse = detections
                                    namedGroups = groups
                                    responseReceived = true
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }) {
                            Text("Send Photo")
                        }
                    } else {
                        Button(onClick = {
                            coroutineScope.launch {
                                try {
                                    sendGroupAssignments(context, assignments)
                                    Toast.makeText(context, "Assignments sent successfully!", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }) {
                            Text("Send Assignments")
                        }
                    }

                    Button(onClick = {
                        capturedPhotoPath = null
                        correctedPhotoPath = null
                        currentImagePath = null
                        serverResponse = null
                        namedGroups = null
                        assignments = emptyMap()
                        selectedGroup = null
                        responseReceived = false
                    }) {
                    },
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                    ) {
                        Text("Retake Photo")
                    }
                }
            }
        }
    }
}
