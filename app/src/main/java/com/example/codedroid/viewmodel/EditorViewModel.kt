package com.example.codedroid.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.codedroid.editor.SyntaxHighlighter
import com.example.codedroid.editor.UndoRedoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class EditorViewModel : ViewModel() {
    var content      = mutableStateOf("")
    var language     = mutableStateOf("text")
    var currentFile  = mutableStateOf<File?>(null)
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

    fun updateContent(text: String) {
        undoRedo.saveState(content.value)
        content.value  = text
        isModified.value = true
        if (autoSave.value) saveFile()
    }

    fun undo() {
        undoRedo.undo()?.let { content.value = it }
    }

    fun redo() {
        undoRedo.redo()?.let { content.value = it }
    }

    fun openFile(file: File) {
        viewModelScope.launch {
            val text = withContext(Dispatchers.IO) {
                runCatching { file.readText() }.getOrDefault("")
            }
            content.value    = text
            currentFile.value= file
            fileName.value   = file.name
            language.value   = SyntaxHighlighter.detectLanguage(file.name)
            isModified.value = false
            undoRedo.clear()
        }
    }

    fun saveFile() {
        val file = currentFile.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { file.writeText(content.value) }
            isModified.value = false
        }
    }

    fun newFile() {
        content.value     = ""
        currentFile.value = null
        fileName.value    = "Untitled"
        language.value    = "text"
        isModified.value  = false
        undoRedo.clear()
    }
}