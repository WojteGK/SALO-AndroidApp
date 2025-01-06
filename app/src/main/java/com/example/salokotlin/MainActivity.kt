package com.example.salokotlin

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat
import com.example.salokotlin.navigation.AppNavigation
import com.example.salokotlin.ui.components.CameraXScreen
import com.example.salokotlin.ui.theme.SaloKotlinTheme
import java.io.File

class MainActivity : ComponentActivity() {

    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
            val storageWriteGranted = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: false
            val storageReadGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: false

            if (cameraGranted && storageWriteGranted && storageReadGranted) {
                Toast.makeText(this, "Permissions granted!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permissions denied!", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!arePermissionsGranted()) {
            requestPermissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            )
        }

        clearCache()
        deleteOldPhotos()

        setContent {
            SaloKotlinTheme {
                AppNavigation()
            }
        }
    }

    private fun arePermissionsGranted(): Boolean {
        val cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val writePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val readPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)

        return cameraPermission == PackageManager.PERMISSION_GRANTED &&
                writePermission == PackageManager.PERMISSION_GRANTED &&
                readPermission == PackageManager.PERMISSION_GRANTED
    }

    private fun clearCache() {
        val cacheDir = externalCacheDir
        cacheDir?.listFiles()?.forEach { it.delete() }
    }
    private fun deleteOldPhotos() {
        val folder = File(getExternalFilesDir("ProcessedPhotos"), "")
        if (!folder.exists()) return

        val files = folder.listFiles()
        files?.sortedByDescending { it.lastModified() }
            ?.drop(30) // Keep the last 30 photos
            ?.forEach { it.delete() }
    }
}
