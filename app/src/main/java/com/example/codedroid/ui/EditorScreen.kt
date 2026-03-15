package com.example.codedroid.ui

import android.content.Context
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.example.codedroid.editor.EditorTheme
import com.example.codedroid.editor.SyntaxHighlighter
import kotlinx.coroutines.launch


@Composable
fun EditorScreen(
    content        : String,
    onContentChange: (String) -> Unit,
    language       : String,
    theme          : EditorTheme,
    fontSize       : Int,
    wordWrap       : Boolean,
    showLineNumbers: Boolean,
    onUndo         : () -> Unit,
    onRedo         : () -> Unit,
    onSave         : () -> Unit
) {
    // Extensions State
    val context    = LocalContext.current
    val installedExts = remember { com.example.codedroid.data.ExtensionManager.getInstalled(context) }
    val isAutoCloseActive = installedExts.contains("auto-close")
    val isWakaTimeActive  = installedExts.contains("wakatime")
    val isPetsActive      = installedExts.contains("pets")
    
    // Theme Override: Cari ekstensi tema yang sedang di-install, ubah ke EditorTheme yang sesuai
    val activeTheme = remember(installedExts, theme) {
        val themeExt = installedExts.find { it.startsWith("theme-") }
        if (themeExt != null) {
            val key = themeExt.removePrefix("theme-")
            com.example.codedroid.editor.EditorThemes.get(key)
        } else theme
    }

    // Fix Backspace: jangan reset tfv setiap kali content berubah dari luar
    var tfv        by remember { mutableStateOf(TextFieldValue(content)) }
    var lastExternal by remember { mutableStateOf(content) }

    // Sync dari luar HANYA jika bukan dari user typing
    LaunchedEffect(content) {
        if (content != tfv.text && content != lastExternal) {
            tfv = TextFieldValue(content, androidx.compose.ui.text.TextRange(content.length))
            lastExternal = content
        }
    }

    // Debounce sync ke ViewModel 500ms agar lebih aman saat mengetik cepat
    LaunchedEffect(tfv.text) {
        if (tfv.text != content) {
            kotlinx.coroutines.delay(500)
            onContentChange(tfv.text)
        }
    }

    val canPreview  = language.lowercase() in listOf("html","htm","css","javascript","js","markdown","md")

    // Fungsi intersepsi perubahan TextFieldValue (termasuk logika Ekstensi)
    val handleTfvChange: (TextFieldValue) -> Unit = { newTfv ->
        var finalTfv = newTfv

        // =======================
        // EXTENSION: AUTO-CLOSE TAG
        // =======================
        if (isAutoCloseActive && canPreview) {
            val oldTxt = tfv.text
            val newTxt = newTfv.text
            val cursor = newTfv.selection.start
            
            // Jika user baru saja mengetik '>'
            if (newTxt.length > oldTxt.length && cursor > 0 && newTxt[cursor - 1] == '>') {
                // Cari kata sebelum '>' untuk menemukan tag, misal "div" dari "<div>"
                val lastOpenBracket = newTxt.lastIndexOf('<', cursor - 2)
                if (lastOpenBracket != -1) {
                    val tagContent = newTxt.substring(lastOpenBracket + 1, cursor - 1)
                    val tagName = tagContent.takeWhile { it.isLetterOrDigit() || it == '-' }
                    
                    // Pastikan bukan tag self-closing atau deklarasi
                    val selfClosing = listOf("img", "br", "hr", "input", "meta", "link", "!--")
                    if (tagName.isNotEmpty() && !tagName.startsWith("/") && tagName.lowercase() !in selfClosing) {
                        val closeTag = "</$tagName>"
                        // Sisipkan tag penutup setelah kursor, tanpa memindahkan kursor
                        val modifiedTxt = newTxt.substring(0, cursor) + closeTag + newTxt.substring(cursor)
                        finalTfv = TextFieldValue(modifiedTxt, androidx.compose.ui.text.TextRange(cursor))
                    }
                }
            }
        }
        
        tfv = finalTfv
    }

    var showPreview by remember { mutableStateOf(false) }
    var findText    by remember { mutableStateOf("") }
    var showFind    by remember { mutableStateOf(false) }

    // AI state
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipManager = LocalClipboardManager.current
    var aiError     by remember { mutableStateOf("") }

    // Vibe Coding state
    var showVibeDialog by remember { mutableStateOf(false) }
    var vibePrompt     by remember { mutableStateOf("") }
    var isVibeCoding   by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {

        // ── VS Code style tab bar ─────────────────────────────────
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // File tab
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Language color dot
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(50))
                            .background(getLanguageColor(language))
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        SyntaxHighlighter.getLanguageLabel(language),
                        fontSize = 12.sp,
                        color    = MaterialTheme.colorScheme.onBackground.copy(0.9f)
                    )
                }

                Spacer(Modifier.weight(1f))

                // Action buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Find
                    IconButton(onClick = { showFind = !showFind }, Modifier.size(32.dp)) {
                        Icon(Icons.Rounded.Search, null,
                            modifier = Modifier.size(16.dp),
                            tint = if (showFind) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurface.copy(0.5f))
                    }
                    // Preview toggle
                    if (canPreview) {
                        IconButton(onClick = { showPreview = !showPreview }, Modifier.size(32.dp)) {
                            Icon(Icons.Rounded.Visibility, null,
                                modifier = Modifier.size(16.dp),
                                tint = if (showPreview) MaterialTheme.colorScheme.primary
                                       else MaterialTheme.colorScheme.onSurface.copy(0.5f))
                        }
                    }
                    // Vibe Coding toggle
                    IconButton(onClick = { showVibeDialog = true }, Modifier.size(32.dp)) {
                        Icon(Icons.Rounded.SmartToy, "Vibe Code",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // ── Find bar ─────────────────────────────────────────────
        if (showFind) {
            Surface(
                color    = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Search, null, Modifier.size(16.dp),
                        MaterialTheme.colorScheme.onSurface.copy(0.5f))
                    Spacer(Modifier.width(6.dp))
                    BasicTextField(
                        value         = findText,
                        onValueChange = { findText = it },
                        textStyle     = TextStyle(
                            fontSize   = 13.sp,
                            color      = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush   = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier      = Modifier.weight(1f),
                        decorationBox = { inner ->
                            if (findText.isEmpty()) Text("Cari di file...", fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(0.4f))
                            inner()
                        }
                    )
                    if (findText.isNotBlank()) {
                        val count = tfv.text.split(findText).size - 1
                        Text("$count hasil", fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(4.dp))
                        // Cari Sebelumnya (Up)
                        IconButton(onClick = {
                            if (count > 0) {
                                val currentIdx = tfv.selection.start
                                var prevIdx = tfv.text.lastIndexOf(findText, currentIdx - 1, ignoreCase = true)
                                if (prevIdx == -1) {
                                    // Wrap around ke akhir
                                    prevIdx = tfv.text.lastIndexOf(findText, ignoreCase = true)
                                }
                                if (prevIdx != -1) {
                                    tfv = tfv.copy(selection = androidx.compose.ui.text.TextRange(prevIdx, prevIdx + findText.length))
                                }
                            }
                        }, Modifier.size(28.dp)) {
                            Icon(Icons.Rounded.KeyboardArrowUp, "Sebelumnya", Modifier.size(18.dp))
                        }
                        // Cari Selanjutnya (Down)
                        IconButton(onClick = {
                            if (count > 0) {
                                val currentIdx = tfv.selection.end
                                var nextIdx = tfv.text.indexOf(findText, currentIdx, ignoreCase = true)
                                if (nextIdx == -1) {
                                    // Wrap around ke awal
                                    nextIdx = tfv.text.indexOf(findText, ignoreCase = true)
                                }
                                if (nextIdx != -1) {
                                    tfv = tfv.copy(selection = androidx.compose.ui.text.TextRange(nextIdx, nextIdx + findText.length))
                                }
                            }
                        }, Modifier.size(28.dp)) {
                            Icon(Icons.Rounded.KeyboardArrowDown, "Selanjutnya", Modifier.size(18.dp))
                        }
                    }
                    IconButton(onClick = { showFind = false; findText = "" }, Modifier.size(28.dp)) {
                        Icon(Icons.Rounded.Close, "Tutup", Modifier.size(18.dp))
                    }
                }
            }
        }

        // ── Clipboard & action bar ────────────────────────────────────
        Surface(color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth()) {
            Row(
                Modifier.horizontalScroll(rememberScrollState())
                    .padding(horizontal = 2.dp, vertical = 0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                EABtn("Copy", Icons.Rounded.ContentCopy) {
                    val sel = tfv.selection
                    val txt = if (sel.start != sel.end) tfv.text.substring(sel.start, sel.end)
                    else {
                        val ls = tfv.text.lastIndexOf('\n', sel.start - 1) + 1
                        val le = tfv.text.indexOf('\n', sel.start).let { if (it < 0) tfv.text.length else it }
                        tfv.text.substring(ls, le)
                    }
                    if (txt.isNotEmpty()) clipManager.setText(AnnotatedString(txt))
                }
                EABtn("Cut", Icons.Rounded.ContentCut) {
                    val sel = tfv.selection
                    if (sel.start != sel.end) {
                        clipManager.setText(AnnotatedString(tfv.text.substring(sel.start, sel.end)))
                        val new = tfv.text.substring(0, sel.start) + tfv.text.substring(sel.end)
                        tfv = TextFieldValue(new, androidx.compose.ui.text.TextRange(sel.start))
                    }
                }
                EABtn("Paste", Icons.Rounded.ContentPaste) {
                    val clip = clipManager.getText()?.text ?: ""
                    if (clip.isNotEmpty()) {
                        val sel = tfv.selection
                        val new = tfv.text.substring(0, sel.start) + clip + tfv.text.substring(sel.end)
                        tfv = TextFieldValue(new, androidx.compose.ui.text.TextRange(sel.start + clip.length))
                    }
                }
                EABtn("Hapus", Icons.Rounded.Backspace) {
                    val sel = tfv.selection
                    if (sel.start != sel.end) {
                        // Hapus blok yang di-select
                        val new = tfv.text.substring(0, sel.start) + tfv.text.substring(sel.end)
                        tfv = TextFieldValue(new, androidx.compose.ui.text.TextRange(sel.start))
                    } else if (sel.start > 0) {
                        // Hapus 1 karakter sebelum kursor (seperti backspace biasa)
                        val new = tfv.text.substring(0, sel.start - 1) + tfv.text.substring(sel.end)
                        tfv = TextFieldValue(new, androidx.compose.ui.text.TextRange(sel.start - 1))
                    }
                }
                EABtn("All", Icons.Rounded.SelectAll) {
                    tfv = tfv.copy(selection = androidx.compose.ui.text.TextRange(0, tfv.text.length))
                }
                VerticalDivider(Modifier.height(18.dp).padding(horizontal = 2.dp))
                EABtn("Undo", Icons.Rounded.Undo) { onUndo() }
                EABtn("Redo", Icons.Rounded.Redo) { onRedo() }
                VerticalDivider(Modifier.height(18.dp).padding(horizontal = 2.dp))
                EABtn("→Tab", Icons.Rounded.FormatIndentIncrease) {
                    val sel = tfv.selection
                    val new = tfv.text.substring(0, sel.start) + "    " + tfv.text.substring(sel.end)
                    tfv = TextFieldValue(new, androidx.compose.ui.text.TextRange(sel.start + 4))
                }
                EABtn("Tab←", Icons.AutoMirrored.Rounded.FormatIndentDecrease) {
                    val sel = tfv.selection
                    val s   = tfv.text.lastIndexOf('\n', sel.start - 1) + 1
                    if (tfv.text.substring(s).startsWith("    ")) {
                        val new = tfv.text.removeRange(s, s + 4)
                        tfv = TextFieldValue(new, androidx.compose.ui.text.TextRange((sel.start - 4).coerceAtLeast(s)))
                    }
                }
                VerticalDivider(Modifier.height(18.dp).padding(horizontal = 2.dp))
                EABtn("Save", Icons.Rounded.Save) { onSave() }
            }
        }

        // ── Quick toolbar ─────────────────────────────────────────
        Surface(
            color    = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            QuickToolbar(
                onInsert = { sym ->
                    val sel = tfv.selection
                    val new = tfv.text.substring(0, sel.start) + sym + tfv.text.substring(sel.end)
                    tfv = TextFieldValue(new,
                        androidx.compose.ui.text.TextRange(sel.start + sym.length))
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)

        // ── Vibe Coding Loading Overlay ──────────────────────────────
        if (isVibeCoding) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primary)
        }

        // ── Vibe Coding Dialog ──────────────────────────────────────
        if (showVibeDialog) {
            AlertDialog(
                onDismissRequest = { if (!isVibeCoding) showVibeDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.SmartToy, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("AI Vibe Coding", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column {
                        Text("Apa yang ingin AI lakukan pada kode ini?", fontSize = 14.sp)
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = vibePrompt,
                            onValueChange = { vibePrompt = it },
                            placeholder = { Text("Contoh: Buatkan fungsi kalkulator...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            enabled = !isVibeCoding
                        )
                        if (aiError.isNotBlank() && isVibeCoding.not()) {
                            Text(aiError, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (vibePrompt.isNotBlank() && !isVibeCoding) {
                                val pair = findActiveProvider(ctx)
                                if (pair == null) {
                                    aiError = "Pilih AI dan atur API Key di Panel AI dulu!"
                                } else {
                                    aiError = ""
                                    scope.launch {
                                        try {
                                            val newCode = vibeCodeRequest(
                                                ctx = ctx,
                                                prov = pair.first,
                                                srcIdx = pair.second,
                                                currentCode = tfv.text,
                                                prompt = vibePrompt,
                                                onLoading = { isVibeCoding = it }
                                            )
                                            tfv = TextFieldValue(newCode)
                                            showVibeDialog = false
                                            vibePrompt = ""
                                        } catch (e: Exception) {
                                            aiError = e.message ?: "Terjadi kesalahan"
                                        }
                                    }
                                }
                            }
                        },
                        enabled = vibePrompt.isNotBlank() && !isVibeCoding
                    ) {
                        if (isVibeCoding) {
                            CircularProgressIndicator(Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                            Spacer(Modifier.width(6.dp))
                        }
                        Text("Terapkan (Vibe)")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showVibeDialog = false }, enabled = !isVibeCoding) {
                        Text("Batal")
                    }
                }
            )
        }
        // ── Editor / Preview Area ──────────────────────────────────
        Box(Modifier.weight(1f).fillMaxWidth()) {
            if (showPreview && canPreview) {
                var isDesktopMode by remember { mutableStateOf(false) }
                var refreshKey    by remember { mutableStateOf(0) }

                Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                    // Preview Toolbar
                    Surface(tonalElevation = 4.dp, color = MaterialTheme.colorScheme.surfaceContainer) {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.Visibility, null, Modifier.size(16.dp), MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("PREVIEW", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            
                            // Desktop Toggle
                            IconButton(onClick = { isDesktopMode = !isDesktopMode }, Modifier.size(32.dp)) {
                                Icon(
                                    if (isDesktopMode) Icons.Rounded.DesktopWindows else Icons.Rounded.Smartphone,
                                    "Desktop Mode",
                                    Modifier.size(18.dp),
                                    tint = if (isDesktopMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(0.6f)
                                )
                            }
                            // Refresh
                            IconButton(onClick = { refreshKey++ }, Modifier.size(32.dp)) {
                                Icon(Icons.Rounded.Refresh, "Refresh", Modifier.size(18.dp))
                            }
                            // Close
                            IconButton(onClick = { showPreview = false }, Modifier.size(32.dp)) {
                                Icon(Icons.Rounded.Close, "Tutup", Modifier.size(18.dp))
                            }
                        }
                    }

                    Box(Modifier.fillMaxSize()) {
                        key(refreshKey, isDesktopMode) {
                            AndroidView(
                                factory = { context ->
                                    WebView(context).apply {
                                        settings.javaScriptEnabled = true
                                        settings.domStorageEnabled = true
                                        settings.databaseEnabled   = true
                                        webViewClient = WebViewClient()
                                    }
                                },
                                update = { wv ->
                                    val desktopUA = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                                    wv.settings.userAgentString = if (isDesktopMode) desktopUA else null
                                    wv.settings.useWideViewPort = isDesktopMode
                                    wv.settings.loadWithOverviewMode = isDesktopMode
                                    
                                    wv.loadDataWithBaseURL(null,
                                        buildPreviewHtml(tfv.text, language), "text/html", "UTF-8", null)
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            } else {
                EditorCore(
                    tfv             = tfv,
                    onTfvChange     = handleTfvChange,
                    theme           = activeTheme,
                    fontSize        = fontSize,
                    wordWrap        = wordWrap,
                    showLineNumbers = showLineNumbers,
                    highlightText   = findText,
                    modifier        = Modifier.fillMaxSize()
                )
            }
        }

        // =======================
        // EXTENSION: VIRTUAL PETS
        // =======================
        if (isPetsActive) {
            VirtualPet(isActive = true, modifier = Modifier.padding(bottom = 2.dp))
        }

        // ── VS Code style status bar ──────────────────────────────
        Surface(
            color    = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 3.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                // Left
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Ln ${tfv.text.take(tfv.selection.start).count { it == '\n' } + 1}, " +
                        "Col ${tfv.text.take(tfv.selection.start).substringAfterLast('\n').length + 1}",
                        fontSize = 11.sp, color = Color.White.copy(0.9f)
                    )
                    Text("${tfv.text.length} chars", fontSize = 11.sp, color = Color.White.copy(0.7f))
                }
                // Right
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // =======================
                    // EXTENSION: WAKATIME
                    // =======================
                    if (isWakaTimeActive) {
                        var sessionTime by remember { mutableStateOf(0) }
                        LaunchedEffect(Unit) {
                            while (true) {
                                kotlinx.coroutines.delay(60000) // update tiap menit
                                sessionTime++
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Timer, null, Modifier.size(11.dp), Color.White.copy(0.7f))
                            Spacer(Modifier.width(4.dp))
                            Text(if (sessionTime > 0) "${sessionTime}m" else "< 1m", fontSize = 11.sp, color = Color.White.copy(0.9f))
                        }
                    }

                    Text(SyntaxHighlighter.getLanguageLabel(language),
                        fontSize = 11.sp, color = Color.White.copy(0.9f))
                    Text("UTF-8", fontSize = 11.sp, color = Color.White.copy(0.7f))
                    Text("CodeDroid ✨", fontSize = 10.sp, color = Color.White.copy(0.5f))
                }
            }
        }
    }
}

@Composable
private fun EditorCore(
    tfv            : TextFieldValue,
    onTfvChange    : (TextFieldValue) -> Unit,
    theme          : EditorTheme,
    fontSize       : Int,
    wordWrap       : Boolean,
    showLineNumbers: Boolean,
    highlightText  : String = "",
    modifier       : Modifier = Modifier
) {
    val vScroll = rememberScrollState()
    val hScroll = rememberScrollState()

    Box(modifier = modifier.background(theme.background)) {
        Row(Modifier.fillMaxSize()) {
                // Line numbers
                if (showLineNumbers) {
                    val lineCount = tfv.text.lines().size.coerceAtLeast(1)
                    Row(
                        modifier = Modifier
                            .fillMaxHeight()
                            .background(theme.background.copy(0.95f))
                    ) {
                        Column(
                            modifier = Modifier
                                .width(48.dp)
                                .verticalScroll(vScroll)
                                .padding(vertical = 4.dp)
                        ) {
                            val currentLine = tfv.text.take(tfv.selection.start).count { it == '\n' } + 1
                            repeat(lineCount) { i ->
                                val lineNum = i + 1
                                Text(
                                    text      = "$lineNum",
                                    fontSize  = (fontSize - 1).sp,
                                    fontFamily= FontFamily.Monospace,
                                    color     = if (lineNum == currentLine) theme.text.copy(0.8f)
                                                else theme.lineNumbers,
                                    textAlign = TextAlign.End,
                                    modifier  = Modifier.fillMaxWidth().padding(end = 10.dp)
                                )
                            }
                        }
                        VerticalDivider(thickness = 0.5.dp, color = theme.lineNumbers.copy(0.2f))
                    }
                }

            BasicTextField(
                value         = tfv,
                onValueChange = onTfvChange,
                textStyle     = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize   = fontSize.sp,
                    color      = theme.text,
                    lineHeight  = (fontSize * 1.65f).sp
                ),
                cursorBrush   = SolidColor(theme.cursor),
                modifier      = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .let { if (!wordWrap) it.horizontalScroll(hScroll) else it }
                    .verticalScroll(vScroll)
                    .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 80.dp)
            )
        }
    }
}

private fun getLanguageColor(language: String): Color = when (language.lowercase()) {
    "kotlin","kt"     -> Color(0xFF7C4DFF)
    "java"            -> Color(0xFFFF6D00)
    "python","py"     -> Color(0xFF00BCD4)
    "html","htm"      -> Color(0xFFE64A19)
    "css","scss"      -> Color(0xFF1565C0)
    "javascript","js" -> Color(0xFFF9A825)
    "typescript","ts" -> Color(0xFF1976D2)
    "php"             -> Color(0xFF6A1B9A)
    "json"            -> Color(0xFF558B2F)
    "xml"             -> Color(0xFF00897B)
    "markdown","md"   -> Color(0xFF6D4C41)
    "bash","sh"       -> Color(0xFF37474F)
    else              -> Color(0xFF607D8B)
}

private fun buildPreviewHtml(content: String, language: String): String {
    return when (language.lowercase()) {
        "html","htm" -> content
        "css" -> """<!DOCTYPE html><html><head>
            <meta name="viewport" content="width=device-width,initial-scale=1">
            <style>body{font-family:sans-serif;padding:16px}$content</style></head><body>
            <h1>Heading 1</h1><h2>Heading 2</h2><p>Paragraf teks biasa.</p>
            <button>Tombol</button><a href="#">Link</a>
            <div class="container"><div class="box">Box</div></div>
            <ul><li>Item 1</li><li>Item 2</li><li>Item 3</li></ul>
            </body></html>""".trimIndent()
        "javascript","js" -> """<!DOCTYPE html><html><head>
            <meta name="viewport" content="width=device-width,initial-scale=1">
            <style>body{background:#1e1e1e;color:#d4d4d4;font-family:monospace;padding:12px}
            #out{background:#252526;padding:10px;border-radius:6px;min-height:60px;white-space:pre-wrap;
                 border:1px solid #3c3c3c;font-size:13px}</style></head><body>
            <div style="color:#858585;font-size:11px;margin-bottom:6px">▶ Console Output:</div>
            <div id="out"></div>
            <script>const el=document.getElementById('out');
            const fmt=(...a)=>a.map(x=>typeof x==='object'?JSON.stringify(x,null,2):x).join(' ');
            console.log=(...a)=>{el.innerHTML+='<span style="color:#a6e3a1">'+fmt(...a)+'</span>\n'};
            console.error=(...a)=>{el.innerHTML+='<span style="color:#f44747">❌ '+fmt(...a)+'</span>\n'};
            console.warn=(...a)=>{el.innerHTML+='<span style="color:#dcdcaa">⚠️ '+fmt(...a)+'</span>\n'};
            try{$content}catch(e){el.innerHTML+='<span style="color:#f44747">❌ '+e+'</span>'}</script>
            </body></html>""".trimIndent()
        "markdown","md" -> {
            val html = content
                .replace(Regex("^### (.+)$", RegexOption.MULTILINE), "<h3>\$1</h3>")
                .replace(Regex("^## (.+)$",  RegexOption.MULTILINE), "<h2>\$1</h2>")
                .replace(Regex("^# (.+)$",   RegexOption.MULTILINE), "<h1>\$1</h1>")
                .replace(Regex("\\*\\*(.+?)\\*\\*"), "<strong>\$1</strong>")
                .replace(Regex("\\*(.+?)\\*"),        "<em>\$1</em>")
                .replace(Regex("`(.+?)`"),             "<code>\$1</code>")
                .replace(Regex("^> (.+)$", RegexOption.MULTILINE), "<blockquote>\$1</blockquote>")
                .replace(Regex("^[-*] (.+)$", RegexOption.MULTILINE), "<li>\$1</li>")
                .replace("\n\n", "<p>")
            """<!DOCTYPE html><html><head>
            <meta name="viewport" content="width=device-width,initial-scale=1">
            <style>body{font-family:system-ui;max-width:100%;padding:20px;line-height:1.7;color:#24292e}
            code{background:#f6f8fa;padding:2px 6px;border-radius:4px;font-family:monospace;font-size:.9em}
            blockquote{border-left:4px solid #0366d6;margin:0;padding-left:16px;color:#6a737d}
            h1,h2{border-bottom:1px solid #eaecef;padding-bottom:6px}
            li{margin:4px 0}</style></head><body>$html</body></html>""".trimIndent()
        }
        else -> "<html><body style='font-family:monospace;padding:16px'><pre>${content.replace("<","<")}</pre></body></html>"
    }
}

// Extension function untuk mempermudah (hanya alias agar tidak error jika dipanggil)
private fun Modifier.sideBorder(color: Color): Modifier = this

@androidx.compose.runtime.Composable
private fun EABtn(
    label  : String,
    icon   : androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick, modifier = androidx.compose.ui.Modifier.size(34.dp)) {
        Icon(icon, label,
            modifier = androidx.compose.ui.Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}