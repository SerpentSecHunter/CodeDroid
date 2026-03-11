package com.example.codedroid.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    LaunchedEffect(tfv.text) {
        if (tfv.text != content) onContentChange(tfv.text)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Quick toolbar
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

        // Editor area
        val vScroll = rememberScrollState()
        val hScroll = rememberScrollState()

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(theme.background)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Line numbers
                if (showLineNumbers) {
                    val lines = tfv.text.lines().size
                    Column(
                        modifier = Modifier
                            .width(44.dp)
                            .fillMaxHeight()
                            .background(theme.background.copy(alpha = 0.8f))
                            .verticalScroll(vScroll)
                            .padding(end = 6.dp)
                    ) {
                        repeat(lines) { i ->
                            Text(
                                text      = "${i + 1}",
                                fontSize  = fontSize.sp,
                                fontFamily= FontFamily.Monospace,
                                color     = theme.lineNumbers,
                                modifier  = Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.End
                            )
                        }
                    }
                }

                // Text field
                BasicTextField(
                    value         = tfv,
                    onValueChange = { tfv = it },
                    textStyle     = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize   = fontSize.sp,
                        color      = theme.text,
                        lineHeight  = (fontSize * 1.5).sp
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

        // Status bar
        StatusBar(
            language  = SyntaxHighlighter.getLanguageLabel(language),
            line      = tfv.text.take(tfv.selection.start).count { it == '\n' } + 1,
            col       = tfv.text.take(tfv.selection.start).substringAfterLast('\n').length + 1,
            charCount = tfv.text.length,
            modifier  = Modifier.fillMaxWidth()
        )
    }
}
