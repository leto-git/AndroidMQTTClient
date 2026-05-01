/*
 * Copyright 2026 Tobias Leikam (RheinMain University of Applied Sciences)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package com.leto.mqttelement.data.local

import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.core.content.edit
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Manages the generation, encryption, and retrieval of the SQLCipher database key
 * using the Android Keystore for hardware-backed security.
 *
 * Implementation is adapted from an article by Pouya Heydari:
 * @see <a href="https://proandroiddev.com/how-to-encrypt-your-room-database-in-android-using-sqlcipher-0bce78328bd6">
 *     How to Encrypt Your Room Database in Android Using SQLCipher </a>
 */
class SqlCipherKeyManager(
    private val sharedPreferences: SharedPreferences
) {
    /**
     * The Android Keystore instance used for storing and retrieving the master key.
     */
    private val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }

    /**
     * Companion object containing constants used for key management.
     */
    companion object {
        private const val KEY_ALIAS = "sqlcipher_keystore_key"
        private const val ENCRYPTED_KEY_PREF = "encrypted_key"
        private const val IV_PREF = "encryption_iv"
    }

    /**
     * Initialize
     */
    init {
        initialize()
    }

    /**
     * Initializes the SqlCipherKeyManager by generating a new key if necessary.
     */
    private fun initialize() {
        generateKeystoreKeyIfNeeded()
        if (!sharedPreferences.contains(ENCRYPTED_KEY_PREF)) {
            generateAndEncryptSqlCipherKey()
        }
    }

    /**
     * Generates a new key in the Android Keystore if it doesn't already exist.
     * This master key is used to encrypt and decrypt the database key.
     */
    private fun generateKeystoreKeyIfNeeded() {
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                "AndroidKeyStore")
            val keyGenSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
            keyGenerator.init(keyGenSpec)
            keyGenerator.generateKey()
        }
    }

    /**
     * Generates a new random key, encrypts it using the master key and stores the
     * encrypted key and it's IV (Initialization Vector) in the SharedPreferences.
     */
    private fun generateAndEncryptSqlCipherKey() {
        val secretKey = getSecretKey()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        val sqlCipherKey = ByteArray(32)
        SecureRandom().nextBytes(sqlCipherKey)

        val encryptedKey = cipher.doFinal(sqlCipherKey)
        val iv = cipher.iv

        sharedPreferences.edit {
            putString(ENCRYPTED_KEY_PREF, Base64.encodeToString(
                encryptedKey,
                Base64.NO_WRAP))
            putString(IV_PREF, Base64.encodeToString(
                iv,
                Base64.NO_WRAP))
        }

        // Zero out the key in memory
        sqlCipherKey.fill(0)
    }

    /**
     * Decrypts the encrypted SQLCipher key using the master key.
     *
     * @param key The encrypted key.
     * @param iv The initialization vector.
     *
     * @return The decrypted key.
     */
    private fun getDecryptedSqlCipherKey(key: String, iv: String): ByteArray {
        val encryptedKey = Base64.decode(key, Base64.NO_WRAP)
        val ivBytes = Base64.decode(iv, Base64.NO_WRAP)

        val secretKey = getSecretKey()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(
            Cipher.DECRYPT_MODE, secretKey,
            GCMParameterSpec(128, ivBytes))

        return cipher.doFinal(encryptedKey)
    }

    /**
     * Gets the secret master-key from the Android Keystore.
     *
     * @return The secret key.
     */
    private fun getSecretKey(): SecretKey =
        (keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey

    /**
     * Gets the SupportOpenHelperFactory for Room.
     *
     * Note: Other than the original implementation, we do NOT clear the decryptedKey
     * here to prevent DB access issues.
     */
    fun getSupportFactory(): SupportOpenHelperFactory {
        val encryptedKey = sharedPreferences.getString(
            ENCRYPTED_KEY_PREF,
            null
        ).orEmpty()
        val iv = sharedPreferences.getString(
            IV_PREF,
            null
        ).orEmpty()
        val decryptedKey = getDecryptedSqlCipherKey(
            encryptedKey,
            iv
        )
        return SupportOpenHelperFactory(decryptedKey)
    }
}