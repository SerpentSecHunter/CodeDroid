package com.example.codedroid.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class Snippet(
    val id       : String = System.currentTimeMillis().toString(),
    val title    : String,
    val code     : String,
    val language : String = "text",
    val createdAt: Long   = System.currentTimeMillis()
)

object SnippetManager {
    private const val PREF = "codedroid_snippets"
    private const val KEY  = "snippet_list"
    private val gson       = Gson()

    fun getAll(context: Context): List<Snippet> {
        val json = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY, "[]") ?: "[]"
        return try {
            val type = object : TypeToken<List<Snippet>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) { emptyList() }
    }

    fun save(context: Context, snippet: Snippet) {
        val list = getAll(context).toMutableList()
        val idx  = list.indexOfFirst { it.id == snippet.id }
        if (idx >= 0) list[idx] = snippet else list.add(0, snippet)
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putString(KEY, gson.toJson(list)).apply()
    }

    fun delete(context: Context, id: String) {
        val list = getAll(context).filter { it.id != id }
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putString(KEY, gson.toJson(list)).apply()
    }
}