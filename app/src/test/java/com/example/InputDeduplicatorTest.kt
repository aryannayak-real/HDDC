package com.example

import com.example.util.InputDeduplicator
import org.junit.Assert.assertEquals
import org.junit.Test

class InputDeduplicatorTest {

    @Test
    fun testSingleCharacterEntry() {
        val result = InputDeduplicator.filter("", "a")
        assertEquals("a", result)
    }

    @Test
    fun testDoubleCharacterInSingleEventDeduplication() {
        // When typing '3', emulator emits "33" in one event
        val result = InputDeduplicator.filter("", "33")
        assertEquals("3", result)
    }

    @Test
    fun testRapidDuplicateKeystrokeFiltered() {
        // Keystroke 1: "9"
        val first = InputDeduplicator.filter("", "9")
        assertEquals("9", first)

        // Rapid duplicate keystroke (< 90ms) from synthetic event
        val second = InputDeduplicator.filter("9", "99")
        assertEquals("9", second)
    }

    @Test
    fun testIntentionalDoubleTypingAfterDelay() {
        // Keystroke 1: "o"
        val first = InputDeduplicator.filter("", "o")
        assertEquals("o", first)

        // Human wait > 100ms
        Thread.sleep(120)

        // Keystroke 2: "oo"
        val second = InputDeduplicator.filter("o", "oo")
        assertEquals("oo", second)
    }

    @Test
    fun testDeletionAndBackspace() {
        val backspace = InputDeduplicator.filter("abc", "ab")
        assertEquals("ab", backspace)

        val clearAll = InputDeduplicator.filter("ab", "")
        assertEquals("", clearAll)
    }

    @Test
    fun testPasteMultiCharacter() {
        val pasted = InputDeduplicator.filter("", "#2PP")
        assertEquals("#2PP", pasted)
    }
}
