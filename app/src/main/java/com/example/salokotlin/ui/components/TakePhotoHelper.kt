package com.example.salokotlin.ui.components

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import android.os.Environment


object TakePhotoHelper {
    fun createPhotoIntent(context: Context): Pair<Intent, Uri>? {
        // Create a directory for the app's images
        val imageDir = File(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "AppImages" // Folder name
        )
        if (!imageDir.exists()) {
            imageDir.mkdirs() // Create the folder if it doesn't exist
        }

        // Create a unique file for the photo
        val photoFile = File(imageDir, "photo_${System.currentTimeMillis()}.jpg")
        val photoUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            photoFile
        )

        android.util.Log.d("TakePhotoHelper", "Photo URI: $photoUri")

        // Create the camera intent
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // Grant URI permissions to all camera apps
        val resolveInfoList = context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        for (resolveInfo in resolveInfoList) {
            val packageName = resolveInfo.activityInfo.packageName
            context.grantUriPermission(
                packageName,
                photoUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }

        return Pair(intent, photoUri)
    }
}