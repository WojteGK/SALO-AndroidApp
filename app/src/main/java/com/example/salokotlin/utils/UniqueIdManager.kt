package com.example.salokotlin.utils

import android.content.Context
import java.util.UUID

object UniqueIdManager {
    private const val PREFS_NAME = "app_prefs"
    private const val KEY_UNIQUE_ID = "unique_id"

    fun getOrCreateUniqueId(context: Context): String {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        var uniqueId = sharedPreferences.getString(KEY_UNIQUE_ID, null)

        if (uniqueId == null) {
            // Generate a short unique ID (5 characters)
            uniqueId = generateShortUniqueId()
            sharedPreferences.edit().putString(KEY_UNIQUE_ID, uniqueId).apply()
        }

        return uniqueId
    }

    private fun generateShortUniqueId(): String {
        return UUID.randomUUID().toString()
            .replace("-", "") // Remove dashes
            .substring(0, 5) // Take the first 5 characters
    }
}
