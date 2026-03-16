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
    val context = LocalContext.current
    // Sync from ViewModel and local TextFieldValue
    var tfv        by remember { mutableStateOf(TextFieldValue(content)) }
    var lastExternal by remember { mutableStateOf(content) }

    LaunchedEffect(content) {
        if (content != tfv.text && content != lastExternal) {
            tfv = TextFieldValue(content, androidx.compose.ui.text.TextRange(content.length))
            lastExternal = content
        }
    }

    LaunchedEffect(tfv.text) {
        if (tfv.text != content) {
            kotlinx.coroutines.delay(500)
            onContentChange(tfv.text)
        }
    }

    val canPreview  = language.lowercase() in listOf("html","htm","css","javascript","js","markdown","md")

    // --- Extension Engine Logic (Silent, No UI impact) ---
    val installedExts = remember { com.example.codedroid.data.ExtensionManager.getInstalled(context) }
    val isAutoCloseActive = installedExts.contains("auto-close")
    val hasSnippets = installedExts.any { it.contains("snippet") } // Detects gen-snippet and core snippets
    
    // Dynamic Theme Support from 500+ Plugins
    val activeTheme = remember(installedExts, theme) {
        val themeExt = installedExts.find { it.contains("theme") }
        if (themeExt != null) {
            // Priority: Core themes first, then generated ones
            val key = when {
                themeExt.startsWith("theme-") -> themeExt.removePrefix("theme-")
                themeExt.contains("gen-theme") -> "monokai" // Fallback to a nice dark theme for generated ones
                else -> ""
            }
            if (key.isNotEmpty()) com.example.codedroid.editor.EditorThemes.get(key) else theme
        } else theme
    }

    val handleTfvChange: (TextFieldValue) -> Unit = { newTfv ->
        var finalTfv = newTfv
        val oldTxt = tfv.text
        val newTxt = newTfv.text
        val cursor = newTfv.selection.start

        if (newTxt.length > oldTxt.length && cursor > 0) {
            val charTyped = newTxt[cursor - 1]

            // 1. Auto-Close Tag (Silent)
            if (isAutoCloseActive && canPreview && charTyped == '>') {
                val lastOpen = newTxt.lastIndexOf('<', cursor - 2)
                if (lastOpen != -1) {
                    val tag = newTxt.substring(lastOpen + 1, cursor - 1).takeWhile { it.isLetterOrDigit() }
                    if (tag.isNotEmpty() && !tag.startsWith("/")) {
                        val close = "</$tag>"
                        val mod = newTxt.substring(0, cursor) + close + newTxt.substring(cursor)
                        finalTfv = TextFieldValue(mod, androidx.compose.ui.text.TextRange(cursor))
                    }
                }
            }

            // 2. Snippet Pack Engine (Silent but Powerful)
            if (hasSnippets && charTyped.isWhitespace()) {
                val lineStart = newTxt.lastIndexOf('\n', cursor - 2).let { if(it == -1) 0 else it + 1 }
                val word = newTxt.substring(lineStart, cursor - 1).substringAfterLast(' ').trim()
                val snippet = when(word) {
                    // Web
                    "doc" -> "<!DOCTYPE html>\n<html>\n<head>\n\t<title></title>\n</head>\n<body>\n\n</body>\n</html>"
                    "div" -> "<div>\n\t\n</div>"
                    "clg" -> "console.log();"
                    // General
                    "fun" -> "function name() {\n\t\n}"
                    "pts" -> "println(\"\")"
                    "main" -> "fun main() {\n\tprintln(\"Hello World\")\n}"
                    // Python
                    "pys" -> "if __name__ == \"__main__\":\n\tmain()"
                    // React-style
                    "rfc" -> "export default function Component() {\n\treturn (<div></div>);\n}"
                    else -> null
                }
                if (snippet != null) {
                    val start = cursor - 1 - word.length
                    val mod = newTxt.replaceRange(start, cursor - 1, snippet)
                    finalTfv = TextFieldValue(mod, androidx.compose.ui.text.TextRange(start + snippet.length))
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
                    else ""
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
                        val new = tfv.text.substring(0, sel.start) + tfv.text.substring(sel.end)
                        tfv = TextFieldValue(new, androidx.compose.ui.text.TextRange(sel.start))
                    } else if (sel.start > 0) {
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
                        if (aiError.isNotBlank() && !isVibeCoding) {
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
                Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                    Surface(tonalElevation = 4.dp, color = MaterialTheme.colorScheme.surfaceContainer) {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.Visibility, null, Modifier.size(16.dp), MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("PREVIEW", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            IconButton(onClick = { isDesktopMode = !isDesktopMode }, Modifier.size(32.dp)) {
                                Icon(if (isDesktopMode) Icons.Rounded.DesktopWindows else Icons.Rounded.Smartphone, null, Modifier.size(18.dp))
                            }
                            IconButton(onClick = { showPreview = false }, Modifier.size(32.dp)) {
                                Icon(Icons.Rounded.Close, "Tutup", Modifier.size(18.dp))
                            }
                        }
                    }
                    AndroidView(
                        factory = { context -> WebView(context).apply { settings.javaScriptEnabled = true; webViewClient = WebViewClient() } },
                        update = { wv -> 
                            wv.settings.userAgentString = if (isDesktopMode) "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36" else null
                            wv.loadDataWithBaseURL(null, buildPreviewHtml(tfv.text, language), "text/html", "UTF-8", null) 
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                EditorCore(
                    tfv             = tfv,
                    onTfvChange     = handleTfvChange,
                    theme           = theme,
                    fontSize        = fontSize,
                    wordWrap        = wordWrap,
                    showLineNumbers = showLineNumbers,
                    highlightText   = findText,
                    modifier        = Modifier.fillMaxSize()
                )
            }
            
            // PETS Logic check
            val installed = remember { com.example.codedroid.data.ExtensionManager.getInstalled(ctx) }
            if (installed.contains("pets")) {
                VirtualPet(isActive = true, modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp))
            }
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
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Ln ${tfv.text.take(tfv.selection.start).count { it == '\n' } + 1}, " +
                        "Col ${tfv.text.take(tfv.selection.start).substringAfterLast('\n').length + 1}",
                        fontSize = 11.sp, color = Color.White
                    )
                    Text("${tfv.text.length} chars", fontSize = 11.sp, color = Color.White.copy(0.7f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(SyntaxHighlighter.getLanguageLabel(language),
                        fontSize = 11.sp, color = Color.White)
                    Text("UTF-8", fontSize = 11.sp, color = Color.White.copy(0.7f))
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
                if (showLineNumbers) {
                    val lineCount = tfv.text.lines().size.coerceAtLeast(1)
                    Column(
                        modifier = Modifier
                            .width(48.dp)
                            .verticalScroll(vScroll)
                            .padding(vertical = 4.dp)
                    ) {
                        repeat(lineCount) { i ->
                            Text(
                                text      = "${i + 1}",
                                fontSize  = (fontSize - 1).sp,
                                fontFamily= FontFamily.Monospace,
                                color     = theme.lineNumbers,
                                textAlign = TextAlign.End,
                                modifier  = Modifier.fillMaxWidth().padding(end = 10.dp)
                            )
                        }
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
    else              -> Color.Gray
}

private fun buildPreviewHtml(content: String, language: String): String = "<html><body>$content</body></html>"

@Composable
private fun EABtn(
    label  : String,
    icon   : androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick, modifier = Modifier.size(34.dp)) {
        Icon(icon, label,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}