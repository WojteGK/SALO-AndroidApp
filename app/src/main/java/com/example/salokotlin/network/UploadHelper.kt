package com.example.salokotlin.network

import android.content.Context
import android.net.Uri
import android.widget.Toast
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

suspend fun uploadPhoto(context: Context, imageUri: Uri): String {
    return try {
        val contentResolver = context.contentResolver
        val inputStream = contentResolver.openInputStream(imageUri)
        val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)
        tempFile.outputStream().use { outputStream ->
            inputStream?.copyTo(outputStream)
        }

        val requestBody = tempFile.asRequestBody("image/jpeg".toMediaType())
        val filePart = MultipartBody.Part.createFormData(
            "file", // Field name expected by the server
            tempFile.name,
            requestBody
        )

        val response = RetrofitClient.apiService.uploadImage(filePart)

        // Log headers and body for debugging
        val headers = response.headers().toMultimap()
        val responseBody = response.body()?.toString() ?: "No Response Body"
        val errorBody = response.errorBody()?.string() ?: "No Error Body"

        // Print response details in the log
        android.util.Log.d("UploadPhoto", "Response Headers: $headers")
        android.util.Log.d("UploadPhoto", "Response Body: $responseBody")
        android.util.Log.d("UploadPhoto", "Error Body: $errorBody")
        android.util.Log.d("UploadPhoto", "Response Code: ${response.code()}")

        // Build user-friendly message
        if (response.isSuccessful) {
            "Upload successful: ${response.message()}"
        } else {
            "Upload failed: ${response.code()} - $errorBody"
        }
    } catch (e: Exception) {
        android.util.Log.e("UploadPhoto", "Error: ${e.message}", e)
        "Error: ${e.message}"
    }
}
