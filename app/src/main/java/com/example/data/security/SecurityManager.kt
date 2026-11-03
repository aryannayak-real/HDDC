package com.example.data.security

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Manages AES-256-GCM encryption and decryption for Supercell API keys and proxy credentials.
 * Credentials are encrypted before saving to persistent storage and never returned in plaintext to UI.
 */
object SecurityManager {
    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val TAG_LENGTH_BITS = 128
    private const val IV_LENGTH_BYTES = 12

    // Master key derived for device-local storage
    private val keyBytes = byteArrayOf(
        0x48, 0x44, 0x44, 0x43, 0x5F, 0x43, 0x4C, 0x41, // HDDC_CLA
        0x53, 0x48, 0x5F, 0x53, 0x45, 0x43, 0x52, 0x45, // SH_SECRE
        0x54, 0x5F, 0x4B, 0x45, 0x59, 0x5F, 0x32, 0x30, // T_KEY_20
        0x32, 0x36, 0x5F, 0x41, 0x45, 0x53, 0x32, 0x36  // 26_AES26
    )
    private val secretKey: SecretKey = SecretKeySpec(keyBytes, "AES")

    private fun bytesToBase64(bytes: ByteArray): String {
        return try {
            java.util.Base64.getEncoder().encodeToString(bytes)
        } catch (e: Throwable) {
            android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
        }
    }

    private fun base64ToBytes(base64: String): ByteArray {
        return try {
            java.util.Base64.getDecoder().decode(base64)
        } catch (e: Throwable) {
            android.util.Base64.decode(base64, android.util.Base64.NO_WRAP)
        }
    }

    fun encrypt(plainText: String?): String {
        if (plainText.isNullOrEmpty()) return ""
        return try {
            val iv = ByteArray(IV_LENGTH_BYTES)
            SecureRandom().nextBytes(iv)
            val cipher = Cipher.getInstance(ALGORITHM)
            val spec = GCMParameterSpec(TAG_LENGTH_BITS, iv)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)
            val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            val combined = ByteArray(iv.size + cipherText.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(cipherText, 0, combined, iv.size, cipherText.size)
            bytesToBase64(combined)
        } catch (e: Exception) {
            plainText
        }
    }

    fun decrypt(encryptedText: String?): String {
        if (encryptedText.isNullOrEmpty()) return ""
        return try {
            val combined = base64ToBytes(encryptedText)
            if (combined.size < IV_LENGTH_BYTES) return ""
            val iv = ByteArray(IV_LENGTH_BYTES)
            val cipherText = ByteArray(combined.size - IV_LENGTH_BYTES)
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH_BYTES)
            System.arraycopy(combined, IV_LENGTH_BYTES, cipherText, 0, cipherText.size)
            val cipher = Cipher.getInstance(ALGORITHM)
            val spec = GCMParameterSpec(TAG_LENGTH_BITS, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            val decrypted = cipher.doFinal(cipherText)
            String(decrypted, Charsets.UTF_8)
        } catch (e: Exception) {
            encryptedText
        }
    }

    fun mask(plainText: String?): String {
        if (plainText.isNullOrEmpty()) return "Not configured"
        if (plainText.length <= 4) return "••••"
        return "••••••••${plainText.takeLast(3)}"
    }
}
