package com.example.lifeguide

import android.graphics.drawable.Drawable

/**
 * Represents information about an application, including its icon.
 */
data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: Drawable
)
