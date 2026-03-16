package com.example.codedroid.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.codedroid.editor.SyntaxHighlighter
import com.example.codedroid.editor.UndoRedoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class InsertMode {
    IMAGE, AUDIO, VIDEO
}

class EditorViewModel : ViewModel() {
    var content      = mutableStateOf("")
    var language     = mutableStateOf("text")
    var currentUri   = mutableStateOf<Uri?>(null)
    var isModified   = mutableStateOf(false)
    var fileName     = mutableStateOf("Untitled")

    // Settings states
    var themeMode    = mutableStateOf("auto")
    var fontSize     = mutableStateOf(14)
    var wordWrap     = mutableStateOf(true)
    var showLineNums = mutableStateOf(true)
    var autoSave     = mutableStateOf(false)
    var tabSize      = mutableStateOf(4)
    var editorTheme  = mutableStateOf("monokai")
    var fontFamily   = mutableStateOf("monospace")

    private val undoRedo = UndoRedoManager()

    fun updateContent(text: String, context: Context? = null) {
        undoRedo.saveState(content.value)
        content.value  = text
        isModified.value = true
        if (autoSave.value && context != null) saveFile(context)
    }

    fun undo() {
        undoRedo.undo()?.let { content.value = it }
    }

    fun redo() {
        undoRedo.redo()?.let { content.value = it }
    }

    fun openFile(uri: Uri, context: Context) {
        viewModelScope.launch {
            val text = withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openInputStream(uri)?.use { 
                        it.bufferedReader().readText() 
                    } ?: ""
                }.getOrDefault("")
            }
            content.value    = text
            currentUri.value = uri
            
            // Dapatkan nama file dari DocumentFile atau Uri
            val name = DocumentFile.fromSingleUri(context, uri)?.name 
                ?: uri.path?.substringAfterLast('/') 
                ?: "Unknown"
            
            fileName.value   = name
            language.value   = SyntaxHighlighter.detectLanguage(name)
            isModified.value = false
            undoRedo.clear()
        }
    }

    fun saveFile(context: Context) {
        val uri = currentUri.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                context.contentResolver.openOutputStream(uri, "wt")?.use { 
                    it.bufferedWriter().use { writer -> writer.write(content.value) }
                }
            }
            isModified.value = false
        }
    }

    fun newFile() {
        content.value     = ""
        currentUri.value  = null
        fileName.value    = "Untitled"
        language.value    = "text"
        isModified.value  = false
        undoRedo.clear()
    }
}