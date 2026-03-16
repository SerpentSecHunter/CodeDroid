package com.example.codedroid.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codedroid.terminal.TerminalManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Precise Termux palette
private val TERM_BG      = Color(0xFF000000)
private val TERM_TEXT    = Color(0xFFE0E0E0)
private val TERM_PROMPT  = Color(0xFF4CAF50)
private val TERM_CURSOR  = Color(0xFFFFFFFF)
private val TERM_BAR_BG  = Color(0xFF1A1A1A) 
private val BORDER_COLOR = Color(0xFF333333)

private val COLOR_RED    = Color(0xFFE57373)
private val COLOR_GREEN  = Color(0xFF81C784)
private val COLOR_YELLOW = Color(0xFFFFF176)
private val COLOR_BLUE   = Color(0xFF64B5F6)
private val COLOR_MAGENTA= Color(0xFFBA68C8)
private val COLOR_CYAN   = Color(0xFF4DD0E1)

@Composable
fun parseAnsi(text: String): androidx.compose.ui.text.AnnotatedString {
    return buildAnnotatedString {
        val parts = text.split("\u001b[")
        append(parts[0])
        for (i in 1 until parts.size) {
            val part = parts[i]
            val mIndex = part.indexOf('m')
            if (mIndex != -1) {
                val code = part.substring(0, mIndex)
                val content = part.substring(mIndex + 1)
                val style = when (code) {
                    "31" -> SpanStyle(color = COLOR_RED)
                    "32" -> SpanStyle(color = COLOR_GREEN)
                    "33" -> SpanStyle(color = COLOR_YELLOW)
                    "34" -> SpanStyle(color = COLOR_BLUE)
                    "35" -> SpanStyle(color = COLOR_MAGENTA)
                    "36" -> SpanStyle(color = COLOR_CYAN)
                    "0"  -> SpanStyle(color = TERM_TEXT)
                    "1"  -> SpanStyle(fontWeight = FontWeight.Bold)
                    else -> SpanStyle()
                }
                pushStyle(style)
                append(content)
                pop()
            } else append("\u001b[" + part)
        }
    }
}

@Composable
fun TerminalScreen(terminalManager: TerminalManager, onOpenDrawer: () -> Unit) {
    val context      = androidx.compose.ui.platform.LocalContext.current
    val scope        = rememberCoroutineScope()
    val listState    = rememberLazyListState()
    val focusReq     = remember { FocusRequester() }
    var input        by remember { mutableStateOf("") }
    val lines        = remember { mutableStateListOf<String>() }
    val cmdHistory   = remember { mutableStateListOf<String>() }
    var histIdx      by remember { mutableStateOf(-1) }
    
    var ctrlActive   by remember { mutableStateOf(false) }
    var altActive    by remember { mutableStateOf(false) }

    val extraKeys = listOf("MENU", "ESC", "CTRL", "ALT", "TAB", "UP", "DOWN", "LEFT", "RIGHT")

    val cursorAlpha by rememberInfiniteTransition(label = "").animateFloat(
        initialValue = 0.1f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(450, easing = LinearEasing), RepeatMode.Reverse), label = ""
    )

    LaunchedEffect(Unit) {
        terminalManager.startSession()
        terminalManager.output.collect { raw ->
            when (raw) {
                "\u0000EXIT" -> { (context as? android.app.Activity)?.finish() }
                "\u0000CLEAR" -> lines.clear()
                else -> {
                    lines.add(raw)
                    if (lines.size > 1500) lines.removeAt(0)
                    scope.launch {
                        delay(50)
                        if (lines.isNotEmpty()) listState.animateScrollToItem(lines.size - 1)
                    }
                }
            }
        }
    }

    // Wrap in Scaffold-like column to handle Insets properly
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TERM_BG)
            .statusBarsPadding() // Space for top status bar
            .pointerInput(Unit) { detectTapGestures { focusReq.requestFocus() } }
    ) {
        // 1. Output (Main Content)
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
        ) {
            items(lines) { line ->
                Text(
                    text = parseAnsi(line),
                    color = TERM_TEXT,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 17.sp
                )
            }
        }

        // 2. Input Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "${terminalManager.getShortPath()} $ ",
                color = TERM_PROMPT,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Box(modifier = Modifier.weight(1f)) {
                BasicTextField(
                    value = input,
                    onValueChange = { input = it; histIdx = -1 },
                    textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 14.sp, color = Color.Transparent),
                    cursorBrush = SolidColor(Color.Transparent),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done, autoCorrectEnabled = false),
                    keyboardActions = KeyboardActions(onDone = {
                        if (input.isNotBlank()) {
                            cmdHistory.add(0, input)
                            terminalManager.sendCommand(input)
                            input = ""
                        }
                    }),
                    modifier = Modifier.fillMaxWidth().focusRequester(focusReq)
                )
                Row {
                    Text(input, color = TERM_TEXT, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
                    Box(Modifier.padding(top = 2.dp).size(8.dp, 16.dp).background(TERM_CURSOR.copy(alpha = cursorAlpha)))
                }
            }
        }

        // 3. Toolbar (Navbar Extra Keys)
        // We use inner column to separate toolbar from system navigation bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(TERM_BAR_BG)
                .imePadding() // Pushes up when keyboard is visible
        ) {
            // Distinct Top Border for the Toolbar
            HorizontalDivider(color = BORDER_COLOR, thickness = 1.dp)
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                extraKeys.forEach { key ->
                    val isActive = (key == "CTRL" && ctrlActive) || (key == "ALT" && altActive)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable {
                                when (key) {
                                    "MENU" -> onOpenDrawer()
                                    "ESC"  -> input = ""
                                    "CTRL" -> ctrlActive = !ctrlActive
                                    "ALT"  -> altActive = !altActive
                                    "TAB"  -> terminalManager.sendCommand("ls")
                                    "UP" -> if (cmdHistory.isNotEmpty() && histIdx < cmdHistory.size - 1) { histIdx++; input = cmdHistory[histIdx] }
                                    "DOWN" -> if (histIdx > 0) { histIdx--; input = cmdHistory[histIdx] } else { histIdx = -1; input = "" }
                                    else -> {}
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = key,
                            color = if (isActive) COLOR_GREEN else TEXT_PRIMARY,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
            
            // 4. System Navigation Padding (This prevents merging with system buttons)
            Spacer(Modifier.navigationBarsPadding())
        }
    }

    LaunchedEffect(Unit) {
        delay(500)
        runCatching { focusReq.requestFocus() }
    }
}

private val TEXT_PRIMARY = Color(0xFFE0E0E0)