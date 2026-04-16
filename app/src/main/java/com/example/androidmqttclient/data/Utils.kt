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

/**
 * Checks if a MQTT topic matches a given pattern.
 *
 * @param topic The topic to check.
 * @param pattern The pattern to match against.
 *
 * @return True if the topic matches the pattern, false otherwise.
 */
fun topicMatchesPattern(topic: String, pattern: String): Boolean {
    val topicParts = topic.split("/")
    val patternParts = pattern.split("/")

    // Iterate over pattern parts
    for( i in patternParts.indices ) {
        val patternPart = patternParts[i]

        if( i >= topicParts.size ) {
            // Only valid if pattern is "#"
            return patternPart == "#"
        } else if( patternPart == "#" ) {
            // Rest of the pattern can be anything
            return true
        } else if( patternPart == "+" ) {
            // '+' matches exactly one level, continue to next level
            continue
        } else if( patternPart != topicParts[i] ) {
            // Pattern and topic don't match
            return false
        }
    }
    // Finished pattern, topic must also have ended
    return topicParts.size == patternParts.size
}