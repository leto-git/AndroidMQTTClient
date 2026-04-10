package com.example.androidmqttclient.data

import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Formats a timestamp into a readable string using the provided pattern.
 *
 * @param timestamp The timestamp to format.
 * @param pattern The pattern to use for formatting.
 *
 * @return The formatted string.
 */
fun formatTimestamp(timestamp: Long, pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return sdf.format(timestamp)
}