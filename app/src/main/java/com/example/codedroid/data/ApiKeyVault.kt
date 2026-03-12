package com.example.codedroid.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object ApiKeyVault {
    private const val FILE = "codedroid_api_keys"

    private fun getPrefs(context: Context) = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
        EncryptedSharedPreferences.create(
            context, FILE, masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
    }

    fun save(context: Context, provider: String, key: String) =
        getPrefs(context).edit().putString(provider, key).apply()

    fun get(context: Context, provider: String): String =
        getPrefs(context).getString(provider, "") ?: ""

    fun delete(context: Context, provider: String) =
        getPrefs(context).edit().remove(provider).apply()

    fun hasKey(context: Context, provider: String): Boolean =
        get(context, provider).isNotBlank()
}