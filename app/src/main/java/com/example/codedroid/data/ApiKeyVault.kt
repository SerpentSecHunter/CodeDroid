package com.example.codedroid.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object ApiKeyVault {
    private const val FILE = "codedroid_keys_v2"

    private fun prefs(ctx: Context) = try {
        val mk = MasterKey.Builder(ctx).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
        EncryptedSharedPreferences.create(
            ctx, FILE, mk,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (_: Exception) {
        ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE)
    }

    fun save(ctx: Context, key: String, value: String) =
        prefs(ctx).edit().putString(key, value).apply()

    fun get(ctx: Context, key: String): String =
        prefs(ctx).getString(key, "") ?: ""

    fun delete(ctx: Context, key: String) =
        prefs(ctx).edit().remove(key).apply()

    fun hasKey(ctx: Context, key: String) = get(ctx, key).isNotBlank()
}