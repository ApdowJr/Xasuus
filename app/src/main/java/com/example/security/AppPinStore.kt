package com.example.security

import android.content.Context
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** Stores a per-PIN salted PBKDF2 digest; the PIN itself is never persisted. */
object AppPinStore {
    private const val PREFS = "xasuus_security"
    private const val HASH = "pin_hash"
    private const val SALT = "pin_salt"
    private const val ITERATIONS = 120_000

    fun isEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).contains(HASH)

    fun setPin(context: Context, pin: String?) {
        val preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (pin.isNullOrBlank()) {
            preferences.edit().remove(HASH).remove(SALT).apply()
        } else {
            val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
            preferences.edit()
                .putString(SALT, salt.toHex())
                .putString(HASH, derive(pin, salt).toHex())
                .apply()
        }
    }

    fun verify(context: Context, pin: String): Boolean {
        val preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val expected = preferences.getString(HASH, null)?.hexToBytes() ?: return false
        val salt = preferences.getString(SALT, null)?.hexToBytes() ?: return false
        return MessageDigest.isEqual(expected, derive(pin, salt))
    }

    private fun derive(pin: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(pin.toCharArray(), salt, ITERATIONS, 256)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    private fun String.hexToBytes(): ByteArray? {
        if (length % 2 != 0) return null
        return runCatching { chunked(2).map { it.toInt(16).toByte() }.toByteArray() }.getOrNull()
    }
}
