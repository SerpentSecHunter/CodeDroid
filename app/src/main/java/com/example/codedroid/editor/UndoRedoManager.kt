package com.example.codedroid.editor

class UndoRedoManager(private val maxHistory: Int = 100) {
    private val undoStack   = ArrayDeque<String>()
    private val redoStack   = ArrayDeque<String>()
    private var currentText = ""

    val canUndo get() = undoStack.isNotEmpty()
    val canRedo get() = redoStack.isNotEmpty()

    fun saveState(text: String) {
        if (text == currentText) return
        undoStack.addLast(currentText)
        if (undoStack.size > maxHistory) undoStack.removeFirst()
        redoStack.clear()
        currentText = text
    }

    fun undo(): String? {
        if (undoStack.isEmpty()) return null
        redoStack.addLast(currentText)
        currentText = undoStack.removeLast()
        return currentText
    }

    fun redo(): String? {
        if (redoStack.isEmpty()) return null
        undoStack.addLast(currentText)
        currentText = redoStack.removeLast()
        return currentText
    }

    fun clear() {
        undoStack.clear()
        redoStack.clear()
        currentText = ""
    }
}