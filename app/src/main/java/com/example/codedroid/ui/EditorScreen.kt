package com.example.codedroid.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import android.webkit.WebView
import android.webkit.WebViewClient
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
    val canPreview = language.lowercase() in listOf("html","htm","css","javascript","js","markdown","md")

    LaunchedEffect(tfv.text) {
        if (tfv.text != content) onContentChange(tfv.text)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // ── Editor toolbar ────────────────────────────────────────
        Surface(tonalElevation = 1.dp) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Language badge
                Surface(
                    color  = MaterialTheme.colorScheme.primary.copy(0.1f),
                    shape  = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        SyntaxHighlighter.getLanguageLabel(language),
                        fontSize = 10.sp,
                        color    = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(Modifier.weight(1f))
                // Preview toggle (hanya untuk web)
                if (canPreview) {
                    IconButton(onClick = { showPreview = !showPreview }) {
                        Icon(
                            if (showPreview) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                            "Toggle Preview",
                            tint = if (showPreview) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurface.copy(0.5f)
                        )
                    }
                }
            }
        }

        // ── Quick toolbar ─────────────────────────────────────────
        QuickToolbar(
            onInsert = { sym ->
                val sel = tfv.selection
                val new = tfv.text.substring(0, sel.start) + sym + tfv.text.substring(sel.end)
                tfv = TextFieldValue(new,
                    androidx.compose.ui.text.TextRange(sel.start + sym.length))
            },
            modifier = Modifier.fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
        )

        HorizontalDivider()

        // ── Editor + Preview area ──────────────────────────────────
        if (showPreview && canPreview) {
            // Split: Editor kiri, Preview kanan (atau atas-bawah)
            Row(Modifier.weight(1f).fillMaxWidth()) {
                // Editor half
                EditorTextField(
                    tfv             = tfv,
                    onTfvChange     = { tfv = it },
                    theme           = theme,
                    fontSize        = fontSize,
                    wordWrap        = wordWrap,
                    showLineNumbers = showLineNumbers,
                    modifier        = Modifier.weight(1f).fillMaxHeight()
                )
                VerticalDivider()
                // Preview half
                Box(Modifier.weight(1f).fillMaxHeight().background(MaterialTheme.colorScheme.background)) {
                    val htmlContent = buildPreviewHtml(tfv.text, language)
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                webViewClient = WebViewClient()
                            }
                        },
                        update  = { wv ->
                            wv.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    // Preview label
                    Surface(
                        modifier = Modifier.align(Alignment.TopCenter),
                        color    = MaterialTheme.colorScheme.primary.copy(0.85f)
                    ) {
                        Text("PREVIEW", fontSize = 9.sp,
                            color    = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                    }
                }
            }
        } else {
            EditorTextField(
                tfv             = tfv,
                onTfvChange     = { tfv = it },
                theme           = theme,
                fontSize        = fontSize,
                wordWrap        = wordWrap,
                showLineNumbers = showLineNumbers,
                modifier        = Modifier.weight(1f).fillMaxWidth()
            )
        }

        // ── Status bar ────────────────────────────────────────────
        StatusBar(
            language  = SyntaxHighlighter.getLanguageLabel(language),
            line      = tfv.text.take(tfv.selection.start).count { it == '\n' } + 1,
            col       = tfv.text.take(tfv.selection.start).substringAfterLast('\n').length + 1,
            charCount = tfv.text.length,
            modifier  = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun EditorTextField(
    tfv            : TextFieldValue,
    onTfvChange    : (TextFieldValue) -> Unit,
    theme          : EditorTheme,
    fontSize       : Int,
    wordWrap       : Boolean,
    showLineNumbers: Boolean,
    modifier       : Modifier = Modifier
) {
    val vScroll = rememberScrollState()
    val hScroll = rememberScrollState()

    Box(
        modifier = modifier.background(theme.background)
    ) {
        Row(Modifier.fillMaxSize()) {
            // Line numbers
            if (showLineNumbers) {
                val lineCount = tfv.text.lines().size
                Column(
                    modifier = Modifier
                        .width(44.dp)
                        .fillMaxHeight()
                        .background(theme.background)
                        .verticalScroll(vScroll)
                        .padding(end = 6.dp, top = 4.dp)
                ) {
                    repeat(lineCount) { i ->
                        Text(
                            text       = "${i + 1}",
                            fontSize   = fontSize.sp,
                            fontFamily = FontFamily.Monospace,
                            color      = theme.lineNumbers,
                            textAlign  = TextAlign.End,
                            modifier   = Modifier.fillMaxWidth()
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
                    lineHeight  = (fontSize * 1.6f).sp
                ),
                cursorBrush   = SolidColor(theme.cursor),
                modifier      = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .let { if (!wordWrap) it.horizontalScroll(hScroll) else it }
                    .verticalScroll(vScroll)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

private fun buildPreviewHtml(content: String, language: String): String {
    return when (language.lowercase()) {
        "html","htm" -> content
        "css" -> """
            <!DOCTYPE html><html><head>
            <meta name="viewport" content="width=device-width,initial-scale=1">
            <style>$content</style></head><body>
            <h1>Heading 1</h1><h2>Heading 2</h2>
            <p>Paragraf teks biasa lorem ipsum.</p>
            <button>Tombol</button>
            <div class="box">Box element</div>
            <ul><li>Item 1</li><li>Item 2</li></ul>
            </body></html>""".trimIndent()
        "javascript","js" -> """
            <!DOCTYPE html><html><head>
            <meta name="viewport" content="width=device-width,initial-scale=1">
            <style>
            body{background:#0d1117;color:#e6edf3;font-family:monospace;padding:12px}
            #out{background:#161b22;padding:10px;border-radius:8px;min-height:60px;
                 white-space:pre-wrap;border:1px solid #30363d}
            </style></head><body>
            <div style="margin-bottom:8px;font-size:12px;color:#8b949e">Console Output:</div>
            <div id="out"></div>
            <script>
            const el=document.getElementById('out');
            const origLog=console.log,origErr=console.error,origWarn=console.warn;
            console.log=(...a)=>{el.innerHTML+='<span style="color:#a6e3a1">'+a.join(' ')+'</span>\n';origLog(...a)};
            console.error=(...a)=>{el.innerHTML+='<span style="color:#f38ba8">❌ '+a.join(' ')+'</span>\n';origErr(...a)};
            console.warn=(...a)=>{el.innerHTML+='<span style="color:#f9e2af">⚠️ '+a.join(' ')+'</span>\n';origWarn(...a)};
            try{$content}catch(e){el.innerHTML+='<span style="color:#f38ba8">❌ '+e.message+'</span>'}
            </script></body></html>""".trimIndent()
        "markdown","md" -> {
            val html = content
                .replace(Regex("^### (.+)$", RegexOption.MULTILINE), "<h3>$1</h3>")
                .replace(Regex("^## (.+)$",  RegexOption.MULTILINE), "<h2>$1</h2>")
                .replace(Regex("^# (.+)$",   RegexOption.MULTILINE), "<h1>$1</h1>")
                .replace(Regex("\\*\\*(.+?)\\*\\*"), "<strong>$1</strong>")
                .replace(Regex("\\*(.+?)\\*"),        "<em>$1</em>")
                .replace(Regex("`(.+?)`"),             "<code>$1</code>")
                .replace(Regex("^> (.+)$", RegexOption.MULTILINE), "<blockquote>$1</blockquote>")
                .replace(Regex("^- (.+)$",  RegexOption.MULTILINE), "<li>$1</li>")
                .replace(Regex("^\\d+\\. (.+)$", RegexOption.MULTILINE), "<li>$1</li>")
                .replace("\n\n", "<br><br>")
            """
            <!DOCTYPE html><html><head>
            <meta name="viewport" content="width=device-width,initial-scale=1">
            <style>
            body{font-family:sans-serif;max-width:100%;padding:16px;line-height:1.7;color:#333}
            code{background:#f0f0f0;padding:2px 5px;border-radius:4px;font-family:monospace;font-size:0.9em}
            blockquote{border-left:4px solid #ddd;margin:0;padding-left:16px;color:#666}
            h1,h2,h3{border-bottom:1px solid #eee;padding-bottom:6px}
            li{margin:4px 0}
            </style></head><body>$html</body></html>""".trimIndent()
        }
        else -> "<html><body><pre>${content.replace("<","<")}</pre></body></html>"
    }
}
