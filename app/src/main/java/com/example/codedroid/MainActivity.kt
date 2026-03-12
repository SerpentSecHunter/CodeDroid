package com.example.codedroid

import android.os.Bundle
import androidx.activity.ComponentActivity
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.codedroid.data.ThemePreference
import com.example.codedroid.editor.EditorThemes
import com.example.codedroid.ui.*
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
    val drawerState  = rememberDrawerState(DrawerValue.Closed)

    val themeMode    by pref.themeFlow.collectAsStateWithLifecycle("auto")
    val fontSize     by pref.fontSizeFlow.collectAsStateWithLifecycle(14)
    val wordWrap     by pref.wordWrapFlow.collectAsStateWithLifecycle(true)
    val showLineNums by pref.showLineNumFlow.collectAsStateWithLifecycle(true)
    val autoSave     by pref.autoSaveFlow.collectAsStateWithLifecycle(false)
    val tabSize      by pref.tabSizeFlow.collectAsStateWithLifecycle(4)
    val editorTheme  by pref.editorThemeFlow.collectAsStateWithLifecycle("monokai")

    var currentPage  by remember { mutableStateOf(NavPage.EDITOR) }

    LaunchedEffect(fontSize)     { editorVM.fontSize.value     = fontSize }
    LaunchedEffect(wordWrap)     { editorVM.wordWrap.value     = wordWrap }
    LaunchedEffect(showLineNums) { editorVM.showLineNums.value = showLineNums }
    LaunchedEffect(autoSave)     { editorVM.autoSave.value     = autoSave }
    LaunchedEffect(tabSize)      { editorVM.tabSize.value      = tabSize }
    LaunchedEffect(editorTheme)  { editorVM.editorTheme.value  = editorTheme }

    ModalNavigationDrawer(
        drawerState   = drawerState,
        drawerContent = {
            AppDrawer(
                currentPage   = currentPage,
                onNavigate    = { currentPage = it },
                onCloseDrawer = { scope.launch { drawerState.close() } },
                fileName      = editorVM.fileName.value,
                isModified    = editorVM.isModified.value
            )
        }
    ) {
        Scaffold(
            topBar = {
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
                            // Tampilkan nama file di title
                            if (currentPage == NavPage.EDITOR) {
                                Text(
                                    text     = (if (editorVM.isModified.value) "● " else "") + editorVM.fileName.value,
                                    fontSize = 10.sp,
                                    color    = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                                )
                            }
                        }
                    },
                    actions = {
                        when (currentPage) {
                            NavPage.EDITOR -> {
                                if (editorVM.isModified.value) {
                                    IconButton(onClick = { editorVM.saveFile() }) {
                                        Icon(Icons.Rounded.Save, "Simpan",
                                            tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                                IconButton(onClick = { editorVM.undo() }) {
                                    Icon(Icons.Rounded.Undo, "Undo")
                                }
                                IconButton(onClick = { editorVM.redo() }) {
                                    Icon(Icons.Rounded.Redo, "Redo")
                                }
                                IconButton(onClick = { editorVM.newFile() }) {
                                    Icon(Icons.Rounded.Add, "File Baru")
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
                            }, "Tema")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface)
                )
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                when (currentPage) {
                    NavPage.EDITOR -> EditorScreen(
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
                    NavPage.FILES -> FileManagerScreen(
                        onOpenFile   = { editorVM.openFile(it); currentPage = NavPage.EDITOR },
                        onInsertCode = { code, _ ->
                            editorVM.updateContent(editorVM.content.value + "\n" + code)
                            currentPage = NavPage.EDITOR
                        }
                    )
                    NavPage.PREVIEW -> {
                        // Preview sekarang ada di dalam editor — redirect ke editor
                        currentPage = NavPage.EDITOR
                    }
                    NavPage.TERMINAL -> TerminalScreen(terminalVM.terminal)
                    NavPage.FTP      -> FtpScreen(
                        onOpenFile = { editorVM.openFile(it); currentPage = NavPage.EDITOR }
                    )
                    NavPage.SNIPPETS -> SnippetsScreen(
                        onInsert = { code ->
                            editorVM.updateContent(editorVM.content.value + code)
                            currentPage = NavPage.EDITOR
                        }
                    )
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
                    NavPage.EXTENSIONS     -> ExtensionsScreen()
                    NavPage.AI_PANEL       -> AiPanelScreen()
                    NavPage.PYTHON_LIBRARY -> PythonLibraryScreen()
                }
            }
        }
    }
}
