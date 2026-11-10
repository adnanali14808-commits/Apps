package com.example.data.security

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.AuthMethodType
import com.example.data.model.SecurityConfig
import com.example.data.model.SecurityCrypto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SecurityRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("aegis_security_store", Context.MODE_PRIVATE)

    private val _securityConfig = MutableStateFlow(loadConfig())
    val securityConfig: StateFlow<SecurityConfig> = _securityConfig.asStateFlow()

    private fun loadConfig(): SecurityConfig {
        val isSetup = prefs.getBoolean(KEY_IS_SETUP, false)
        val activeMethodStr = prefs.getString(KEY_ACTIVE_METHOD, AuthMethodType.PIN.name) ?: AuthMethodType.PIN.name
        val activeMethod = try {
            AuthMethodType.valueOf(activeMethodStr)
        } catch (_: Exception) {
            AuthMethodType.PIN
        }

        // PIN
        val isPin = prefs.getBoolean(KEY_PIN_CONFIGURED, false)
        val pinSalt = prefs.getString(KEY_PIN_SALT, "") ?: ""
        val pinHash = prefs.getString(KEY_PIN_HASH, "") ?: ""

        // Pattern
        val isPattern = prefs.getBoolean(KEY_PATTERN_CONFIGURED, false)
        val patternSalt = prefs.getString(KEY_PATTERN_SALT, "") ?: ""
        val patternHash = prefs.getString(KEY_PATTERN_HASH, "") ?: ""

        // Password
        val isPassword = prefs.getBoolean(KEY_PASSWORD_CONFIGURED, false)
        val passwordSalt = prefs.getString(KEY_PASSWORD_SALT, "") ?: ""
        val passwordHash = prefs.getString(KEY_PASSWORD_HASH, "") ?: ""

        // Legacy compatibility
        val legacySalt = prefs.getString(KEY_SALT, "") ?: ""
        val legacyHash = prefs.getString(KEY_HASH, "") ?: ""
        val legacyMethod = prefs.getString(KEY_AUTH_METHOD, null)

        val hasAnyConfigured = isPin || isPattern || isPassword || (isSetup && legacyHash.isNotEmpty())

        val resolvedIsPin = isPin || (legacyMethod == AuthMethodType.PIN.name && legacyHash.isNotEmpty())
        val resolvedPinSalt = if (pinSalt.isNotEmpty()) pinSalt else if (legacyMethod == AuthMethodType.PIN.name) legacySalt else ""
        val resolvedPinHash = if (pinHash.isNotEmpty()) pinHash else if (legacyMethod == AuthMethodType.PIN.name) legacyHash else ""

        val resolvedIsPattern = isPattern || (legacyMethod == AuthMethodType.PATTERN.name && legacyHash.isNotEmpty())
        val resolvedPatternSalt = if (patternSalt.isNotEmpty()) patternSalt else if (legacyMethod == AuthMethodType.PATTERN.name) legacySalt else ""
        val resolvedPatternHash = if (patternHash.isNotEmpty()) patternHash else if (legacyMethod == AuthMethodType.PATTERN.name) legacyHash else ""

        val resolvedIsPassword = isPassword || (legacyMethod == AuthMethodType.PASSWORD.name && legacyHash.isNotEmpty())
        val resolvedPasswordSalt = if (passwordSalt.isNotEmpty()) passwordSalt else if (legacyMethod == AuthMethodType.PASSWORD.name) legacySalt else ""
        val resolvedPasswordHash = if (passwordHash.isNotEmpty()) passwordHash else if (legacyMethod == AuthMethodType.PASSWORD.name) legacyHash else ""

        val failedAttempts = prefs.getInt(KEY_FAILED_ATTEMPTS, 0)
        val lockoutUntil = prefs.getLong(KEY_LOCKOUT_UNTIL, 0L)

        return SecurityConfig(
            isSetupComplete = isSetup && hasAnyConfigured,
            activeMethod = activeMethod,
            isPinConfigured = resolvedIsPin,
            pinSalt = resolvedPinSalt,
            pinHash = resolvedPinHash,
            isPatternConfigured = resolvedIsPattern,
            patternSalt = resolvedPatternSalt,
            patternHash = resolvedPatternHash,
            isPasswordConfigured = resolvedIsPassword,
            passwordSalt = resolvedPasswordSalt,
            passwordHash = resolvedPasswordHash,
            failedAttempts = failedAttempts,
            lockoutUntilMs = lockoutUntil
        )
    }

    fun isSetupComplete(): Boolean {
        return loadConfig().isSetupComplete
    }

    fun saveMasterCredential(method: AuthMethodType, credential: String) {
        val salt = SecurityCrypto.generateSalt()
        val hash = SecurityCrypto.hashCredential(credential, salt)

        val editor = prefs.edit()
            .putBoolean(KEY_IS_SETUP, true)
            .putString(KEY_ACTIVE_METHOD, method.name)
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .putLong(KEY_LOCKOUT_UNTIL, 0L)

        when (method) {
            AuthMethodType.PIN -> {
                editor.putBoolean(KEY_PIN_CONFIGURED, true)
                    .putString(KEY_PIN_SALT, salt)
                    .putString(KEY_PIN_HASH, hash)
            }
            AuthMethodType.PATTERN -> {
                editor.putBoolean(KEY_PATTERN_CONFIGURED, true)
                    .putString(KEY_PATTERN_SALT, salt)
                    .putString(KEY_PATTERN_HASH, hash)
            }
            AuthMethodType.PASSWORD -> {
                editor.putBoolean(KEY_PASSWORD_CONFIGURED, true)
                    .putString(KEY_PASSWORD_SALT, salt)
                    .putString(KEY_PASSWORD_HASH, hash)
            }
        }
        editor.apply()
        _securityConfig.value = loadConfig()
    }

    fun removeCredential(method: AuthMethodType): Boolean {
        val current = _securityConfig.value
        val configured = current.configuredMethods
        if (configured.size <= 1) {
            // Cannot remove the only remaining credential
            return false
        }

        val editor = prefs.edit()
        when (method) {
            AuthMethodType.PIN -> {
                editor.remove(KEY_PIN_CONFIGURED).remove(KEY_PIN_SALT).remove(KEY_PIN_HASH)
            }
            AuthMethodType.PATTERN -> {
                editor.remove(KEY_PATTERN_CONFIGURED).remove(KEY_PATTERN_SALT).remove(KEY_PATTERN_HASH)
            }
            AuthMethodType.PASSWORD -> {
                editor.remove(KEY_PASSWORD_CONFIGURED).remove(KEY_PASSWORD_SALT).remove(KEY_PASSWORD_HASH)
            }
        }

        val remaining = configured.filter { it != method }
        if (current.activeMethod == method && remaining.isNotEmpty()) {
            editor.putString(KEY_ACTIVE_METHOD, remaining.first().name)
        }
        editor.apply()
        _securityConfig.value = loadConfig()
        return true
    }

    fun setActiveMethod(method: AuthMethodType) {
        prefs.edit().putString(KEY_ACTIVE_METHOD, method.name).apply()
        _securityConfig.value = loadConfig()
    }

    fun verifyCredential(method: AuthMethodType, input: String): VerifyResult {
        val current = _securityConfig.value
        val now = System.currentTimeMillis()

        if (current.lockoutUntilMs > now) {
            val remainingSec = ((current.lockoutUntilMs - now) / 1000).toInt() + 1
            return VerifyResult.LockedOut(remainingSec)
        }

        val (salt, hash) = when (method) {
            AuthMethodType.PIN -> Pair(current.pinSalt, current.pinHash)
            AuthMethodType.PATTERN -> Pair(current.patternSalt, current.patternHash)
            AuthMethodType.PASSWORD -> Pair(current.passwordSalt, current.passwordHash)
        }

        val isValid = SecurityCrypto.verifyCredential(input, salt, hash)
        return if (isValid) {
            // Reset failed attempts on success
            prefs.edit()
                .putInt(KEY_FAILED_ATTEMPTS, 0)
                .putLong(KEY_LOCKOUT_UNTIL, 0L)
                .apply()
            _securityConfig.value = loadConfig()
            VerifyResult.Success
        } else {
            val newFailed = current.failedAttempts + 1
            val lockout = if (newFailed >= MAX_FAILED_ATTEMPTS) {
                now + LOCKOUT_DURATION_MS
            } else {
                0L
            }
            prefs.edit()
                .putInt(KEY_FAILED_ATTEMPTS, newFailed)
                .putLong(KEY_LOCKOUT_UNTIL, lockout)
                .apply()
            _securityConfig.value = loadConfig()

            if (lockout > 0) {
                VerifyResult.LockedOut((LOCKOUT_DURATION_MS / 1000).toInt())
            } else {
                VerifyResult.Failed(attemptsLeft = MAX_FAILED_ATTEMPTS - newFailed)
            }
        }
    }

    fun resetSecurity() {
        prefs.edit().clear().apply()
        _securityConfig.value = loadConfig()
    }

    sealed interface VerifyResult {
        data object Success : VerifyResult
        data class Failed(val attemptsLeft: Int) : VerifyResult
        data class LockedOut(val remainingSeconds: Int) : VerifyResult
    }

    companion object {
        private const val KEY_IS_SETUP = "key_is_setup"
        private const val KEY_ACTIVE_METHOD = "key_active_method"

        private const val KEY_PIN_CONFIGURED = "key_pin_configured"
        private const val KEY_PIN_SALT = "key_pin_salt"
        private const val KEY_PIN_HASH = "key_pin_hash"

        private const val KEY_PATTERN_CONFIGURED = "key_pattern_configured"
        private const val KEY_PATTERN_SALT = "key_pattern_salt"
        private const val KEY_PATTERN_HASH = "key_pattern_hash"

        private const val KEY_PASSWORD_CONFIGURED = "key_password_configured"
        private const val KEY_PASSWORD_SALT = "key_password_salt"
        private const val KEY_PASSWORD_HASH = "key_password_hash"

        // Legacy keys
        private const val KEY_AUTH_METHOD = "key_auth_method"
        private const val KEY_SALT = "key_salt"
        private const val KEY_HASH = "key_hash"

        private const val KEY_FAILED_ATTEMPTS = "key_failed_attempts"
        private const val KEY_LOCKOUT_UNTIL = "key_lockout_until"

        private const val MAX_FAILED_ATTEMPTS = 5
        private const val LOCKOUT_DURATION_MS = 30_000L // 30 seconds
    }
}
