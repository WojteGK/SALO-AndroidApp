package com.example.salokotlin.network

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.example.salokotlin.utils.UniqueIdManager
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

import android.media.ExifInterface
import android.graphics.Matrix

suspend fun uploadPhoto(context: Context, imageUri: Uri): String {
    return try {
        val uniqueId = UniqueIdManager.getOrCreateUniqueId(context)

        // Convert the image URI to a Bitmap
        val contentResolver = context.contentResolver
        val inputStream = contentResolver.openInputStream(imageUri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        // Correct the orientation using EXIF data
        val exif = ExifInterface(contentResolver.openInputStream(imageUri)!!)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        val rotatedBitmap = correctBitmapOrientation(originalBitmap, orientation)

        // Rescale the image to fit within a 450x450 bounding box while maintaining aspect ratio
        val maxDimension = 450
        val width = rotatedBitmap.width
        val height = rotatedBitmap.height
        val scalingFactor = minOf(maxDimension.toFloat() / width, maxDimension.toFloat() / height)

        val scaledWidth = (width * scalingFactor).toInt()
        val scaledHeight = (height * scalingFactor).toInt()
        val resizedBitmap = Bitmap.createScaledBitmap(rotatedBitmap, scaledWidth, scaledHeight, true)

        // Encode the Bitmap to a ByteArray
        val byteArrayOutputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream) // Use PNG to avoid compression
        val byteArray = byteArrayOutputStream.toByteArray()

        // Create a RequestBody from the byte array
        val requestBody = byteArray.toRequestBody("image/png".toMediaType())
        val filePart = MultipartBody.Part.createFormData(
            "file", // Field name expected by the server
            "photo.png", // File name sent to the server
            requestBody
        )

        // Add the unique ID as a header
        val headers = mapOf("X-Device-ID" to uniqueId)

        // Make the network request
        val response = RetrofitClient.apiService.uploadImageWithHeaders(filePart, headers)

        Log.d("UploadPhoto", "Response Code: ${response.code()}")
        Log.d("UploadPhoto", "Response Message: ${response.message()}")
        Log.d("UploadPhoto", "Response Headers: ${response.headers().toMultimap()}")

        val responseBody = response.body()?.string() ?: ""
        Log.d("UploadPhoto", "Response Body: $responseBody")

        if (responseBody.isEmpty()) {
            throw IllegalArgumentException("Empty response body")
        }

        if (!response.isSuccessful) {
            throw IllegalArgumentException("Upload failed: ${response.code()} - ${response.errorBody()?.string()}")
        }

        return responseBody
    } catch (e: Exception) {
        Log.e("UploadPhoto", "Error: ${e.message}", e)
        throw e
    }
}

/**
 * Correct the orientation of a Bitmap using EXIF metadata.
 */
fun correctBitmapOrientation(bitmap: Bitmap, orientation: Int): Bitmap {
    val matrix = Matrix()
    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        // Add other EXIF orientations if necessary
        else -> return bitmap // No rotation needed
    }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

