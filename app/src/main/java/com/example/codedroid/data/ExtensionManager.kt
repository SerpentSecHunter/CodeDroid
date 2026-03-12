package com.example.codedroid.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object ExtensionManager {
    private const val PREF = "codedroid_extensions"
    private const val KEY  = "installed"
    private val gson = Gson()

    fun getInstalled(context: Context): Set<String> {
        val json = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY, "[]") ?: "[]"
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            (gson.fromJson<List<String>>(json, type) ?: emptyList()).toSet()
        } catch (e: Exception) { emptySet() }
    }

    fun install(context: Context, id: String) {
        val set = getInstalled(context).toMutableSet()
        set.add(id)
        save(context, set)
    }

    fun uninstall(context: Context, id: String) {
        val set = getInstalled(context).toMutableSet()
        set.remove(id)
        save(context, set)
    }

    fun isInstalled(context: Context, id: String) = getInstalled(context).contains(id)

    private fun save(context: Context, set: Set<String>) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putString(KEY, gson.toJson(set.toList())).apply()
    }
}