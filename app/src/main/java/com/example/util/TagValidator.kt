package com.example.util

import java.net.URLEncoder

object TagValidator {
    private val VALID_TAG_REGEX = Regex("^#[0289PYLQGRJCUV]+$")

    /**
     * Normalizes a tag string by removing whitespace, converting to uppercase,
     * and ensuring a single leading '#' symbol.
     */
    fun normalize(rawTag: String): String {
        val trimmed = rawTag.trim().uppercase().replace(" ", "").replace("O", "0")
        return if (trimmed.startsWith("#")) trimmed else "#$trimmed"
    }

    /**
     * Validates whether a tag adheres to Supercell's tag alphabet and format.
     */
    fun isValid(tag: String): Boolean {
        val normalized = normalize(tag)
        return normalized.length in 4..15 && VALID_TAG_REGEX.matches(normalized)
    }

    /**
     * URL-encodes the tag for REST endpoints, converting '#' to '%23'.
     */
    fun urlEncode(tag: String): String {
        val normalized = normalize(tag)
        return URLEncoder.encode(normalized, "UTF-8")
    }
}
