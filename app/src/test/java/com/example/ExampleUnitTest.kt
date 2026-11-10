package com.example

import com.example.data.model.SecurityCrypto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun `crypto hashing produces valid verifiable hash for 4 to 8 digit pin`() {
        val salt = SecurityCrypto.generateSalt()
        val pin8Digit = "12345678"
        val hash = SecurityCrypto.hashCredential(pin8Digit, salt)

        assertTrue(SecurityCrypto.verifyCredential("12345678", salt, hash))
        assertFalse(SecurityCrypto.verifyCredential("1234", salt, hash))
        assertFalse(SecurityCrypto.verifyCredential("87654321", salt, hash))
        assertFalse(SecurityCrypto.verifyCredential("", salt, hash))
    }

    @Test
    fun `different salts produce different hashes for same credential`() {
        val salt1 = SecurityCrypto.generateSalt()
        val salt2 = SecurityCrypto.generateSalt()
        val pin = "5678"

        val hash1 = SecurityCrypto.hashCredential(pin, salt1)
        val hash2 = SecurityCrypto.hashCredential(pin, salt2)

        assertNotEquals(salt1, salt2)
        assertNotEquals(hash1, hash2)
    }
}
