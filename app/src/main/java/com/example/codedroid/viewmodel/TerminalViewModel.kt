package com.example.codedroid.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.codedroid.terminal.TerminalManager

class TerminalViewModel(app: Application) : AndroidViewModel(app) {
    val terminal = TerminalManager(app.applicationContext)

    override fun onCleared() {
        super.onCleared()
        terminal.stop()
    }
}
