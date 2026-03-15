package com.example.codedroid.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.codedroid.terminal.TerminalManager

class TerminalViewModel(application: Application) : AndroidViewModel(application) {
    val terminalManager = TerminalManager(application)

    init {
        terminalManager.startSession()
    }

    override fun onCleared() {
        super.onCleared()
        terminalManager.stopSession()
    }
}
