package com.example.codedroid.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore by preferencesDataStore("codedroid_prefs")

class ThemePreference(private val context: Context) {
    companion object {
        val THEME_KEY         = stringPreferencesKey("theme")
        val FONT_SIZE_KEY     = intPreferencesKey("font_size")
        val WORD_WRAP_KEY     = booleanPreferencesKey("word_wrap")
        val TAB_SIZE_KEY      = intPreferencesKey("tab_size")
        val EDITOR_THEME_KEY  = stringPreferencesKey("editor_theme")
        val SHOW_LINE_NUM_KEY = booleanPreferencesKey("show_line_numbers")
        val AUTO_SAVE_KEY     = booleanPreferencesKey("auto_save")
        val FONT_FAMILY_KEY   = stringPreferencesKey("font_family")
    }

    private val dataFlow = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }

    val themeFlow       : Flow<String>  = dataFlow.map { it[THEME_KEY]         ?: "auto" }
    val fontSizeFlow    : Flow<Int>     = dataFlow.map { it[FONT_SIZE_KEY]     ?: 14 }
    val wordWrapFlow    : Flow<Boolean> = dataFlow.map { it[WORD_WRAP_KEY]     ?: true }
    val tabSizeFlow     : Flow<Int>     = dataFlow.map { it[TAB_SIZE_KEY]      ?: 4 }
    val editorThemeFlow : Flow<String>  = dataFlow.map { it[EDITOR_THEME_KEY]  ?: "monokai" }
    val showLineNumFlow : Flow<Boolean> = dataFlow.map { it[SHOW_LINE_NUM_KEY] ?: true }
    val autoSaveFlow    : Flow<Boolean> = dataFlow.map { it[AUTO_SAVE_KEY]     ?: false }
    val fontFamilyFlow  : Flow<String>  = dataFlow.map { it[FONT_FAMILY_KEY]   ?: "monospace" }

    suspend fun saveTheme(v: String)        = context.dataStore.edit { it[THEME_KEY]         = v }
    suspend fun saveFontSize(v: Int)        = context.dataStore.edit { it[FONT_SIZE_KEY]     = v }
    suspend fun saveWordWrap(v: Boolean)    = context.dataStore.edit { it[WORD_WRAP_KEY]     = v }
    suspend fun saveTabSize(v: Int)         = context.dataStore.edit { it[TAB_SIZE_KEY]      = v }
    suspend fun saveEditorTheme(v: String)  = context.dataStore.edit { it[EDITOR_THEME_KEY]  = v }
    suspend fun saveShowLineNum(v: Boolean) = context.dataStore.edit { it[SHOW_LINE_NUM_KEY] = v }
    suspend fun saveAutoSave(v: Boolean)    = context.dataStore.edit { it[AUTO_SAVE_KEY]     = v }
    suspend fun saveFontFamily(v: String)   = context.dataStore.edit { it[FONT_FAMILY_KEY]   = v }
}