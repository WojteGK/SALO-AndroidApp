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

    private val defaultPhotoLimit = 30 // Default number of photos to retain
    private val sharedPreferencesName = "AppSettings"

    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
            val storageWriteGranted = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: false
            val storageReadGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: false

            if (cameraGranted && storageWriteGranted && storageReadGranted) {
                Toast.makeText(this, "Permissions granted!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permissions denied! The app may not function correctly.", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check and request permissions
        if (!arePermissionsGranted()) {
            requestPermissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            )
        }

        // Clear app cache and maintain photo history
        clearCache()
        deleteOldPhotos(getMaxPhotoLimit())

        // Launch Compose UI
        setContent {
            SaloKotlinTheme {
                AppNavigation(context = this)
            }
        }
    }

    /**
     * Check if all necessary permissions are granted.
     */
    private fun arePermissionsGranted(): Boolean {
        val cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val writePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val readPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)

        return cameraPermission == PackageManager.PERMISSION_GRANTED &&
                writePermission == PackageManager.PERMISSION_GRANTED &&
                readPermission == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Clear app's external cache directory.
     */
    private fun clearCache() {
        val cacheDir = externalCacheDir
        cacheDir?.listFiles()?.forEach { file ->
            if (file.isFile) file.delete()
        }
    }

    /**
     * Delete photos from the ProcessedPhotos directory, keeping only the most recent photos as per the user setting.
     */
    private fun deleteOldPhotos(maxPhotosToRetain: Int) {
        val folder = File(getExternalFilesDir("ProcessedPhotos"), "")
        if (!folder.exists()) return

        val files = folder.listFiles()
        files?.sortedByDescending { it.lastModified() }
            ?.drop(maxPhotosToRetain) // Keep the user-specified number of photos
            ?.forEach { it.delete() }
    }

    /**
     * Save the maximum number of photos to retain in history.
     */
    fun saveMaxPhotoLimit(maxPhotos: Int) {
        val sharedPreferences = getSharedPreferences(sharedPreferencesName, MODE_PRIVATE)
        sharedPreferences.edit().putInt("maxPhotosToRetain", maxPhotos).apply()
    }

    /**
     * Retrieve the maximum number of photos to retain in history.
     */
    fun getMaxPhotoLimit(): Int {
        val sharedPreferences = getSharedPreferences(sharedPreferencesName, MODE_PRIVATE)
        return sharedPreferences.getInt("maxPhotosToRetain", defaultPhotoLimit)
    }
}