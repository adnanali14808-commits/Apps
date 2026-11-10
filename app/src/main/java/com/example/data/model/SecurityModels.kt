package com.example.data.model

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

enum class AuthMethodType(val label: String, val description: String) {
    PIN("Numeric PIN", "4–8 digit secure numeric code"),
    PATTERN("Pattern Lock", "3x3 graphic tactile connection path"),
    PASSWORD("Alphanumeric Password", "Custom high-entropy text passphrase")
}

data class SecurityConfig(
    val isSetupComplete: Boolean = false,
    val activeMethod: AuthMethodType = AuthMethodType.PIN,
    // PIN credentials
    val isPinConfigured: Boolean = false,
    val pinSalt: String = "",
    val pinHash: String = "",
    // Pattern credentials
    val isPatternConfigured: Boolean = false,
    val patternSalt: String = "",
    val patternHash: String = "",
    // Password credentials
    val isPasswordConfigured: Boolean = false,
    val passwordSalt: String = "",
    val passwordHash: String = "",
    // Lockout protection
    val failedAttempts: Int = 0,
    val lockoutUntilMs: Long = 0L
) {
    val configuredMethods: List<AuthMethodType>
        get() {
            val list = mutableListOf<AuthMethodType>()
            if (isPinConfigured) list.add(AuthMethodType.PIN)
            if (isPatternConfigured) list.add(AuthMethodType.PATTERN)
            if (isPasswordConfigured) list.add(AuthMethodType.PASSWORD)
            return if (list.isNotEmpty()) list else listOf(activeMethod)
        }
}

object SecurityCrypto {
    fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return Base64.getEncoder().encodeToString(salt)
    }

    fun hashCredential(credential: String, salt: String): String {
        val combined = "$salt:$credential"
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(combined.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(hash)
    }

    fun verifyCredential(input: String, salt: String, expectedHash: String): Boolean {
        if (salt.isEmpty() || expectedHash.isEmpty()) return false
        val computedHash = hashCredential(input, salt)
        return computedHash == expectedHash
    }
}
