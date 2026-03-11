package com.example.codedroid.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object RecentFilesManager {
    private const val PREF = "codedroid_recent"
    private const val KEY  = "recent_files"
    private const val MAX  = 20
    private val gson       = Gson()

    fun getAll(context: Context): List<String> {
        val json = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY, "[]") ?: "[]"
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) { emptyList() }
    }

    fun add(context: Context, path: String) {
        val list = getAll(context).toMutableList()
        list.remove(path)
        list.add(0, path)
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putString(KEY, gson.toJson(list.take(MAX))).apply()
    }
}