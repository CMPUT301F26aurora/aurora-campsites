package com.example.auroracampsites.data.session

import android.content.Context
import android.provider.Settings

/**
 * Provides a consistent device-based user ID.
 */
object UserSession {

    fun getCurrentUserId(context: Context): String {
        val deviceId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )

        return requireNotNull(deviceId) {
            "Unable to retrieve device ID."
        }
    }
}