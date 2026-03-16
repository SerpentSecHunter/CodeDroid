package com.example.codedroid

import android.widget.Toast
import com.example.codedroid.util.SecurityCheck

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.automirrored.rounded.Redo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.PaddingValues
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.codedroid.data.ThemePreference
import com.example.codedroid.editor.EditorThemes
import com.example.codedroid.ui.*
import com.example.codedroid.ui.theme.CodeDroidTheme
import android.net.Uri
import com.example.codedroid.viewmodel.EditorViewModel
import com.example.codedroid.viewmodel.TerminalViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Security Integrity Check
        if (SecurityCheck.isSecurityCompromised(this)) {
            setContent {
                CodeDroidTheme(darkTheme = true) {
                    AlertDialog(
                        onDismissRequest = { finish() },
                        title = { Text("Security Alert", color = Color.Red) },
                        text = { Text("This application has been tampered with or is running in an insecure environment (Root/Mod). For your safety, CodeDroid will now close.") },
                        confirmButton = {
                            Button(onClick = { finish() }) { Text("Exit") }
                        }
                    )
                }
            }
            return
        }

        // Screenshots and recordings are now allowed for demonstration purposes.
        
        enableEdgeToEdge()
        setContent {
            // Anti-overlay protection at View level
            SideEffect {
                val view = window.decorView
                view.filterTouchesWhenObscured = true
            }
            
            val context   = LocalContext.current
            val pref      = remember { ThemePreference(context) }
            val themeMode by pref.themeFlow.collectAsStateWithLifecycle("auto")
            val isDark    = when (themeMode) {
                "dark"  -> true
                "light" -> false
                else    -> isSystemInDarkTheme()
            }
            CodeDroidTheme(darkTheme = isDark) {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context      = LocalContext.current
    val scope        = rememberCoroutineScope()
    val pref         = remember { ThemePreference(context) }
    val editorVM     : EditorViewModel   = viewModel()
    val terminalVM   : TerminalViewModel = viewModel()
    val drawerState  = rememberDrawerState(DrawerValue.Closed)
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current

    // Hide keyboard when drawer opens
    LaunchedEffect(drawerState.isOpen) {
        if (drawerState.isOpen) {
            keyboardController?.hide()
        }
    }

    val themeMode    by pref.themeFlow.collectAsStateWithLifecycle("auto")
    val fontSize     by pref.fontSizeFlow.collectAsStateWithLifecycle(14)
    val wordWrap     by pref.wordWrapFlow.collectAsStateWithLifecycle(true)
    val showLineNumbers by pref.showLineNumFlow.collectAsStateWithLifecycle(true)
    val autoSave     by pref.autoSaveFlow.collectAsStateWithLifecycle(false)
    val tabSize      by pref.tabSizeFlow.collectAsStateWithLifecycle(4)
    val editorTheme  by pref.editorThemeFlow.collectAsStateWithLifecycle("monokai")

    val currentPage  = remember { mutableStateOf(NavPage.EDITOR) }
    val showExitDialog = remember { mutableStateOf(false) }

    // Exit Confirmation Dialog
    if (showExitDialog.value) {
        AlertDialog(
            onDismissRequest = { showExitDialog.value = false },
            title = { Text("Exit Application") },
            text  = { Text("Are you sure you want to exit CodeDroid?") },
            confirmButton = {
                Button(onClick = { (context as? ComponentActivity)?.finish() }) {
                    Text("Exit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog.value = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Handle system back button
    BackHandler {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else {
            showExitDialog.value = true
        }
    }

    LaunchedEffect(fontSize)     { editorVM.fontSize.value     = fontSize }
    LaunchedEffect(wordWrap)     { editorVM.wordWrap.value     = wordWrap }
    LaunchedEffect(showLineNumbers) { editorVM.showLineNums.value = showLineNumbers }
    LaunchedEffect(autoSave)     { editorVM.autoSave.value     = autoSave }
    LaunchedEffect(tabSize)      { editorVM.tabSize.value      = tabSize }
    LaunchedEffect(editorTheme)  { editorVM.editorTheme.value  = editorTheme }

    ModalNavigationDrawer(
        drawerState   = drawerState,
        drawerContent = {
            AppDrawer(
                currentPage   = currentPage.value,
                onNavigate    = { currentPage.value = it },
                onCloseDrawer = { scope.launch { drawerState.close() } },
                fileName      = editorVM.fileName.value,
                isModified    = editorVM.isModified.value
            )
        }
    ) {
        Scaffold(
            topBar = {
                if (currentPage.value != NavPage.TERMINAL) {
                    TopAppBar(
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Rounded.Menu, "Menu")
                            }
                        },
                        title = {
                            Column {
                                Row {
                                    Text("</", fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                                    Text("CodeDroid", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                    Text(">", fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                                }
                                // Show filename in title
                                if (currentPage.value == NavPage.EDITOR) {
                                    Text(
                                        text     = (if (editorVM.isModified.value) "● " else "") + editorVM.fileName.value,
                                        fontSize = 10.sp,
                                        color    = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                                    )
                                }
                            }
                        },
                        actions = {
                            when (currentPage.value) {
                                NavPage.EDITOR -> {
                                    if (editorVM.isModified.value) {
                                        IconButton(onClick = { editorVM.saveFile(context) }) {
                                            Icon(Icons.Rounded.Save, "Save",
                                                tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                    IconButton(onClick = { editorVM.undo() }) {
                                        Icon(Icons.AutoMirrored.Rounded.Undo, "Undo")
                                    }
                                    IconButton(onClick = { editorVM.redo() }) {
                                        Icon(Icons.AutoMirrored.Rounded.Redo, "Redo")
                                    }
                                    IconButton(onClick = { editorVM.newFile() }) {
                                        Icon(Icons.Rounded.Add, "New File")
                                    }
                                }
                                else -> {}
                            }
                            // Theme toggle
                            IconButton(onClick = {
                                scope.launch {
                                    val next = when (themeMode) {
                                        "auto"  -> "dark"
                                        "dark"  -> "light"
                                        else    -> "auto"
                                    }
                                    pref.saveTheme(next)
                                }
                            }) {
                                Icon(when (themeMode) {
                                    "dark"  -> Icons.Rounded.DarkMode
                                    "light" -> Icons.Rounded.LightMode
                                    else    -> Icons.Rounded.Brightness4
                                }, "Theme")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface)
                    )
                }
            }
        ) { padding ->
            val zero = 0.dp
            val finalPadding = if (currentPage.value == NavPage.TERMINAL) PaddingValues(zero) else padding
            Box(Modifier.fillMaxSize().padding(finalPadding)) {
                when (currentPage.value) {
                    NavPage.EDITOR -> EditorScreen(
                        content         = editorVM.content.value,
                        onContentChange = { editorVM.updateContent(it, context) },
                        language        = editorVM.language.value,
                        theme           = EditorThemes.get(editorTheme),
                        fontSize        = fontSize,
                        wordWrap        = wordWrap,
                        showLineNumbers = showLineNumbers,
                        onUndo          = { editorVM.undo() },
                        onRedo          = { editorVM.redo() },
                        onSave          = { editorVM.saveFile(context) }
                    )
                    NavPage.FILES -> FileManagerScreen(
                        onOpenFile   = { uri -> editorVM.openFile(uri, context); currentPage.value = NavPage.EDITOR },
                        onInsertCode = { code, _ ->
                            editorVM.updateContent(editorVM.content.value + "\n" + code, context)
                            currentPage.value = NavPage.EDITOR
                        }
                    )
                    NavPage.PREVIEW -> {
                        // Preview is now inside the editor - redirect to editor
                        currentPage.value = NavPage.EDITOR
                    }
                    NavPage.TERMINAL -> TerminalScreen(terminalVM.terminalManager) { scope.launch { drawerState.open() } }
                    NavPage.FTP      -> FtpScreen(
                        onOpenFile = { file -> 
                            editorVM.openFile(Uri.fromFile(file), context)
                            currentPage.value = NavPage.EDITOR 
                        }
                    )
                    NavPage.SNIPPETS -> SnippetsScreen(
                        onInsert = { code ->
                            editorVM.updateContent(editorVM.content.value + code, context)
                            currentPage.value = NavPage.EDITOR
                        }
                    )
                    NavPage.SETTINGS -> SettingsScreen(
                        themeMode      = themeMode,
                        onThemeChange  = { scope.launch { pref.saveTheme(it) } },
                        fontSize       = fontSize,
                        onFontSize     = { scope.launch { pref.saveFontSize(it) } },
                        wordWrap       = wordWrap,
                        onWordWrap     = { scope.launch { pref.saveWordWrap(it) } },
                        showLineNums   = showLineNumbers,
                        onShowLineNums = { scope.launch { pref.saveShowLineNum(it) } },
                        autoSave       = autoSave,
                        onAutoSave     = { scope.launch { pref.saveAutoSave(it) } },
                        tabSize        = tabSize,
                        onTabSize      = { scope.launch { pref.saveTabSize(it) } },
                        editorTheme    = editorTheme,
                        onEditorTheme  = { scope.launch { pref.saveEditorTheme(it) } }
                    )
                    NavPage.EXTENSIONS     -> ExtensionsScreen()
                    NavPage.AI_PANEL       -> AiPanelScreen()
                    NavPage.PYTHON_LIBRARY -> PythonLibraryScreen()
                }
            }
        }
    }
}
