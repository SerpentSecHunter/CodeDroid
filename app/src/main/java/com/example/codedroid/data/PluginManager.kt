package com.example.codedroid.data

import android.content.Context
import android.util.Base64
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

data class PluginAction(
    val id      : String = "",
    val label   : String = "",
    val shortcut: String = ""
)

data class Plugin(
    val id         : String             = "",
    val name       : String             = "",
    val version    : String             = "1.0.0",
    val author     : String             = "",
    val description: String             = "",
    val icon       : String             = "🔌",
    val category   : String             = "utility",
    val builtin    : Boolean            = false,
    val enabled    : Boolean            = false,
    val actions    : List<PluginAction> = emptyList()
)

object PluginManager {
    private const val PREF        = "codedroid_plugins"
    private const val ENABLED_KEY = "enabled_plugins"
    private val gson              = Gson()

    fun loadBuiltinPlugins(context: Context): List<Plugin> {
        val plugins = mutableListOf<Plugin>()
        return try {
            val files = context.assets.list("plugins") ?: return emptyList()
            for (file in files) {
                if (!file.endsWith(".json")) continue
                try {
                    val json   = context.assets.open("plugins/$file").bufferedReader().readText()
                    val plugin = gson.fromJson(json, Plugin::class.java)
                    if (plugin != null) plugins.add(plugin)
                } catch (_: Exception) { }
            }
            plugins
        } catch (_: Exception) { emptyList() }
    }

    fun getAllPlugins(context: Context): List<Plugin> {
        val builtin    = loadBuiltinPlugins(context)
        val installed  = getInstalledPlugins(context)
        val enabledIds = getEnabledIds(context)
        val builtinUpdated = builtin.map { p -> p.copy(enabled = enabledIds.contains(p.id)) }
        return builtinUpdated + installed
    }

    private fun getInstalledPlugins(context: Context): List<Plugin> {
        val pluginDir = File(context.filesDir, "plugins")
        if (!pluginDir.exists()) return emptyList()
        return pluginDir.listFiles { f -> f.extension == "json" }
            ?.mapNotNull { file ->
                try { gson.fromJson(file.readText(), Plugin::class.java) }
                catch (_: Exception) { null }
            } ?: emptyList()
    }

    fun setEnabled(context: Context, pluginId: String, enabled: Boolean) {
        val ids = getEnabledIds(context).toMutableSet()
        if (enabled) ids.add(pluginId) else ids.remove(pluginId)
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit()
            .putString(ENABLED_KEY, gson.toJson(ids.toList())).apply()
    }

    fun isEnabled(context: Context, pluginId: String): Boolean =
        getEnabledIds(context).contains(pluginId)

    private fun getEnabledIds(context: Context): Set<String> {
        val json = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(ENABLED_KEY, "[]") ?: "[]"
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            (gson.fromJson<List<String>>(json, type) ?: emptyList()).toSet()
        } catch (_: Exception) { emptySet() }
    }

    fun installPlugin(context: Context, jsonContent: String): Boolean {
        return try {
            val plugin = gson.fromJson(jsonContent, Plugin::class.java) ?: return false
            if (plugin.id.isBlank() || plugin.name.isBlank()) return false
            val pluginDir = File(context.filesDir, "plugins").also { it.mkdirs() }
            File(pluginDir, "${plugin.id}.json").writeText(gson.toJson(plugin))
            true
        } catch (_: Exception) { false }
    }

    fun uninstallPlugin(context: Context, pluginId: String): Boolean {
        return try {
            setEnabled(context, pluginId, false)
            File(context.filesDir, "plugins/${pluginId}.json").delete()
        } catch (_: Exception) { false }
    }

    fun executeAction(context: Context, pluginId: String, actionId: String, inputText: String = ""): String {
        return when ("$pluginId:$actionId") {
            "com.codedroid.formatter:format_code"      -> formatCode(inputText)
            "com.codedroid.formatter:indent_fix"       -> fixIndentation(inputText)
            "com.codedroid.formatter:trim_whitespace"  -> inputText.lines().joinToString("\n") { it.trimEnd() }
            "com.codedroid.base64:encode_base64"       -> Base64.encodeToString(inputText.toByteArray(), Base64.DEFAULT).trim()
            "com.codedroid.base64:decode_base64"       -> try {
                String(Base64.decode(inputText.trim(), Base64.DEFAULT))
            } catch (_: Exception) { "[Error: Input bukan Base64 yang valid]" }
            else -> "[Plugin action tidak dikenal: $pluginId:$actionId]"
        }
    }

    private fun formatCode(code: String): String {
        if (code.isBlank()) return code
        val result      = StringBuilder()
        var indentLevel = 0
        val indent      = "    "
        for (rawLine in code.lines()) {
            val line = rawLine.trim()
            if (line.isEmpty()) { result.append("\n"); continue }
            if (line.startsWith("}") || line.startsWith(")") ||
                line.startsWith("]") || line.startsWith("</")) {
                indentLevel = maxOf(0, indentLevel - 1)
            }
            result.append(indent.repeat(indentLevel)).append(line).append("\n")
            if (line.endsWith("{") || line.endsWith("(") || line.endsWith("[") ||
                (line.endsWith(">") && !line.startsWith("</"))) {
                indentLevel++
            }
        }
        return result.toString().trimEnd()
    }

    private fun fixIndentation(code: String): String =
        code.lines().joinToString("\n") { line ->
            val trimmed = line.trimStart()
            val spaces  = line.length - trimmed.length
            " ".repeat((spaces / 4) * 4) + trimmed
        }
}