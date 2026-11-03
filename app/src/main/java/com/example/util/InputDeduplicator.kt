package com.example.util

/**
 * Filters out duplicate keystrokes injected by browser streaming emulators (e.g. WebRTC / Cuttlefish)
 * where a single physical key press can fire both a hardware KeyEvent and an IME commit,
 * resulting in characters duplicating immediately (e.g. typing '3' becomes '33').
 */
object InputDeduplicator {
    private var lastChar: Char? = null
    private var lastTime: Long = 0L

    fun filter(oldText: String, newText: String): String {
        // If deleting or shortening, never filter
        if (newText.length <= oldText.length) {
            lastChar = null
            lastTime = System.currentTimeMillis()
            return newText
        }

        val diff = newText.length - oldText.length
        val now = System.currentTimeMillis()

        // Case 1: Two identical characters inserted simultaneously in one event (e.g. "33" typed instead of "3")
        if (diff == 2) {
            var prefixLen = 0
            while (prefixLen < oldText.length && oldText[prefixLen] == newText[prefixLen]) {
                prefixLen++
            }
            if (prefixLen + 1 < newText.length) {
                val char1 = newText[prefixLen]
                val char2 = newText[prefixLen + 1]
                if (char1 == char2) {
                    lastChar = char1
                    lastTime = now
                    return newText.removeRange(prefixLen, prefixLen + 1)
                }
            }
        }

        // Case 2: One character inserted within < 90ms matching the previous character
        if (diff == 1) {
            var prefixLen = 0
            while (prefixLen < oldText.length && oldText[prefixLen] == newText[prefixLen]) {
                prefixLen++
            }
            if (prefixLen < newText.length) {
                val insertedChar = newText[prefixLen]
                val timeSinceLast = now - lastTime

                if (insertedChar == lastChar && timeSinceLast < 90L) {
                    // Duplicate keystroke received from streaming bridge within 90ms
                    return oldText
                }

                lastChar = insertedChar
                lastTime = now
                return newText
            }
        }

        // For multi-character paste (3+ chars), allow all
        lastChar = null
        lastTime = now
        return newText
    }
}
