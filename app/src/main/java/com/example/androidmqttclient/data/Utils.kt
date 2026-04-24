package com.example.androidmqttclient.data

import android.net.InetAddresses
import android.os.Build
import android.util.Patterns
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

/**
 * Checks if a protocol is valid.
 *
 * @param protocol The protocol string to check.
 *
 * @return True if the protocol is valid, false otherwise.
 */
fun isValidProtocolString(protocol: String): Boolean {
    // Get all protocols from the enum
    val supportedProtocols = TransportProtocol.entries.map { it.prefix }
    return protocol in supportedProtocols
}

/**
 * Checks if a server address is valid.
 *
 * @param address The server address to check.
 *
 * @return True if the server address is valid, false otherwise.
 */
fun isValidServerAddress(address: String): Boolean {
    if (address.isBlank()) return false

    // Allow localhost as a valid address
    if (address == "localhost") return true

    // Check if it's a numeric IP (v4 or v6)
    val isNumericIp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // Use the modern API 29+ way to clear the warning
        InetAddresses.isNumericAddress(address)
    } else {
        // Fallback for older Android versions
        Patterns.IP_ADDRESS.matcher(address).matches()
    }

    // Check if it's a valid Domain Name (e.g., broker.example.com)
    val isDomain = Patterns.DOMAIN_NAME.matcher(address).matches()

    return isNumericIp || isDomain
}

/**
 * Checks if a WebSocket path is valid.
 *
 * @param path The WebSocket path to check.
 *
 * @return True if the WebSocket path is valid, false otherwise.
 */
fun isValidWebSocketPath(path: String): Boolean {
    // A path can be empty (defaults to root) or must start with /
    if (path.isNotEmpty() && !path.startsWith("/")) return false

    return try {
        // We use a dummy host to validate the path structure
        val uri = java.net.URI("ws://localhost$path")
        // Ensure there is no query (?foo=bar) or fragment (#section)
        // as these are not typically used in MQTT WS paths
        uri.query == null && uri.fragment == null
    } catch (_: Exception) {
        false
    }
}

/**
 * Checks if a client ID is valid.
 *
 * @param clientId The client ID to check.
 *
 * @return True if the client ID is valid, false otherwise.
 */
fun isValidClientId(clientId: String): Boolean {
    val bytes = clientId.toByteArray(Charsets.UTF_8)
    if( bytes.isEmpty() || bytes.size > 65535 ) return false

    // NOTE: The Standard says that servers MUST accept alpha-numeric characters, but we
    // also allow - and _ as they are commonly supported by servers.
    val allowedChars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-_"
    return !clientId.any { it !in allowedChars }
}

/**
 * Checks if a connection is valid.
 *
 * @param connection The connection to check.
 *
 * @return True if the connection is valid, false otherwise.
 */
fun isValidConnection(connection: AMCServerConnection): Boolean {
    val isValid = isValidProtocolString(connection.protocol) &&
            isValidServerAddress(connection.serverAddress) &&
            connection.serverPort in 1..65535 &&
            isValidClientId(connection.clientID) &&
            isValidWebSocketPath(connection.webSocketPath)

    // If no topic is provided, ignore the Will check.
    val isWillValid = if (connection.willTopic.isNotEmpty()) {
        connection.willQos in 0..2 && isValidForPublishing(connection.willTopic)
    } else {
        true
    }

    return isValid && isWillValid
}

/**
 * Checks if a topic is valid.
 *
 * @param topic The topic to check.
 *
 * @return True if the topic is valid, false otherwise.
 */
fun isValidForSubscribing(topic: String): Boolean {
    // Topic can not be empty
    if( topic.isEmpty() ) return false
    // Topic can not contain null characters
    if( topic.contains("\u0000") ) return false
    // Topic can not be larger than 65535 bytes
    if( topic.toByteArray(Charsets.UTF_8).size > 65535 ) return false
    // Topic can not contain the '$' character
    if( topic.contains('$') ) return false

    // '#' wildcard character
    val hashCount = topic.count {it == '#'}
    // Topic can not contain more than one '#' wildcard character
    if( hashCount > 1 ) return false
    if( hashCount == 1 ) {
        // Topic must end with '#' if it contains one
        if( !topic.endsWith("#") ) return false
        // If '#' is not the only character, it must follow a '/' character
        // (e.g.  “sport/tennis/#” is valid, “sport/tennis#” is not valid)
        if( topic.length > 1 && topic[topic.length - 2] != '/' ) return false
    }

    // '+' wildcard character must occupy an entire level
    // (e.g. “sport+/player1” is not valid, "sport/+/player1 is valid)
    val parts = topic.split('/')
    for (part in parts) {
        if (part.contains('+') && part != "+") {
            return false
        }
    }

    return true
}

/**
 * Checks if a topic is valid for publishing.
 * A publishing topic can not contain '#' or '+' wildcard characters.
 *
 * @param topic The topic to check.
 *
 * @return True if the topic is valid for publishing, false otherwise.
 */
fun isValidForPublishing(topic: String): Boolean {
    return isValidForSubscribing(topic) && !topic.contains('#') && !topic.contains('+')
}