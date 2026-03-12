package com.example.codedroid.ui

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.codedroid.editor.EditorTheme
import com.example.codedroid.editor.SyntaxHighlighter

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
    var tfv by remember(content) { mutableStateOf(TextFieldValue(content)) }
    var showPreview by remember { mutableStateOf(false) }
    var findText    by remember { mutableStateOf("") }
    var showFind    by remember { mutableStateOf(false) }
    val canPreview  = language.lowercase() in listOf("html","htm","css","javascript","js","markdown","md")

    LaunchedEffect(tfv.text) {
        if (tfv.text != content) onContentChange(tfv.text)
    }

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
                    }
                    IconButton(onClick = { showFind = false; findText = "" }, Modifier.size(28.dp)) {
                        Icon(Icons.Rounded.Close, null, Modifier.size(14.dp))
                    }
                }
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

        // ── Editor + Preview ──────────────────────────────────────
        if (showPreview && canPreview) {
            Row(Modifier.weight(1f).fillMaxWidth()) {
                EditorCore(
                    tfv             = tfv,
                    onTfvChange     = { tfv = it },
                    theme           = theme,
                    fontSize        = fontSize,
                    wordWrap        = wordWrap,
                    showLineNumbers = showLineNumbers,
                    highlightText   = findText,
                    modifier        = Modifier.weight(1f).fillMaxHeight()
                )
                Box(
                    modifier = Modifier
                        .width(0.5.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.outline)
                )
                Box(Modifier.weight(1f).fillMaxHeight()) {
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                webViewClient = WebViewClient()
                            }
                        },
                        update  = { wv ->
                            wv.loadDataWithBaseURL(null,
                                buildPreviewHtml(tfv.text, language), "text/html", "UTF-8", null)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    Surface(
                        color    = MaterialTheme.colorScheme.primary.copy(0.9f),
                        shape    = RoundedCornerShape(bottomStart = 6.dp),
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text("PREVIEW", fontSize = 9.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color    = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                    }
                }
            }
        } else {
            EditorCore(
                tfv             = tfv,
                onTfvChange     = { tfv = it },
                theme           = theme,
                fontSize        = fontSize,
                wordWrap        = wordWrap,
                showLineNumbers = showLineNumbers,
                highlightText   = findText,
                modifier        = Modifier.weight(1f).fillMaxWidth()
            )
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
                    Text(SyntaxHighlighter.getLanguageLabel(language),
                        fontSize = 11.sp, color = Color.White.copy(0.9f))
                    Text("UTF-8", fontSize = 11.sp, color = Color.White.copy(0.7f))
                    Text("CodeDroid v2.3", fontSize = 10.sp, color = Color.White.copy(0.5f))
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
                Column(
                    modifier = Modifier
                        .width(48.dp)
                        .fillMaxHeight()
                        .background(theme.background.copy(0.95f))
                        .border(end = 0.5.dp, color = theme.lineNumbers.copy(0.2f))
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

// Extension function untuk Modifier border sisi tertentu
private fun Modifier.border(end: androidx.compose.ui.unit.Dp, color: Color): Modifier = this