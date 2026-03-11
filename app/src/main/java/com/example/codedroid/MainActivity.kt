package com.example.codedroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.codedroid.data.ThemePreference
import com.example.codedroid.editor.EditorThemes
import com.example.codedroid.ui.*
import com.example.codedroid.ui.NavPage
import com.example.codedroid.ui.theme.CodeDroidTheme
import com.example.codedroid.viewmodel.EditorViewModel
import com.example.codedroid.viewmodel.TerminalViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
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

    val themeMode    by pref.themeFlow.collectAsStateWithLifecycle("auto")
    val fontSize     by pref.fontSizeFlow.collectAsStateWithLifecycle(14)
    val wordWrap     by pref.wordWrapFlow.collectAsStateWithLifecycle(true)
    val showLineNums by pref.showLineNumFlow.collectAsStateWithLifecycle(true)
    val autoSave     by pref.autoSaveFlow.collectAsStateWithLifecycle(false)
    val tabSize      by pref.tabSizeFlow.collectAsStateWithLifecycle(4)
    val editorTheme  by pref.editorThemeFlow.collectAsStateWithLifecycle("monokai")

    var currentPage  by remember { mutableStateOf(NavPage.EDITOR) }
    var showThemeMenu by remember { mutableStateOf(false) }

    LaunchedEffect(fontSize)     { editorVM.fontSize.value     = fontSize }
    LaunchedEffect(wordWrap)     { editorVM.wordWrap.value     = wordWrap }
    LaunchedEffect(showLineNums) { editorVM.showLineNums.value = showLineNums }
    LaunchedEffect(autoSave)     { editorVM.autoSave.value     = autoSave }
    LaunchedEffect(tabSize)      { editorVM.tabSize.value      = tabSize }
    LaunchedEffect(editorTheme)  { editorVM.editorTheme.value  = editorTheme }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row {
                        Text("</", fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                        Text("CodeDroid", fontSize = 16.sp)
                        Text(">", fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                    }
                },
                actions = {
                    if (currentPage == NavPage.EDITOR) {
                        val modified = editorVM.isModified.value
                        if (modified) {
                            IconButton(onClick = { editorVM.saveFile() }) {
                                BadgedBox(badge = { Badge() }) {
                                    Icon(Icons.Rounded.Save, "Simpan")
                                }
                            }
                        }
                        IconButton(onClick = { editorVM.undo() }) {
                            Icon(Icons.Rounded.Undo, "Undo")
                        }
                        IconButton(onClick = { editorVM.redo() }) {
                            Icon(Icons.Rounded.Redo, "Redo")
                        }
                        IconButton(onClick = { editorVM.newFile() }) {
                            Icon(Icons.Rounded.Add, "Baru")
                        }
                        // Run button for python files
                        if (editorVM.language.value == "python") {
                            IconButton(onClick = { currentPage = NavPage.PREVIEW }) {
                                Icon(Icons.Rounded.PlayArrow, "Run",
                                    tint = MaterialTheme.colorScheme.tertiary)
                            }
                        }
                        // Preview button for web files
                        if (editorVM.language.value in listOf("html","css","javascript","markdown")) {
                            IconButton(onClick = { currentPage = NavPage.PREVIEW }) {
                                Icon(Icons.Rounded.Visibility, "Preview",
                                    tint = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }
                    Box {
                        IconButton(onClick = { showThemeMenu = true }) {
                            Icon(when (themeMode) {
                                "dark"  -> Icons.Rounded.DarkMode
                                "light" -> Icons.Rounded.LightMode
                                else    -> Icons.Rounded.Brightness4
                            }, "Tema")
                        }
                        DropdownMenu(showThemeMenu, { showThemeMenu = false }) {
                            DropdownMenuItem({ Text("🌙 Gelap") },   {
                                scope.launch { pref.saveTheme("dark") };  showThemeMenu = false })
                            DropdownMenuItem({ Text("☀️ Terang") },  {
                                scope.launch { pref.saveTheme("light") }; showThemeMenu = false })
                            DropdownMenuItem({ Text("🔄 Otomatis") },{
                                scope.launch { pref.saveTheme("auto") };  showThemeMenu = false })
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            ModernNavBar(
                currentPage = currentPage,
                onNavigate  = { currentPage = it }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (currentPage) {
                NavPage.EDITOR   -> EditorScreen(
                    content         = editorVM.content.value,
                    onContentChange = { editorVM.updateContent(it) },
                    language        = editorVM.language.value,
                    theme           = EditorThemes.get(editorTheme),
                    fontSize        = fontSize,
                    wordWrap        = wordWrap,
                    showLineNumbers = showLineNums,
                    onUndo          = { editorVM.undo() },
                    onRedo          = { editorVM.redo() },
                    onSave          = { editorVM.saveFile() }
                )
                NavPage.FILES    -> FileManagerScreen(
                    onOpenFile   = { editorVM.openFile(it); currentPage = NavPage.EDITOR },
                    onInsertCode = { code, _ ->
                        editorVM.updateContent(editorVM.content.value + "\n" + code)
                        currentPage = NavPage.EDITOR
                    }
                )
                NavPage.PREVIEW  -> PreviewScreen(
                    content         = editorVM.content.value,
                    language        = editorVM.language.value,
                    fileName        = editorVM.fileName.value,
                    terminalManager = terminalVM.terminal,
                    currentFilePath = editorVM.currentFile.value?.absolutePath ?: ""
                )
                NavPage.FTP      -> FtpScreen(
                    onOpenFile = { editorVM.openFile(it); currentPage = NavPage.EDITOR }
                )
                NavPage.SNIPPETS -> SnippetsScreen(
                    onInsert = { code ->
                        editorVM.updateContent(editorVM.content.value + code)
                        currentPage = NavPage.EDITOR
                    }
                )
                NavPage.TERMINAL -> TerminalScreen(terminalVM.terminal)
                NavPage.SETTINGS -> SettingsScreen(
                    themeMode      = themeMode,
                    onThemeChange  = { scope.launch { pref.saveTheme(it) } },
                    fontSize       = fontSize,
                    onFontSize     = { scope.launch { pref.saveFontSize(it) } },
                    wordWrap       = wordWrap,
                    onWordWrap     = { scope.launch { pref.saveWordWrap(it) } },
                    showLineNums   = showLineNums,
                    onShowLineNums = { scope.launch { pref.saveShowLineNum(it) } },
                    autoSave       = autoSave,
                    onAutoSave     = { scope.launch { pref.saveAutoSave(it) } },
                    tabSize        = tabSize,
                    onTabSize      = { scope.launch { pref.saveTabSize(it) } },
                    editorTheme    = editorTheme,
                    onEditorTheme  = { scope.launch { pref.saveEditorTheme(it) } }
                )
            }
        }
    }
}