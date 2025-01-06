package com.example.salokotlin.utils

import android.content.Context
import android.graphics.*
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Draw bounding boxes on the photo based on provided coordinates.
 *
 * @param photoPath Path to the photo file.
 * @param boundingBoxes List of bounding boxes to draw on the photo.
 * @return A Bitmap with bounding boxes drawn.
 */
fun drawBoundingBoxesOnPhoto(photoPath: String, boundingBoxes: List<Rect>): Bitmap {
    val originalBitmap = BitmapFactory.decodeFile(photoPath)
    val mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)

    val canvas = Canvas(mutableBitmap)
    val paint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 8f
    }

    for (box in boundingBoxes) {
        canvas.drawRect(box, paint)
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
 * Parse bounding boxes from the server response.
 *
 * @param response The server response as a JSON string.
 * @return A list of Rect objects representing bounding boxes.
 */
fun parseBoundingBoxesFromResponse(response: String): List<Rect> {
    val boundingBoxes = mutableListOf<Rect>()

    try {
        val jsonObject = JSONObject(response)
        val boxesArray = jsonObject.getJSONArray("bounding_boxes")

        for (i in 0 until boxesArray.length()) {
            val boxObject = boxesArray.getJSONObject(i)
            val x = boxObject.getInt("x")
            val y = boxObject.getInt("y")
            val width = boxObject.getInt("width")
            val height = boxObject.getInt("height")

            boundingBoxes.add(Rect(x, y, x + width, y + height))
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return boundingBoxes
}