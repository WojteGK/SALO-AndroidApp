package com.example.salokotlin.utils

import android.content.Context
import android.graphics.*
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface


/**
 * Draw bounding boxes on the photo based on provided coordinates.
 *
 * @param photoPath Path to the photo file.
 * @param boundingBoxes List of bounding boxes to draw on the photo.
 * @return A Bitmap with bounding boxes drawn.
 */
fun drawBoundingBoxesOnPhoto(
    imagePath: String,
    boundingBoxes: Map<String, List<Rect>>,
    selectedGroup: String? = null
): Bitmap {
    Log.d("PhotoUtils", "Drawing boxes on $imagePath with $boundingBoxes")
    val originalBitmap = BitmapFactory.decodeFile(imagePath)
    val scaledBitmap = scaleBitmapToFitBoundingBox(originalBitmap)

    val mutableBitmap = scaledBitmap.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(mutableBitmap)
    val paint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.RED
        strokeWidth = 5f
    }

    boundingBoxes.forEach { (group, boxes) ->
        if (selectedGroup == null || selectedGroup == group) {
            boxes.forEach { rect ->
                canvas.drawRect(rect, paint)
            }
        }
    }

    return mutableBitmap
}

/**
 * Save a Bitmap to a file.
 *
 * @param context The context for accessing file storage.
 * @param bitmap The Bitmap to save.
 * @return The path of the saved file.
 */
fun saveBitmapToFile(context: Context, bitmap: Bitmap): String {
    val folder = File(context.getExternalFilesDir("ProcessedPhotos"), "")
    if (!folder.exists()) folder.mkdirs()

    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())
    val file = File(folder, "${timestamp}_photo.jpg")

    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
    }

    return file.absolutePath
}


/**
 * Parse grouped bounding boxes from the server response.
 *
 * @param response The server response as a JSON string.
 * @return A map of group names to lists of Rect objects representing bounding boxes.
 */
fun parseGroupedBoundingBoxesFromResponse(response: String): Pair<Map<String, List<Rect>>, List<String>> {
    try {
        val jsonObject = JSONObject(response)

        // Parse detections
        val detections = jsonObject.getJSONObject("detections")
        val boundingBoxes = mutableMapOf<String, List<Rect>>()
        for (groupKey in detections.keys()) {
            val groupArray = detections.getJSONArray(groupKey)
            val groupRects = mutableListOf<Rect>()
            for (i in 0 until groupArray.length()) {
                val box = groupArray.getJSONObject(i)
                val rect = Rect(box.getInt("x1"), box.getInt("y1"), box.getInt("x2"), box.getInt("y2"))
                groupRects.add(rect)
            }
            boundingBoxes[groupKey] = groupRects
        }

        // Parse groups
        val groupNames = mutableListOf<String>()
        val groupsArray = jsonObject.getJSONArray("groups")
        for (i in 0 until groupsArray.length()) {
            groupNames.add(groupsArray.getString(i))
        }

        return Pair(boundingBoxes, groupNames)
    } catch (e: Exception) {
        throw IllegalArgumentException("Error parsing response: ${e.message}")
    }
}


fun scaleBitmapToFitBoundingBox(bitmap: Bitmap, maxDimension: Int = 450): Bitmap {
    val width = bitmap.width
    val height = bitmap.height
    val scalingFactor = minOf(maxDimension.toFloat() / width, maxDimension.toFloat() / height)

    val scaledWidth = (width * scalingFactor).toInt()
    val scaledHeight = (height * scalingFactor).toInt()
    return Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)
}

fun correctBitmapOrientation(filePath: String): Bitmap {
    val bitmap = BitmapFactory.decodeFile(filePath)

    // Read EXIF metadata to determine the orientation
    val exif = ExifInterface(filePath)
    val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

    val matrix = Matrix()
    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
    }

    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}